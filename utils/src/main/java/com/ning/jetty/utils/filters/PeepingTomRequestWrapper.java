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

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * HttpServletResquest wrapper that exposes the underlying stream and other attributes
 */
public class PeepingTomRequestWrapper extends HttpServletRequestWrapper
{
    private OpenableServletInputStream stream = null;

    private final String method;
    private final String requestURI;
    private final Map<String, String[]> parameterMap;

    public PeepingTomRequestWrapper(final HttpServletRequest request)
    {
        super(request);
        this.method = request.getMethod();
        this.requestURI = request.getRequestURI();
        this.parameterMap = request.getParameterMap();
    }

    @Override
    public String getMethod()
    {
        return method;
    }

    @Override
    public String getRequestURI()
    {
        return requestURI;
    }

    private class OpenableServletInputStream extends ServletInputStream
    {
        final int MAX_BUF_SIZE = 1024 * 1024;
        int pos = 0;

        final byte[] bytes = new byte[MAX_BUF_SIZE];
        private final ServletInputStream originalInputStream;

        public OpenableServletInputStream(final ServletInputStream originalInputStream)
        {
            this.originalInputStream = originalInputStream;
        }

        @Override
        public int read() throws IOException
        {
            if (pos >= MAX_BUF_SIZE) {
                return -1;
            }
            final int n = originalInputStream.read(bytes, pos, 1);
            pos++;

            if (n > 0) {
                return bytes[pos - 1];
            }
            else {
                return n;
            }
        }

        public ByteArrayInputStream getInputStream() throws IOException
        {
            if (bytes[0] == 0) {
                return null;
            }
            else {
                return new ByteArrayInputStream(bytes);
            }
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        if (stream == null) {
            final ServletInputStream originalInputStream = super.getInputStream();
            stream = new OpenableServletInputStream(originalInputStream);
        }
        return stream;
    }

    @Override
    public Map<String, String[]> getParameterMap()
    {
        return parameterMap;
    }

    @Override
    public String getParameter(final String name)
    {
        final String[] strings = parameterMap.get(name);
        if (strings.length == 0) {
            return null;
        }
        else {
            return strings[0];
        }
    }

    public ByteArrayInputStream getUnderlyingStream()
    {
        try {
            return ((OpenableServletInputStream) getInputStream()).getInputStream();
        }
        catch (IOException e) {
            return null;
        }
    }
}