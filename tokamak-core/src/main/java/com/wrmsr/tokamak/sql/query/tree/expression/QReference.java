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

package com.wrmsr.tokamak.sql.query.tree.expression;

import com.wrmsr.tokamak.sql.query.QName;

import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class QReference
        extends QExpression
{
    private final QName name;

    public QReference(QName name)
    {
        this.name = checkNotNull(name);
    }

    @Override
    public <R, C> R accept(QExpressionVisitor<R, C> visitor, C context)
    {
        return visitor.visitQReference(this, context);
    }
}
