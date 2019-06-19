FROM ubuntu:14.04
COPY .dockertimestamp /

RUN apt-get update && apt-get install -y apt-utils

RUN ( \
    apt-get install -y \
        gcc \
        unzip \
)

WORKDIR /tokamak

CMD ["python", "-c", "import signal, subprocess, sys, os; [signal.signal(s, lambda *_: sys.exit(0)) for s in [signal.SIGTERM, signal.SIGINT]]; subprocess.Popen(['sh', '-c', 'sleep infinity']); os.wait()"]
