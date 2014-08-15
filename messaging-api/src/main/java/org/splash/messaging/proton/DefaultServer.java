package org.splash.messaging.proton;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.splash.logging.Logger;
import org.splash.messaging.ConnectionSettings;
import org.splash.messaging.EventHandler;
import org.splash.messaging.NetworkException;
import org.splash.messaging.Server;
import org.splash.threading.Threading;

public class DefaultServer implements Server, Runnable
{
    private static final Logger _logger = Logger.get(DefaultServer.class);

    private final AtomicBoolean _closed = new AtomicBoolean(false);

    private final EventHandler[] _handlers;

    private final ConnectionSettings _settings;

    private ServerSocket _serverSocket;

    private Thread _acceptThread;

    private NetworkException _shutdownException = null;

    DefaultServer(ConnectionSettings settings, EventHandler... handlers) throws NetworkException
    {
        _settings = settings;
        _handlers = handlers;
        try
        {
            _serverSocket = new ServerSocket();
            _serverSocket.bind(new InetSocketAddress(_settings.getHost(), _settings.getPort()));
            _serverSocket.setReuseAddress(true);
        }
        catch (IOException e)
        {
            throw new NetworkException(String.format("Error setting up socket for %s : %s", _settings.getHost(),
                    _settings.getPort()), e);
        }

        try
        {
            _acceptThread = Threading.getThreadFactory().createThread(this);
        }
        catch (Exception e)
        {
            shutdown();
            throw new NetworkException("Error creating accept thread. Server shutting down", e);
        }
        _acceptThread.start();
    }

    @Override
    public void run()
    {
        try
        {
            while (!_closed.get())
            {
                Socket socket = null;
                try
                {
                    socket = _serverSocket.accept();

                    DefaultConnection connection = new DefaultConnection(_settings, _handlers);
                    NetworkConnection network = new NetworkConnection(_settings, socket, connection);
                    connection.bind(network);
                    for (EventHandler handler : _handlers)
                    {
                        handler.onNetworkConnection(connection, connection);
                    }
                }
                catch (IOException e)
                {
                    _logger.error(e, "IO Error when accepting connection on %s", _serverSocket.getInetAddress());
                    shutdownImpl();
                }
                catch (NetworkException e)
                {
                    _logger.error("Transport error when accepting connection on %s", _serverSocket.getInetAddress());
                    shutdownImpl();
                }
                catch (RuntimeException e)
                {
                    _logger.error("Runtime error when accepting connection on %s", _serverSocket.getInetAddress());
                    shutdownImpl();
                }
            }
        }
        finally
        {
            _logger.warn("Acceptor thread exiting, no new connections will be accepted on address %s",
                    _serverSocket.getInetAddress());
        }
    }

    @Override
    public void shutdown() throws NetworkException
    {
        _shutdownException = null;
        shutdownImpl();
        if (_shutdownException != null)
        {
            throw _shutdownException;
        }
    }

    void shutdownImpl()
    {
        if (!_closed.get())
        {
            _closed.set(true);
            try
            {
                if (!_serverSocket.isClosed())
                {
                    _serverSocket.close();
                }
            }
            catch (IOException e)
            {
                _logger.warn(e, "Error during shutdown for %s", _serverSocket.getInetAddress());
                _shutdownException = new NetworkException("Error during shutdown for " + _serverSocket.getInetAddress());
            }
        }
    }

}