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

public interface EventHandler
{
    void onNetworkConnection(Connection conn, Action action);

    void onNetworkFailed(Connection conn, NetworkException exp);

    void onNetworkReconnected(Connection conn);

    void onConnectionOpen(Connection con);

    void onConnectionClosed(Connection conn);

    void onSession(Session ssn, Action action);

    void onSessionClosed(Session ssn);

    void onOutboundLink(OutboundLink link, Action action);

    void onOutboundLinkClosed(OutboundLink link);

    void onOutboundLinkCredit(OutboundLink link, int credits);

    void onClearToSend(OutboundLink link);

    void onSettled(Tracker tracker);

    void onInboundLink(InboundLink link, Action action);

    void onInboundLinkClosed(InboundLink link);

    void onCreditOffered(InboundLink link, int offered);

    void onMessage(InboundLink link, Message msg);
}