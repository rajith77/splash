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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.qpid.proton.engine.Link;
import org.splash.messaging.MessagingException;
import org.splash.messaging.NetworkException;

abstract class BaseLink extends ContextAwareImpl
{
    String _address;

    Link _link;

    SessionImpl _ssn;

    AtomicBoolean _closed = new AtomicBoolean(false);

    boolean _dynamic = false;

    BaseLink(SessionImpl ssn, String address, Link link)
    {
        _address = address;
        _link = link;
        _ssn = ssn;
    }

    public String getAddress()
    {
        return _address;
    }

    abstract void init() throws NetworkException;

    void checkClosed() throws MessagingException
    {
        if (_closed.get())
        {
            throw new MessagingException("Link is closed");
        }
    }

    public void close() throws NetworkException
    {
        closeImpl();
        _ssn.removeLink(_link);
        _ssn.getConnection().write();
    }

    void closeImpl()
    {
        _closed.set(true);
        _link.close();
    }

    void setDynamicAddress(boolean dynamic)
    {
        _dynamic = dynamic;
    }
    
    SessionImpl getSession()
    {
        return _ssn;
    }
}