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

package com.ning.jetty.jdbi.guice.providers;

import java.util.concurrent.TimeUnit;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.TimingCollector;
import org.skife.jdbi.v2.logging.FormattedLog;
import org.skife.jdbi.v2.tweak.SQLLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.ning.jetty.jdbi.config.DaoConfig;
import com.ning.jetty.jdbi.config.LogLevel;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import com.yammer.metrics.jdbi.strategies.BasicSqlNameStrategy;

public class DBIProvider implements Provider<DBI> {
    private static final Logger logger = LoggerFactory.getLogger(DBIProvider.class);

    private final MetricsRegistry metricsRegistry;
    private final DaoConfig config;

    private final class Slf4jLogging extends FormattedLog {
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

    @Inject
    public DBIProvider(final MetricsRegistry metricsRegistry, final DaoConfig config) {
        this.metricsRegistry = metricsRegistry;
        this.config = config;
    }

    @Override
    public DBI get() {
        final BoneCPConfig dbConfig = new BoneCPConfig();
        dbConfig.setJdbcUrl(config.getJdbcUrl());
        dbConfig.setUsername(config.getUsername());
        dbConfig.setPassword(config.getPassword());
        dbConfig.setMinConnectionsPerPartition(config.getMinIdle());
        dbConfig.setMaxConnectionsPerPartition(config.getMaxActive());
        dbConfig.setConnectionTimeout(config.getConnectionTimeout().getPeriod(), config.getConnectionTimeout().getUnit());
        dbConfig.setIdleMaxAge(config.getIdleMaxAge().getPeriod(), config.getIdleMaxAge().getUnit());
        dbConfig.setMaxConnectionAge(config.getMaxConnectionAge().getPeriod(), config.getMaxConnectionAge().getUnit());
        dbConfig.setIdleConnectionTestPeriod(config.getIdleConnectionTestPeriod().getPeriod(), config.getIdleConnectionTestPeriod().getUnit());
        dbConfig.setPartitionCount(1);
        dbConfig.setDefaultTransactionIsolation("READ_COMMITTED");
        dbConfig.setDisableJMX(false);

        final BoneCPDataSource ds = new BoneCPDataSource(dbConfig);
        final DBI dbi = new DBI(ds);
        final SQLLog log = new Slf4jLogging();
        dbi.setSQLLog(log);

        final BasicSqlNameStrategy basicSqlNameStrategy = new BasicSqlNameStrategy();
        final TimingCollector timingCollector = new InstrumentedTimingCollector(metricsRegistry, basicSqlNameStrategy, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        dbi.setTimingCollector(timingCollector);

        return dbi;
    }
}
