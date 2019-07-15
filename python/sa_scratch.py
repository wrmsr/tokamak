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
import sqlalchemy as sa


def main():
    engine: sa.engine.Engine = sa.create_engine('sqlite://')
    with engine.connect() as conn:
        stmt = sa.select([
            sa.literal_column('1')
        ])
        ret = conn.scalar(stmt)
        print(ret)

        conn.execute("""create table t(id integer primary key, i integer, s varchar(1024))""")

        stmt = sa.select([
            sa.column('i')
        ]).select_from(
            sa.table('t')
        ).where(
            sa.column('id') == 420
        )
        ret = conn.scalar(stmt)
        print(ret)


if __name__ == '__main__':
    main()
