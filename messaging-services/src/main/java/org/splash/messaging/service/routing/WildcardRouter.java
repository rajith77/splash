/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.splash.messaging.service.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.splash.messaging.Message;
import org.splash.messaging.service.management.ManageableEntity;
import org.splash.messaging.service.management.ManageableEntityFactory;
import org.splash.messaging.service.management.ManageableEntityType;
import org.splash.messaging.service.management.ManagementAttribute;

@ManageableEntityType("org.splash.WildcardRouter")
public class WildcardRouter extends Router
{
    public static class Factory implements ManageableEntityFactory
    {
        static Factory _INSTANCE = new Factory();

        public static Factory get()
        {
            return _INSTANCE;
        }

        public ManageableEntity create(String id, String name, Map<String, Object> args)
                throws IllegalArgumentException
        {
            return new WildcardRouter(id, name, args);
        }
    }

    static final String ROUTING_KEY_0_10 = "x-amqp-0-10.routing-key";

    @ManagementAttribute("pattern")
    protected String _pattern;

    protected final Map<String, RouteEntry> _routes = new HashMap<String, RouteEntry>();

    WildcardRouter(String id, String name, Map<String, Object> args)
    {
        super(id, name, args);
    }

    String getPattern()
    {
        return _pattern;
    }

    @Override
    public List<String> route(Message msg)
    {
        List<String> dests = new LinkedList<String>();

        String key = msg.getSubject();
        if (key == null)
        {
            if (msg.getApplicationProperties() != null && msg.getApplicationProperties().containsKey(ROUTING_KEY_0_10))
            {
                key = (String) msg.getApplicationProperties().get(ROUTING_KEY_0_10);
            }
        }

        for (String str : _routes.keySet())
        {
            if (_routes.get(str)._pattern.matcher(key).matches())
            {
                for (WildcardRoute route : _routes.get(str)._routes)
                {
                    dests.add(route.getDestination());
                }
            }
        }

        if (dests.size() == 0 && _altAddress != null)
        {
            dests.add(_altAddress);
        }

        return dests;
    }

    @Override
    public String getType()
    {
        return this.getClass().getAnnotation(ManageableEntityType.class).value();
    }

    @Override
    public void addRoute(Route route)
    {
        WildcardRoute wildcardRoute = (WildcardRoute) route;
        if (_routes.containsKey(wildcardRoute.getPattern()))
        {
            _routes.get(wildcardRoute.getPattern())._routes.add(wildcardRoute);
        }
        else
        {
            RouteEntry entry = this.new RouteEntry(Pattern.compile(wildcardRoute.getPattern()));
            entry._routes.add(wildcardRoute);
            _routes.put(wildcardRoute.getPattern(), entry);
        }
    }

    @Override
    public void updateRoute(Route route)
    {
        addRoute(route);
    }

    @Override
    public void removeRoute(Route route)
    {
        WildcardRoute wildcardRoute = (WildcardRoute) route;
        if (_routes.containsKey(wildcardRoute.getPattern()))
        {
            _routes.get(wildcardRoute.getPattern())._routes.remove(wildcardRoute);
            if (_routes.get(wildcardRoute.getPattern())._routes.isEmpty())
            {
                _routes.remove(wildcardRoute.getPattern());
            }
        }
    }

    class RouteEntry
    {
        Pattern _pattern;

        List<WildcardRoute> _routes;

        RouteEntry(Pattern pattern)
        {
            _pattern = pattern;
            _routes = new ArrayList<WildcardRoute>();
        }
    }
}