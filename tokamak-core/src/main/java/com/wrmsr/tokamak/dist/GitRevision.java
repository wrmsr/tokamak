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

import com.google.common.io.CharStreams;
import com.wrmsr.tokamak.util.lazy.SupplierLazyValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public final class GitRevision
{
    private GitRevision()
    {
    }

    private static final SupplierLazyValue<Optional<String>> revision = new SupplierLazyValue<>();

    public static Optional<String> get()
    {
        return revision.get(() -> {
            InputStream stream = GitRevision.class.getResourceAsStream(".revision");
            if (stream != null) {
                try {
                    String rev = CharStreams.toString(new InputStreamReader(stream));
                    return Optional.of(rev.trim());
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                return Optional.empty();
            }
        });
    }
}
