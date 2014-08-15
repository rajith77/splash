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
package org.splash.messaging.proton;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.Message;
import org.splash.messaging.Connection;
import org.splash.messaging.EventHandler;
import org.splash.messaging.InboundLink;
import org.splash.messaging.OutboundLink;

public final class Events
{
    private Events()
    {
    }

    public static void dispatchProtonEvents(Event event, EventHandler[] handlers)
    {
        if (handlers.length < 1)
            return;
        switch (event.getType())
        {
        case CONNECTION_REMOTE_OPEN:
            Connection con = (Connection) event.getConnection().getContext();
            for (EventHandler handler : handlers)
            {
                handler.onConnectionOpen(con);
            }
            break;
        case CONNECTION_FINAL:
            con = (Connection) event.getConnection().getContext();
            for (EventHandler handler : handlers)
            {
                handler.onConnectionClosed(con);
            }
            break;
        case SESSION_REMOTE_OPEN:
            SessionImpl ssn = (SessionImpl) event.getSession().getContext();
            for (EventHandler handler : handlers)
            {
                handler.onSession(ssn, ssn);
            }
            break;
        case SESSION_FINAL:
            ssn = (SessionImpl) event.getSession().getContext();
            for (EventHandler handler : handlers)
            {
                handler.onSessionClosed(ssn);
            }
            break;
        case LINK_REMOTE_OPEN:
            Link link = event.getLink();
            if (link instanceof Receiver)
            {
                InboundLinkImpl inboundLink = (InboundLinkImpl) link.getContext();
                for (EventHandler handler : handlers)
                {
                    handler.onInboundLink(inboundLink, inboundLink);
                }
            }
            else
            {
                OutboundLinkImpl outboundLink = (OutboundLinkImpl) link.getContext();
                for (EventHandler handler : handlers)
                {
                    handler.onOutboundLink(outboundLink, outboundLink);
                }
            }
            break;
        case LINK_FLOW:
            link = event.getLink();
            if (link instanceof Sender)
            {
                OutboundLink outboundLink = (OutboundLink) link.getContext();
                for (EventHandler handler : handlers)
                {
                    handler.onOutboundLinkCredit(outboundLink, link.getCredit());
                }
            }
            break;
        case LINK_FINAL:
            link = event.getLink();
            if (link instanceof Receiver)
            {
                InboundLink inboundLink = (InboundLink) link.getContext();
                for (EventHandler handler : handlers)
                {
                    handler.onInboundLinkClosed(inboundLink);
                }
            }
            else
            {
                OutboundLink outboundLink = (OutboundLink) link.getContext();
                for (EventHandler handler : handlers)
                {
                    handler.onOutboundLinkClosed(outboundLink);
                }
            }
            break;
        case TRANSPORT:
            // TODO
            break;
        case DELIVERY:
            onDelivery(event.getDelivery(), handlers);
            break;
        default:
            break;
        }
    }

    static void onDelivery(Delivery d, EventHandler[] handlers)
    {
        Link link = d.getLink();
        if (link instanceof Receiver)
        {
            if (d.isPartial())
            {
                return;
            }

            Receiver receiver = (Receiver) link;
            byte[] bytes = new byte[d.pending()];
            int read = receiver.recv(bytes, 0, bytes.length);
            Message pMsg = Proton.message();
            pMsg.decode(bytes, 0, read);

            InboundLinkImpl inLink = (InboundLinkImpl) link.getContext();
            SessionImpl ssn = inLink.getSession();
            MessageImpl msg = new InboundMessage(ssn.getID(), d.getTag(), ssn.getNextIncommingSequence(), pMsg);
            for (EventHandler handler : handlers)
            {
                handler.onMessage(inLink, msg);
            }
        }
        else
        {
            if (d.remotelySettled())
            {
                TrackerImpl tracker = (TrackerImpl) d.getContext();
                tracker.setDisposition(d.getRemoteState());
                tracker.markSettled();
                for (EventHandler handler : handlers)
                {
                    handler.onSettled(tracker);
                }
            }
        }
    }
}