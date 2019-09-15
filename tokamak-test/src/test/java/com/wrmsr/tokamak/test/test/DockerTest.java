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
package com.wrmsr.tokamak.test.test;

import com.google.common.base.Splitter;
import com.wrmsr.tokamak.test.Docker;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DockerTest
        extends TestCase
{
    public void testDocker()
            throws Throwable
    {
        List<Docker.Container> containers = Docker.queryDockerContainers();
        Map<String, List<Docker.Container>> containersByImage = containers.stream()
                .collect(Collectors.groupingBy(c -> Splitter.on(':').splitToList(c.getImage()).get(0)));
        System.out.println(containersByImage);
    }
}
