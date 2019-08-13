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

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

public class Logger
{
    private final java.util.logging.Logger logger;

    protected Logger(java.util.logging.Logger logger)
    {
        this.logger = logger;
    }

    public static Logger get(Class<?> clazz)
    {
        return get(clazz.getName());
    }

    public static Logger get(String name)
    {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        return new Logger(logger);
    }

    public void debug(Throwable exception, String message)
    {
        logger.log(FINE, message, exception);
    }

    public void debug(String message)
    {
        logger.fine(message);
    }

    public void debug(String format, Object... args)
    {
        if (logger.isLoggable(FINE)) {
            logger.fine(String.format(format, args));
        }
    }

    public void debug(Throwable exception, String format, Object... args)
    {
        if (logger.isLoggable(FINE)) {
            logger.log(FINE, String.format(format, args), exception);
        }
    }

    public void info(String message)
    {
        logger.info(message);
    }

    public void info(String format, Object... args)
    {
        if (logger.isLoggable(INFO)) {
            logger.info(String.format(format, args));
        }
    }

    public void warn(Throwable exception, String message)
    {
        logger.log(WARNING, message, exception);
    }

    public void warn(String message)
    {
        logger.warning(message);
    }

    public void warn(Throwable exception, String format, Object... args)
    {
        if (logger.isLoggable(WARNING)) {
            logger.log(WARNING, String.format(format, args), exception);
        }
    }

    public void warn(String format, Object... args)
    {
        warn(null, format, args);
    }

    public void error(Throwable exception, String message)
    {
        logger.log(SEVERE, message, exception);
    }

    public void error(String message)
    {
        logger.severe(message);
    }

    public void error(Throwable exception, String format, Object... args)
    {
        if (logger.isLoggable(SEVERE)) {
            logger.log(SEVERE, String.format(format, args), exception);
        }
    }

    public void error(Throwable exception)
    {
        if (logger.isLoggable(SEVERE)) {
            logger.log(SEVERE, exception.getMessage(), exception);
        }
    }

    public void error(String format, Object... args)
    {
        error(null, format, args);
    }

    public boolean isDebugEnabled()
    {
        return logger.isLoggable(FINE);
    }

    public boolean isInfoEnabled()
    {
        return logger.isLoggable(INFO);
    }
}
