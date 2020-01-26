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
package com.wrmsr.tokamak.core.plan.node.visitor;

import com.google.common.collect.ImmutableSet;
import com.wrmsr.tokamak.core.layout.field.annotation.FieldAnnotation;
import com.wrmsr.tokamak.core.plan.node.PNode;
import com.wrmsr.tokamak.core.plan.node.annotation.PNodeAnnotation;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollection;
import com.wrmsr.tokamak.core.util.annotation.AnnotationCollectionMap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

public final class PNodeRewriters
{
    private PNodeRewriters()
    {
    }

    public static PNode rewrite(PNode root, Map<PNode, PNode> newNodes)
    {
        return new PNodeRewriter<Void>(newHashMap(newNodes)) {}.process(root, null);
    }

    public static PNode rewriteOne(
            PNode targetNode,
            Optional<String> newName,
            Optional<AnnotationCollection<PNodeAnnotation>> newAnnotations,
            Optional<AnnotationCollectionMap<String, FieldAnnotation>> newFieldAnnotations)
    {
        Set<PNode> targetSources = ImmutableSet.copyOf(targetNode.getSources());
        return new PNodeRewriter<Void>()
        {
            @Override
            public PNode process(PNode node, Void context)
            {
                if (node == targetNode) {
                    return super.process(targetNode, context);
                }
                else if (targetSources.contains(node)) {
                    return node;
                }
                else {
                    throw new IllegalStateException(Objects.toString(node));
                }
            }

            @Override
            protected String visitNodeName(String name, Void context)
            {
                if (name.equals(targetNode.getName())) {
                    return newName.orElse(targetNode.getName());
                }
                else {
                    return name;
                }
            }

            @Override
            protected AnnotationCollection<PNodeAnnotation> visitNodeAnnotations(
                    PNode node,
                    AnnotationCollection<PNodeAnnotation> annotations,
                    Void context)
            {
                if (node == targetNode) {
                    return newAnnotations.orElse(annotations);
                }
                else {
                    return annotations;
                }
            }

            @Override
            protected AnnotationCollectionMap<String, FieldAnnotation> visitNodeFieldAnnotations(
                    PNode node,
                    AnnotationCollectionMap<String, FieldAnnotation> fieldAnnotations,
                    Void context)
            {
                if (node == targetNode) {
                    return newFieldAnnotations.orElse(fieldAnnotations);
                }
                else {
                    return fieldAnnotations;
                }
            }
        }.process(targetNode, null);
    }
}
