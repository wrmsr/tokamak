FROM ubuntu:18.04
COPY .dockertimestamp /


# Packages

RUN sed -i 's/^# deb-src/deb-src/' /etc/apt/sources.list
RUN apt-get update && apt-get install -y apt-utils

RUN ( \
    apt-get install -y \
\
        build-essential \
        curl \
        dnsutils \
        gcc \
        gdb \
        git \
        htop \
        jq \
        less \
        make \
        man \
        moreutils \
        ncdu \
        net-tools \
        netcat \
        perl \
        python \
        silversearcher-ag \
        socat \
        software-properties-common \
        strace \
        sudo \
        tcpdump \
        tmux \
        unzip \
        vim \
        wget \
        zip \
\
)


# Python build

RUN apt-get build-dep -y python3

RUN ( \
    apt-get install -y \
\
        libbz2-dev \
        libdb5.3-dev \
        libexpat1-dev \
        libffi-dev \
        libgdbm-dev \
        liblzma-dev \
        libncurses5-dev \
        libncursesw5-dev \
        libreadline-dev \
        libsqlite3-dev \
        libssl-dev \
        uuid-dev \
        zlib1g-dev \
\
)


# Innotop

RUN ( \
    export INNOTOP_VERSION='1.12.0' && \
    export INNOTOP_INST_DIR=$(mktemp -d) && \
    cd "$INNOTOP_INST_DIR" && \
    wget "https://github.com/innotop/innotop/archive/v$INNOTOP_VERSION.tar.gz" && \
    tar xvf "v$INNOTOP_VERSION.tar.gz" && \
    cd "innotop-$INNOTOP_VERSION/" && \
    perl Makefile.PL && \
    make install && \
    rm -rf "$INNOTOP_INST_DIR" \
)


# Percona Tools

RUN ( \
    apt-get install -y \
\
        libdbd-mysql-perl \
        libdbi-perl \
        libio-socket-ssl-perl \
        libterm-readkey-perl \
\
)

RUN ( \
    export PERCONA_VERSION='3.0.13' && \
    export PERCONA_SUFFIX='-1.xenial_amd64' && \
    export PERCONA_INST_DIR=$(mktemp -d) && \
    cd "$PERCONA_INST_DIR" && \
    wget "https://www.percona.com/downloads/percona-toolkit/${PERCONA_VERSION}/binary/debian/xenial/x86_64/percona-toolkit_${PERCONA_VERSION}${PERCONA_SUFFIX}.deb" && \
    dpkg -i "percona-toolkit_${PERCONA_VERSION}${PERCONA_SUFFIX}.deb" && \
    rm -f "percona-toolkit_${PERCONA_VERSION}${PERCONA_SUFFIX}.deb" && \
    rm -rf "$PERCONA_INST_DIR" \
)


# JDK

RUN ( \
    sudo add-apt-repository ppa:openjdk-r/ppa && \
    sudo apt-get update && \
    sudo apt-get install -y openjdk-8-jdk openjdk-13-jdk \
)

RUN ( \
    # https://stackoverflow.com/questions/6784463/error-trustanchors-parameter-must-be-non-empty \
    /usr/bin/printf '\xfe\xed\xfe\xed\x00\x00\x00\x02\x00\x00\x00\x00\xe2\x68\x6e\x45\xfb\x43\xdf\xa4\xd9\x92\xdd\x41\xce\xb6\xb2\x1c\x63\x30\xd7\x92' > /etc/ssl/certs/java/cacerts && \
    /var/lib/dpkg/info/ca-certificates-java.postinst configure \
)


# pyenv

RUN ( \
    cd ~ && git clone https://github.com/pyenv/pyenv .pyenv && \
	echo "\
        PYENV_ROOT=~/.pyenv \n\
        PATH=\"~/.pyenv/bin:$PATH\" \n\
    " >> ~/.bashrc \
)


# jp

RUN ( \
    sudo wget https://github.com/jmespath/jp/releases/download/0.1.2/jp-linux-amd64 -O /usr/local/bin/jp && \
    sudo chmod +x /usr/local/bin/jp \
)


# Configuration files

RUN echo "\
setw -g mode-keys vi \n\
set -g status-keys vi \n\
set -sg escape-time 0 \n\
set -g status-fg black \n\
set-option -g history-limit 20000 \n\
" >> ~/.tmux.conf

RUN echo "\
set number \n\
syntax on \n\
" >> ~/.vimrc

RUN echo "\
TERM=screen-256color \n\
" >> ~/.bashrc

RUN echo 'source ~/.bashrc' >> ~/.bash_profile


# Entrypoint

WORKDIR /tokamak

CMD ["python", "-c", "import signal, subprocess, sys, os; [signal.signal(s, lambda *_: sys.exit(0)) for s in [signal.SIGTERM, signal.SIGINT]]; subprocess.Popen(['sh', '-c', 'sleep infinity']); os.wait()"]
