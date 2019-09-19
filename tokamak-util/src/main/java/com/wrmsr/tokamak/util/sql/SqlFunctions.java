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
package com.wrmsr.tokamak.util.sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SqlFunctions
{
    private SqlFunctions()
    {
    }

    @FunctionalInterface
    public interface SqlRunnable
    {
        void run()
                throws SQLException, IOException;
    }

    public static Runnable withSqlExceptions(SqlRunnable runnable)
    {
        return () -> {
            try {
                runnable.run();
            }
            catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static void runWithSqlExceptions(SqlRunnable runnable)
    {
        withSqlExceptions(runnable).run();
    }

    @FunctionalInterface
    public interface SqlSupplier<T>
    {
        T get()
                throws SQLException, IOException;
    }

    public static <T> Supplier<T> withSqlExceptions(SqlSupplier<T> supplier)
    {
        return () -> {
            try {
                return supplier.get();
            }
            catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T> T getWithSqlExceptions(SqlSupplier<T> supplier)
    {
        return withSqlExceptions(supplier).get();
    }

    @FunctionalInterface
    public interface SqlFunction<T, R>
    {
        R apply(T t)
                throws SQLException, IOException;
    }

    public static <T, R> Function<T, R> withSqlExceptions(SqlFunction<T, R> function)
    {
        return (t) -> {
            try {
                return function.apply(t);
            }
            catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, R> R applyWithSqlExceptions(SqlFunction<T, R> function, T t)
    {
        return withSqlExceptions(function).apply(t);
    }
}
