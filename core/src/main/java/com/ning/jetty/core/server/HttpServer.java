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

package com.ning.jetty.core.server;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.xml.XmlConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Embed Jetty using a jetty.xml file
 */
public class HttpServer
{
    private final Server server;

    public HttpServer(final String jettyXml) throws Exception
    {
        server = new Server();

        final XmlConfiguration configuration = new XmlConfiguration(Resources.getResource(jettyXml));
        configuration.configure(server);
    }

    @PostConstruct
    public void start() throws Exception
    {
        server.start();
        Preconditions.checkState(server.isRunning(), "server is not running");
    }

    @PreDestroy
    public void stop() throws Exception
    {
        server.stop();
    }
}