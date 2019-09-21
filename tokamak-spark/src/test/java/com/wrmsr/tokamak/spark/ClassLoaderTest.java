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
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.wrmsr.tokamak.util.Jdk;
import com.wrmsr.tokamak.util.ParentFirstClassLoader;
import io.airlift.resolver.ArtifactResolver;
import io.airlift.resolver.DefaultArtifact;
import junit.framework.TestCase;
import org.sonatype.aether.artifact.Artifact;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public class ClassLoaderTest
        extends TestCase
{
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

    @SuppressWarnings({"unchecked"})
    public void testClassLoader()
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

        List<URL> sparkClasspath = artifacts.stream().map(a -> {
            try {
                return a.getFile().toURI().toURL();
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).collect(toImmutableList());

        System.out.println(sparkClasspath);

        List<URL> classpath = Splitter.on(":").splitToList(Jdk.getClasspath()).stream().map(jar -> {
            try {

                return new File(jar).toURI().toURL();
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).collect(toImmutableList());

        ClassLoader sparkCl = new ParentFirstClassLoader(
                sparkClasspath,
                new URLClassLoader(
                        classpath.toArray(new URL[] {}),
                        ClassLoader.getSystemClassLoader().getParent()),
                ImmutableList.of(),
                ImmutableList.of(
                        "org.apache.hadoop",
                        "org.apache.spark"
                ));

        Class tc = sparkCl.loadClass("com.wrmsr.tokamak.spark.SparkTest$ExampleJob");

        Thread.currentThread().setContextClassLoader(sparkCl);

        tc.getDeclaredMethod("main", String[].class).invoke(null, new Object[] {new String[] {
                "tokamak-spark/src/test/resources/transactions.tsv",
                "tokamak-spark/src/test/resources/users.tsv",
                "../temp/spark.out",
        }});
    }
}
