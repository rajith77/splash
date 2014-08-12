package org.splash.messaging;
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

/**
 * Provides an entry point for using the messaging API.
 * It provides several methods for obtaining a connection 
 * or listening for connections which could then
 * be used to create the appropriate constructs to send and receive messages.
 * 
 * It also acts as a factory for Message objects.
 * 
 * <h4>Connection URL Syntax</h4>
 *
 *  The URL has the following form:
 *<pre>
 *    [ amqp[s]:// ] [user[:password]@] domain]
 *
 *  Where domain can be one of:
 *
 *    host | host:port | ip | ip:port | name
 *
 *  The following are valid examples of addresses:
 *
 *   - example.org
 *   - example.org:1234
 *   - amqp://example.org
 *   - amqps://example.org
 *   - amqps://fred:trustno1@example.org
 *   - 127.0.0.1:1234
 *   - amqps://127.0.0.1:1234
 *</pre> 
 */
public class Messaging
{
    private static MessagingFactory MESSAGING_FACTORY;

    static
    {
        try
        {
            Class<? extends MessagingFactory> clazz = Class.forName(
                    System.getProperty("splash.messaging.factory",
                            "org.splash.messaging.amqp.proton.DefaultMessagingFactory"))
                    .asSubclass(MessagingFactory.class);
            MESSAGING_FACTORY = clazz.newInstance();
        }
        catch (Exception e)
        {
            throw new Error("Unable to load implementation", e);
        }
    }

    private Messaging()
    {
    }

    /**
     * Provides a concrete instance of the Message interface that can be used
     * for sending.
     * 
     * @see Message
     */
    public static Message message()
    {
        return MESSAGING_FACTORY.message();
    }

    /**
     * Connects to the remote peer identified by the given URL.
     * 
     * @param url
     *            : See above for URL formats.
     * @param handlers
     *            : The handler(s) responsible for handling messaging events. @see
     *            EventHandler
     * @return : Connection
     * @throws MessagingException
     * @throws NetworkException
     */
    public static Connection connect(String url, EventHandler... handlers) throws MessagingException, NetworkException
    {
        return MESSAGING_FACTORY.connect(url, handlers);
    }

    /**
     * Connects to the remote peer identified by the host, port combination.
     * 
     * @param host
     * @param port
     * @param handlers
     *            : The handler(s) responsible for handling messaging events. @see
     *            EventHandler
     * @return : Connection
     * @throws MessagingException
     * @throws NetworkException
     */
    public static Connection connect(String host, int port, EventHandler... handlers) throws MessagingException,
            NetworkException
    {
        return MESSAGING_FACTORY.connect(host, port, handlers);
    }

    /**
     * Connects to the remote peer identified by the given @see
     * ConnectionSettings.
     * 
     * @param url
     *            : See above for URL formats.
     * @param handlers
     *            : The handler(s) responsible for handling messaging events. @see
     *            EventHandler
     * @return : Connection
     * @throws MessagingException
     * @throws NetworkException
     */
    public static Connection connect(ConnectionSettings settings, EventHandler... handlers) throws MessagingException,
            NetworkException
    {
        return MESSAGING_FACTORY.connect(settings, handlers);
    }

    /**
     * Binds to the socket identified by the host:port given in the @see
     * ConnectionSettings and listens for incoming messages.
     * 
     * @param settings
     * @param handlers
     *            : The handler(s) responsible for handling messaging events. @see
     *            EventHandler
     * @return Server
     * @throws MessagingException
     * @throws NetworkException
     */
    public static Server listen(ConnectionSettings settings, EventHandler... handlers) throws MessagingException,
            NetworkException
    {
        return MESSAGING_FACTORY.listen(settings, handlers);
    }

    /**
     * Binds to the socket identified by the host:port and listens for incoming
     * messages.
     * 
     * @param host
     * @param port
     * @param handlers
     *            : The handler(s) responsible for handling messaging events. @see
     *            EventHandler
     * @return Server
     * @throws MessagingException
     * @throws NetworkException
     */
    public static Server listen(String host, int port, EventHandler... handlers) throws MessagingException,
            NetworkException
    {
        return MESSAGING_FACTORY.listen(host, port, handlers);
    }
}