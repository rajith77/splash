package org.splash.messaging.management;

import org.splash.messaging.MessagingException;

@SuppressWarnings("serial")
public class ManagementException extends MessagingException
{
    private ResponseCode _code;

    public ManagementException(ResponseCode code, String msg, Throwable t)
    {
        super(msg, t);
        _code = code;
    }

    public ManagementException(ResponseCode code, String msg)
    {
        super(msg);
        _code = code;
    }

    public ResponseCode getCode()
    {
        return _code;
    }
}
