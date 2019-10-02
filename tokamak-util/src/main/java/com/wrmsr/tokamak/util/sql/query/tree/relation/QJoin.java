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
package com.wrmsr.tokamak.util.sql.query.tree.relation;

import com.wrmsr.tokamak.util.sql.query.tree.expression.QExpression;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MorePreconditions.checkNotEmpty;

@Immutable
public final class QJoin
        extends QRelation
{
    public interface Mode
    {
        String getText();
    }

    private static final class ModeImpl
            implements Mode
    {
        private final String text;

        public ModeImpl(String text)
        {
            this.text = checkNotEmpty(text);
        }

        @Override
        public String getText()
        {
            return text;
        }
    }

    public static final Mode INNER = new ModeImpl("INNER");
    public static final Mode LEFT_OUTER = new ModeImpl("LEFT OUTER");
    public static final Mode RIGHT_OUTER = new ModeImpl("RIGHT OUTER");
    public static final Mode CROSS = new ModeImpl("CROSS");

    private QRelation left;
    private Optional<Mode> mode;
    private QRelation right;
    private Optional<QExpression> on;

    public QJoin(QRelation left, Optional<Mode> mode, QRelation right, Optional<QExpression> on)
    {
        this.left = checkNotNull(left);
        this.mode = checkNotNull(mode);
        this.right = checkNotNull(right);
        this.on = checkNotNull(on);
    }

    public QRelation getLeft()
    {
        return left;
    }

    public Optional<Mode> getMode()
    {
        return mode;
    }

    public QRelation getRight()
    {
        return right;
    }

    public Optional<QExpression> getOn()
    {
        return on;
    }

    @Override
    public <R, C> R accept(QRelationVisitor<R, C> visitor, C context)
    {
        return visitor.visitJoin(this, context);
    }
}
