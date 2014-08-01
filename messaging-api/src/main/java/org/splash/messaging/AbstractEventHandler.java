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

public class AbstractEventHandler implements EventHandler
{

    @Override
    public void onConnection(Connection con)
    {
    }

    @Override
    public void onConnectionClosed(Connection conn)
    {
    }

    @Override
    public void onNetworkFailed(Connection conn, NetworkException exp)
    {
    }

    @Override
    public void onNetworkReconnected(Connection conn)
    {
    }

    @Override
    public void onSession(Session ssn)
    {
    }

    @Override
    public void onSessionClosed(Session ssn)
    {
    }

    @Override
    public void onOutboundLink(OutboundLink link)
    {
    }

    @Override
    public void onOutboundLinkClosed(OutboundLink link)
    {
    }

    @Override
    public void onOutboundLinkCredit(OutboundLink link, int credits)
    {
    }

    @Override
    public void onClearToSend(OutboundLink link)
    {
    }

    @Override
    public void onDispositionChange(Tracker tracker)
    {
    }

    @Override
    public void onSettled(Tracker tracker)
    {
    }

    @Override
    public void onInboundLink(InboundLink link)
    {
    }

    @Override
    public void onInboundLinkClosed(InboundLink link)
    {
    }

    @Override
    public void onCreditOffered(InboundLink link, int offered)
    {
    }

    @Override
    public void onMessage(InboundLink link, Message msg)
    {
    }
}