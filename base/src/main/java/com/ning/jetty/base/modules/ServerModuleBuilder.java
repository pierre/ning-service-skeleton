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

import com.google.inject.Module;
import com.ning.jetty.core.modules.ServerModule;
import com.yammer.metrics.core.HealthCheck;

import java.util.ArrayList;
import java.util.List;

public class ServerModuleBuilder
{
    private final List<Class> configs = new ArrayList<Class>();
    private final List<Class<? extends HealthCheck>> healthchecks = new ArrayList<Class<? extends HealthCheck>>();
    private final List<Class> beans = new ArrayList<Class>();
    private String areciboProfile = null;
    private boolean trackRequests = false;
    private final List<String> resources = new ArrayList<String>();
    private final List<Module> modules = new ArrayList<Module>();

    public ServerModuleBuilder()
    {
    }

    public ServerModuleBuilder addConfig(final Class config)
    {
        configs.add(config);
        return this;
    }

    public ServerModuleBuilder addHealthCheck(final Class<? extends HealthCheck> healtcheck)
    {
        healthchecks.add(healtcheck);
        return this;
    }

    public ServerModuleBuilder addJMXExport(final Class bean)
    {
        beans.add(bean);
        return this;
    }

    public ServerModuleBuilder setAreciboProfile(final String areciboProfile)
    {
        this.areciboProfile = areciboProfile;
        return this;
    }

    public ServerModuleBuilder trackRequests()
    {
        this.trackRequests = true;
        return this;
    }

    public ServerModuleBuilder addResource(final String resource)
    {
        this.resources.add(resource);
        return this;
    }

    public ServerModuleBuilder addModule(final Module module)
    {
        this.modules.add(module);
        return this;
    }

    public ServerModule build()
    {
        return new BaseServerModule(configs,
            healthchecks,
            beans,
            areciboProfile,
            trackRequests,
            resources,
            modules);
    }
}
