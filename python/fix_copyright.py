import pathlib
import re

LICENSE_LINK = 'http://www.apache.org/licenses/LICENSE-2.0'
LICENSE_PATTERN = re.compile(
    fr'^\s*'
    fr'(?P<license>/\*.*?{re.escape(LICENSE_LINK)}.*?\*/)'
    fr'(?P<dupes>(\s*/\*.*{re.escape(LICENSE_LINK)}.*\*/))*'
    fr'\s*'
    fr'(?P<src>package com\..*)'
    fr'$',
    re.DOTALL
)


def main():
    for path in pathlib.Path('.').glob('tokamak-*/**/*.java'):
        with open(str(path), 'r') as f:
            src = f.read()
        if 'CaseInsensitiveCharStream' in str(path):
            breakpoint()
        match = LICENSE_PATTERN.match(src)
        if match:
            newsrc = match.group('license').strip() + '\n' + match.group('src').strip() + '\n'
            if match.group('dupes'):
                print(newsrc)
            # with open(str(path), 'w') as f:
            #     f.write(newsrc)


if __name__ == '__main__':
    main()
