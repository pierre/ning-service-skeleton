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

package com.ning.jetty.core.log4j;

import org.apache.log4j.Level;
import org.weakref.jmx.Managed;

import java.util.concurrent.atomic.AtomicLong;

public class LogLevelCounter
{
    private final boolean[] levelCountsEnabled;
    private final AtomicLong[] levelCounts;

    public enum LevelIndex
    {
        DEBUG_INDEX(0, Level.DEBUG, "DEBUG"),
        INFO_INDEX(1, Level.INFO, "INFO"),
        WARN_INDEX(2, Level.WARN, "WARN"),
        ERROR_INDEX(3, Level.ERROR, "ERROR");

        private final int index;
        private final Level level;
        private final String levelString;

        LevelIndex(final int index, final Level level, final String levelString)
        {
            this.index = index;
            this.level = level;
            this.levelString = levelString;
        }

        public int getIndex()
        {
            return index;
        }

        public Level getLevel()
        {
            return level;
        }

        public String getLevelString()
        {
            return levelString;
        }

        public static LevelIndex getLevelIndexFromLevel(final Level level)
        {
            for (final LevelIndex lIndex : LevelIndex.values()) {
                if (lIndex.level.equals(level)) {
                    return lIndex;
                }
            }
            return null;
        }

        public static LevelIndex getLevelIndexFromLevelString(String levelString)
        {
            levelString = levelString.toUpperCase();
            for (final LevelIndex lIndex : LevelIndex.values()) {
                if (lIndex.levelString.equals(levelString)) {
                    return lIndex;
                }
            }
            return null;
        }

        public static Level getLevelFromLevelString(final String levelString)
        {
            final LevelIndex lIndex = getLevelIndexFromLevelString(levelString);
            return lIndex.level;
        }

        public static int getNumLevels()
        {
            return values().length;
        }
    }

    public LogLevelCounter()
    {
        levelCountsEnabled = new boolean[LevelIndex.values().length];
        levelCounts = new AtomicLong[LevelIndex.values().length];

        for (int i = 0; i < levelCountsEnabled.length; i++) {
            // TODO: Allow injected initial values for enabled log levels
            // for now default to enable only WARN & ERROR
            levelCountsEnabled[i] = i >= LevelIndex.WARN_INDEX.getIndex();
            levelCounts[i] = new AtomicLong(0L);
        }
    }

    @Managed
    public void logLevelEvent(final Level level)
    {
        final LevelIndex levelIndex = LevelIndex.getLevelIndexFromLevel(level);

        // Could be an unhandled level type (e.g. TRACE, FATAL)
        if (levelIndex == null) {
            return;
        }

        final int index = levelIndex.getIndex();

        if (!levelCountsEnabled[index]) {
            return;
        }

        levelCounts[index].incrementAndGet();
    }

    @Managed
    public long[] getLogLevelCounts()
    {
        final long[] counts = new long[LevelIndex.values().length];
        for (int i = 0; i < levelCountsEnabled.length; i++) {
            counts[i] = levelCounts[i].get();
        }

        return counts;
    }

    @Managed
    public void resetAllLogLevelCounts()
    {
        for (final AtomicLong count : levelCounts) {
            count.set(0L);
        }
    }

    @Managed
    public void setCountingEnabledByLevel(final Level level, final boolean enabled)
    {
        final LevelIndex lIndex = LevelIndex.getLevelIndexFromLevel(level);
        levelCountsEnabled[lIndex.getIndex()] = enabled;
    }

    @Managed
    public boolean getCountingEnabledByLevel(final Level level)
    {
        final LevelIndex lIndex = LevelIndex.getLevelIndexFromLevel(level);
        return levelCountsEnabled[lIndex.getIndex()];
    }
}
