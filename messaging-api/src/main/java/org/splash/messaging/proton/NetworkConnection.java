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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.splash.messaging.ConnectionSettings;

public class NetworkConnection implements Runnable
{
    private Socket _socket;

    private int _bufferSize = 65535;

    private Exception _exception;

    private final WritableByteChannel _out;

    private final Thread _receiverThread;

    private final ByteReceiver _receiver;

    private final AtomicBoolean _closed = new AtomicBoolean(false);

    private final ConnectionSettings _settings;

    public NetworkConnection(ConnectionSettings settings, ByteReceiver receiver)
    {
        this(settings, new Socket(), receiver);
    }

    public NetworkConnection(ConnectionSettings settings, Socket socket, ByteReceiver receiver)
    {
        _socket = socket;
        _settings = settings;
        if (!_socket.isConnected())
        {
            try
            {
                InetAddress address = InetAddress.getByName(_settings.getHost());
                _socket.connect(new InetSocketAddress(address, _settings.getPort()), 60 * 1000);
            }
            catch (UnknownHostException e)
            {
                throw new RuntimeException("Error connecting to given host", e);
            }
            catch (IOException e)
            {
                throw new RuntimeException("IO error when connecting to peer", e);
            }
        }
        try
        {
            _socket.setReuseAddress(true);
            _socket.setTcpNoDelay(true);
            _socket.setSendBufferSize(_bufferSize);
            _socket.setReceiveBufferSize(_bufferSize);
            _out = Channels.newChannel(_socket.getOutputStream());
        }
        catch (SocketException e)
        {
            throw new RuntimeException("Error setting socket parameters", e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("IO error when connecting to peer", e);
        }

        _receiver = receiver;
        _receiverThread = new Thread(this);
        _receiverThread.start();
    }

    public void send(ByteBuffer buf) throws IOException
    {
        if (_closed.get())
        {
            throw new RuntimeException("Connection is closed");
        }
        _out.write(buf);
    }

    public void run()
    {
        final int threshold = _bufferSize / 2;

        // I set the read buffer size similar to SO_RCVBUF
        // Haven't tested with a lower value to see if it's better or worse
        byte[] buffer = new byte[_bufferSize];
        try
        {
            InputStream in = _socket.getInputStream();
            int read = 0;
            int offset = 0;
            while (read != -1)
            {
                try
                {
                    while ((read = in.read(buffer, offset, _bufferSize - offset)) != -1)
                    {
                        if (read > 0)
                        {
                            ByteBuffer b = ByteBuffer.wrap(buffer, offset, read);
                            _receiver.received(b);
                            offset += read;
                            if (offset > threshold)
                            {
                                offset = 0;
                                buffer = new byte[_bufferSize];
                            }
                        }
                    }
                }
                catch (SocketTimeoutException e)
                {
                    // TODO
                }
            }
        }
        catch (Exception e)
        {
            _exception = e;
            if (!_closed.get())
            {
                _closed.set(true);
                _receiver.exception(e);
            }
        }
        finally
        {
            try
            {
                _socket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error closing socket");
            }
        }
    }

    public void close()
    {
        _closed.set(true);
        try
        {
            _socket.shutdownInput();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public interface ByteReceiver
    {
        void received(ByteBuffer buf);

        void exception(Exception e);
    }
}
