package org.splash.messaging.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.splash.logging.Logger;
import org.splash.messaging.InboundLink;
import org.splash.messaging.Message;
import org.splash.messaging.OutboundLink;
import org.splash.messaging.management.AbstractManagementEventHandler;
import org.splash.messaging.management.ManageableEntity;
import org.splash.messaging.management.ManageableEntityType;
import org.splash.messaging.management.ManagementEventHandler;
import org.splash.messaging.management.ManagementException;
import org.splash.messaging.management.ManagementMessageFactory;
import org.splash.messaging.management.ManagementNode;
import org.splash.messaging.management.ManagementPropertyNames;
import org.splash.messaging.management.Request;
import org.splash.messaging.management.Response;
import org.splash.messaging.management.ResponseCode;

public class DefaultManagementNode extends AbstractManagementEventHandler implements ManagementNode
{
    private static final Logger _logger = Logger.get(DefaultManagementNode.class);

    private InboundLink _inLink;

    private OutboundLink _outLink;

    private ManagementMessageFactory _msgFactory;

    private ManagementEventHandler _handlers[];

    private final Map<String, ManageableEntity> _byId = new ConcurrentHashMap<String, ManageableEntity>();

    private final Map<String, ManageableEntity> _byName = new ConcurrentHashMap<String, ManageableEntity>();

    private final Map<String, ManageableEntityTypeEntry> _types = new ConcurrentHashMap<String, ManageableEntityTypeEntry>();

    DefaultManagementNode(InboundLink inLink, OutboundLink outLink, ManagementEventHandler... handlers)
            throws MessagingServiceException
    {
        _inLink = inLink;
        _outLink = outLink;
        _msgFactory = ManagementMessageFactory.Factory.create();
        _handlers = handlers == null ? new ManagementEventHandler[0] : handlers;
    }

    @Override
    public String getAddress()
    {
        return _inLink.getAddress();
    }

    @Override
    public void registerEntity(ManageableEntity entity)
    {
        _byId.put(entity.getID(), entity);
        _byName.put(entity.getName(), entity);
        registerTypes(entity);
    }

    @Override
    public void deRegisterEntity(ManageableEntity entity)
    {
        _byId.remove(entity.getID());
        _byName.remove(entity.getName());
        deregisterTypes(entity);
    }

    @Override
    public void onMessage(InboundLink link, Message msg)
    {
        // This message is intended for this agent.
        if (link.equals(_inLink))
        {
            if (msg.getApplicationProperties().containsKey(ManagementPropertyNames.STATUS_CODE))
            {
                Response res = _msgFactory.parseResponse(msg);
                onResponse(res);
            }
            else
            {
                Request req = _msgFactory.parseRequest(msg);
                onRequest(req);
            }
        }
    }

