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
 * Represents a logical <i>grouping for a set of inbound or outbound links</i>
 * for exchanging of messages.
 * 
 * <h4>Exceptions</h4>
 * <ul>
 * <li>NetworkException : Thrown when the underlying network connection fails.</li>
 * <li>MessagingException : Thrown when the Session gets to an erroneous state.</li>
 * </ul>
 */
public interface Session
{
    /**
     * Flag for use with {@link Session#accept(Message, int...)},
     * {@link Session#reject(Message, int...)} and
     * {@link Session#release(Message, int...)} methods. When used with the
     * above methods, all messages upto that point will be affected by the given
     * action.
     */
    static final int CUMULATIVE = 0x01;

    /**
     * Establishes a logical <u>Outbound Link</u> with the remote peer for
     * sending messages to the specified destination identified by the address.
     * 
     * @param address
     *            The address is an arbitrary string identifying a logical
     *            "destination" within the remote peer, which is capable of
     *            receiving the messages.
     * @param mode
     *            The OutboundLinkMode specifies the level of reliability
     *            expected by the application when sending messages.
     * @see OutboundLinkMode
     */
    OutboundLink createOutboundLink(String address, OutboundLinkMode mode) throws NetworkException, MessagingException;

    /**
     * Establishes a logical <u>Inbound Link</u> with the remote peer for
     * receiving messages from the specified source identified by the address.
     * 
     * @param address
     *            The address is an arbitrary string identifying a logical
     *            "message source" within the remote peer.
     * @param mode
     *            The InboundLinkMode specifies the level of reliability
     *            expected by the application when receiving messages.
     * 
     *            * @param creditMode The CreditMode specifies how credit is
     *            replenished.
     * @see InboundLinkMode
     */
    InboundLink createInboundLink(String address, InboundLinkMode mode, CreditMode creditMode) throws NetworkException,
            MessagingException;

    /**
     * Establishes a logical Link with the remote peer for receiving messages
     * from the specified address.
     * 
     * @param address
     *            The address is an arbitrary string identifying a logical
     *            "message source" within the remote peer
     * @param mode
     *            The ReceiverMode specifies the level of reliability expected
     *            by the application.
     * @param creditMode
     *            The CreditMode specifies how credit is replenished.
     * @see InboundLinkMode
     * @see CreditMode
     */

    /**
     * Accepts the inbound Session.
     * 
     * @exception UnsupportedOperationException
     *                will be thrown if the connection is not an inbound
     *                Session.
     */
    void accept();

    /**
     * Rejects by closing the session. This is useful if the application is
     * unable to accept any new sessions at this point. It will provide a
     * reason-code if the underlying protocol supports it.
     * 
     * @exception UnsupportedOperationException
     *                will be thrown if the connection is not an inbound
     *                session.
     */
    void reject(ReasonCode code, String desc);

    /**
     * Terminates the Session and free any resources associated with this
     * Session. If there are any active Links, it will close them first before
     * closing the Session.
     */
    void close() throws NetworkException;
}