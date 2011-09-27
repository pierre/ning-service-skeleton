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

import com.ning.metrics.eventtracker.smile.org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
public class RequestLog
{
    protected long eventDate;
    protected String method;
    protected String protocol;
    protected String host;
    protected String path;
    protected String referer;
    protected String userAgent;
    protected String proximateIp;
    protected String forwardedForChain;
    protected String contentType;
    protected short responseCode;
    protected int responseLength;
    protected int responseTime;
    protected int timeToFirstByte;
    protected String coreHost;
    protected int corePort;

    public RequestLog(
        final long eventDate,
        final String method,
        final String protocol,
        final String host,
        final String path,
        final String referer,
        final String userAgent,
        final String proximateIp,
        final String forwardedForChain,
        final String contentType,
        final short responseCode,
        final int responseLength,
        final int responseTime,
        final int timeToFirstByte,
        final String coreHost,
        final int corePort
    )
    {
        this.eventDate = eventDate;
        this.method = method;
        this.protocol = protocol;
        this.host = host;
        this.path = path;
        this.referer = referer;
        this.userAgent = userAgent;
        this.proximateIp = proximateIp;
        this.forwardedForChain = forwardedForChain;
        this.contentType = contentType;
        this.responseCode = responseCode;
        this.responseLength = responseLength;
        this.responseTime = responseTime;
        this.timeToFirstByte = timeToFirstByte;
        this.coreHost = coreHost;
        this.corePort = corePort;
    }
}
