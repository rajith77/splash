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
 * Provides an entry point for using the messaging library.
 * It provides several methods for obtaining a connection which can then
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
            Class<? extends MessagingFactory> clazz = Class
                    .forName(
                            System.getProperty("splash.messaging.impl",
                                    "org.splash.messaging.amqp.proton.MessagingFactoryImpl")).asSubclass(
                            MessagingFactory.class);
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
     * Constructs a Connection object with the given URL. <br>
     * This does not establish the underlying physical connection. The
     * application needs to call connect() in order to establish the physical
     * connection to the peer.
     * 
     * @see Connection#connect()
     */
    public static Connection connect(String url)
    {
        return MESSAGING_FACTORY.connect(url);
    }

    /**
     * Constructs a Connection object with the given host and port. <br>
     * This does not establish the underlying physical connection. The
     * application needs to call connect() in order to establish the physical
     * connection to the peer.
     * 
     * @see Connection#connect()
     */
    public static Connection connect(String host, int port)
    {
        return MESSAGING_FACTORY.connect(host, port);
    }

    /**
     * Constructs a Connection object with the given ConnectionSettings.
     * 
     * @see ConnectionSettings This does not establish the underlying physical
     *      connection. The application needs to call connect() in order to
     *      establish the physical connection to the peer.
     * @see Connection#connect()
     */
    public static Connection connect(ConnectionSettings settings)
    {
        return MESSAGING_FACTORY.connect(settings);
    }

    /**
     * Constructs an InboundConnector for accepting inbound connections.
     * 
     * @see InboundConnector
     */
    public static Server server(ConnectionSettings settings)
    {
        return MESSAGING_FACTORY.server(settings);
    }

    /**
     * Constructs an InboundConnector for accepting inbound connections.
     * 
     * @see InboundConnector
     */
    public static Server sever(String host, int port)
    {
        return MESSAGING_FACTORY.server(host, port);
    }
}