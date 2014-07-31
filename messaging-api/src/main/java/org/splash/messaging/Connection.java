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
package org.splash.messaging;

/**
 * 
 * Represents a logical <i>Connection</i> to a remote peer within a messaging
 * network.
 * 
 * <h4>Exceptions</h4>
 * <ul>
 * <li>NetworkException : Thrown when the underlying network connection fails.</li>
 * <li>MessagingException  : Thrown when the Connection gets to an erroneous state.</li>
 * <li>TimeoutException   : Thrown when an operation exceeds the connection timeout.</li>
 * </ul>
 * 
 * Connection timeout defaults to 60 secs. This value can be changed via the
 * <i>"splash.connection.timeout"</i> system property, or by providing an
 * application specific ConnectionSettings implementation when creating the
 * Connection object.
 * 
 * @see ConnectionSettings
 */
public interface Connection
{
    /**
     * An ID set by the application to identify the connection.
     */
    void setLocalID(String id);

    /**
     * Retrieve the local identifier set by the application
     */
    String getLocalID();

    /**
     * Return the ID assigned by the remote peer if exposed by the underlying
     * protocol.
     */
    String getRemoteID();

    /**
     * Return the ID assigned by the remote peer if exposed by the underlying
     * protocol or the network layer.
     */
    String getRemoteHostname();

    /**
     * Establishes a logical Session for exchanging of messages.
     */
    Session createSession() throws NetworkException, MessagingException, TimeoutException;

    /**
     * Accepts the inbound connection.
     * 
     * @exception UnsupportedOperationException
     *                will be thrown if the connection is not an inbound
     *                connection.
     */
    void accept();

    /**
     * Rejects by closing the connection. This is useful if the application is
     * unable to accept any new connections at this point. It will provide a
     * reason-code if the underlying protocol supports it.
     * 
     * Optionally, it could provide an alternate address to connect to.
     * 
     * @exception UnsupportedOperationException
     *                will be thrown if the connection is not an inbound
     *                connection.
     */
    void reject(ReasonCode code, String desc, String alternateAddress);

    /**
     * Terminates the Connection and free any resources associated with this
     * Connection. If there are any active sessions, it will close them first
     * before closing the Connection.
     */
    void close() throws NetworkException, MessagingException, TimeoutException;
}