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

import com.ning.jetty.core.modules.ServerModule;

import com.google.common.collect.Maps;
import com.google.inject.Module;
import com.yammer.metrics.core.HealthCheck;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerModuleBuilder
{
    private final Map<Class, Object> bindings = new HashMap<Class, Object>();
    private final List<Class> configs = new ArrayList<Class>();
    private final List<Class<? extends HealthCheck>> healthchecks = new ArrayList<Class<? extends HealthCheck>>();
    private final List<Class> beans = new ArrayList<Class>();
    private String areciboProfile = null;
    private boolean trackRequests = false;
    private boolean log4jEnabled = false;
    // By default, proxy all requests to the Guice/Jersey servlet
    private String jerseyUriPattern = "/.*";
    private final List<String> jerseyResources = new ArrayList<String>();
    private final List<Module> modules = new ArrayList<Module>();
    private final Map<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>> filters = new HashMap<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>>();
    private final Map<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>> filtersRegex = new HashMap<String, ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>>();
    private final Map<String, Class<? extends HttpServlet>> jerseyServlets = new HashMap<String, Class<? extends HttpServlet>>();
    private final Map<String, Class<? extends HttpServlet>> jerseyServletsRegex = new HashMap<String, Class<? extends HttpServlet>>();
    private final Map<String, Class<? extends HttpServlet>> servlets = new HashMap<String, Class<? extends HttpServlet>>();
    private final Map<String, Class<? extends HttpServlet>> servletsRegex = new HashMap<String, Class<? extends HttpServlet>>();

    public ServerModuleBuilder()
    {
    }

    public ServerModuleBuilder addBindings(final Map<Class, Object> bindings)
    {
        this.bindings.putAll(bindings);
        return this;
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

    public ServerModuleBuilder enableLog4J()
    {
        this.log4jEnabled = true;
        return this;
    }

    /**
     * Specify the Uri pattern to use for the Guice/Jersey servlet
     *
     * @param jerseyUriPattern Any Java-style regular expression
     * @return the current module builder
     * @see ServerModuleBuilder#addJerseyResource(String)
     */
    public ServerModuleBuilder setJerseyUriPattern(final String jerseyUriPattern)
    {
        this.jerseyUriPattern = jerseyUriPattern;
        return this;
    }

    /**
     * Add a package to be scanned for the Guice/Jersey servlet
     *
     * @param resource package to scan
     * @return the current module builder
     */
    public ServerModuleBuilder addJerseyResource(final String resource)
    {
        this.jerseyResources.add(resource);
        return this;
    }

    public ServerModuleBuilder addModule(final Module module)
    {
        this.modules.add(module);
        return this;
    }

    public ServerModuleBuilder addFilter(final String urlPattern, final Class<? extends Filter> filterKey)
    {
        return addFilter(urlPattern, filterKey, new HashMap<String, String>());
    }

    public ServerModuleBuilder addFilter(final String urlPattern, final Class<? extends Filter> filterKey, final Map<String, String> initParams)
    {
        if (this.filters.get(urlPattern) == null) {
            this.filters.put(urlPattern, new ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>());
        }

        this.filters.get(urlPattern).add(Maps.<Class<? extends Filter>, Map<String, String>>immutableEntry(filterKey, initParams));
        return this;
    }

    public ServerModuleBuilder addFilterRegex(final String urlPattern, final Class<? extends Filter> filterKey)
    {
        return addFilterRegex(urlPattern, filterKey, new HashMap<String, String>());
    }

    public ServerModuleBuilder addFilterRegex(final String urlPattern, final Class<? extends Filter> filterKey, final Map<String, String> initParams)
    {
        if (this.filtersRegex.get(urlPattern) == null) {
            this.filtersRegex.put(urlPattern, new ArrayList<Map.Entry<Class<? extends Filter>, Map<String, String>>>());
        }

        this.filtersRegex.get(urlPattern).add(Maps.<Class<? extends Filter>, Map<String, String>>immutableEntry(filterKey, initParams));
        return this;
    }

    public ServerModuleBuilder addServlet(final String urlPattern, final Class<? extends HttpServlet> filterKey)
    {
        this.servlets.put(urlPattern, filterKey);
        return this;
    }

    public ServerModuleBuilder addServletRegex(final String urlPattern, final Class<? extends HttpServlet> filterKey)
    {
        this.servletsRegex.put(urlPattern, filterKey);
        return this;
    }

    public ServerModuleBuilder addJerseyServlet(final String urlPattern, final Class<? extends HttpServlet> filterKey)
    {
        this.jerseyServlets.put(urlPattern, filterKey);
        return this;
    }

    public ServerModuleBuilder addJerseyServletRegex(final String urlPattern, final Class<? extends HttpServlet> filterKey)
    {
        this.jerseyServletsRegex.put(urlPattern, filterKey);
        return this;
    }

    public ServerModule build()
    {
        return new BaseServerModule(
                bindings,
                configs,
                healthchecks,
                beans,
                areciboProfile,
                trackRequests,
                log4jEnabled,
                jerseyUriPattern,
                jerseyResources,
                modules,
                filters,
                filtersRegex,
                jerseyServlets,
                jerseyServletsRegex,
                servlets,
                servletsRegex
        );
    }
}
