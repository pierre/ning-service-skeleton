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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.ning.arecibo.jmx.AreciboMonitoringModule;
import com.ning.arecibo.metrics.guice.AreciboMetricsModule;
import com.ning.jetty.core.modules.ServerModule;
import com.ning.jetty.utils.arecibo.Jetty7AreciboConnector;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.yammer.metrics.core.HealthCheck;
import org.apache.commons.lang.StringUtils;
import org.skife.config.ConfigurationObjectFactory;
import org.weakref.jmx.guice.ExportBuilder;
import org.weakref.jmx.guice.MBeanModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.sun.jersey.api.core.PackagesResourceConfig.PROPERTY_PACKAGES;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS;
import static com.sun.jersey.api.core.ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS;


public class BaseServerModule extends ServerModule
{
    private static final List<String> FILTERS = ImmutableList.of(
        GZIPContentEncodingFilter.class.getName(),
        LoggingFilter.class.getName()
    );

    private static final ImmutableMap.Builder<String, String> JERSEY_PARAMS = new ImmutableMap.Builder<String, String>()
        .put(PROPERTY_CONTAINER_REQUEST_FILTERS, StringUtils.join(FILTERS, ";"))
            // Though it would seem to make sense that filters should be applied to responses in reverse order, in fact the
            // response filters appear to wrap each other up before executing, with the result being that execution order
            // is the reverse of the declared order.
        .put(PROPERTY_CONTAINER_RESPONSE_FILTERS, StringUtils.join(FILTERS, ";"));

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
    // Jersey resources
    final List<String> resources = new ArrayList<String>();
    // Extra Guice modules to install
    final List<Module> modules = new ArrayList<Module>();

    public BaseServerModule(final List<Class> configs,
                            final List<Class<? extends HealthCheck>> healthchecks,
                            final List<Class> beans,
                            final String areciboProfile,
                            final boolean trackRequests,
                            final List<String> resources,
                            final List<Module> modules)
    {
        this.props = System.getProperties();
        this.configs.addAll(configs);
        this.healthchecks.addAll(healthchecks);
        this.beans.addAll(beans);
        this.areciboProfile = areciboProfile;
        this.trackRequests = trackRequests;
        this.resources.addAll(resources);
        this.modules.addAll(modules);
    }

    @Override
    public void configureServlets()
    {
        super.configureServlets();

        installHealthChecks();
        installArecibo();
        installExtraModules();

        configureJersey();
    }

    @Override
    protected void configureConfig()
    {
        super.configureConfig();
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
    }

    protected void installHealthChecks()
    {
        final Multibinder<HealthCheck> healthChecksBinder = Multibinder.newSetBinder(binder(), HealthCheck.class);

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
    }

    private void installExtraModules()
    {
        for (final Module module : modules) {
            install(module);
        }
    }

    protected void configureJersey()
    {
        if (resources.size() == 0) {
            return;
        }

        JERSEY_PARAMS.put(PROPERTY_PACKAGES, StringUtils.join(resources, ";"));
        filter("*").through(GuiceContainer.class, JERSEY_PARAMS.build());
    }
}
