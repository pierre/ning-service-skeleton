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

package com.ning.jetty.utils.filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ning.jetty.core.CoreConfig;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Logs stats about every request to the tracker
 */
@Singleton
public class TrackerFilter implements Filter
{
    private final CoreConfig config;
    private final Tracker tracker;

    private FilterConfig filterConfig;

    @Inject
    public TrackerFilter(final CoreConfig config, final Tracker controller)
    {
        this.config = config;
        this.tracker = controller;
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException
    {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        final long start = new DateTime(DateTimeZone.UTC).getMillis();
        final PeepingTomResponseWrapper wrappedResponse = new PeepingTomResponseWrapper(response);

        try {
            chain.doFilter(request, wrappedResponse);
        }
        finally {
            try {
                logEvent(start, (HttpServletRequest) request, wrappedResponse);
            }
            catch (Throwable t) {
                filterConfig.getServletContext().log("Unable to capture request event", t);
            }
        }
    }

    private void logEvent(final long startMillis, final HttpServletRequest request, final PeepingTomResponseWrapper response) throws IOException
    {
        final long elapsed = System.currentTimeMillis() - startMillis;
        final Long timeOfFirstByte = response.getTimeOfFirstByte();

        final int timeToFirstByte;
        if (timeOfFirstByte == null) {
            timeToFirstByte = 0;
        }
        else {
            timeToFirstByte = (int) (timeOfFirstByte - startMillis);
        }

        final String query = request.getQueryString();
        String path = request.getRequestURI();
        if (query != null && !query.isEmpty()) {
            path += "?" + query;
        }

        final Enumeration xffChainEnumeration = request.getHeaders("X-Forwarded-For");
        String xffChain = "";
        while (xffChainEnumeration.hasMoreElements()) {
            xffChain += (String) xffChainEnumeration.nextElement();
        }

        final RequestLog event = new RequestLog(
            startMillis,
            request.getMethod(),
            request.getScheme(),
            request.getHeader("Host"),
            path,
            request.getHeader("Referer"),
            request.getHeader("User-Agent"),
            request.getRemoteAddr(),
            xffChain,
            response.getContentType(),
            (short) response.getStatus(),
            response.getUnderlyingStream() == null ? 0 : response.getUnderlyingStream().size(),
            (int) elapsed,
            timeToFirstByte,
            config.getServerHost(),
            config.getServerPort()
        );

        tracker.trackRequest(event);
    }
}
