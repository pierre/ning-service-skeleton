/*
 * Copyright 2010-2012 Ning, Inc.
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

package com.ning.jetty.jdbi.log;

import javax.inject.Inject;

import org.skife.jdbi.v2.logging.FormattedLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.jetty.jdbi.config.DaoConfig;
import com.ning.jetty.jdbi.config.LogLevel;

public class Slf4jLogging extends FormattedLog {
    private static final Logger logger = LoggerFactory.getLogger(Slf4jLogging.class);

    private final DaoConfig config;

    @Inject
    public Slf4jLogging(final DaoConfig config) {
        this.config = config;
    }

    /**
     * Used to ask implementations if logging is enabled.
     *
     * @return true if statement logging is enabled
     */
    @Override
    protected boolean isEnabled() {
        return true;
    }

    /**
     * Log the statement passed in
     *
     * @param msg the message to log
     */
    @Override
    protected void log(final String msg) {
        if (config.getLogLevel().equals(LogLevel.DEBUG)) {
            logger.debug(msg);
        } else if (config.getLogLevel().equals(LogLevel.TRACE)) {
            logger.trace(msg);
        } else if (config.getLogLevel().equals(LogLevel.INFO)) {
            logger.info(msg);
        } else if (config.getLogLevel().equals(LogLevel.WARN)) {
            logger.warn(msg);
        } else if (config.getLogLevel().equals(LogLevel.ERROR)) {
            logger.error(msg);
        }
    }
}
