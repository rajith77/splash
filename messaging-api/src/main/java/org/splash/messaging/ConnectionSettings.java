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
 * Applications could extend this class to provide their own implementation of
 * the ConnectionSettings based on their configuration.
 */
public abstract class ConnectionSettings
{
    protected String _scheme = "amqp";

    protected String _host = "localhost";

    protected int _port = 5672;

    protected String _user = "";

    protected String _pass = "";

    protected boolean _tcpNodelay = false;

    protected int _readBufferSize = 65535;

    protected int _writeBufferSize = 65535;

    protected long _connectTimeout = Long.getLong("splash.connection.timeout", 60000);

    protected long _idleTimeout = Long.getLong("splash.connection.idle_timeout", 60000);

    public String getHost()
    {
        return _host;
    }

    public int getPort()
    {
        return _port;
    }

    public boolean isTcpNodelay()
    {
        return _tcpNodelay;
    }

    public int getReadBufferSize()
    {
        return _readBufferSize;
    }

    public int getWriteBufferSize()
    {
        return _writeBufferSize;
    }

    public long getConnectTimeout()
    {
        return _connectTimeout;
    }

    public long getIdleTimeout()
    {
        return _idleTimeout;
    }
}