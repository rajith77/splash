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
package org.splash.messaging.management;

import java.util.Map;

import org.splash.messaging.Message;
import org.splash.messaging.MessagingException;

public interface ManagementMessageFactory
{
    public static final class Factory
    {
        final static String className = System.getProperty("splash.management.messagefactory",
                "org.splash.messaging.proton.ManagementMessageFactoryImpl");

        public static ManagementMessageFactory create() throws MessagingException
        {
            try
            {
                Class<? extends ManagementMessageFactory> c = Class.forName(className).asSubclass(
                        ManagementMessageFactory.class);
                return c.newInstance();
            }
            catch (Exception e)
            {
                throw new MessagingException("Unable to instantiate the ManagementMessageFactory class", e);
            }
        }
    }

    void setLocales(String locales);

    Request parseRequest(Message m) throws ManagementException;

    Response parseResponse(Message m) throws ManagementException;

    Message response(Request req, ResponseCode code, Map<String, ? extends Object> attributes);

    Message response(Request req, ResponseCode code, String desc, Map<String, ? extends Object> attributes);

    Message create(String correlationId, String name, String type, Map<String, ? extends Object> attributes);

    Message updateByName(String correlationId, String name, String type, Map<String, ? extends Object> attributes);

    Message updateByID(String correlationId, String id, String type, Map<String, ? extends Object> attributes);

    Message readByName(String correlationId, String name, String type);

    Message readByID(String correlationId, String id, String type);

    Message deleteByName(String correlationId, String name, String type);

    Message deleteByID(String correlationId, String id, String type);
}