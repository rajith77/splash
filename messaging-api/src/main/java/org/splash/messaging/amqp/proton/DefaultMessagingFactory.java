package org.splash.messaging.amqp.proton;

import org.splash.messaging.Connection;
import org.splash.messaging.ConnectionSettings;
import org.splash.messaging.EventHandler;
import org.splash.messaging.Message;
import org.splash.messaging.MessagingException;
import org.splash.messaging.MessagingFactory;
import org.splash.messaging.NetworkException;
import org.splash.messaging.Server;
import org.splash.util.URLParser;

public class DefaultMessagingFactory implements MessagingFactory
{
    @Override
    public Message message()
    {
        return new MessageImpl();
    }

    @Override
    public Connection connect(String url, EventHandler... handlers) throws MessagingException, NetworkException
    {
        return connect(URLParser.parse(url), handlers);
    }

    @Override
    public Connection connect(String host, int port, EventHandler... handlers) throws MessagingException,
            NetworkException
    {
        ConnectionSettings settings = new ConnectionSettings();
        settings.setHost(host);
        settings.setPort(port);
        return connect(settings, handlers);
    }

    @Override
    public Connection connect(ConnectionSettings settings, EventHandler... handlers) throws MessagingException,
            NetworkException
    {
        DefaultConnection connection = new DefaultConnection(settings, handlers);
        NetworkConnection network = new NetworkConnection(settings, connection);
        connection.bind(network);
        connection.init();
        return connection;
    }

    @Override
    public Server listen(ConnectionSettings settings, EventHandler... handlers) throws MessagingException,
            NetworkException
    {
        DefaultServer server = new DefaultServer(settings, handlers);
        return server;
    }

    @Override
    public Server listen(String host, int port, EventHandler... handlers) throws MessagingException, NetworkException
    {
        ConnectionSettings settings = new ConnectionSettings();
        settings.setHost(host);
        settings.setPort(port);
        return listen(settings, handlers);
    }

}