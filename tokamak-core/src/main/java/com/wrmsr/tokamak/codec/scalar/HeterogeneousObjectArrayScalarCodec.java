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
package com.wrmsr.tokamak.codec.scalar;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.codec.Input;
import com.wrmsr.tokamak.codec.Output;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class HeterogeneousObjectArrayScalarCodec
        implements ScalarCodec<Object[]>
{
    private final List<ScalarCodec> children;

    public HeterogeneousObjectArrayScalarCodec(List<ScalarCodec> children)
    {
        this.children = ImmutableList.copyOf(children);
    }

    public List<ScalarCodec> getChildren()
    {
        return children;
    }

    @Override
    public void encode(Object[] value, Output output)
    {
        checkArgument(value.length == children.size());
        for (int i = 0; i < value.length; ++i) {
            children.get(i).encode(value[i], output);
        }
    }

    @Override
    public Object[] decode(Input input)
    {
        Object[] ret = new Object[children.size()];
        for (int i = 0; i < children.size(); ++i) {
            ret[i] = children.get(i).decode(input);
        }
        return ret;
    }
}
