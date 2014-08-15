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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.qpid.proton.engine.Collector;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.Transport;
import org.splash.messaging.Action;
import org.splash.messaging.ConnectionSettings;
import org.splash.messaging.EventHandler;
import org.splash.messaging.MessagingException;
import org.splash.messaging.NetworkException;
import org.splash.messaging.ReasonCode;
import org.splash.messaging.TimeoutException;

abstract class BaseConnection extends ContextAwareImpl implements org.splash.messaging.Connection, Action
{
    Collector _collector;

    Transport _transport;

    Connection _connection;

    EventHandler[] _handlers;

    ConnectionSettings _settings;

    AtomicBoolean _closed = new AtomicBoolean(false);

    AtomicBoolean _connected = new AtomicBoolean(false);

    final Object _lock = new Object();

    final Map<Session, SessionImpl> _sessions = new HashMap<Session, SessionImpl>();

    void init() throws NetworkException
    {
        synchronized (_lock)
        {
            _collector = Collector.Factory.create();
            _transport = Transport.Factory.create();
            _connection = Connection.Factory.create();
            String id = _settings.getId();
            _connection.collect(_collector);
            _connection.setContainer(id == null || id.trim().equals("") ? UUID.randomUUID().toString() : id);
            _connection.setHostname(_settings.getHost());
            _transport.bind(_connection);
            Sasl sasl = _transport.sasl();
            sasl.client();
            sasl.setMechanisms(new String[] { "ANONYMOUS" });
            _connection.open();
            _connected.set(true);
            write();
        }
    }

    void processEvents()
    {
        while (true)
        {
            Event event = _collector.peek();
            if (event == null)
                break;
            Events.dispatchProtonEvents(event, _handlers);
            _collector.pop();
        }
    }
    
    @Override
    public String getLocalID()
    {
        return _settings != null ? _settings.getId() : null;
    }

    @Override
    public String getRemoteID()
    {
        return _connection != null ? _connection.getRemoteContainer() : null;
    }

    @Override
    public String getRemoteHostname()
    {
        return _connection != null ? _connection.getRemoteHostname() : null;
    }

    @Override
    public org.splash.messaging.Session createSession() throws NetworkException, MessagingException, TimeoutException
    {
        synchronized (_lock)
        {
            if (_closed.get())
            {
                throw new MessagingException("Connection is closed");
            }
            Session ssn = _connection.session();
            SessionImpl session = new SessionImpl(this, ssn);
            _sessions.put(ssn, session);
            session.init();
            return session;
        }
    }

    @Override
    public void accept() throws NetworkException
    {
        synchronized (_lock)
        {
            init();
        }
    }

    @Override
    public void reject(ReasonCode code, String desc, String alternateAddress)
    {
        synchronized (_lock)
        {
            // TODO;
        }
    }

    @Override
    public void close() throws NetworkException, MessagingException, TimeoutException
    {
        synchronized (_lock)
        {
            if (!_closed.get())
            {
                _closed.set(true);
                if (_connected.get())
                {
                    for (Session ssn : _sessions.keySet())
                    {
                        _sessions.get(ssn).closeImpl();
                    }
                    _sessions.clear();
                    _connection.close();
                    write();
                    closeNetworkConnection();
                }
            }
        }
    }

    void removeSession(Session ssn)
    {
        synchronized (_lock)
        {
            _sessions.remove(ssn);
        }
    }
    
    abstract void write() throws NetworkException;

    abstract void closeNetworkConnection();
}