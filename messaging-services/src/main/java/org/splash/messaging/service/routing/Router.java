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

import java.util.List;
import java.util.Map;

import org.splash.messaging.InboundLink;
import org.splash.messaging.Message;
import org.splash.messaging.management.ManagementException;
import org.splash.messaging.management.ResponseCode;
import org.splash.messaging.service.management.ManageableEntity;
import org.splash.messaging.service.management.ManageableEntityType;
import org.splash.messaging.service.management.ManagementAttribute;

@ManageableEntityType("org.splash.Router")
public abstract class Router implements ManageableEntity
{
    static final String ADDRESS = "address";

    static final String ALTERNATE_ADDRESS = "alt_address";

    protected String _id;

    protected String _name;

    protected Map<String, Object> _args;

    @ManagementAttribute("address")
    protected String _address;

    @ManagementAttribute("altAddress")
    protected String _altAddress;

    protected InboundLink _link;

    public Router(String id, String name, Map<String, Object> args)
    {
        _id = id;
        _name = name;
        _args = args;
        if (args.containsKey(ADDRESS))
        {
            _address = (String) args.get(ADDRESS);
        }
        else
        {
            throw new IllegalArgumentException("address not specified");
        }
        if (args.containsKey(ALTERNATE_ADDRESS))
        {
            _altAddress = (String) args.get(ALTERNATE_ADDRESS);
        }
    }

    String getAddress()
    {
        return _address;
    }

    String getAltAddress()
    {
        return _altAddress;
    }

    void setLink(InboundLink link)
    {
        _link = link;
    }

    InboundLink getLink()
    {
        return _link;
    }

    public abstract List<String> route(Message msg);

    public abstract void addRoute(Route route);

    public abstract void updateRoute(Route route);

    public abstract void removeRoute(Route route);

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
        if (attributes.containsKey(ALTERNATE_ADDRESS))
        {
            _args.put(ALTERNATE_ADDRESS, attributes.get(ALTERNATE_ADDRESS));
            _altAddress = (String) attributes.get(ALTERNATE_ADDRESS);
        }
        return _args;
    }

    @Override
    public void delete() throws ManagementException
    {
        try
        {
            _link.close();
        }
        catch (Exception e)
        {
            throw new ManagementException(ResponseCode.INTERNAL_ERROR, "Error closing the link for address " + _address
                    + " due to : " + e.getMessage(), e);
        }
    }
}