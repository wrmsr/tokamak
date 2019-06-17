FROM postgres:9.6
COPY .dockertimestamp /

RUN \
  sed -i "s/.*jessie-updates.*/deb [check-valid-until=no] http:\/\/archive.debian.org\/debian jessie-backports main/" /etc/apt/sources.list && \
  apt-get -o Acquire::Check-Valid-Until=false update && \
  apt-get install -y \
    apt-utils \
    iputils-ping \
    vim

# RUN \
#   sed -i 's/^#wal_level = minimal/wal_level = logical/' /usr/share/postgresql/postgresql.conf.sample && \
#   sed -i 's/^#max_replication_slots = 0/max_replication_slots = 10/' /usr/share/postgresql/postgresql.conf.sample && \
#   sed -i 's/^#max_wal_senders = 0/max_wal_senders = 10/' /usr/share/postgresql/postgresql.conf.sample && \
#   echo 'echo "host replication all 0.0.0.0/0 $authMethod" >> "$PGDATA/pg_hba.conf"' > /docker-entrypoint-initdb.d/add-replication-hba.sh

ENV PG_MAX_WAL_SENDERS 8
ENV PG_WAL_KEEP_SEGMENTS 8

COPY docker/tokamak-postgres/setup-replication.sh /docker-entrypoint-initdb.d/
COPY docker/tokamak-postgres/docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod +x /docker-entrypoint-initdb.d/setup-replication.sh /docker-entrypoint.sh

RUN ( \
    apt-get install -y \
      gcc \
      make \
      postgresql-server-dev-9.6 \
      wget \
      && \
    mkdir /build && \
    cd /build && \
    wget https://github.com/eulerto/wal2json/archive/wal2json_1_0.tar.gz && \
    tar xvzf wal2json_1_0.tar.gz && \
    cd wal2json-wal2json_1_0 && \
    export PATH=pg_config:$PATH && \
    USE_PGXS=1 make && \
    USE_PGXS=1 make install \
)
