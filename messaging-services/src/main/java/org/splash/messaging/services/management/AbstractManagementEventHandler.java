package org.splash.messaging.services.management;

import org.splash.messaging.AbstractEventHandler;
import org.splash.messaging.management.Request;
import org.splash.messaging.management.Response;

public class AbstractManagementEventHandler extends AbstractEventHandler implements ManagementEventHandler
{

    @Override
    public void onRequest(Request req)
    {
    }

    @Override
    public void onResponse(Response res)
    {
    }

    @Override
    public void onCreate(Request req)
    {
    }

    @Override
    public void onRead(Request req, ManageableEntity entity)
    {
    }

    @Override
    public void onUpdate(Request req, ManageableEntity entity)
    {
    }

    @Override
    public void onDelete(Request req, ManageableEntity entity)
    {
    }

    @Override
    public void onQuery(Request req)
    {
    }

    @Override
    public void onGetTypes(Request req)
    {
    }

    @Override
    public void onGetAttributes(Request req)
    {
    }

    @Override
    public void onGetOperations(Request req)
    {
    }

    @Override
    public void onGetManagementNodes(Request req)
    {
    }

    @Override
    public void onRegister(Request req)
    {
    }

    @Override
    public void onDeregister(Request req)
    {
    }
}
