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

package com.ning.jetty.utils.arecibo;

import com.google.inject.Inject;
import com.ning.arecibo.jmx.AreciboProfile;
import com.ning.arecibo.jmx.MBeanRegistrar;

/**
 * Exposes Jetty7 stats to Arecibo. To use it:
 * <p/>
 * bind(JettyAreciboConnector.class).asEagerSingleton();
 */
public class Jetty7AreciboConnector
{
    @Inject
    public Jetty7AreciboConnector(final AreciboProfile profile)
    {
        new MBeanRegistrar("org.eclipse.jetty.server.nio:type=selectchannelconnector,id=0")
            .addCounter("requests")
            .addCounter("connections")
            .addValue("connectionsDurationMax")
            .addValue("connectionsDurationMean")
            .addValue("connectionsDurationStdDev")
            .addCounter("connectionsDurationTotal")
            .addValue("connectionsOpen")
            .addValue("connectionsOpenMax")
            .addValue("connectionsRequestsMax")
            .addValue("connectionsRequestsMean")
            .addValue("connectionsRequestsStdDev")
            .register(profile);

        new MBeanRegistrar("org.eclipse.jetty.server.ssl:type=sslselectchannelconnector,id=0")
            .addCounter("requests")
            .addCounter("connections")
            .addValue("connectionsDurationMax")
            .addValue("connectionsDurationMean")
            .addValue("connectionsDurationStdDev")
            .addCounter("connectionsDurationTotal")
            .addValue("connectionsOpen")
            .addValue("connectionsOpenMax")
            .addValue("connectionsRequestsMax")
            .addValue("connectionsRequestsMean")
            .addValue("connectionsRequestsStdDev")
            .register(profile);

        new MBeanRegistrar("org.eclipse.jetty.util.thread:type=queuedthreadpool,id=0")
            .addValue("idleThreads")
            .addValue("idleThreads")
            .register(profile);
    }
}
