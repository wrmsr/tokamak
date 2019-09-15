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
package com.wrmsr.tokamak.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.wrmsr.tokamak.util.config.Compilation;
import com.wrmsr.tokamak.util.config.Config;
import com.wrmsr.tokamak.util.config.ConfigDetail;
import com.wrmsr.tokamak.util.config.ConfigMetadata;
import com.wrmsr.tokamak.util.config.Configs;
import com.wrmsr.tokamak.util.config.Flattening;
import com.wrmsr.tokamak.util.config.props.ConfigProperty;
import junit.framework.TestCase;

import java.util.Map;

public class ConfigTest
        extends TestCase
{
    public interface ThingConfig
            extends Config
    {
        ConfigProperty<String> someStr();

        @ConfigDetail(doc = "uh")
        ConfigProperty<String> someOtherStr();
    }

    public void testThingConfig()
            throws Throwable
    {
        ConfigMetadata cmd = Configs.getMetadata(ThingConfig.class);
        Class<?> cls = Compilation.compileAndLoad(cmd);
        Compilation.ImplFactory<ThingConfig> thingConfigImplFactory = (Compilation.ImplFactory<ThingConfig>) cls.getDeclaredField("FACTORY").get(null);
        ThingConfig cfg = thingConfigImplFactory.build(cmd);
        System.out.println(cfg);
        System.out.println(cfg.someStr().get());
        cfg.someStr().set("hi");
        System.out.println(cfg.someStr().get());
        System.out.println(cfg.someOtherStr().doc());

        // Map map = ImmutableMap.of(
        //         "someStr", "hi"
        // );
        //
        // ThingConfig cfg = Json.readValue(Json.writeValue(map), ThingConfig.class);
        // System.out.println(cfg);
        //
        // map = (Map) Json.roundTrip(cfg);
        // cfg = Json.readValue(Json.writeValue(map), ThingConfig.class);
        // System.out.println(cfg);
        //
        // assertEquals("hi", cfg.someStr().get());
    }

    public interface OuterConfig
            extends Config
    {
        ConfigProperty<ThingConfig> thing();
    }

    // public void testOuterConfig()
    //         throws Throwable
    // {
    //     OuterConfig oc = new OuterConfig()
    //             .setThing(new ThingConfig()
    //                     .setBeanInt(420));
    //
    //     Map map = (Map) Json.roundTrip(oc);
    //
    //     OuterConfig oc2 = Json.readValue(Json.writeValue(map), OuterConfig.class);
    //
    //     System.out.println(oc2);
    //
    //     assertEquals(420, oc2.getThing().getBeanInt());
    // }

    public void testFlattening()
    {
        Map<String, Object> m = ImmutableMap.of(
                "a", 1,
                "b", ImmutableMap.of(
                        "c", 2
                ),
                "d", ImmutableList.of(
                        "e",
                        ImmutableMap.of(
                                "f", 3
                        )
                ),
                "g", ImmutableList.of(
                        ImmutableList.of(
                                "a",
                                "b"
                        ),
                        ImmutableList.of(
                                "c",
                                "d"
                        )
                )
        );

        System.out.println(m);

        Map<String, Object> f = Flattening.defaultFlatten(m);

        System.out.println(f);

        Map<String, Object> uf = Flattening.defaultUnflatten(f);

        System.out.println(uf);

        assertEquals(m, uf);
    }
}
