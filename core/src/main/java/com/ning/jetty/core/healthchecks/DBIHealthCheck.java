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

package com.ning.jetty.core.healthchecks;

import com.google.inject.Inject;
import com.yammer.metrics.core.HealthCheck;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;
import org.weakref.jmx.Managed;

public class DBIHealthCheck extends HealthCheck
{
    @Inject
    private DBI dbi;

    @Override
    public String name()
    {
        return "DBIHealthCheck";
    }

    @Override
    public Result check()
    {
        Handle handle = null;
        try {
            handle = dbi.open();
            final int test = handle.createQuery("select 1 as test").map(IntegerMapper.FIRST).first();
            if (test == 1) {
                return Result.healthy();
            }
            else {
                return Result.unhealthy(String.format("%s != 1", test));
            }
        }
        catch (Throwable t) {
            return Result.unhealthy(t.toString());
        }
        finally {
            if (handle != null) {
                handle.close();
            }
        }
    }

    @Managed
    public boolean isHealthy()
    {
        return check().isHealthy();
    }
}