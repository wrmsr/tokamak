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
package com.wrmsr.tokamak.main.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.concurrent.Immutable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class Status
{
    private float uptime;
    private final Optional<String> revision;
    private final Optional<String> javaVersion;

    @JsonCreator
    public Status(
            @JsonProperty("uptime") float uptime,
            @JsonProperty("revision") Optional<String> revision,
            @JsonProperty("javaVersion") Optional<String> javaVersion)
    {
        this.uptime = uptime;
        this.revision = checkNotNull(revision);
        this.javaVersion = checkNotNull(javaVersion);
    }

    @JsonProperty("uptime")
    public float getUptime()
    {
        return uptime;
    }

    @JsonProperty("revision")
    public Optional<String> getRevision()
    {
        return revision;
    }

    @JsonProperty("javaVersion")
    public Optional<String> getJavaVersion()
    {
        return javaVersion;
    }
}
