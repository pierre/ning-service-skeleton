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
import com.ning.metrics.eventtracker.CollectorController;
import com.ning.metrics.eventtracker.smile.com.ning.metrics.serialization.event.SmileEnvelopeEvent;
import com.ning.metrics.eventtracker.smile.org.codehaus.jackson.JsonNode;
import com.ning.metrics.eventtracker.smile.org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Tracker implementation that uses the eventtracker library, to send events to the Ning collector. To use it:
 * <p/>
 * install(new CollectorControllerSmileModule());
 * install(new CollectorControllerHttpMBeanModule());
 * bind(Tracker.class).to(CollectorTracker.class).asEagerSingleton();
 */
public class CollectorTracker implements Tracker
{
    // Make sure to use the eventtracker mapper
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(CollectorTracker.class);
    private final CollectorController controller;
    private final String eventName = System.getProperty("com.ning.core.eventtracker.requestLogEventName", "RequestLogEvent");

    @Inject
    public CollectorTracker(final CollectorController controller)
    {
        this.controller = controller;
    }

    @Override
    public void trackRequest(final RequestLog request)
    {
        final JsonNode root = mapper.valueToTree(request);
        try {
            controller.offerEvent(new SmileEnvelopeEvent(eventName, root));
        }
        catch (IOException e) {
            log.warn("Got I/O exception trying to send RequestLog [{}]: {}", request, e.toString());
        }
    }
}
