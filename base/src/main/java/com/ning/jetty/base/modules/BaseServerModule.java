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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.yammer.metrics.core.HealthCheck;
import org.apache.commons.lang.StringUtils;
import org.skife.config.ConfigurationObjectFactory;
import org.weakref.jmx.guice.ExportBuilder;
import org.weakref.jmx.guice.MBeanModule;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.sun.jersey.api.core.PackagesResourceConfig.PROPERTY_PACKAGES;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS;

public class BaseServerModule extends ServerModule
{
    /**
     * These configuration parameters are normally specified as init-param entries in the web.xml. We have to set
     * them here because GuiceFilter instead has you pass them as arguments to
     * {@link com.google.inject.servlet.ServletModule.ServletKeyBindingBuilder#with(Class, java.util.Map)}.
     *
     * @see com.google.inject.servlet.ServletModule#configureServlets()
     */
    private static final Iterable<String> requestFilterClassNames = ImmutableList.of(
            // The logging filter is still incompatible with the GZIP filter
            //GZIPContentEncodingFilter.class.getName(),
            LoggingFilter.class.getName()
    );

    /**
     * These items are <i>intentionally</i> in the reverse order of the above filters. Jersey respects the order in which
     * these items appear, and they should be applied to the response in the reverse order of their application to
     * the request.
     */
    private static final Iterable<String> responseFilterClassNames = ImmutableList.of(
            LoggingFilter.class.getName()
            //GZIPContentEncodingFilter.class.getName()
    );

    private static final ImmutableMap.Builder<String, String> JERSEY_PARAMS = new ImmutableMap.Builder<String, String>()
            .put(PROPERTY_CONTAINER_REQUEST_FILTERS, Joiner.on(';').join(requestFilterClassNames))
            .put(PROPERTY_CONTAINER_RESPONSE_FILTERS, Joiner.on(';').join(responseFilterClassNames))
                    // The LoggingFilter will log the body by default, which breaks StreamingOutput
            .put("com.sun.jersey.config.feature.logging.DisableEntitylogging", "true");

    // Extra Guice bindings
    private final Map<Class, Object> bindings = new HashMap<Class, Object>();
    // System properties
    private final Properties props;
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
    // Jersey resources
    final String jerseyUriPattern;
    final List<String> jerseyResources = new ArrayList<String>();
    // Extra Guice modules to install
    final List<Module> modules = new ArrayList<Module>();
    private final Map<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>> filters;
    private final Map<String, Class<? extends HttpServlet>> jerseyServlets;
    private final Map<String, Class<? extends HttpServlet>> servlets;

    public BaseServerModule(final Map<Class, Object> bindings,
                            final List<Class> configs,
                            final List<Class<? extends HealthCheck>> healthchecks,
                            final List<Class> beans,
                            final String areciboProfile,
                            final boolean trackRequests,
                            final boolean log4jEnabled,
                            final String jerseyUriPattern,
                            final List<String> jerseyResources,
                            final List<Module> modules,
                            final Map<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>> filters,
                            final Map<String, Class<? extends HttpServlet>> jerseyServlets,
                            final Map<String, Class<? extends HttpServlet>> servlets)
    {
        this.bindings.putAll(bindings);
        this.props = System.getProperties();
        this.configs.addAll(configs);
        this.healthchecks.addAll(healthchecks);
        this.beans.addAll(beans);
        this.areciboProfile = areciboProfile;
        this.log4jEnabled = log4jEnabled;
        this.trackRequests = trackRequests;
        this.jerseyUriPattern = jerseyUriPattern;
        this.jerseyResources.addAll(jerseyResources);
        this.modules.addAll(modules);
        this.filters = filters;
        this.jerseyServlets = jerseyServlets;
        this.servlets = servlets;

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
        configureRegularServlets();
        configureJersey();
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
            bind(configClass).toInstance(new ConfigurationObjectFactory(props).build(configClass));
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

    protected void configureRegularServlets()
    {
        for (final String urlPattern : servlets.keySet()) {
            serveRegex(urlPattern).with(servlets.get(urlPattern));
        }
    }

    protected void configureJersey()
    {
        for (final String urlPattern : jerseyServlets.keySet()) {
            serveRegex(urlPattern).with(jerseyServlets.get(urlPattern), JERSEY_PARAMS.build());
        }

        // Catch-all resources
        if (jerseyResources.size() != 0) {
            JERSEY_PARAMS.put(PROPERTY_PACKAGES, StringUtils.join(jerseyResources, ";"));
            serveRegex(jerseyUriPattern).with(GuiceContainer.class, JERSEY_PARAMS.build());
        }
    }
}
