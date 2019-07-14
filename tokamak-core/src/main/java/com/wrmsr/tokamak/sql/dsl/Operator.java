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

package com.wrmsr.tokamak.sql.dsl;

import java.util.List;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkArgument;

public interface Operator
{
    OptionalInt getNumArgs();

    String getSigil();

    String renderArgs(List<Object> args);

    interface Boolean
            extends Operator
    {
    }

    class Unary
            implements Operator
    {
        private final String sigil;

        public Unary(String sigil)
        {
            this.sigil = sigil;
        }

        @Override
        public OptionalInt getNumArgs()
        {
            return OptionalInt.of(1);
        }

        @Override
        public String getSigil()
        {
            return sigil;
        }

        @Override
        public String renderArgs(List<Object> args)
        {
            checkArgument(args.size() == 1);
            return sigil + args.get(0).toString();
        }
    }

    class Binary
            implements Operator
    {
        private final String sigil;

        public Binary(String sigil)
        {
            this.sigil = sigil;
        }

        @Override
        public OptionalInt getNumArgs()
        {
            return OptionalInt.of(2);
        }

        @Override
        public String getSigil()
        {
            return sigil;
        }

        @Override
        public String renderArgs(List<Object> args)
        {
            checkArgument(args.size() == 2);
            return args.get(0).toString() + " " + sigil + " " + args.get(1).toString();
        }
    }

    Binary eq = new Binary("=");
    Binary ne = new Binary("!=");
    Binary lt = new Binary("<");
    Binary le = new Binary("<=");
    Binary gt = new Binary(">");
    Binary ge = new Binary(">=");

    Binary and = new Binary("&");
    Binary or = new Binary("|");
    Unary not = new Unary("!");

    Binary add = new Binary("+");
    Binary sub = new Binary("-");
    Binary mul = new Binary("*");
    Binary div = new Binary("/");
}
