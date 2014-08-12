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
package org.splash.messaging.amqp.proton;

import java.nio.ByteBuffer;

import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import org.splash.messaging.Action;
import org.splash.messaging.Message;
import org.splash.messaging.MessageFormatException;
import org.splash.messaging.MessagingException;
import org.splash.messaging.NetworkException;
import org.splash.messaging.OutboundLink;
import org.splash.messaging.ReasonCode;
import org.splash.messaging.Tracker;

class OutboundLinkImpl extends BaseLink implements Action, OutboundLink
{

    OutboundLinkImpl(SessionImpl ssn, String address, Link link)
    {
        super(ssn, address, link);
    }

    @Override
    public void offerCredits(int credits) throws MessagingException, NetworkException
    {
        ((Sender) _link).offer(credits);
        _ssn.getConnection().write();
    }

    @Override
    void init() throws NetworkException
    {
        _link.open();
        _ssn.getConnection().write();
    }

    @Override
    public int getUnsettled() throws MessagingException
    {
        checkClosed();
        return _link.getUnsettled();
    }

    @Override
    public Tracker send(Message msg) throws MessageFormatException, MessagingException, NetworkException
    {
        checkClosed();
        if (msg instanceof MessageImpl)
        {
            Sender sender = (Sender) _link;
            byte[] tag = longToBytes(_ssn.getNextDeliveryTag());
            Delivery delivery = sender.delivery(tag);
            TrackerImpl tracker = new TrackerImpl(_ssn);
            delivery.setContext(tracker);
            if (sender.getSenderSettleMode() == SenderSettleMode.SETTLED)
            {
                delivery.settle();
                tracker.markSettled();
            }

            org.apache.qpid.proton.message.Message m = ((MessageImpl) msg).getProtocolMessage();
            if (m.getAddress() == null)
            {
                m.setAddress(_address);
            }
            byte[] buffer = new byte[1024];
            int encoded = m.encode(buffer, 0, buffer.length);
            sender.send(buffer, 0, encoded);
            sender.advance();
            _ssn.getConnection().write();
            return tracker;
        }
        else
        {
            throw new MessageFormatException("Unsupported message implementation");
        }
    }

    @Override
    public void accept() throws NetworkException
    {
        init();
    }

    @Override
    public void reject(ReasonCode code, String desc, String alternateAddress) throws NetworkException
    {
        // TODO Auto-generated method stub
    }

    private static byte[] longToBytes(final long value)
    {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(value);
        return buffer.array();
    }
}