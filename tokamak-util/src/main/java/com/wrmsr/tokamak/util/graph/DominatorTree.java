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

// https://svn.apache.org/repos/asf/flex/falcon/trunk/compiler/src/org/apache/flex/abc/graph/algorithms/DominatorTree.java
package com.wrmsr.tokamak.util.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of the O(n log n) Lengauer-Tarjan algorithm for building the
 * <a href="http://en.wikipedia.org/wiki/Dominator_%28graph_theory%29">dominator tree</a>
 * of a flowgraph.
 */
public class DominatorTree<V, E>
{
    public interface DirectedGraph<V, E>
    {
        Collection<V> getSuccessors(V vertex);

        Iterator<V> depthFirstIterate(V root);
    }

    private final DirectedGraph<V, E> graph;

    private final Dfs<V, E> dfs;

    private final Map<V, V> idom;

    public DominatorTree(DirectedGraph<V, E> graph, V root)
    {
        this.graph = checkNotNull(graph);
        dfs = new Dfs<>(graph, root);
        idom = new IdomComputer<>(dfs).computeIdoms();
    }

    public Map<V, V> getImmediateDominators()
    {
        return idom;
    }

    private SupplierLazyValue<SetMultimap<V, V>> dominatorTree = new SupplierLazyValue<>();

    public SetMultimap<V, V> getDominatorTree()
    {
        return dominatorTree.get(() -> {
            SetMultimap<V, V> dominatorTree = HashMultimap.create();

            for (V node : idom.keySet()) {
                dominatorTree.get(idom.get(node)).add(node);
            }

            return dominatorTree;
        });
    }

    private SupplierLazyValue<SetMultimap<V, V>> dominanceFrontiers = new SupplierLazyValue<>();

    public SetMultimap<V, V> getDominanceFrontiers()
    {
        return dominanceFrontiers.get(() -> {
            SetMultimap<V, V> dominanceFrontiers = HashMultimap.create();

            for (V x : getReverseTopologicalTraversal()) {
                Set<V> dfx = dominanceFrontiers.get(x);

                //  Compute DF(local)
                for (V y : graph.getSuccessors(x)) {
                    if (idom.get(y) != x) {
                        dfx.add(y);
                    }
                }

                //  Compute DF(up)
                for (V z : getDominatorTree().get(x)) {
                    for (V y : dominanceFrontiers.get(z)) {
                        if (idom.get(y) != x) {
                            dfx.add(y);
                        }
                    }
                }
            }

            return ImmutableSetMultimap.copyOf(dominanceFrontiers);
        });
    }

    private final SupplierLazyValue<List<V>> topologicalTraversal = new SupplierLazyValue<>();

    public List<V> getTopologicalTraversal()
    {
        return topologicalTraversal.get(() -> ImmutableList.copyOf(getToplogicalTraversalImpl()));
    }

    private final SupplierLazyValue<List<V>> reverseTopologicalTraversal = new SupplierLazyValue<>();

    public List<V> getReverseTopologicalTraversal()
    {
        return reverseTopologicalTraversal.get(() -> ImmutableList.copyOf(getToplogicalTraversalImpl().descendingIterator()));
    }

    private static final class Dfs<V, E>
    {
        /**
         * Semidominator numbers by block.
         */
        private final Map<V, Integer> semi;

        /**
         * Blocks in DFS order; used to look up a block from its semidominator
         * numbering.
         */
        private final List<V> vertex;

        /**
         * Parents by block.
         */
        private final Map<V, V> parent;

        /**
         * Predecessors by block.
         */
        private final Multimap<V, V> pred;

        /**
         * Auxiliary data structure used by the O(m log n) eval/link implementation:
         * node with least semidominator seen during traversal of a path from node
         * to subtree root in the forest.
         */
        private final Map<V, V> label;

        private Dfs(DirectedGraph<V, E> graph, V root)
        {
            Map<V, Integer> semi = new HashMap<>();
            ImmutableList.Builder<V> vertex = ImmutableList.builder();
            ImmutableMap.Builder<V, V> parent = ImmutableMap.builder();
            ImmutableSetMultimap.Builder<V, V> pred = ImmutableSetMultimap.builder();
            ImmutableMap.Builder<V, V> label = ImmutableMap.builder();

            Iterator<V> it = graph.depthFirstIterate(root);

            while (it.hasNext()) {
                V node = it.next();

                if (!semi.containsKey(node)) {
                    vertex.add(node);

                    //  Initial assumption: the node's semidominator is itself.
                    semi.put(node, semi.size());
                    label.put(node, node);

                    for (V child : graph.getSuccessors(node)) {
                        pred.put(child, node);
                        if (!semi.containsKey(child)) {
                            parent.put(child, node);
                        }
                    }
                }
            }

            this.semi = ImmutableMap.copyOf(semi);
            this.vertex = vertex.build();
            this.parent = parent.build();
            this.pred = pred.build();
            this.label = label.build();
        }
    }

