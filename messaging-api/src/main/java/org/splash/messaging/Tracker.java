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

import java.util.concurrent.TimeUnit;

/**
 * Provides a handle for tracking outgoing messages.
 */
public interface Tracker extends ContextAware
{
    static final int CUMULATIVE = 0x01;

    /**
     * The current state of the message delivery tracked by this tracker.
     * 
     * @return TrackerState
     */
    public DeliveryState getState();

    /**
     * The current known disposition of the message tracked by this tracker.
     * 
     * @return TrackerState
     */
    public MessageDisposition getDisposition();

    /**
     * Blocks until the message is marked settled or the associated link reaches
     * an erroneous state.
     * 
     * @param flags
     *            : If {@link Tracker#CUMULATIVE} is specified, this will block
     *            until all deliveries up to this point is marked settled.
     * 
     * @throws MessagingException
     *             If the underlying connection failed.
     */
    public void awaitSettlement(int... flags) throws MessagingException;

    /**
     * Blocks for the duration of the timeout and throws a TimeoutException
     * unless the message is marked settled or the associated link reaches an
     * erroneous state, before the time limit expires.
     * 
     * @param timeout
     * @param unit
     * @param flags
     *            : If {@link Tracker#CUMULATIVE} is specified, this will block
     *            until all deliveries up to this point is marked settled.
     * @throws MessagingException
     *             If the underlying connection failed.
     * @throws TimeoutException
     */
    public void awaitSettlement(long timeout, TimeUnit unit, int... flags) throws MessagingException, TimeoutException;

    /**
     * If OutboundLinkMode is {@link OutboundLinkMode#AT_MOST_ONCE}, this will
     * return true as soon as the message is sent. <br>
     * If SenderMode is {@link OutboundLinkMode#AT_LEAST_ONCE}, this will return
     * true when the remote peer settles the delivery.
     */
    public boolean isSettled();
}