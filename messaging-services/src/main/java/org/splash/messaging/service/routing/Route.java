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

import java.util.Map;

import org.splash.messaging.management.ManagementException;
import org.splash.messaging.service.management.ManageableEntity;
import org.splash.messaging.service.management.ManageableEntityType;
import org.splash.messaging.service.management.ManagementAttribute;

@ManageableEntityType("org.splash.Route")
public abstract class Route implements ManageableEntity
{
    static final String DESTINATION = "destination";

    static final String ROUTER_ID = "routerId";

    protected String _id;

    protected String _name;

    protected Map<String, Object> _args;

    @ManagementAttribute("destination")
    protected String _dest;

    /**
     * To which router this route belongs to
     */
    @ManagementAttribute("routerId")
    protected String _routerId;

    protected Router _router;

    public Route(String id, String name, Map<String, Object> args) throws IllegalArgumentException
    {
        _id = id;
        _name = name;
        _args = args;
        if (args.containsKey(DESTINATION))
        {
            _dest = (String) args.get(DESTINATION);
        }
        else
        {
            throw new IllegalArgumentException("destination not specified");
        }
        if (args.containsKey(ROUTER_ID))
        {
            _routerId = (String) args.get(ROUTER_ID);
        }
        else
        {
            throw new IllegalArgumentException("destination not specified");
        }
    }

    String getRouterId()
    {
        return _routerId;
    }

    String getDestination()
    {
        return _dest;
    }
    
    void setRouter(Router router)
    {
        _router = router;
    }

    Router getRouter()
    {
        return _router;
    }

    // ----------- Management ---------------
    @Override
    public String getName()
    {
        return _name;
    }

    @Override
    public String getID()
    {
        return _id;
    }

    @Override
    public Map<String, Object> read()
    {
        return _args;
    }

    @Override
    public Map<String, Object> update(Map<String, Object> attributes) throws IllegalArgumentException
    {
        if (attributes.containsKey(DESTINATION))
        {
            _args.put(DESTINATION, attributes.get(DESTINATION));
            _dest = (String) attributes.get(DESTINATION);
        }
        _router.updateRoute(this);
        return _args;
    }

    @Override
    public void delete() throws ManagementException
    {
        _router.removeRoute(this);
    }

}
