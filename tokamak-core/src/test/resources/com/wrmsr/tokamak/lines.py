import json
import sys


def _main():
    while True:
        line = sys.stdin.readline()
        if not line:
            break
        obj = json.loads(line)
        obj['processed'] = True
        print(json.dumps(obj))
        sys.stdout.flush()


if __name__ == '__main__':
    _main()
