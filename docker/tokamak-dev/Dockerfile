FROM ubuntu:14.04
COPY .dockertimestamp /

RUN apt-get update && apt-get install -y apt-utils

RUN ( \
    apt-get install -y \
\
        dnsutils \
        gcc \
        gdb \
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
        tcpdump \
        tmux \
        unzip \
        vim \
        wget \
        zip \
\
)

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

RUN ( \
    apt-get install -y \
        libdbd-mysql-perl \
        libdbi-perl \
        libio-socket-ssl-perl \
        libterm-readkey-perl \
)

RUN ( \
    export PERCONA_VERSION='3.0.13' && \
    export PERCONA_SUFFIX='-1.trusty_amd64' && \
    export PERCONA_INST_DIR=$(mktemp -d) && \
    cd "$PERCONA_INST_DIR" && \
    wget "https://www.percona.com/downloads/percona-toolkit/${PERCONA_VERSION}/binary/debian/trusty/x86_64/percona-toolkit_${PERCONA_VERSION}${PERCONA_SUFFIX}.deb" && \
    dpkg -i "percona-toolkit_${PERCONA_VERSION}${PERCONA_SUFFIX}.deb" && \
    rm -f "percona-toolkit_${PERCONA_VERSION}${PERCONA_SUFFIX}.deb" && \
    rm -rf "$PERCONA_INST_DIR" \
)

RUN ( \
    sudo add-apt-repository ppa:openjdk-r/ppa && \
    sudo apt-get update && \
    sudo apt-get install -y openjdk-8-jdk \
)

RUN echo -e "\
setw -g mode-keys vi \n\
set -g status-keys vi \n\
set -sg escape-time 0 \n\
set -g status-fg black \n\
set-option -g history-limit 20000 \n\
" > ~/.tmux.conf

RUN echo -e "\
set number \n\
syntax on \n\
" > ~/.vimrc

RUN echo -e "\
TERM=screen-256color \n\
" > ~/.bashrc

RUN echo 'if [ -f ~/.bashrc ]; then source ~/.bashrc fi' > ~/.bash_profile

WORKDIR /tokamak

CMD ["python", "-c", "import signal, subprocess, sys, os; [signal.signal(s, lambda *_: sys.exit(0)) for s in [signal.SIGTERM, signal.SIGINT]]; subprocess.Popen(['sh', '-c', 'sleep infinity']); os.wait()"]
