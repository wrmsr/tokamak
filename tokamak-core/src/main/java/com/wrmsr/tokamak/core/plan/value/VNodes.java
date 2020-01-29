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
package com.wrmsr.tokamak.core.plan.value;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.exec.Executable;
import com.wrmsr.tokamak.core.plan.node.PFunction;
import com.wrmsr.tokamak.core.type.hier.Type;

import java.util.Optional;

import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public final class VNodes
{
    private VNodes()
    {
    }

    public static VConstant constant(Object value, Type type)
    {
        return new VConstant(value, type);
    }

    public static VField field(String field)
    {
        return new VField(field);
    }

    public static VFunction function(PFunction function, Iterable<VNode> args)
    {
        return new VFunction(function, ImmutableList.copyOf(args));
    }

    public static VFunction function(PFunction function, VNode... args)
    {
        return new VFunction(function, ImmutableList.copyOf(args));
    }

    public static Optional<String> getIdentityFunctionDirectValueField(VFunction fn)
    {
        if (fn.getFunction().getPurity() != Executable.Purity.IDENTITY) {
            return Optional.empty();
        }
        VNode arg = checkSingle(fn.getArgs());
        if (!(arg instanceof VField)) {
            return Optional.empty();
        }
        return Optional.of(((VField) arg).getField());
    }
}
