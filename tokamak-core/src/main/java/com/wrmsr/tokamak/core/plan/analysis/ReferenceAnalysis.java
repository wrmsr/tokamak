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
package com.wrmsr.tokamak.core.plan.analysis;

import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.PNodeField;
import com.wrmsr.tokamak.core.plan.node.PState;
import com.wrmsr.tokamak.core.plan.node.visitor.CachingPNodeVisitor;
import com.wrmsr.tokamak.core.plan.node.visitor.PNodeVisitors;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ReferenceAnalysis
{
    /*
    TODO:
     - include OriginAnalysis or not?
    */

    private ReferenceAnalysis()
    {
    }

    @Immutable
    public static abstract class Entry
    {
        protected final PNode referrer;
        protected final PNodeField referent;

        protected Entry(PNode referrer, PNodeField referent)
        {
            this.referrer = checkNotNull(referrer);
            this.referent = checkNotNull(referent);
        }

        public PNode getReferrer()
        {
            return referrer;
        }

        public PNodeField getReferent()
        {
            return referent;
        }
    }

    @Immutable
    public static final class GenericEntry
            extends Entry
    {
        protected final Object object;

        public GenericEntry(PNode referrer, PNodeField referent, Object object)
        {
            super(referrer, referent);
            this.object = checkNotNull(object);
        }

        public Object getObject()
        {
            return object;
        }
    }

    public static ReferenceAnalysis analyze(Plan plan)
    {
        List<Entry> entries = new ArrayList<>();

        PNodeVisitors.postWalk(plan.getRoot(), new CachingPNodeVisitor<Void, Void>()
        {
            @Override
            public Void visitState(PState node, Void context)
            {
                node.getInvalidations().forEach((n, i) -> {
                    entries.add(new GenericEntry(node, PNodeField.of(plan.getNode(n), i.getField()), i));
                });

                return null;
            }
        }, null);

        throw new IllegalStateException();
    }
}
