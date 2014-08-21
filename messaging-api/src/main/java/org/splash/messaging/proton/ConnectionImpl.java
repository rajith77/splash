package org.splash.messaging.proton;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.splash.messaging.ConnectionSettings;
import org.splash.messaging.EventHandler;
import org.splash.messaging.NetworkException;

class ConnectionImpl extends BaseConnection implements NetworkConnection.ByteReceiver
{
    private NetworkConnection _network;

    private ConnectionSettings _settings;

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