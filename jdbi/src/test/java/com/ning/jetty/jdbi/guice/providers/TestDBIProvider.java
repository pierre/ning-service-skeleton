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

package com.ning.jetty.jdbi.guice.providers;

import java.util.Properties;

import org.skife.config.ConfigurationObjectFactory;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.tweak.SQLLog;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.ning.jetty.jdbi.config.DaoConfig;
import com.ning.jetty.jdbi.log.Slf4jLogging;
import com.yammer.metrics.core.MetricsRegistry;

public class TestDBIProvider {
    @Test(groups = "fast")
    public void testInjection() throws Exception {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION, new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(MetricsRegistry.class).toInstance(new MetricsRegistry());
                binder.bind(DaoConfig.class).toInstance(new ConfigurationObjectFactory(new Properties()).build(DaoConfig.class));
                binder.bind(SQLLog.class).to(Slf4jLogging.class).asEagerSingleton();

                binder.bind(DBI.class).toProvider(DBIProvider.class).asEagerSingleton();
            }
        });

        final DBI dbi = injector.getInstance(DBI.class);
        Assert.assertNotNull(dbi);
    }

    @Test(groups = "fast")
    public void testOptionalInjection() throws Exception {
        final Injector injector = Guice.createInjector(Stage.PRODUCTION, new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(MetricsRegistry.class).toInstance(new MetricsRegistry());
                binder.bind(DaoConfig.class).toInstance(new ConfigurationObjectFactory(new Properties()).build(DaoConfig.class));

                binder.bind(DBI.class).toProvider(DBIProvider.class).asEagerSingleton();
            }
        });

        final DBI dbi = injector.getInstance(DBI.class);
        Assert.assertNotNull(dbi);
    }
}
