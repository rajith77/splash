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
 * Provides a handle for tracking outgoing messages.
 */
public interface Tracker
{
    /**
     * The current state of the Tracker.
     */
    public TrackerState getState();

    /**
     * Blocks until the delivery state changes from {@link TrackerState#UNKNOWN}
     * to any of the other states.
     */
    public void awaitSettlement();

    /**
     * If SenderMode is {@link OutboundLinkMode#AT_MOST_ONCE}, this will return true
     * as soon as the message is sent. <br>
     * If SenderMode is {@link OutboundLinkMode#AT_LEAST_ONCE}, this will return true
     * when the remote peer settles the delivery.
     */
    public boolean isSettled();
}