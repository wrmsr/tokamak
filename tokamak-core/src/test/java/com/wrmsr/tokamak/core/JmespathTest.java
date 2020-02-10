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
package com.wrmsr.tokamak.core;

import junit.framework.TestCase;

public class JmespathTest
        extends TestCase
{
    /*
    public static class ConcatFunction
            extends BaseFunction
    {
        public ConcatFunction()
        {
            super(ArgumentConstraints.listOf(2, ArgumentConstraints.anyValue()));
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            StringBuilder sb = new StringBuilder();
            for (FunctionArgument<T> arg : arguments) {
                T value = arg.value();
                if (runtime.typeOf(value) != JmesPathType.NULL) {
                    sb.append(runtime.toString(value));
                }
            }
            return runtime.createString(sb.toString());
        }
    }

    public static class LowerCaseFunction
            extends BaseFunction
    {
        public LowerCaseFunction()
        {
            super(ArgumentConstraints.typeOf(JmesPathType.STRING));
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            T arg = arguments.get(0).value();
            return runtime.createString(runtime.toString(arg).toLowerCase());
        }
    }

    public static class MatchesFunction
            extends RegularExpressionFunction
    {
        public MatchesFunction()
        {
            super(ArgumentConstraints.listOf(2, 3, ArgumentConstraints.typeOf(JmesPathType.STRING)));
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            return runtime.createBoolean(getPattern(runtime, arguments).matcher(getInputString(runtime, arguments)).find());
        }
    }

    public static class NormalizeSpaceFunction
            extends BaseFunction
    {
         // The zero-argument form of this function is not supported
         // since it is just a shorthand of using the current context
         // ('.' in XPath and '@' in JmesPath)
        public NormalizeSpaceFunction()
        {
            super(ArgumentConstraints.typeOf(JmesPathType.STRING));
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            T arg = arguments.get(0).value();
            return runtime.createString(runtime.toString(arg).replaceAll("\\s+", " ").trim());
        }
    }

    public static abstract class RegularExpressionFunction
            extends SubstringMatchingFunction
    {
        public RegularExpressionFunction(ArgumentConstraint argumentConstraints)
        {
            super(argumentConstraints);
        }

        protected <T> String getInputString(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            return getStringParam(runtime, arguments, inputArgumentPosition());
        }

        protected <T> Pattern getPattern(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            String regex = getStringParam(runtime, arguments, patternArgumentPosition());
            Pattern pattern = Pattern.compile(regex, getFlags(runtime, arguments));
            if (pattern.matcher("").matches()) {
                throw new PatternSyntaxException("pattern matches zero-length string", pattern.pattern(), -1);
            }
            return pattern;
        }

        protected <T> int getFlags(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            if (arguments.size() <= flagArgumentPosition()) {
                return 0;
            }
            return convertPatternFlags(getStringParam(runtime, arguments, flagArgumentPosition()));
        }

        protected <T> String getStringParam(Adapter<T> runtime, List<FunctionArgument<T>> arguments, int i)
        {
            return runtime.toString(arguments.get(i).value());
        }

         // Subclasses may override these methods if parameter positions are different than usual.
        protected int inputArgumentPosition()
        {
            return 0;
        }

        protected int patternArgumentPosition()
        {
            return 1;
        }

        protected int flagArgumentPosition()
        {
            return 2;
        }

        private int convertPatternFlags(String flagStr)
        {
            int flags = 0;
            for (int i = 0; i < flagStr.length(); ++i) {
                final char c = flagStr.charAt(i);
                switch (c) {
                    case 's':
                        flags |= Pattern.DOTALL;
                        break;
                    case 'm':
                        flags |= Pattern.MULTILINE;
                        break;
                    case 'i':
                        flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
                        break;
                    case 'x':
                        flags |= Pattern.COMMENTS;
                        break;
                    case 'q':
                        flags |= Pattern.LITERAL;
                        break;
                    default:
                        throw new InvalidRegexFlagException(c, flagStr);
                }
            }
            return flags;
        }

        private class InvalidRegexFlagException
                extends RuntimeException
        {
            private final char unknownFlag;
            private final String flagStr;

            public InvalidRegexFlagException(char flag, String flagStr)
            {
                this.unknownFlag = flag;
                this.flagStr = flagStr;
            }

            public char getUnknownFlag()
            {
                return unknownFlag;
            }

            public String getFlagStr()
            {
                return flagStr;
            }

            public String toString()
            {
                return "Unknown regex flag: " + getUnknownFlag() + " in " + getFlagStr();
            }
        }
    }

    public static class ReplaceFunction
            extends RegularExpressionFunction
    {
        public ReplaceFunction()
        {
            super(ArgumentConstraints.listOf(3, 4, ArgumentConstraints.typeOf(JmesPathType.STRING)));
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            return runtime.createString(getPattern(runtime, arguments)
                    .matcher(getInputString(runtime, arguments))
                    .replaceAll(getStringParam(runtime, arguments, 2)));
        }

        @Override
        protected int flagArgumentPosition()
        {
            return 3;
        }
    }

    public static class SubstringAfterFunction
            extends SubstringMatchingFunction
    {
        public SubstringAfterFunction()
        {
            super(
                    ArgumentConstraints.anyValue(),
                    ArgumentConstraints.anyValue()
            );
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            T arg1 = arguments.get(0).value();
            T arg2 = arguments.get(1).value();
            String haystack = runtime.typeOf(arg1) != JmesPathType.NULL ? runtime.toString(arg1) : "";
            String needle = runtime.typeOf(arg2) != JmesPathType.NULL ? runtime.toString(arg2) : "";

            if (isEmpty(haystack) || isEmpty(needle)) {
                return runtime.createString("");
            }
            final int index = haystack.indexOf(needle);
            if (-1 == index || index + needle.length() > haystack.length()) {
                return runtime.createString("");
            }
            else {
                return runtime.createString(haystack.substring(index + needle.length()));
            }
        }
    }

    public static class SubstringBeforeFunction
            extends SubstringMatchingFunction
    {
        public SubstringBeforeFunction()
        {
            super(
                    ArgumentConstraints.anyValue(),
                    ArgumentConstraints.anyValue()
            );
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            T arg1 = arguments.get(0).value();
            T arg2 = arguments.get(1).value();
            String haystack = runtime.typeOf(arg1) != JmesPathType.NULL ? runtime.toString(arg1) : "";
            String needle = runtime.typeOf(arg2) != JmesPathType.NULL ? runtime.toString(arg2) : "";

            if (isEmpty(haystack) || isEmpty(needle)) {
                return runtime.createString("");
            }
            final int index = haystack.indexOf(needle);
            if (-1 == index) {
                return runtime.createString("");
            }
            else {
                return runtime.createString(haystack.substring(0, index));
            }
        }
    }

    public static abstract class SubstringMatchingFunction
            extends BaseFunction
    {
        SubstringMatchingFunction(ArgumentConstraint... argumentConstraints)
        {
            super(argumentConstraints);
        }

        static boolean isEmpty(String str)
        {
            return str == null || str.length() == 0;
        }
    }

    public static class TokenizeFunction
            extends RegularExpressionFunction
    {
        public TokenizeFunction()
        {
            super(ArgumentConstraints.listOf(1, 3, ArgumentConstraints.typeOf(JmesPathType.STRING)));
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            List<T> result = new LinkedList<>();
            Pattern pattern = (arguments.size() > 1) ? getPattern(runtime, arguments) : Pattern.compile("\\s+");
            for (String parts : pattern.split(getInputString(runtime, arguments), -1)) {
                if (customPattern(arguments) || !isEmpty(parts)) {
                    result.add(runtime.createString(parts));
                }
            }
            return runtime.createArray(result);
        }

        private <T> boolean customPattern(List<FunctionArgument<T>> arguments)
        {
            return 1 < arguments.size();
        }
    }

    public static class TranslateFunction
            extends SubstringMatchingFunction
    {
        public TranslateFunction()
        {
            super(ArgumentConstraints.listOf(3, 3, ArgumentConstraints.typeOf(JmesPathType.STRING)));
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            String arg = runtime.toString(arguments.get(0).value());
            String map = runtime.toString(arguments.get(1).value());
            String trans = runtime.toString(arguments.get(2).value());

            if (isEmpty(arg)) {
                return runtime.createString("");
            }
            else {
                return runtime.createString(replaceChars(arg, map, trans));
            }
        }

        protected static String replaceChars(String input, String from, String to)
        {
            StringBuilder sb = new StringBuilder();
            Map<Character, Character> map = buildTranslationMap(from, to);

            for (int i = 0; i < input.length(); ++i) {
                Character ch = input.charAt(i);
                if (map.containsKey(ch)) {
                    Character tr = map.get(ch);
                    if (null != tr) {
                        sb.append(tr);
                    }
                }
                else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        private static Map<Character, Character> buildTranslationMap(String from, String to)
        {
            Map<Character, Character> map = new HashMap<>();
            for (int i = 0; i < from.length(); ++i) {
                Character ch = from.charAt(i);
                if (!map.containsKey(ch)) {
                    map.put(from.charAt(i), i < to.length() ? to.charAt(i) : null);
                }
            }
            return map;
        }
    }

    public static class UpperCaseFunction
            extends BaseFunction
    {
        public UpperCaseFunction()
        {
            super(ArgumentConstraints.typeOf(JmesPathType.STRING));
        }

        @Override
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments)
        {
            T arg = arguments.get(0).value();
            return runtime.createString(runtime.toString(arg).toUpperCase());
        }
    }

    public void testJmespath()
            throws Throwable
    {
        FunctionRegistry defaultFunctions = FunctionRegistry.defaultRegistry();

        FunctionRegistry customFunctions = defaultFunctions.extend(new LowerCaseFunction(),
                new UpperCaseFunction(),
                new ConcatFunction());

        RuntimeConfiguration configuration = new RuntimeConfiguration.Builder()
                .withFunctionRegistry(customFunctions)
                .build();

        // JmesPath<JsonNode> runtime = new JacksonRuntime(configuration);
        // JsonNode input = null;
        // JsonNode result = runtime.compile("concat(lower_case(first_name), ' ', upper_case(last_name))").search(input);

        JmesPath<Object> runtime = new JcfRuntime(configuration);
        Object input = ImmutableList.of(
                ImmutableMap.of(
                        "first_name", "aBc",
                        "last_name", "dEf"
                ),
                ImmutableMap.of(
                        "first_name", "gHi",
                        "last_name", "JkL"
                )
        );
        for (String q : ImmutableList.of(
                "[]",
                "[].first_name",
                "[].concat(lower_case(first_name), ' ', upper_case(last_name))"
        )) {
            Object result = runtime.compile(q).search(input);
            System.out.println(result);
        }
    }
    */
}
