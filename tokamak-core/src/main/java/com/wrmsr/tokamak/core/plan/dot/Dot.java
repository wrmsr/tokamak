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
package com.wrmsr.tokamak.core.plan.dot;

import com.wrmsr.tokamak.core.plan.Plan;
import com.wrmsr.tokamak.core.plan.node.PNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.wrmsr.tokamak.util.MoreFiles.createTempDirectory;

public final class Dot
{
    private Dot()
    {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String buildPlanDot(Plan plan)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");

        for (PNode node : plan.getReverseToposortedNodes()) {
            NodeRendering rendering = checkNotNull(NodeRenderings.NODE_RENDERING_MAP.get().get(node.getClass()));
            sb.append(rendering.render(new NodeRendering.Context(node, plan)));
            sb.append("\n");
        }

        for (PNode node : plan.getToposortedNodes()) {
            for (PNode source : node.getSources()) {
                sb.append(String.format("\"%s\" -> \"%s\" [dir=back];\n", node.getName(), source.getName()));
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    public static void openDot(String gv)
            throws Exception
    {
        Path tempDir = createTempDirectory("tokamak-dot");
        Path outGv = tempDir.resolve("out.gv");
        Files.write(outGv, gv.getBytes());
        Path outPng = tempDir.resolve("out.pdf");

        Process p = new ProcessBuilder()
                .directory(tempDir.toFile())
                .command("dot", "-Tpdf", "out.gv")
                .redirectOutput(outPng.toFile())
                .start();
        if (!p.waitFor(3600, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException();
        }

        new ProcessBuilder()
                .directory(tempDir.toFile())
                .command("open", "out.pdf")
                .start()
                .waitFor(30, TimeUnit.SECONDS);
        Thread.sleep(500);
    }
}
