/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.jetty.log4j;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class LogLevelCounterAppender implements Appender
{
    private static final Logger log = Logger.getLogger(LogLevelCounterAppender.class);
    private static LogLevelCounter registeredCounter = null;

    // Declare this flag as volatile, to allow unsynchronized checking of the flag, but
    // it's ok if initial race conditions occur as it transitions from false to true
    private static volatile boolean isCounterRegistered = false;

    public static void registerLogLevelCounter(final LogLevelCounter counter)
    {
        synchronized (LogLevelCounterAppender.class) {
            // Only allow one registered LogLevelCounter
            if (isCounterRegistered) {
                log.warn("Attempt to register multiple LogLevelCounter's -- IGNORED");
                return;
            }

            registeredCounter = counter;
        }

        // This should be the only time this flag ever changes
        isCounterRegistered = true;

        if (log.isInfoEnabled()) {
            log.info("LogLevelCounter registered, LogLevelCounterAppender now enabled");
        }
    }

    public void doAppend(final LoggingEvent event)
    {
        if (isCounterRegistered) {
            registeredCounter.logLevelEvent(event.getLevel());
        }
    }

    public void setName(final String name)
    {
    }

    public String getName()
    {
        return null;
    }

    public void addFilter(final Filter newFilter)
    {
    }

    public Filter getFilter()
    {
        return null;
    }

    public void clearFilters()
    {
    }

    public boolean requiresLayout()
    {
        return false;
    }

    public Layout getLayout()
    {
        return null;
    }

    public void setLayout(final Layout layout)
    {
    }

    public ErrorHandler getErrorHandler()
    {
        return null;
    }

    public void setErrorHandler(final ErrorHandler errorHandler)
    {
    }

    public void close()
    {
    }
}
