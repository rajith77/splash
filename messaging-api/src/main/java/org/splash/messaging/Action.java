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
 * Provides a uniform interface for acting on incoming requests from a peer. Ex
 * A request for a new Connection, Session or a Link
 */
public interface Action
{
    /**
     * Accepts the inbound Connection, Session or Link represented by this
     * action.
     */
    void accept() throws NetworkException;

    /**
     * Rejects by closing the inbound Connection, Session or Link represented by
     * this action. This is useful if the application is unable to accept any
     * new requests at this point. It will provide a reason-code if the
     * underlying protocol supports it.
     * 
     * Optionally, it could provide an alternate address to connect to.
     * 
     */
    void reject(ReasonCode code, String desc, String alternateAddress) throws NetworkException;

}