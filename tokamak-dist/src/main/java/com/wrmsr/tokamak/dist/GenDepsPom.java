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
package com.wrmsr.tokamak.dist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.wrmsr.tokamak.util.MorePreconditions.checkSingle;

public class GenDepsPom
{
    /*
    TODO:
     - build doc from scratch, sort inputs
    */

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

    public static String getChildNodeText(Node node, String name)
    {
        for (int i = 0; i < node.getChildNodes().getLength(); ++i) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals(name)) {
                return child.getTextContent();
            }
        }
        return null;
    }

    public static void main(String[] args)
            throws Throwable
    {
        // DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // DocumentBuilder db = dbf.newDocumentBuilder();
        // Document dom = db.newDocument();
        // Element root = dom.createElement("project");

        File pomFile = new File("../pom.xml");

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pomFile);

        //http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        Node projectNode = checkSingle(evaluateXPath(doc, "/project"));
        Node dependencyManagementNode = checkSingle(evaluateXPath(doc, "/project/dependencyManagement"));
        Node dependenciesNode = checkSingle(evaluateXPath(doc, "/project/dependencyManagement/dependencies"));
        Node newDependenciesNode = projectNode.insertBefore(dependenciesNode.cloneNode(true), dependencyManagementNode);
        projectNode.removeChild(dependencyManagementNode);

        Node modulesNode = checkSingle(evaluateXPath(doc, "/project/modules"));
        projectNode.removeChild(modulesNode);

        for (int i = 0; i < newDependenciesNode.getChildNodes().getLength(); ) {
            Node dep = newDependenciesNode.getChildNodes().item(i);
            String groupId = getChildNodeText(dep, "groupId");
            if (groupId != null && groupId.equals("com.wrmsr.tokamak")) {
                newDependenciesNode.removeChild(dep);
            }
            else {
                ++i;
            }
        }

        List<Node> pluginNodes = evaluateXPath(doc, "/project/build/plugins/plugin");
        for (Node pluginNode : pluginNodes) {
            Element dep = doc.createElement("dependency");
            for (String k : new String[] {"groupId", "artifactId", "version"}) {
                Element e = doc.createElement(k);
                e.appendChild(doc.createTextNode(getChildNodeText(pluginNode, k)));
                dep.appendChild(e);
            }
            newDependenciesNode.appendChild(dep);
        }

        Node pluginsNode = checkSingle(evaluateXPath(doc, "/project/build/plugins"));
        pluginsNode.getParentNode().removeChild(pluginsNode);

        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        File pomDepsFile = new File("../pom-deps.xml");

        tr.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(pomDepsFile)));

        // List<Artifact> artifacts = resolver.resolvePom(pomFile);
        // artifacts.forEach(System.out::println);
    }
}
