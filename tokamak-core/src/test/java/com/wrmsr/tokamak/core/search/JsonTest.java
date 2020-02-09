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
package com.wrmsr.tokamak.core.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.wrmsr.tokamak.core.search.node.SNode;
import com.wrmsr.tokamak.util.json.Json;
import junit.framework.TestCase;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class JsonTest
        extends TestCase
{
    private String readResource(String path)
            throws Exception
    {
        try (InputStream in = getClass().getResourceAsStream(path);
                InputStreamReader isr = new InputStreamReader(in)) {
            return CharStreams.toString(isr);
        }
    }

    private static List<String> splitLines(String str)
    {
        return ImmutableList.copyOf(Splitter.onPattern("\r?\n").trimResults().omitEmptyStrings().split(str));
    }

    private static final class Case
    {
        private final String expression;
        private final Object result;
        private final Object error;
        private final String bench;
        private final String comment;

        @JsonCreator
        public Case(
                @JsonProperty("expression") String expression,
                @JsonProperty("result") Object result,
                @JsonProperty("error") Object error,
                @JsonProperty("bench") String bench,
                @JsonProperty("comment") String comment)
        {
            this.expression = expression;
            this.result = result;
            this.error = error;
            this.bench = bench;
            this.comment = comment;
        }
    }

    private static final class Suite
    {
        private final Object given;
        private final String comment;
        private final List<Case> cases;

        @JsonCreator
        public Suite(
                @JsonProperty("given") Object given,
                @JsonProperty("comment") String comment,
                @JsonProperty("cases") List<Case> cases)
        {
            this.given = given;
            this.comment = comment;
            this.cases = cases;
        }
    }

    public void testJsonFiles()
            throws Exception
    {
        ObjectMapper om = Json.mapper();
        List<String> files = splitLines(readResource("json")).stream()
                .filter(f -> f.endsWith(".json") && !f.equals("schema.json"))
                .collect(toImmutableList());

        for (String file : files) {
            System.out.println(file);
            System.out.println();

            List<Suite> suites = om.readValue(readResource("json/" + file), new TypeReference<List<Suite>>() {});

            for (Suite suite : suites) {
                System.out.println(suite.comment);
                System.out.println(suite.given);
                System.out.println();

                for (Case scase : suite.cases) {
                    if (scase.error == null) {
                        try {
                            System.out.println(scase.comment);
                            System.out.println(scase.expression);
                            SNode search = SearchParsing.build(SearchParsing.parse(scase.expression).singleExpression());
                            System.out.println(search);

                            System.out.println(scase.result);

                            Evaluation.HeapRuntime runtime = new Evaluation.HeapRuntime();
                             Object ret = Evaluation.evaluate(search, runtime, suite.given);
                             System.out.println(ret);

                             System.out.println();
                        }
                        catch (RuntimeException e) {
                            System.err.println(e);
                        }
                    }
                }

                System.out.println();
            }

            System.out.println();
            System.out.println();
        }
    }
}
