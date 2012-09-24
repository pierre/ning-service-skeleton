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

package com.ning.jetty.base.modules;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Future;

public class TestBaseServerModule
{
    @Test(groups = "slow")
    public void testJettyStartup() throws Exception
    {
        final ServerModuleBuilder builder = new ServerModuleBuilder();
        final ServletModule module = builder.build();
        final Server server = startServer(module);
        server.stop();
    }

    @Test(groups = "slow")
    public void testJerseyIntegration() throws Exception
    {
        final ServerModuleBuilder builder = new ServerModuleBuilder();
        builder.addJaxRSResource("com.ning.jetty.base.modules");
        builder.addModule(new HelloModule());
        final ServletModule module = builder.build();
        final Server server = startServer(module);

        final AsyncHttpClient client = new AsyncHttpClient();
        final Future<Response> responseFuture = client.prepareGet("http://127.0.0.1:" + server.getConnectors()[0].getPort() + "/hello/alhuile/").execute();
        final String body = responseFuture.get().getResponseBody();
        Assert.assertEquals(body, "Hello alhuile");

        server.stop();
    }

    private Server startServer(final ServletModule module) throws Exception
    {
        final Injector injector = Guice.createInjector(module);

        final Server server = new Server(getPort());
        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.addEventListener(new GuiceServletContextListener()
        {
            @Override
            protected Injector getInjector()
            {
                return injector;
            }
        });

        servletContextHandler.addFilter(GuiceFilter.class, "/*", null);
        servletContextHandler.addServlet(DefaultServlet.class, "/");
        server.setHandler(servletContextHandler);
        server.start();

        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    server.join();
                }
                catch (InterruptedException ignored) {
                }
            }
        };
        Assert.assertTrue(server.isRunning());
        return server;
    }

    private int getPort()
    {
        final int port;
        try {
            final ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();
        }
        catch (IOException e) {
            Assert.fail();
            return -1;
        }

        return port;
    }
}
