To see an example of an application using this skeleton, see how the maven-jetty-plugin is configured (relevant files under [src/test/](https://github.com/pierre/ning-service-skeleton/tree/master/core/src/test)).

Guice
-----

As a bare minimum, you should install and/or extend `com.ning.jetty.core.modules.ServerModule`, and add the following dependency:


        <dependency>
            <groupId>com.ning.jetty</groupId>
            <artifactId>ning-service-skeleton-core</artifactId>
        </dependency>


Filters
-------

1.  Request and Response utilities

    `com.ning.jetty.utils.filters.PeepingTomRequestWrapper` and `com.ning.jetty.utils.filters.PeepingTomResponseWrapper`
    wrap `HttpServletRequestWrapper` and `ServletResponseWrapper` respectfully and cache certain attributes. You can use
    these classes in your filters to look into the request and/or response streams and various attributes
    (e.g. response code or headers).

    See `com.ning.jetty.utils.filters.TrackerFilter` for an example on how to use them.

2.  TrackerFilter

    You can use the provided `TrackerFilter` to send request logs to your log aggregating system. A default implementation is provided
    for the [collector](https://github.com/pierre/pierre) via the [eventtracker](https://github.com/pierre/eventtracker) library.
    See `com.ning.jetty.utils.filters.CollectorTracker`.

    In addition to the eventtracker system properties, you can set the name of the event via `com.ning.core.eventtracker.requestLogEventName`
    (`RequestLogEvent` by default).


Servlets
--------

1.  LogInvalidResourcesServlet

    The `com.ning.jetty.core.servlets.LogInvalidResourcesServlet` will log any request and return a 404 to the client. This is useful
    as a back-end servlet for Guice for example (the Guice filter requires a back-end servlet).

2.  HttpProxyServlet

    The `com.ning.jetty.utils.servlets.HttpProxyServlet` can be used to proxy request to a remote host. The remote host needs to be
    provided by implementing the `ServiceFinder` interface.


Log4j
-----

1.  LogLevelCounterAppender

    The `com.ning.jetty.log4j.LogLevelCounterAppender` log4j appender can be used to gather statistics on a per logger
    and level basis (via JMX).

    To use it, configure your log4j.xml:

        <appender name="LOG_LEVEL_COUNTER" class="com.ning.jetty.log4j.LogLevelCounterAppender"/>
        <root>
            <priority value="WARN"/>
            <appender-ref ref="LOG_LEVEL_COUNTER"/>
        </root>

Misc.
-----

1.  Adding healthchecks

   To add healthchecks, you need to extend `com.yammer.metrics.core.HealthCheck` and register them.

2.  Available providers

   You can use `com.ning.jetty.utils.providers.DBIProvider` to create DBI objects for your dao. This provider will setup metrics and BoneCP for you.
