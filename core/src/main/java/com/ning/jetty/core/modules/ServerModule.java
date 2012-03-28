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

package com.ning.jetty.core.modules;

import com.ning.jersey.metrics.TimedResourceModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.jaxrs.json.Annotations;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.guice.InstrumentationModule;
import com.yammer.metrics.guice.servlet.AdminServletModule;
import org.weakref.jmx.guice.MBeanModule;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;

public class ServerModule extends ServletModule
{
    protected Multibinder<HealthCheck> healthChecksBinder;

    @Override
    public void configureServlets()
    {
        installJackson();
        installJMX();
        installStats();
    }

    protected void installJackson()
    {
        final ObjectMapper mapper = getJacksonProvider().get();

        bind(ObjectMapper.class).toInstance(mapper);
        bind(JacksonJsonProvider.class)
                .toInstance(new JacksonJsonProvider(mapper, new Annotations[]{Annotations.JACKSON, Annotations.JAXB}));
    }

    /**
     * Override this method to provide your own Jackson provider.
     *
     * @return ObjectMapper provider for Jackson
     */
    protected Provider<ObjectMapper> getJacksonProvider()
    {
        return new Provider<ObjectMapper>()
        {
            @Override
            public ObjectMapper get()
            {
                final ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JodaModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                return mapper;
            }
        };
    }

    protected void installJMX()
    {
        final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        binder().bind(MBeanServer.class).toInstance(mBeanServer);

        install(new MBeanModule());
    }

    protected void installStats()
    {
        // Codahale's metrics
        install(new InstrumentationModule());

        // Healthchecks
        healthChecksBinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
        install(new AdminServletModule("/1.0/healthcheck", "/1.0/metrics", "/1.0/ping", "/1.0/threads"));

        // Metrics/Jersey integration
        install(new TimedResourceModule());
    }
}
