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

import com.ning.jetty.core.listeners.SetupJULBridge;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.xml.XmlConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Map;

/**
 * Embed Jetty
 */
public class HttpServer
{
    private final Server server;

    public HttpServer(final String jettyXml) throws Exception
    {
        server = new Server();
        configure(jettyXml);
    }

    public void configure(final String jettyXml) throws Exception
    {
        final XmlConfiguration configuration = new XmlConfiguration(Resources.getResource(jettyXml));
        configuration.configure(server);
    }

    public void configure(final MBeanServer mbeanServer, final boolean isStatsOn, final String localIp, final int localPort,
                          final Iterable<EventListener> eventListeners, final Map<FilterHolder, String> filterHolders)
    {
        server.setStopAtShutdown(true);

        // Setup JMX
        configureJMX(mbeanServer);

        // Configure main connector
        configureMainConnector(isStatsOn, localIp, localPort);

        final ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        // Required! See ContextHandler#getResource and http://docs.codehaus.org/display/JETTY/Embedding+Jetty
        final String webapp = this.getClass().getClassLoader().getResource("webapp").toExternalForm();
        context.setResourceBase(webapp);

        // Jersey insists on using java.util.logging (JUL)
        final EventListener listener = new SetupJULBridge();
        context.addEventListener(listener);

        for (final EventListener eventListener : eventListeners) {
            context.addEventListener(eventListener);
        }

        for (final FilterHolder filterHolder : filterHolders.keySet()) {
            context.addFilter(filterHolder, filterHolders.get(filterHolder), EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));
        }

        // Make sure Guice filter all requests
        final FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
        context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));

        // Backend servlet for Guice - never used
        final ServletHolder sh = new ServletHolder(DefaultServlet.class);
        context.addServlet(sh, "/*");
    }

    public void enableSSL(final boolean isStatsOn, final int localSslPort, final String sslKeyStorePath, final String sslKeyStorePassword)
    {
        configureSslConnector(isStatsOn, localSslPort, sslKeyStorePath, sslKeyStorePassword);
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

    private void configureJMX(MBeanServer mbeanServer)
    {
        final MBeanContainer mbContainer = new MBeanContainer(mbeanServer);
        server.getContainer().addEventListener(mbContainer);
        server.addBean(mbContainer);
        mbContainer.addBean(Log.getLogger(HttpServer.class));
    }

    private void configureMainConnector(boolean isStatsOn, String localIp, int localPort)
    {
        final Connector connector = new SelectChannelConnector();
        connector.setStatsOn(isStatsOn);
        connector.setHost(localIp);
        connector.setPort(localPort);
        server.addConnector(connector);
    }

    private void configureSslConnector(boolean isStatsOn, int localSslPort, String sslKeyStorePath, String sslKeyStorePassword)
    {
        final SslConnector sslConnector = new SslSelectChannelConnector();
        sslConnector.setStatsOn(isStatsOn);
        sslConnector.setPort(localSslPort);
        SslContextFactory sslContextFactory = sslConnector.getSslContextFactory();
        sslContextFactory.setKeyStorePath(sslKeyStorePath);
        sslContextFactory.setKeyStorePassword(sslKeyStorePassword);
        server.addConnector(sslConnector);
    }
}