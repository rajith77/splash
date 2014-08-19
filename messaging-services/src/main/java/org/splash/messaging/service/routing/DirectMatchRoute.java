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

import org.splash.messaging.service.management.ManageableEntity;
import org.splash.messaging.service.management.ManageableEntityFactory;
import org.splash.messaging.service.management.ManageableEntityType;
import org.splash.messaging.service.management.ManagementAttribute;

@ManageableEntityType("org.splash.DirectMatchRoute")
public class DirectMatchRoute extends Route
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
            return new DirectMatchRoute(id, name, args);
        }
    }

    static final String PATTERN = "pattern";

    @ManagementAttribute("pattern")
    protected String _pattern;

    public DirectMatchRoute(String id, String name, Map<String, Object> args) throws IllegalArgumentException
    {
        super(id, name, args);
        if (args.containsKey(PATTERN))
        {
            _args.put(PATTERN, args.get(PATTERN));
            _pattern = (String) args.get(PATTERN);
        }
        else
        {
            throw new IllegalArgumentException("pattern not specified");
        }
    }

    String getPattern()
    {
        return _pattern;
    }

    @Override
    public String getType()
    {
        return this.getClass().getAnnotation(ManageableEntityType.class).value();
    }

    @Override
    public Map<String, Object> update(Map<String, Object> attributes) throws IllegalArgumentException
    {
        // As the routes are keyed by pattern, you need to remove the old entry
        // and add the updated entry
        if (attributes.containsKey(PATTERN))
        {
            _router.removeRoute(this);
            _args.put(PATTERN, attributes.get(PATTERN));
            _pattern = (String) attributes.get(PATTERN);
            _router.addRoute(this);
        }
        super.update(attributes);
        return _args;
    }
}
