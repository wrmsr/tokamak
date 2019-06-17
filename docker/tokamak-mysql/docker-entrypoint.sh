#!/bin/bash
set -eo pipefail
shopt -s nullglob

# if command starts with an option, prepend mysqld
if [ "${1:0:1}" = '-' ]; then
    set -- mysqld "$@"
fi

# skip setup if they want an option that stops mysqld
wantHelp=
for arg; do
    case "$arg" in
        -'?'|--help|--print-defaults|-V|--version)
            wantHelp=1
            break
            ;;
    esac
done

# usage: file_env VAR [DEFAULT]
#    ie: file_env 'XYZ_DB_PASSWORD' 'example'
# (will allow for "$XYZ_DB_PASSWORD_FILE" to fill in the value of
#  "$XYZ_DB_PASSWORD" from a file, especially for Docker's secrets feature)
file_env() {
    local var="$1"
    local fileVar="${var}_FILE"
    local def="${2:-}"
    if [ "${!var:-}" ] && [ "${!fileVar:-}" ]; then
        echo >&2 "error: both $var and $fileVar are set (but are exclusive)"
        exit 1
    fi
    local val="$def"
    if [ "${!var:-}" ]; then
        val="${!var}"
    elif [ "${!fileVar:-}" ]; then
        val="$(< "${!fileVar}")"
    fi
    export "$var"="$val"
    unset "$fileVar"
}

_check_config() {
    toRun=( "$@" --verbose --help --log-bin-index="$(mktemp -u)" )
    if ! errors="$("${toRun[@]}" 2>&1 >/dev/null)"; then
        cat >&2 <<-EOM

ERROR: mysqld failed while attempting to check config
command was: "${toRun[*]}"

$errors
EOM
        exit 1
    fi
}

# Fetch value from server config
# We use mysqld --verbose --help instead of my_print_defaults because the
# latter only show values present in config files, and not server defaults
_get_config() {
    local conf="$1"; shift
    "$@" --verbose --help --log-bin-index="$(mktemp -u)" 2>/dev/null | awk '$1 == "'"$conf"'" { print $2; exit }'
}

_setup_replication_prestart() {
    if [ "x$REPLICATE_FROM" == "x" ] ; then
        sudo -u root bash -c "cat >/etc/mysql/conf.d/replication.cnf" <<-EOM
[mysqld]
server-id=1
binlog-ignore-db=mysql
binlog-ignore-db=informationschema

log-bin=mysql-bin
innodb_flush_log_at_trx_commit=1
sync_binlog=1
binlog-format=row
EOM
        echo
    else
        sudo -u root bash -c "cat >/etc/mysql/conf.d/replication.cnf" <<-EOM
[mysqld]
server-id=2
replicate-ignore-db=mysql
replicate-ignore-db=informationschema
EOM
        echo
    fi
}

_setup_replication_poststart() {
    if [ "x$REPLICATE_FROM" == "x" ] ; then
        echo
    else
        MYSQL_MASTER_WAIT_TIME=${MYSQL_MASTER_WAIT_TIME:-30}

        # Wait for eg. 10 seconds for the master to come up
        # do at least one iteration
        for i in $(seq $((MYSQL_MASTER_WAIT_TIME + 1))); do
            if ! mysql "-u$MYSQL_USER" "-p$MYSQL_PASSWORD" "-h$REPLICATE_FROM" -e 'select 1;' | grep -q 1; then
                echo >&2 "Waiting for $MYSQL_USER@$REPLICATE_FROM"
                sleep 1
            else
                break
            fi
        done

        if [ "$i" -gt "$MYSQL_MASTER_WAIT_TIME" ]; then
            echo 2>&1 "Master is not reachable"
            exit 1
        fi

        mysql "-uroot" "-p$MYSQL_ROOT_PASSWORD" "-h$REPLICATE_FROM" -e "GRANT REPLICATION SLAVE ON *.* TO '$MYSQL_USER'@'%'"
        mysql "-uroot" "-p$MYSQL_ROOT_PASSWORD" "-h$REPLICATE_FROM" -e "FLUSH PRIVILEGES"

        # Get master position and set it on the slave. NB: MASTER_PORT and MASTER_LOG_POS must not be quoted
        MASTER_POSITION=$(mysql "-uroot" "-p$MYSQL_ROOT_PASSWORD" "-h$REPLICATE_FROM" -e "SHOW MASTER STATUS \G" | awk '/Position/ {print $2}')
        MASTER_FILE=$(mysql  "-uroot" "-p$MYSQL_ROOT_PASSWORD" "-h$REPLICATE_FROM" -e "SHOW MASTER STATUS \G" | awk '/File/ {print $2}')
        echo "CHANGE MASTER TO MASTER_HOST='$REPLICATE_FROM', MASTER_PORT=3306, MASTER_USER='$MYSQL_USER', MASTER_PASSWORD='$MYSQL_PASSWORD', MASTER_LOG_FILE='$MASTER_FILE', MASTER_LOG_POS=$MASTER_POSITION;"  | "${mysql[@]}"

        echo "START SLAVE;"  | "${mysql[@]}"
    fi
}

# allow the container to be started with `--user`
if [ "$1" = 'mysqld' -a -z "$wantHelp" -a "$(id -u)" = '0' ]; then
    _check_config "$@"
    DATADIR="$(_get_config 'datadir' "$@")"
    mkdir -p "$DATADIR"
    chown -R mysql:mysql "$DATADIR"
    exec gosu mysql "$BASH_SOURCE" "$@"
fi

