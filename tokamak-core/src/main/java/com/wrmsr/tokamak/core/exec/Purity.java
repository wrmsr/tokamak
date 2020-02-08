/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wrmsr.tokamak.core.exec;

import static com.google.common.base.Preconditions.checkArgument;

public enum Purity
{
    /*
    TODO:
     - bijection?
    */

    PURE(true),
    IMPURE(false),

    CONST(true) {
        @Override
        public void validate(Executable executable)
        {
            checkArgument(executable.getType().getParams().isEmpty());
        }
    },
    EXTERNAL(false) {
        @Override
        public void validate(Executable executable)
        {
            checkArgument(executable.getType().getParams().isEmpty());
        }
    },

    IDENTITY(true) {
        @Override
        public void validate(Executable executable)
        {
            checkArgument(executable.getType().getParams().size() == 1);
        }
    },
    TRANSMUTATION(true) {
        @Override
        public void validate(Executable executable)
        {
            checkArgument(executable.getType().getParams().size() == 1);
        }
    },

    ;

    private final boolean isDeterministic;

    Purity(boolean isDeterministic)
    {
        this.isDeterministic = isDeterministic;
    }

    public boolean isDeterministic()
    {
        return isDeterministic;
    }

    public void validate(Executable executable)
    {
    }
}
