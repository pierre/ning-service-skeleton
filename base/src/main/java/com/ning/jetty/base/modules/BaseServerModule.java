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

package com.ning.jetty.base.modules;

import com.ning.arecibo.jmx.AreciboMonitoringModule;
import com.ning.arecibo.metrics.guice.AreciboMetricsModule;
import com.ning.jetty.core.modules.ServerModule;
import com.ning.jetty.eventtracker.CollectorTracker;
import com.ning.jetty.eventtracker.Tracker;
import com.ning.jetty.eventtracker.config.TrackerConfig;
import com.ning.jetty.eventtracker.filters.TrackerFilter;
import com.ning.jetty.jdbi.config.DaoConfig;
import com.ning.jetty.log4j.Log4JMBean;
import com.ning.jetty.utils.arecibo.Jetty7AreciboConnector;
import com.ning.jetty.utils.arecibo.Log4JMBeanAreciboConnector;
import com.ning.metrics.eventtracker.CollectorControllerHttpMBeanModule;
import com.ning.metrics.eventtracker.CollectorControllerSmileModule;

import com.google.inject.Module;
import com.yammer.metrics.core.HealthCheck;

import org.skife.config.ConfigSource;
import org.skife.config.ConfigurationObjectFactory;
import org.weakref.jmx.guice.ExportBuilder;
import org.weakref.jmx.guice.MBeanModule;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseServerModule extends ServerModule
{
    // Extra Guice bindings
    private final Map<Class, Object> bindings = new HashMap<Class, Object>();
    private final ConfigSource configSource;
    // config-magic classes
    private final ArrayList<Class> configs = new ArrayList<Class>();
    // Healthcheck classes
    private final ArrayList<Class<? extends HealthCheck>> healthchecks = new ArrayList<Class<? extends HealthCheck>>();
    // JMX beans to export
    private final ArrayList<Class> beans = new ArrayList<Class>();
    // Arecibo integration
    private String areciboProfile = null;
    // eventtracker integration
    private boolean trackRequests = false;
    // Whether log4j is used
    private boolean log4jEnabled = false;
    // Extra Guice modules to install
    final List<Module> modules = new ArrayList<Module>();
    private final Map<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>> filters;
    private final Map<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>> filtersRegex;
    private final Map<String, Class<? extends HttpServlet>> servlets;
    private final Map<String, Class<? extends HttpServlet>> servletsRegex;

    // Jax-RS resources
    final String jaxRSUriPattern;
    final List<String> jaxRSResources = new ArrayList<String>();
    final Map<String, Class<? extends HttpServlet>> jaxRSServlets;
    final Map<String, Class<? extends HttpServlet>> jaxRSServletsRegex;

    public BaseServerModule(final Map<Class, Object> bindings,
                            final ConfigSource configSource,
                            final List<Class> configs,
                            final List<Class<? extends HealthCheck>> healthchecks,
                            final List<Class> beans,
                            final String areciboProfile,
                            final boolean trackRequests,
                            final boolean log4jEnabled,
                            final String jaxRSUriPattern,
                            final List<String> jaxRSResources,
                            final List<Module> modules,
                            final Map<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>> filters,
                            final Map<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>> filtersRegex,
                            final Map<String, Class<? extends HttpServlet>> jaxRSServlets,
                            final Map<String, Class<? extends HttpServlet>> jaxRSServletsRegex,
                            final Map<String, Class<? extends HttpServlet>> servlets,
                            final Map<String, Class<? extends HttpServlet>> servletsRegex)
    {
        this.bindings.putAll(bindings);
        this.configSource = configSource;
        this.configs.addAll(configs);
        this.healthchecks.addAll(healthchecks);
        this.beans.addAll(beans);
        this.areciboProfile = areciboProfile;
        this.log4jEnabled = log4jEnabled;
        this.trackRequests = trackRequests;
        this.jaxRSUriPattern = jaxRSUriPattern;
        this.jaxRSResources.addAll(jaxRSResources);
        this.modules.addAll(modules);
        this.filters = filters;
        this.filtersRegex = filtersRegex;
        this.jaxRSServlets = jaxRSServlets;
        this.jaxRSServletsRegex = jaxRSServletsRegex;
        this.servlets = servlets;
        this.servletsRegex = servletsRegex;

        this.configs.add(DaoConfig.class);
        this.configs.add(TrackerConfig.class);
    }

    @Override
    public void configureServlets()
    {
        super.configureServlets();

        configureConfig();

        installExtraBindings();
        installHealthChecks();
        installArecibo();
        installEventtracker();
        installLog4j();
        installExtraModules();

        configureFilters();
        configureFiltersRegex();
        configureRegularServlets();
        configureRegularServletsRegex();
        configureResources();
    }

    private void installExtraBindings()
    {
        for (final Class clazz : bindings.keySet()) {
            binder().bind(clazz).toInstance(bindings.get(clazz));
        }
    }

    private void installEventtracker()
    {
        if (!trackRequests) {
            return;
        }

        install(new CollectorControllerSmileModule());
        install(new CollectorControllerHttpMBeanModule());
        bind(Tracker.class).to(CollectorTracker.class).asEagerSingleton();
        filter("*").through(TrackerFilter.class);
    }

    private void installLog4j()
    {
        if (!log4jEnabled) {
            return;
        }

        bind(Log4JMBean.class).asEagerSingleton();
    }

    protected void configureConfig()
    {
        for (final Class configClass : configs) {
            bind(configClass).toInstance(new ConfigurationObjectFactory(configSource).build(configClass));
        }
    }

    @Override
    protected void installJMX()
    {
        super.installJMX();

        final ExportBuilder builder = MBeanModule.newExporter(binder());
        for (final Class beanClass : beans) {
            builder.export(beanClass).withGeneratedName();
        }

        if (log4jEnabled) {
            builder.export(Log4JMBean.class).withGeneratedName();
        }
    }

    protected void installHealthChecks()
    {
        for (final Class<? extends HealthCheck> healthCheckClass : healthchecks) {
            healthChecksBinder.addBinding().to(healthCheckClass).asEagerSingleton();
        }
    }

    protected void installArecibo()
    {
        if (areciboProfile == null) {
            return;
        }

        install(new AreciboMonitoringModule(areciboProfile));
        // Expose metrics objects to Arecibo
        install(new AreciboMetricsModule());
        bind(Jetty7AreciboConnector.class).asEagerSingleton();

        if (log4jEnabled) {
            bind(Log4JMBeanAreciboConnector.class).asEagerSingleton();
        }
    }

    private void installExtraModules()
    {
        for (final Module module : modules) {
            install(module);
        }
    }

    protected void configureFilters()
    {
        for (final String urlPattern : filters.keySet()) {
            for (final Map.Entry<Class<? extends Filter>, Map<String, String>> filter : filters.get(urlPattern)) {
                filter(urlPattern).through(filter.getKey(), filter.getValue());
            }
        }
    }

    protected void configureFiltersRegex()
    {
        for (final String urlPattern : filtersRegex.keySet()) {
            for (final Map.Entry<Class<? extends Filter>, Map<String, String>> filter : filtersRegex.get(urlPattern)) {
                filterRegex(urlPattern).through(filter.getKey(), filter.getValue());
            }
        }
    }

    protected void configureRegularServlets()
    {
        for (final String urlPattern : servlets.keySet()) {
            serve(urlPattern).with(servlets.get(urlPattern));
        }
    }

    protected void configureRegularServletsRegex()
    {
        for (final String urlPattern : servletsRegex.keySet()) {
            serveRegex(urlPattern).with(servletsRegex.get(urlPattern));
        }
    }

    protected abstract void configureResources();
}
