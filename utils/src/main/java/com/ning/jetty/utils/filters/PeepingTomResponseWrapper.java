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

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpServletResponse wrapper that exposes the underlying stream and other attributes
 */
public class PeepingTomResponseWrapper extends ServletResponseWrapper implements HttpServletResponse
{
    private final Map<String, List<String>> headers = new HashMap<String, List<String>>();

    private OpenableServletOutputStream stream = null;
    private volatile int status = -1;
    private volatile Long firstByteReceived = null;

    public PeepingTomResponseWrapper(final ServletResponse response)
    {
        super(response);
    }

    public Map<String, List<String>> getHeaders()
    {
        return headers;
    }

    public int getStatus()
    {
        return status;
    }

    public Long getTimeOfFirstByte()
    {
        return firstByteReceived;
    }

    private class OpenableServletOutputStream extends ServletOutputStream
    {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private ServletOutputStream originalOutputStream = null;
        private PrintWriter originalWriter = null;

        public OpenableServletOutputStream(final ServletOutputStream originalOutputStream)
        {
            this.originalOutputStream = originalOutputStream;
        }

        public OpenableServletOutputStream(final PrintWriter originalWriter)
        {
            this.originalWriter = originalWriter;
        }

        @Override
        public void write(final int b) throws IOException
        {
            outputStream.write(b);
            if (originalOutputStream != null) {
                originalOutputStream.write(b);
            }
            else {
                originalWriter.write(b);
            }
            firstByteReceived = System.currentTimeMillis();
        }

        public ByteArrayOutputStream getOutputStream()
        {
            return outputStream;
        }

        @Override
        public String toString()
        {
            return String.valueOf(outputStream);
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException
    {
        if (stream == null) {
            try {
                final ServletOutputStream originalOutputStream = super.getOutputStream();
                stream = new OpenableServletOutputStream(originalOutputStream);
            }
            catch (IllegalStateException e) {
                final PrintWriter originalWriter = super.getWriter();
                stream = new OpenableServletOutputStream(originalWriter);
            }
        }
        return stream;
    }

    public ByteArrayOutputStream getUnderlyingStream()
    {
        if (stream == null) {
            return null;
        }
        else {
            return stream.getOutputStream();
        }
    }

    // The rest of this class is boilerplate from HttpServletResponseWrapper

    @Override
    public void addCookie(final Cookie cookie)
    {
        this._getHttpServletResponse().addCookie(cookie);
    }

    @Override
    public boolean containsHeader(final String name)
    {
        return this._getHttpServletResponse().containsHeader(name);
    }

    @Override
    public String encodeURL(final String url)
    {
        return this._getHttpServletResponse().encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(final String url)
    {
        return this._getHttpServletResponse().encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(final String url)
    {
        return this._getHttpServletResponse().encodeUrl(url);
    }

    @Override
    public String encodeRedirectUrl(final String url)
    {
        return this._getHttpServletResponse().encodeRedirectUrl(url);
    }

    @Override
    public void sendError(final int sc, final String msg) throws IOException
    {
        this._getHttpServletResponse().sendError(sc, msg);
    }

    @Override
    public void sendError(final int sc) throws IOException
    {
        this._getHttpServletResponse().sendError(sc);
    }

    @Override
    public void sendRedirect(final String location) throws IOException
    {
        this._getHttpServletResponse().sendRedirect(location);
    }

    @Override
    public void setDateHeader(final String name, final long date)
    {
        this._getHttpServletResponse().setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(final String name, final long date)
    {
        this._getHttpServletResponse().addDateHeader(name, date);
    }

    @Override
    public void setHeader(final String name, final String value)
    {
        headers.put(name, new ArrayList<String>());
        headers.get(name).add(value);
        this._getHttpServletResponse().setHeader(name, value);
    }

    @Override
    public void addHeader(final String name, final String value)
    {
        if (headers.get(name) == null) {
            headers.put(name, new ArrayList<String>());
        }
        headers.get(name).add(value);
        this._getHttpServletResponse().addHeader(name, value);
    }

    @Override
    public void setIntHeader(final String name, final int value)
    {
        headers.put(name, new ArrayList<String>());
        headers.get(name).add(String.valueOf(value));
        this._getHttpServletResponse().setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(final String name, final int value)
    {
        if (headers.get(name) == null) {
            headers.put(name, new ArrayList<String>());
        }
        headers.get(name).add(String.valueOf(value));
        this._getHttpServletResponse().addIntHeader(name, value);
    }

    @Override
    public String getHeader(final String name)
    {
        final List<String> foundHeaders = headers.get(name);
        if (foundHeaders != null) {
            return foundHeaders.get(0);
        }
        else {
            return null;
        }
    }

    @Override
    public Collection<String> getHeaders(final String name)
    {
        return headers.get(name);
    }

    @Override
    public Collection<String> getHeaderNames()
    {
        return headers.keySet();
    }

    @Override
    public void setStatus(final int sc)
    {
        status = sc;
        this._getHttpServletResponse().setStatus(sc);
    }

    @Override
    public void setStatus(final int sc, final String sm)
    {
        status = sc;
        this._getHttpServletResponse().setStatus(sc, sm);
    }

    private HttpServletResponse _getHttpServletResponse()
    {
        return (HttpServletResponse) super.getResponse();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("PeepingTomResponseWrapper");
        sb.append("{firstByteReceived=").append(firstByteReceived);
        sb.append(", headers=").append(headers);
        sb.append(", status=").append(status);
        sb.append(", stream=").append(stream);
        sb.append('}');
        return sb.toString();
    }
}
