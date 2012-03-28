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

package com.ning.jetty.core.modules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.Assert;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Map;

@Guice(modules = ServerModule.class)
public class TestInjection
{
    @Inject
    private ObjectMapper mapper;

    static final class WorldCupChampion
    {
        public String country;
        public DateTime date;

        public String getCountry()
        {
            return country;
        }

        public DateTime getDate()
        {
            return date;
        }
    }

    @Test(groups = "fast")
    public void testJackson() throws Exception
    {
        final String country = "France";
        final String date = "1998-07-12T00:00:00.000Z";
        final String someJson = "{\"country\":\"" + country + "\",\"date\":\"" + date + "\"}";

        final Map<String, String> jsonObject = mapper.readValue(someJson, new TypeReference<Map<String, String>>()
        {
        });

        Assert.assertEquals(jsonObject.get("country"), country);
        Assert.assertEquals(jsonObject.get("date"), date);
        Assert.assertEquals(mapper.writeValueAsString(jsonObject), someJson);

        final WorldCupChampion champion = mapper.readValue(someJson, WorldCupChampion.class);
        Assert.assertEquals(champion.getCountry(), country);
        Assert.assertEquals(champion.getDate(), new DateTime(date, DateTimeZone.UTC));
        Assert.assertEquals(mapper.writeValueAsString(champion), someJson);
    }
}
