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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.weakref.jmx.Managed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.ning.jetty.log4j.LogLevelCounter.LevelIndex;

public class Log4JMBean
{
    private final AtomicLong lastResetTimeMillis = new AtomicLong(System.currentTimeMillis());

    private LogLevelCounter counter = null;

    public Log4JMBean()
    {
        initLogLevelCounting();
    }

    private void initLogLevelCounting()
    {
        this.counter = new LogLevelCounter();
        LogLevelCounterAppender.registerLogLevelCounter(counter);
    }

    @Managed(description = "Set the log level on a per logger basis")
    public void setLevel(final String loggerName, final String levelString)
    {
        final Logger logger;

        if (!StringUtils.isEmpty(loggerName)) {
            logger = LogManager.getLogger(loggerName);
        }
        else {
            logger = LogManager.getRootLogger();
        }

        logger.setLevel(Level.toLevel(levelString));
    }

    @Managed(description = "Get the log level for the specified logger")
    public String getLevel(final String loggerName)
    {
        final Logger logger;

        if (!StringUtils.isEmpty(loggerName)) {
            logger = LogManager.getLogger(loggerName);
        }
        else {
            logger = LogManager.getRootLogger();
        }

        return logger.getLevel().toString();
    }

    @Managed(description = "Retrieve the parent logger name")
    public String getParentLoggerName(final String loggerName)
    {
        if (!StringUtils.isEmpty(loggerName)) {
            return LogManager.getLogger(loggerName).getParent().getName();
        }

        return "";
    }

    @Managed(description = "Get the current loggers names")
    public String[] getLoggerNames()
    {
        final Enumeration<Logger> e = LogManager.getCurrentLoggers();

        final List<String> loggers = new ArrayList<String>();

        while (e.hasMoreElements()) {
            final Logger logger = e.nextElement();
            loggers.add(logger.getName());
        }

        Collections.sort(loggers);

        return loggers.toArray(new String[0]);
    }

    @Managed(description = "Get the count of log events for the specified level")
    public long getLogLevelCountByLevel(final String levelString)
    {
        final long[] counts = counter.getLogLevelCounts();
        final LevelIndex lIndex = LevelIndex.getLevelIndexFromLevelString(levelString);
        if (lIndex == null) {
            return -1;
        }
        else {
            return counts[lIndex.getIndex()];
        }
    }

    @Managed(description = "Get the count of log events per level")
    public String[] getLogLevelCounts()
    {
        final long[] counts = counter.getLogLevelCounts();

        final String[] levelMessages = new String[LevelIndex.getNumLevels()];
        for (final LevelIndex lIndex : LevelIndex.values()) {
            final String levelString = lIndex.getLevelString();
            levelMessages[lIndex.getIndex()] = levelString + ": " + counts[lIndex.getIndex()];
        }

        return levelMessages;
    }

    @Managed(description = "Reset all counters")
    public void resetStats()
    {
        counter.resetAllLogLevelCounts();
        lastResetTimeMillis.set(System.currentTimeMillis());
    }

    @Managed(description = "Retrieve the last time the counters were reset")
    public Date getLastStatsResetTime()
    {
        return new Date(lastResetTimeMillis.get());
    }

    @Managed(description = "Retrieve the status of log counting per level")
    public String[] getLogLevelCountingEnabled()
    {
        final String[] levelMessages = new String[LevelIndex.getNumLevels()];
        for (final LevelIndex lIndex : LevelIndex.values()) {
            final String levelString = lIndex.getLevelString();
            levelMessages[lIndex.getIndex()] = levelString + ": " + counter.getCountingEnabledByLevel(lIndex.getLevel());
        }

        return levelMessages;
    }

    @Managed(description = "Enable or disable log counting per level")
    public void setLogLevelCountingEnabledByLevel(final String levelString, final boolean enabled)
    {
        final Level level = LevelIndex.getLevelFromLevelString(levelString);
        counter.setCountingEnabledByLevel(level, enabled);
    }

    /*
     * The following is mostly to expose stats to Arecibo
     */

    @Managed(description = "Get the count of log events for DEBUG")
    public long getLogLevelCountForDebug()
    {
        return getLogLevelCountByLevel(LevelIndex.DEBUG_INDEX.getLevelString());
    }

    @Managed(description = "Get the count of log events for INFO")
    public long getLogLevelCountForInfo()
    {
        return getLogLevelCountByLevel(LevelIndex.INFO_INDEX.getLevelString());
    }

    @Managed(description = "Get the count of log events for WARN")
    public long getLogLevelCountForWarn()
    {
        return getLogLevelCountByLevel(LevelIndex.WARN_INDEX.getLevelString());
    }

    @Managed(description = "Get the count of log events for ERROR")
    public long getLogLevelCountForError()
    {
        return getLogLevelCountByLevel(LevelIndex.ERROR_INDEX.getLevelString());
    }
}