    @Override
    public void onRequest(Request req)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onRequest(req);
        }
        switch (req.getOpCode())
        {
        case CREATE:
            onCreate(req);
            break;
        case READ:
            onRead(req, findEntity(req));
            break;
        case UPDATE:
            onUpdate(req, findEntity(req));
            break;
        case DELETE:
            onDelete(req, findEntity(req));
            break;
        case QUERY:
            onQuery(req);
            break;
        case GET_TYPES:
            onGetTypes(req);
            break;
        case GET_ATTRIBUTES:
            onGetAttributes(req);
            break;
        case GET_OPERATIONS:
            onGetOperations(req);
            break;
        case GET_MGMT_NODES:
            onGetManagementNodes(req);
            break;
        case REGISTER:
            onRegister(req);
            break;
        case DEREGISTER:
            onDeregister(req);
            break;
        default:
            onUnknownOp(req);
            break;
        }
    }

    @Override
    public void onResponse(Response res)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onResponse(res);
        }
    }

    ManageableEntity findEntity(Request req)
    {
        ManageableEntity entity = null;
        if (req.getId() != null)
        {
            entity = _byId.get(req.getId());
        }
        else if (req.getName() != null)
        {
            entity = _byName.get(req.getName());
        }

        if (entity == null)
        {
            Message msg = _msgFactory.response(req, ResponseCode.NOT_FOUND, null);
            send(msg);
        }
        return entity;
    }

    @Override
    public void onCreate(Request req)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onCreate(req);
        }
    }

    @Override
    public void onRead(Request req, ManageableEntity entity)
    {
        if (entity != null)
        {
            try
            {
                Message msg = _msgFactory.response(req, ResponseCode.OK, entity.read());
                send(msg);
            }
            catch (ManagementException e)
            {
                Message msg = _msgFactory.response(req, e.getCode(), null);
                send(msg);
            }
            for (ManagementEventHandler handler : _handlers)
            {
                handler.onRead(req, entity);
            }
        }
    }

    @Override
    public void onUpdate(Request req, ManageableEntity entity)
    {
        if (entity != null)
        {
            try
            {
                Message msg = _msgFactory.response(req, ResponseCode.OK, entity.update(req.getBody()));
                send(msg);
            }
            catch (ManagementException e)
            {
                Message msg = _msgFactory.response(req, e.getCode(), null);
                send(msg);
            }
            for (ManagementEventHandler handler : _handlers)
            {
                handler.onUpdate(req, entity);
            }
        }
    }

    @Override
    public void onDelete(Request req, ManageableEntity entity)
    {
        if (entity != null)
        {
            try
            {
                entity.delete();
                Message msg = _msgFactory.response(req, ResponseCode.OK, null);
                send(msg);
            }
            catch (ManagementException e)
            {
                Message msg = _msgFactory.response(req, e.getCode(), null);
                send(msg);
            }
            for (ManagementEventHandler handler : _handlers)
            {
                handler.onDelete(req, entity);
            }
        }
    }

    @Override
    public void onQuery(Request req)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onQuery(req);
        }
    }

    @Override
    public void onGetTypes(Request req)
    {
        Map<String, List<String>> results = new HashMap<String, List<String>>();
        if (req.getAppProps().containsKey(ManagementPropertyNames.ENTITY_TYPE))
        {
            String givenTypeName = (String) req.getAppProps().get(ManagementPropertyNames.ENTITY_TYPE);
            if (_types.containsKey(givenTypeName))
            {
                Class<? extends ManageableEntityType> givenClass = _types.get(givenTypeName)._type;
                List<String> superTypes = new ArrayList<String>();
                results.put(givenClass.getName(), superTypes);
                for (String typeName : _types.keySet())
                {
                    ManageableEntityTypeEntry e = _types.get(typeName);
                    if (e._type != givenClass && e._type.isAssignableFrom(givenClass))
                    {
                        superTypes.add(e._type.getName());
                    }

                }
            }
        }
        else
        {
            for (String typeName : _types.keySet())
            {
                Class<? extends ManageableEntityType> givenClass = _types.get(typeName)._type;
                List<String> superTypes = new ArrayList<String>();
                results.put(typeName, superTypes);
                for (String tName : _types.keySet())
                {
                    ManageableEntityTypeEntry e = _types.get(tName);
                    if (e._type != givenClass && e._type.isAssignableFrom(givenClass))
                    {
                        superTypes.add(e._type.getName());
                    }

                }
            }
        }
        Message msg = _msgFactory.response(req, ResponseCode.OK, results);
        send(msg);

        for (ManagementEventHandler handler : _handlers)
        {
            handler.onGetTypes(req);
        }
    }

    @Override
    public void onGetAttributes(Request req)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onGetAttributes(req);
        }
    }

    @Override
    public void onGetOperations(Request req)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onGetOperations(req);
        }
    }

    @Override
    public void onGetManagementNodes(Request req)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onGetManagementNodes(req);
        }
    }

    @Override
    public void onRegister(Request req)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onRegister(req);
        }
    }

    @Override
    public void onDeregister(Request req)
    {
        for (ManagementEventHandler handler : _handlers)
        {
            handler.onDeregister(req);
        }
    }

    void onUnknownOp(Request req)
    {
        Message msg = _msgFactory.response(req, ResponseCode.NOT_IMPLEMENTED, null);
        send(msg);
    }

    void send(Message msg)
    {
        try
        {
            _outLink.send(msg);
        }
        catch (Exception e)
        {
            _logger.warn(e, "Exception when trying to send response messages");
        }
    }

    void registerTypes(ManageableEntity entity)
    {
        Class[] interfaces = entity.getType().getClass().getInterfaces();
        for (Class interfaze : interfaces)
        {
            if (ManageableEntityType.class.isAssignableFrom(interfaze))
                ;
            {
                if (_types.containsKey(interfaze.getName()))
                {
                    ManageableEntityTypeEntry entry = _types.get(interfaze.getName());
                    entry.addEnitity(entity);
                }
                else
                {
                    ManageableEntityTypeEntry entry = new ManageableEntityTypeEntry(interfaze);
                    entry.addEnitity(entity);
                    _types.put(interfaze.getName(), entry);
                }

            }
        }
    }

    void deregisterTypes(ManageableEntity entity)
    {
        Class[] interfaces = entity.getType().getClass().getInterfaces();
        for (Class interfaze : interfaces)
        {
            if (ManageableEntityType.class.isAssignableFrom(interfaze))
                ;
            {
                if (_types.containsKey(interfaze.getName()))
                {
                    ManageableEntityTypeEntry entry = _types.get(interfaze.getName());
                    entry.removeEnitity(entity);
                    if (entry.size() == 0)
                    {
                        _types.remove(interfaze.getName());
                    }
                }
            }
        }
    }

    class ManageableEntityTypeEntry
    {
        final Class<? extends ManageableEntityType> _type;

        final List<ManageableEntity> _entities;

        ManageableEntityTypeEntry(Class<? extends ManageableEntityType> type)
        {
            _type = type;
            _entities = new ArrayList<ManageableEntity>();
        }

        void addEnitity(ManageableEntity entity)
        {
            _entities.add(entity);
        }

        void removeEnitity(ManageableEntity entity)
        {
            _entities.remove(entity);
        }

        int size()
        {
            return _entities.size();
        }
    }
}