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
package com.wrmsr.tokamak.parse.tree;

import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.parse.tree.visitor.AstVisitor;

import java.util.List;

public final class QualifiedName
        extends Expression
{
    private final List<String> parts;

    public QualifiedName(List<String> parts)
    {
        this.parts = ImmutableList.copyOf(parts);
    }

    @Override
    public String toString()
    {
        return "QualifiedName{" +
                "parts=" + parts +
                '}';
    }

    public List<String> getParts()
    {
        return parts;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitQualifiedName(this, context);
    }
}