    private static final class IdomComputer<V, E>
    {
        private final Dfs<V, E> dfs;

        /**
         * Auxiliary data structure used by the O(m log n) eval/link implementation:
         * ancestor relationships in the forest (the processed tree as it's built
         * back up).
         */
        private final Map<V, V> ancestor = new HashMap<>();

        public IdomComputer(Dfs<V, E> dfs)
        {
            this.dfs = dfs;
        }

        /**
         * Steps 2, 3, and 4 of Lengauer-Tarjan.
         */
        public Map<V, V> computeIdoms()
        {
            Map<V, V> idom = new HashMap<>();
            SetMultimap<V, V> bucket = HashMultimap.create();
            ancestor.clear();

            int lastSemiNumber = dfs.semi.size() - 1;

            for (int i = lastSemiNumber; i > 0; i--) {
                V w = dfs.vertex.get(i);
                V p = dfs.parent.get(w);

                //  step 2: compute semidominators
                //  for each v in pred(w)...
                int semidominator = dfs.semi.get(w);
                for (V v : dfs.pred.get(w)) {
                    semidominator = Math.min(semidominator, dfs.semi.get(eval(v)));
                }

                dfs.semi.put(w, semidominator);
                bucket.get(dfs.vertex.get(semidominator)).add(w);

                //  Link w into the forest via its parent, p
                link(p, w);

                //  step 3: implicitly compute idominators
                //  for each v in bucket(parent(w)) ...
                for (V v : bucket.get(p)) {
                    V u = eval(v);

                    if (dfs.semi.get(u) < dfs.semi.get(v)) {
                        idom.put(v, u);
                    }
                    else {
                        idom.put(v, p);
                    }
                }

                bucket.get(p).clear();
            }

            // step 4: explicitly compute idominators
            for (int i = 1; i <= lastSemiNumber; i++) {
                V w = dfs.vertex.get(i);

                if (idom.get(w) != dfs.vertex.get(dfs.semi.get(w))) {
                    idom.put(w, idom.get(idom.get(w)));
                }
            }

            return ImmutableMap.copyOf(idom);
        }

        /**
         * Extract the node with the least-numbered semidominator in the (processed) ancestors of the given node.
         *
         * @param v - the node of interest.
         * @return "If v is the root of a tree in the forest, return v. Otherwise,
         * let r be the root of the tree which contains v. Return any vertex u != r
         * of miniumum semi(u) on the path r-*v."
         */
        private V eval(V v)
        {
            //  This version of Lengauer-Tarjan implements
            //  eval(v) as a path-compression procedure.
            compress(v);
            return dfs.label.get(v);
        }

        /**
         * Traverse ancestor pointers back to a subtree root, then propagate the least semidominator seen along this path through the "label" map.
         */
        private void compress(V v)
        {
            Stack<V> worklist = new Stack<V>();
            worklist.add(v);

            V a = ancestor.get(v);

            //  Traverse back to the subtree root.
            while (ancestor.containsKey(a)) {
                worklist.push(a);
                a = ancestor.get(a);
            }

            //  Propagate semidominator information forward.
            V ancestor = worklist.pop();
            int leastSemi = dfs.semi.get(dfs.label.get(ancestor));

            while (!worklist.empty()) {
                V descendent = worklist.pop();
                int currentSemi = dfs.semi.get(dfs.label.get(descendent));

                if (currentSemi > leastSemi) {
                    dfs.label.put(descendent, dfs.label.get(ancestor));
                }
                else {
                    leastSemi = currentSemi;
                }

                //  Prepare to process the next iteration.
                ancestor = descendent;
            }
        }

        /**
         * Simple version of link(parent,child) simply links the child into the
         * parent's forest, with no attempt to balance the subtrees or otherwise
         * optimize searching.
         */
        private void link(V parent, V child)
        {
            ancestor.put(child, parent);
        }
    }

    private final SupplierLazyValue<LinkedList<V>> topologicalTraversalImpl = new SupplierLazyValue<>();

    /**
     * Create/fetch the topological traversal of the dominator tree.
     *
     * @return {@link this.topologicalTraversal}, the traversal of the dominator tree such that for any node n with a dominator,
     * n appears before idom(n).
     */
    private LinkedList<V> getToplogicalTraversalImpl()
    {
        return topologicalTraversalImpl.get(() -> {
            LinkedList<V> topologicalTraversalImpl = new LinkedList<V>();

            for (V node : dfs.vertex) {
                int idx = topologicalTraversalImpl.indexOf(idom.get(node));

                if (idx != -1) {
                    topologicalTraversalImpl.add(idx + 1, node);
                }
                else {
                    topologicalTraversalImpl.add(node);
                }
            }

            return topologicalTraversalImpl;
        });
    }
}
