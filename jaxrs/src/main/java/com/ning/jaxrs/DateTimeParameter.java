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

package com.ning.jaxrs;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Convert a Jersey timestamp parameter to a DateTime
 * <p/>
 * Usage:
 *
 * @GET public Response getSomethingByDate(@QueryParam("date") final DateTimeParameter date)
 * <p/>
 * Valid values:
 * <ul>
 * <li>null (now)</li>
 * <li>2012</li>
 * <li>2012-03</li>
 * <li>2012-03-21</li>
 * <li>2012-03-21T12:42</li>
 * <li>2012-03-21T15:31:06.126Z</li>
 * <li>2012-03-21T05:33:05.966-10:00</li>
 * </ul>
 */
public class DateTimeParameter
{
    private final DateTime value;

    public DateTimeParameter(final String value)
    {
        if (value != null && !value.isEmpty()) {
            this.value = new DateTime(value);
        }
        else {
            // Default value is "now".
            this.value = new DateTime(DateTimeZone.UTC);
        }
    }

    public DateTime getValue()
    {
        return value;
    }
}