if [ "$1" = 'mysqld' -a -z "$wantHelp" ]; then
    # still need to check config, container may have started with --user
    _check_config "$@"
    # Get config
    DATADIR="$(_get_config 'datadir' "$@")"

    if [ ! -d "$DATADIR/mysql" ]; then
        file_env 'MYSQL_ROOT_PASSWORD'
        if [ -z "$MYSQL_ROOT_PASSWORD" -a -z "$MYSQL_ALLOW_EMPTY_PASSWORD" -a -z "$MYSQL_RANDOM_ROOT_PASSWORD" ]; then
            echo >&2 'error: database is uninitialized and password option is not specified '
            echo >&2 '  You need to specify one of MYSQL_ROOT_PASSWORD, MYSQL_ALLOW_EMPTY_PASSWORD and MYSQL_RANDOM_ROOT_PASSWORD'
            exit 1
        fi

        mkdir -p "$DATADIR"

        echo 'Initializing database'
        mysqld --datadir="$DATADIR" --initialize-insecure
        echo 'Database initialized'

        _setup_replication_prestart

        SOCKET="$(_get_config 'socket' "$@")"
        "$@" --skip-networking --socket="${SOCKET}" &
        pid="$!"

        mysql=( mysql --protocol=socket -uroot -hlocalhost --socket="${SOCKET}" )

        _setup_replication_poststart

        for i in {30..0}; do
            if echo 'SELECT 1' | "${mysql[@]}" &> /dev/null; then
                break
            fi
            echo 'MySQL init process in progress...'
            sleep 1
        done
        if [ "$i" = 0 ]; then
            echo >&2 'MySQL init process failed.'
            exit 1
        fi

        if [ -z "$MYSQL_INITDB_SKIP_TZINFO" ]; then
            # sed is for https://bugs.mysql.com/bug.php?id=20545
            mysql_tzinfo_to_sql /usr/share/zoneinfo | sed 's/Local time zone must be set--see zic manual page/FCTY/' | "${mysql[@]}" mysql
        fi

        if [ ! -z "$MYSQL_RANDOM_ROOT_PASSWORD" ]; then
            export MYSQL_ROOT_PASSWORD="$(pwgen -1 32)"
            echo "GENERATED ROOT PASSWORD: $MYSQL_ROOT_PASSWORD"
        fi

        rootCreate=
        # default root to listen for connections from anywhere
        file_env 'MYSQL_ROOT_HOST' '%'
        if [ ! -z "$MYSQL_ROOT_HOST" -a "$MYSQL_ROOT_HOST" != 'localhost' ]; then
            # no, we don't care if read finds a terminating character in this heredoc
            # https://unix.stackexchange.com/questions/265149/why-is-set-o-errexit-breaking-this-read-heredoc-expression/265151#265151
            read -r -d '' rootCreate <<-EOSQL || true
CREATE USER 'root'@'${MYSQL_ROOT_HOST}' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}' ;
GRANT ALL ON *.* TO 'root'@'${MYSQL_ROOT_HOST}' WITH GRANT OPTION ;
EOSQL
        fi

        "${mysql[@]}" <<-EOSQL
-- What's done in this file shouldn't be replicated
--  or products like mysql-fabric won't work
SET @@SESSION.SQL_LOG_BIN=0;

DELETE FROM mysql.user WHERE user NOT IN ('mysql.sys', 'mysqlxsys', 'root') OR host NOT IN ('localhost') ;
SET PASSWORD FOR 'root'@'localhost'=PASSWORD('${MYSQL_ROOT_PASSWORD}') ;
GRANT ALL ON *.* TO 'root'@'localhost' WITH GRANT OPTION ;
${rootCreate}
DROP DATABASE IF EXISTS test ;
FLUSH PRIVILEGES ;
EOSQL

        if [ ! -z "$MYSQL_ROOT_PASSWORD" ]; then
            mysql+=( -p"${MYSQL_ROOT_PASSWORD}" )
        fi

        file_env 'MYSQL_DATABASE'
        if [ "$MYSQL_DATABASE" ]; then
            echo "CREATE DATABASE IF NOT EXISTS \`$MYSQL_DATABASE\` ;" | "${mysql[@]}"
            mysql+=( "$MYSQL_DATABASE" )
        fi

        file_env 'MYSQL_USER'
        file_env 'MYSQL_PASSWORD'
        if [ "$MYSQL_USER" -a "$MYSQL_PASSWORD" ]; then
            echo "CREATE USER '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD' ;" | "${mysql[@]}"

            if [ "$MYSQL_DATABASE" ]; then
                echo "GRANT ALL ON \`$MYSQL_DATABASE\`.* TO '$MYSQL_USER'@'%' ;" | "${mysql[@]}"
            fi

            echo 'FLUSH PRIVILEGES ;' | "${mysql[@]}"
        fi

        echo
        for f in /docker-entrypoint-initdb.d/*; do
            case "$f" in
                *.sh)     echo "$0: running $f"; . "$f" ;;
                *.sql)    echo "$0: running $f"; "${mysql[@]}" < "$f"; echo ;;
                *.sql.gz) echo "$0: running $f"; gunzip -c "$f" | "${mysql[@]}"; echo ;;
                *)        echo "$0: ignoring $f" ;;
            esac
            echo
        done

        if [ ! -z "$MYSQL_ONETIME_PASSWORD" ]; then
            "${mysql[@]}" <<-EOSQL
ALTER USER 'root'@'%' PASSWORD EXPIRE;
EOSQL
        fi
        if ! kill -s TERM "$pid" || ! wait "$pid"; then
            echo >&2 'MySQL init process failed.'
            exit 1
        fi

        echo
        echo 'MySQL init process done. Ready for start up.'
        echo
    fi
fi

exec "$@"
