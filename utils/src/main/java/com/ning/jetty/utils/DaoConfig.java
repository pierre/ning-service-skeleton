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

package com.ning.jetty.utils;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.Description;
import org.skife.config.TimeSpan;

public interface DaoConfig
{
    @Description("The jdbc url for the database")
    @Config("com.ning.core.dao.url")
    @Default("jdbc:mysql://127.0.0.1:3306/information_schema")
    String getJdbcUrl();

    @Description("The jdbc user name for the database")
    @Config("com.ning.core.dao.user")
    @Default("root")
    String getUsername();

    @Description("The jdbc password for the database")
    @Config("com.ning.core.dao.password")
    @Default("root")
    String getPassword();

    @Description("The minimum allowed number of idle connections to the database")
    @Config("com.ning.core.dao.minIdle")
    @Default("1")
    int getMinIdle();

    @Description("The maximum allowed number of active connections to the database")
    @Config("com.ning.core.dao.maxActive")
    @Default("10")
    int getMaxActive();

    @Description("How long to wait before a connection attempt to the database is considered timed out")
    @Config("com.ning.core.dao.connectionTimeout")
    @Default("10s")
    TimeSpan getConnectionTimeout();

    @Description("The time for a connection to remain unused before it is closed off")
    @Config("com.ning.core.dao.idleMaxAge")
    @Default("60m")
    TimeSpan getIdleMaxAge();

    @Description("Any connections older than this setting will be closed off whether it is idle or not. Connections " +
            "currently in use will not be affected until they are returned to the pool")
    @Config("com.ning.core.dao.maxConnectionAge")
    @Default("0m")
    TimeSpan getMaxConnectionAge();

    @Description("Time for a connection to remain idle before sending a test query to the DB")
    @Config("com.ning.core.dao.idleConnectionTestPeriod")
    @Default("5m")
    TimeSpan getIdleConnectionTestPeriod();
}