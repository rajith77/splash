package org.splash.messaging;

/**
 * Thrown when the underlying network connection gets to an erroneous state.
 */
@SuppressWarnings("serial")
public class NetworkException extends MessagingException
{
    public NetworkException(String msg)
    {
        super(msg);
    }

    public NetworkException(String msg, Throwable t)
    {
        super(msg, t);
    }
}