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
import java.util.List;
import java.util.Map;

import org.splash.messaging.Message;
import org.splash.messaging.service.management.ManageableEntity;
import org.splash.messaging.service.management.ManageableEntityFactory;
import org.splash.messaging.service.management.ManageableEntityType;

@ManageableEntityType("org.splash.DirectMatchRouter")
public class DirectMatchRouter extends Router
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
            return new DirectMatchRouter(id, name, args);
        }
    }

    static final String ROUTING_KEY_0_10 = "x-amqp-0-10.routing-key";

    protected final Map<String, List<Route>> _routes = new HashMap<String, List<Route>>();

    DirectMatchRouter(String id, String name, Map<String, Object> args)
    {
        super(id, name, args);
    }

    @Override
    public List<String> route(Message msg)
    {
        List<String> destinations = new ArrayList<String>();
        String key = msg.getSubject();
        if (key == null)
        {
            if (msg.getApplicationProperties() != null && msg.getApplicationProperties().containsKey(ROUTING_KEY_0_10))
            {
                key = (String) msg.getApplicationProperties().get(ROUTING_KEY_0_10);
            }
        }

        if (_routes.containsKey(key))
        {
            for (Route route : _routes.get(key))
            {
                destinations.add(route.getDestination());
            }
        }

        if (destinations.isEmpty())
        {
            destinations.add(_altAddress);
        }
        return destinations;
    }

    @Override
    public String getType()
    {
        return this.getClass().getAnnotation(ManageableEntityType.class).value();
    }

    @Override
    public void addRoute(Route route)
    {
        DirectMatchRoute directRoute = (DirectMatchRoute) route;
        if (_routes.containsKey(directRoute.getPattern()))
        {
            _routes.get(directRoute.getPattern()).add(directRoute);
        }
        else
        {
            List<Route> list = new ArrayList<Route>();
            list.add(directRoute);
            _routes.put(directRoute.getPattern(), list);
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
        DirectMatchRoute directRoute = (DirectMatchRoute) route;
        if (_routes.containsKey(directRoute.getPattern()))
        {
            _routes.get(directRoute.getPattern()).remove(directRoute);
            if (_routes.get(directRoute.getPattern()).isEmpty())
            {
                _routes.remove(directRoute.getPattern());
            }
        }
    }

    @Override
    public String toString()
    {
        return String.format("[name=%s, address=%s, alt-address=%s, type=%s]", _name, _address, _altAddress, getType());
    }
}