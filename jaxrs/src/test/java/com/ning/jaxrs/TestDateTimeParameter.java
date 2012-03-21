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
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDateTimeParameter
{
    private static final DateTimeFormatter ISO_DATE = ISODateTimeFormat.dateTime();

    @Test(groups = "fast")
    public void testGetValue() throws Exception
    {
        System.setProperty("user.timezone", "UTC");

        // Compare Epoch
        Assert.assertEquals(new DateTimeParameter(null).getValue().toDateTime(DateTimeZone.UTC).getMillis() / 1000, new DateTime(DateTimeZone.UTC).getMillis() / 1000);

        Assert.assertEquals(new DateTimeParameter("2012").getValue().getChronology().getZone(), DateTimeZone.UTC);

        Assert.assertEquals(new DateTimeParameter("2012").getValue().toDateTime(DateTimeZone.UTC), ISO_DATE.parseDateTime("2012-01-01T00:00:00.0Z"));
        Assert.assertEquals(new DateTimeParameter("2012-03").getValue().toDateTime(DateTimeZone.UTC), ISO_DATE.parseDateTime("2012-03-01T00:00:00.0Z"));
        Assert.assertEquals(new DateTimeParameter("2012-03-21").getValue().toDateTime(DateTimeZone.UTC), ISO_DATE.parseDateTime("2012-03-21T00:00:00.0Z"));
        Assert.assertEquals(new DateTimeParameter("2012-03-21T12:42").getValue().toDateTime(DateTimeZone.UTC), ISO_DATE.parseDateTime("2012-03-21T12:42:00.0Z"));
        Assert.assertEquals(new DateTimeParameter("2012-03-21T15:31:06.126Z").getValue(), ISO_DATE.parseDateTime("2012-03-21T15:31:06.126Z"));

        Assert.assertEquals(new DateTimeParameter("2012-03-21T05:33:05.966-10:00").getValue(), ISO_DATE.parseDateTime("2012-03-21T05:33:05.966-10:00"));
    }
}
