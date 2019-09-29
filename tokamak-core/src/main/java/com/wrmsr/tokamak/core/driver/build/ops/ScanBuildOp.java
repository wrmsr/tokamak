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
package com.wrmsr.tokamak.core.driver.build.ops;

import com.wrmsr.tokamak.api.Key;
import com.wrmsr.tokamak.core.catalog.Scanner;
import com.wrmsr.tokamak.core.driver.build.Builder;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public final class ScanBuildOp
        extends AbstractBuildOp
{
    private final Scanner scanner;
    private final Key key;
    private final Consumer<List<Map<String, Object>>> callback;

    public ScanBuildOp(Builder origin, Scanner scanner, Key key, Consumer<List<Map<String, Object>>> callback)
    {
        super(origin);
        this.scanner = checkNotNull(scanner);
        this.key = checkNotNull(key);
        this.callback = checkNotNull(callback);
    }

    @Override
    public String toString()
    {
        return "ScanBuildOp{" +
                "origin=" + origin +
                ", scanner=" + scanner +
                ", key=" + key +
                '}';
    }

    public Scanner getScanner()
    {
        return scanner;
    }

    public Key getKey()
    {
        return key;
    }

    public Consumer<List<Map<String, Object>>> getCallback()
    {
        return callback;
    }
}
