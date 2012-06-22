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
import org.skife.jdbi.v2.tweak.SQLLog;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.ning.jetty.jdbi.config.DaoConfig;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.jdbi.InstrumentedTimingCollector;
import com.yammer.metrics.jdbi.strategies.BasicSqlNameStrategy;

public class DBIProvider implements Provider<DBI> {
    private final MetricsRegistry metricsRegistry;
    private final DaoConfig config;
    private SQLLog sqlLog;

    @Inject
    public DBIProvider(final MetricsRegistry metricsRegistry, final DaoConfig config) {
        this.metricsRegistry = metricsRegistry;
        this.config = config;
    }

    @Inject(optional = true)
    public void setSqlLog(final SQLLog sqlLog) {
        this.sqlLog = sqlLog;
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
        if (sqlLog != null) {
            dbi.setSQLLog(sqlLog);
        }

        final BasicSqlNameStrategy basicSqlNameStrategy = new BasicSqlNameStrategy();
        final TimingCollector timingCollector = new InstrumentedTimingCollector(metricsRegistry, basicSqlNameStrategy, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        dbi.setTimingCollector(timingCollector);

        return dbi;
    }
}
