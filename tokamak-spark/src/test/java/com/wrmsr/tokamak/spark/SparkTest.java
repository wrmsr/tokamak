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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.ParentFirstClassLoader;
import io.airlift.resolver.ArtifactResolver;
import io.airlift.resolver.DefaultArtifact;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.PairFunction;
import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import scala.Tuple2;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

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
            JavaSparkContext sc = new JavaSparkContext(new SparkConf().setAppName("SparkJoins").setMaster("local"));
            ExampleJob job = new ExampleJob(sc);
            JavaPairRDD<String, String> outputRdd = job.run(args[0], args[1]);
            outputRdd.saveAsHadoopFile(args[2], String.class, String.class, TextOutputFormat.class);
            sc.close();
        }
    }

    private static List<Node> evaluateXPath(Document document, String xpathExpression)
    {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        List<Node> values = new ArrayList<>();
        try {
            XPathExpression expr = xpath.compile(xpathExpression);
            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                values.add(nodes.item(i));
            }
        }
        catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return values;
    }

    @Test
    public void testClassloader()
            throws Throwable
    {
        ArtifactResolver resolver = new ArtifactResolver(
                ArtifactResolver.USER_LOCAL_REPO,
                ImmutableList.of(ArtifactResolver.MAVEN_CENTRAL_URI));

        File pomFile = new File("../pom.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pomFile);

        //http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        String groupId = "org.apache.spark";
        Node sparkCoreNode = checkSingle(
                evaluateXPath(
                        doc,
                        "/project/dependencyManagement/dependencies/dependency" +
                                "[groupId='" + groupId + "' and starts-with(artifactId, 'spark-core')]"));

        String artifactId = null;
        String version = null;
        for (int i = 0; i < sparkCoreNode.getChildNodes().getLength(); ++i) {
            Node child = sparkCoreNode.getChildNodes().item(i);
            if (child.getNodeName().equals("artifactId")) {
                artifactId = child.getTextContent();
            }
            else if (child.getNodeName().equals("version")) {
                version = child.getTextContent();
            }
        }

        String coords = Joiner.on(":").join(groupId, artifactId, version);

        List<Artifact> artifacts = resolver.resolveArtifacts(new DefaultArtifact(coords));

        List<String> classpath = artifacts.stream().map(a -> a.getFile().getAbsolutePath()).collect(toImmutableList());
        System.out.println(classpath);

        new ParentFirstClassLoader()
    }
}
