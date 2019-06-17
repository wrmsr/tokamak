FROM mysql:5.7
COPY .dockertimestamp /

RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 5072E1F5

RUN \
  sed -i "s/.*jessie-updates.*/deb [check-valid-until=no] http:\/\/archive.debian.org\/debian jessie-backports main/" /etc/apt/sources.list && \
  apt-get -o Acquire::Check-Valid-Until=false update && \
  apt-get install -y \
    apt-utils \
    iputils-ping \
    sudo \
    vim

RUN ( \
    echo "root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    echo "mysql ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers \
)

COPY docker/tokamak-mysql/docker-entrypoint.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/docker-entrypoint.sh
