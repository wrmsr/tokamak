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
package com.wrmsr.tokamak.spark;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNodeAnnotations;
import com.wrmsr.tokamak.core.plan.node.PValues;
import com.wrmsr.tokamak.core.type.Types;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SparkTest
{
    public static class ExampleJob
    {
        private final JavaSparkContext sc;

        public ExampleJob(JavaSparkContext sc)
        {
            this.sc = sc;
        }

        public static final PairFunction<Tuple2<Integer, Optional<String>>, Integer, String> KEY_VALUE_PAIRER = (a) -> {
            // a._2.isPresent()
            return new Tuple2<>(a._1, a._2.get());
        };

        public static JavaRDD<Tuple2<Integer, Optional<String>>> joinData(JavaPairRDD<Integer, Integer> t, JavaPairRDD<Integer, String> u)
        {
            JavaRDD<Tuple2<Integer, Optional<String>>> leftJoinOutput = t.leftOuterJoin(u).values().distinct();
            return leftJoinOutput;
        }

        public static JavaPairRDD<Integer, String> modifyData(JavaRDD<Tuple2<Integer, Optional<String>>> d)
        {
            return d.mapToPair(KEY_VALUE_PAIRER);
        }

        public static Map<Integer, Long> countData(JavaPairRDD<Integer, String> d)
        {
            Map<Integer, Long> result = d.countByKey();
            return result;
        }

        public JavaPairRDD<String, String> run(String t, String u)
        {
            JavaRDD<String> transactionInputFile = sc.textFile(t);
            JavaPairRDD<Integer, Integer> transactionPairs = transactionInputFile.mapToPair((s) -> {
                String[] transactionSplit = s.split("\t");
                return new Tuple2<>(Integer.valueOf(transactionSplit[2]), Integer.valueOf(transactionSplit[1]));
            });

            JavaRDD<String> customerInputFile = sc.textFile(u);
            JavaPairRDD<Integer, String> customerPairs = customerInputFile.mapToPair((s) -> {
                String[] customerSplit = s.split("\t");
                return new Tuple2<>(Integer.valueOf(customerSplit[0]), customerSplit[3]);
            });

            Map<Integer, Long> result = countData(modifyData(joinData(transactionPairs, customerPairs)));

            List<Tuple2<String, String>> output = new ArrayList<>();
            for (Map.Entry<Integer, Long> entry : result.entrySet()) {
                output.add(new Tuple2<>(entry.getKey().toString(), String.valueOf((long) entry.getValue())));
            }

            JavaPairRDD<String, String> outputRdd = sc.parallelizePairs(output);
            return outputRdd;
        }

        public static void main(String[] args)
                throws Exception
        {
            Plan plan = Plan.of(
                    new PValues(
                            "values",
                            PNodeAnnotations.empty(),
                            ImmutableMap.of("x", Types.LONG),
                            ImmutableList.of(ImmutableList.of(420L)),
                            java.util.Optional.empty(),
                            PValues.Strictness.NON_STRICT));

            JavaSparkContext sc = new JavaSparkContext(new SparkConf().setAppName("SparkJoins").setMaster("local"));
            ExampleJob job = new ExampleJob(sc);
            JavaPairRDD<String, String> outputRdd = job.run(args[0], args[1]);
            outputRdd.saveAsHadoopFile(args[2], String.class, String.class, TextOutputFormat.class);
            sc.close();
        }
    }
}
