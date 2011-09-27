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

package com.ning.jetty.utils.servlets;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class HttpProxyServlet extends HttpServlet
{
    // Timeout of 2MSL to close TIME_WAIT sockets
    private static final long TWO_MSL = 240000000000L;

    @Inject
    private ServiceFinder serviceFinder;

    private long proxyAge = -1;
    private Proxy proxy = null;

    public Proxy createProxy() throws ServletException
    {
        proxy = new Proxy(serviceFinder);
        proxy.init(getServletConfig());
        return proxy;
    }

    public Proxy getProxy()
    {
        return proxy;
    }

    private void proxyService(final ServletRequest req, final ServletResponse resp) throws ServletException, IOException
    {
        try {
            // Poor man's load balancing
            if (proxy == null || System.nanoTime() - proxyAge >= TWO_MSL) {
                proxy = createProxy();
                proxyAge = System.nanoTime();
            }

            proxy.service(req, resp);
        }
        catch (IOException e) {
            // Remote host down? Recycle the proxy just in case.
            proxy.destroy();
            proxy = null;
            throw e;
        }
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        proxyService(req, resp);
    }

    @Override
    public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException
    {
        proxyService(req, res);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        proxyService(req, resp);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        proxyService(req, resp);
    }

    @Override
    protected void doHead(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        proxyService(req, resp);
    }

    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        proxyService(req, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        proxyService(req, resp);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        proxyService(req, resp);
    }

    @Override
    protected void doTrace(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        proxyService(req, resp);
    }
}
