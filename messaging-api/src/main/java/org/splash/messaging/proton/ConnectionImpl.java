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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.splash.messaging.ConnectionSettings;
import org.splash.messaging.EventHandler;
import org.splash.messaging.NetworkException;

class ConnectionImpl extends BaseConnection implements NetworkConnection.ByteReceiver
{
    private NetworkConnection _network;

    ConnectionImpl(ConnectionSettings settings, EventHandler ... handlers)
    {
        _settings = settings;
        _handlers = handlers;
    }

    void bind(NetworkConnection network) throws NetworkException
    {
        _network = network;
    }

    @Override
    void init() throws NetworkException
    {
        super.init();
        _network.start();
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    void write()
    {
        while (_transport.pending() > 0)
        {
            ByteBuffer data = _transport.getOutputBuffer();
            try
            {
                _network.send(data);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Exception writing to socket", e);
            }
            _transport.outputConsumed();
        }
    }

    @Override
    void closeNetworkConnection()
    {
        _network.close();
    }

    @Override
    public void received(ByteBuffer data)
    {
        while (data.hasRemaining())
        {
            ByteBuffer buf = _transport.getInputBuffer();
            int maxAllowed = Math.min(data.remaining(), buf.remaining());
            ByteBuffer temp = data.duplicate();
            temp.limit(data.position() + maxAllowed);
            buf.put(temp);
            _transport.processInput();
            data.position(data.position() + maxAllowed);
        }
        processEvents();
        write();
    }

    @Override
    public void exception(Exception e)
    {
                
    }
}