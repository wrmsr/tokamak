# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import io
import os
import subprocess
import tempfile
import typing as ta

import sqlalchemy as sa
# import sqlalchemy.dialects.mysql
# import sqlalchemy.dialects.postgresql


def is_sa(obj: ta.Any) -> bool:
    if isinstance(obj, type(sa)):
        return obj.__name__.startswith('sqlalchemy.')
    elif isinstance(obj, type):
        return obj.__module__.startswith('sqlalchemy.')
    else:
        raise TypeError(obj)


def get_sa_types() -> ta.Set[type]:
    seen_mods = set()
    mod_stack = [sa]
    ret = set()
    while mod_stack:
        mod = mod_stack.pop()
        print(mod)
        seen_mods.add(mod)
        for att in dir(mod):
            val = getattr(mod, att)
            if isinstance(val, type(sa)):
                if is_sa(val) and val not in seen_mods:
                    mod_stack.append(val)
            elif isinstance(val, type):
                if is_sa(val):
                    print((att, val))
                    ret.add(val)
    return ret


def build_types_gv(typs: ta.Iterable[type]) -> str:
    typs = set(typs)
    sb = io.StringIO()
    idxs_by_typ = {t: i for i, t in enumerate(sorted(typs, key=lambda t: (t.__module__, t.__qualname__)))}
    sb.write('digraph G {\n')
    sb.write('rankdir=LR;\n')
    for typ, i in idxs_by_typ.items():
        sb.write(f't{i} [label="{".".join(typ.__module__.split(".")[1:])}.{typ.__qualname__}"];\n')
        for btyp in typ.__bases__:
            if btyp in idxs_by_typ:
                bi = idxs_by_typ[btyp]
                sb.write(f't{bi} -> t{i};\n')
    sb.write('}\n')
    sb.seek(0)
    return sb.read()


def write_gv_pdf(gv_src: str) -> str:
    tmp_dir = tempfile.mkdtemp()
    gv_path = os.path.join(tmp_dir, 'o.gv')
    with open(gv_path, 'w', encoding='utf-8') as f:
        f.write(gv_src)
    pdf_path = os.path.join(tmp_dir, 'o.pdf')
    with open(pdf_path, 'wb') as f:
        subprocess.run(
            ['/usr/local/bin/dot', '-Tpdf', 'o.gv'],
            cwd=tmp_dir,
            stdout=f,
            timeout=10,
        )
    return pdf_path


def open_pdf(path: str) -> None:
    subprocess.run(
        ['/usr/bin/open', path],
        timeout=10,
    )


open_pdf(write_gv_pdf(build_types_gv(get_sa_types())))
