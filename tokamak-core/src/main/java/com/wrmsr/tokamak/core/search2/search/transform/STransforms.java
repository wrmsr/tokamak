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
package com.wrmsr.tokamak.core.search2.search.transform;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.core.search2.search.node.SNode;
import com.wrmsr.tokamak.core.search2.search.node.SSequence;
import com.wrmsr.tokamak.core.search2.search.node.visitor.SNodeRewriter;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public final class STransforms
{
    private STransforms()
    {
    }

    public static SNode inlineSequences(SNode root)
    {
        return root.accept(new SNodeRewriter<Void>()
        {
            @Override
            public SNode visitSequence(SSequence node, Void context)
            {
                return new SSequence(
                        node.getSources().stream()
                                .map(i -> process(i, context))
                                .map(i -> i instanceof SSequence ? ((SSequence) i).getSources() : ImmutableList.of(i))
                                .flatMap(List::stream)
                                .collect(toImmutableList()));
            }
        }, null);
    }
}
