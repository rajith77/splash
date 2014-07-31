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
 * Represents a logical <i>link</i> where messages travel <u>outwards</u> from
 * the containers perspective.
 * 
 * <h4>Tracking message delivery</h4> If the OutboundLinkMode is
 * {@link #AT_LEAST_ONCE} the application has two options for tracking messages. <br>
 * 
 * <ul>
 * <li>Synchronously track messages by using {@link Tracker#awaitSettlement()}
 * 
 * <pre>
 * Ex:  
 * {@code link.send(msg).awaitSettlement();} or <br>
 * {@code         
 * Tracker tracker = link.send(msg);
 * .....
 * tracker.awaitSettlement();
 * }
 * </pre>
 * 
 * </li>
 * <li>Receive completions asynchronously via the
 * {@link EventHandler#onSettled(Tracker) interface}</li>
 * </ul>
 * 
 * <h4>Exceptions</h4>
 * <ul>
 * <li>NetworkException : Thrown when the underlying network connection fails.
 * </li>
 * <li>MessagingException : Thrown when the link gets to an erroneous state.</li>
 * </ul>
 */
public interface OutboundLink
{
    /**
     * The address used for establishing the Link
     */
    String getAddress();

    /**
     * Provides a hint to the peer about the availability of messages.
     */
    void offerCredits(int credits) throws MessagingException, NetworkException;

    /**
     * Outstanding message deliveries that the peer has not yet confirmed as
     * settled.
     */
    int getUnsettled() throws MessagingException, NetworkException;

    /**
     * 
     * @param msg
     *            {@link Message} to be sent.
     * 
     * @return A {@link Tracker} object that can be used to track the status of
     *         the message delivery.
     * 
     * @throws MessageFormatException
     *             A MessageFormatException will be thrown if the message is
     *             invalid or malformed.
     * 
     * @throws MessagingException
     *             A MessagingException will be thrown if the link gets to an
     *             erroneous state.
     * 
     * @throws NetworkException
     *             A TransportException will be thrown if the underlying network
     *             connection fails during the send.
     */
    Tracker send(Message msg) throws MessageFormatException, MessagingException, NetworkException;

    /**
     * Close the Link and free any resources associated with it.
     */
    void close() throws NetworkException;
}