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
package com.wrmsr.tokamak.core.type.hier.special.struct;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.type.hier.Type;
import com.wrmsr.tokamak.core.type.hier.special.SpecialType;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreCollections.checkOrdered;
import static com.wrmsr.tokamak.util.MoreCollections.enumerate;
import static com.wrmsr.tokamak.util.MoreCollections.immutableMapValues;
import static com.wrmsr.tokamak.util.MoreCollectors.toImmutableMap;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;
import static java.util.function.UnaryOperator.identity;

@Immutable
public abstract class AbstractStructType
        implements SpecialType
{
    @Immutable
    public static final class Member
    {
        private final String name;
        private final Type type;
        private final int position;

        public Member(String name, Type type, int position)
        {
            this.name = checkNotEmpty(name);
            this.type = checkNotNull(type);
            this.position = position;
        }

        public String getName()
        {
            return name;
        }

        public Type getType()
        {
            return type;
        }

        public int getPosition()
        {
            return position;
        }
    }

    private final Map<String, Member> members;

    public AbstractStructType(Map<String, Type> members)
    {
        List<Map.Entry<String, Type>> entryList = ImmutableList.copyOf(
                immutableMapValues(checkOrdered(members), Type.class::cast).entrySet());
        this.members = enumerate(entryList.stream())
                .map(i -> new Member(i.getItem().getKey(), i.getItem().getValue(), i.getIndex()))
                .collect(toImmutableMap(Member::getName, identity()));
    }

    @Override
    public Map<String, Object> getKwargs()
    {
        return immutableMapValues(members, Member::getType);
    }

    public Map<String, Member> getMembers()
    {
        return members;
    }

    public Member getMember(String name)
    {
        return checkNotNull(members.get(checkNotEmpty(name)));
    }
}
