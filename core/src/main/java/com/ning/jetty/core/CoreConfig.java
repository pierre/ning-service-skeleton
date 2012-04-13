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

package com.ning.jetty.core;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

public interface CoreConfig
{
    @Config("com.ning.core.server.ip")
    @Default("127.0.0.1")
    String getServerHost();

    @Config("com.ning.core.server.port")
    @Default("8080")
    int getServerPort();

    @Config("com.ning.core.server.server.ssl.enabled")
    @Default("false")
    boolean isSSLEnabled();

    @Config("com.ning.core.server.server.ssl.port")
    @Default("8443")
    int getServerSslPort();

    @Config("com.ning.core.server.jetty.stats")
    @Default("true")
    boolean isJettyStatsOn();

    @Config("com.ning.core.server.jetty.ssl.keystore")
    @DefaultNull
    String getSSLkeystoreLocation();

    @Config("com.ning.core.server.jetty.ssl.keystore.password")
    @DefaultNull
    String getSSLkeystorePassword();

    @Config("com.ning.core.server.jetty.maxThreads")
    @Default("2000")
    int getMaxThreads();

    @Config("com.ning.core.server.jetty.minThreads")
    @Default("2")
    int getMinThreads();

    @Config("com.ning.core.server.jetty.logPath")
    @Default(".logs")
    String getLogPath();

    @Config("com.ning.core.server.jetty.resourceBase")
    @DefaultNull
    String getResourceBase();
}
