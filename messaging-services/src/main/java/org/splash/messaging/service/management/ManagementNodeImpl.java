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
package org.splash.messaging.service.management;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.splash.logging.Logger;
import org.splash.messaging.InboundLink;
import org.splash.messaging.Message;
import org.splash.messaging.MessagingException;
import org.splash.messaging.OutboundLink;
import org.splash.messaging.management.ManagementException;
import org.splash.messaging.management.ManagementMessageFactory;
import org.splash.messaging.management.ManagementPropertyNames;
import org.splash.messaging.management.Request;
import org.splash.messaging.management.Response;
import org.splash.messaging.management.ResponseCode;
import org.splash.messaging.service.MessagingServiceException;

public class ManagementNodeImpl extends AbstractManagementEventHandler implements ManagementNode
{
    protected static final Logger _logger = Logger.get(ManagementNodeImpl.class);

    protected static final List<String> STD_OPS = new ArrayList<String>();

    static
    {
        STD_OPS.add("CREATE");
        STD_OPS.add("READ");
        STD_OPS.add("UPDATE");
        STD_OPS.add("DELETE");
    }

    protected static final List<String> STD_ATTR = new ArrayList<String>();

    static
    {
        STD_ATTR.add(ManagementPropertyNames.IDENTITY);
        STD_ATTR.add(ManagementPropertyNames.NAME);
        STD_ATTR.add(ManagementPropertyNames.TYPE);
    }

    protected InboundLink _inLink;

    protected OutboundLink _outLink;

    protected ManagementMessageFactory _msgFactory;

    protected ManagementEventHandler _mgtEventHandlers[];

    protected ManageableEntityLifecycleHandler _lifeCycleHandler;

    protected final Map<String, ManageableEntity> _byId = new ConcurrentHashMap<String, ManageableEntity>();

    protected final Map<String, ManageableEntity> _byName = new ConcurrentHashMap<String, ManageableEntity>();

    protected final Map<String, TypeRegistryEntry> _typeRegistry = new ConcurrentHashMap<String, TypeRegistryEntry>();

    // Keeps a list of entities for a given type
    protected final Map<String, TypeToEntityEntry> _typeToEntity = new ConcurrentHashMap<String, TypeToEntityEntry>();

    public ManagementNodeImpl()
    {
    }

    @Override
    public void init(InboundLink inLink, OutboundLink outLink, ManageableEntityLifecycleHandler lifeCycleHandler,
            ManagementEventHandler... handlers) throws MessagingServiceException
    {
        _inLink = inLink;
        _outLink = outLink;
        try
        {
            _msgFactory = ManagementMessageFactory.Factory.create();
        }
        catch (MessagingException e)
        {
            throw new MessagingServiceException("Error loading ManagementMessageFactory", e);
        }
        _mgtEventHandlers = handlers == null ? new ManagementEventHandler[0] : handlers;
        _lifeCycleHandler = lifeCycleHandler;
    }

    @Override
    public String getAddress()
    {
        return _inLink.getAddress();
    }

    @Override
    public void onMessage(InboundLink link, Message msg)
    {
        // This message is intended for this agent.
        if (link.equals(_inLink))
        {
            if (msg.getApplicationProperties().containsKey(ManagementPropertyNames.STATUS_CODE))
            {
                Response res = null;
                try
                {
                    res = _msgFactory.parseResponse(msg);
                }
                catch (ManagementException e)
                {
                    _logger.error(e, "Error parsing management response msg %s", msg);
                    return;
                }
                onResponse(res);
            }
            else
            {
                Request req = null;
                try
                {
                    req = _msgFactory.parseRequest(msg);
                }
                catch (ManagementException e)
                {
                    _logger.error(e, "Error parsing management request msg %s", msg);
                    Message res = _msgFactory.response(req, e.getCode(), e.getMessage(), null);
                    send(res);
                    return;
                }
                onRequest(req);
            }
        }
    }

    @Override
    public void onRequest(Request req)
    {
        _logger.debug("Received management request %s", req);

        for (ManagementEventHandler handler : _mgtEventHandlers)
        {
            handler.onRequest(req);
        }
        switch (req.getOperation())
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
        for (ManagementEventHandler handler : _mgtEventHandlers)
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
        String type = req.getType();

        if (_typeRegistry.containsKey(type))
        {
            try
            {
                String id = UUID.randomUUID().toString();
                String name = (String) req.getAppProps().get(ManagementPropertyNames.NAME);
                TypeRegistryEntry entry = _typeRegistry.get(type);
                ManageableEntity entity = null;
                try
                {
                    entity = entry.factory().create(id, name, req.getBody());
                }
                catch (IllegalArgumentException e)
                {
                    Message msg = _msgFactory.response(req, ResponseCode.BAD_REQUEST, e.getMessage(), null);
                    send(msg);
                    return;
                }
                _byId.put(entity.getID(), entity);
                _byName.put(entity.getName(), entity);
                addEntityToTypeRegistry(entry.type(), entity);

                if (_lifeCycleHandler != null)
                {
                    _lifeCycleHandler.entityCreated(entity);
                }
                Message msg = _msgFactory.response(req, ResponseCode.OK, entity.read());
                send(msg);
            }
            catch (Exception e)
            {
                Message msg = _msgFactory.response(req, ResponseCode.INTERNAL_ERROR, null);
                send(msg);
            }
        }
        else
        {

        }

        for (ManagementEventHandler handler : _mgtEventHandlers)
        {
            handler.onCreate(req);
        }
    }

    @Override
    public void onRead(Request req, ManageableEntity entity)
    {
        if (entity != null)
        {
            Message msg = _msgFactory.response(req, ResponseCode.OK, entity.read());
            send(msg);

            for (ManagementEventHandler handler : _mgtEventHandlers)
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
                if (_lifeCycleHandler != null)
                {
                    _lifeCycleHandler.entityUpdated(entity);
                }
                send(msg);
            }
            catch (IllegalArgumentException e)
            {
                Message msg = _msgFactory.response(req, ResponseCode.BAD_REQUEST, e.getMessage(), null);
                send(msg);
            }
            for (ManagementEventHandler handler : _mgtEventHandlers)
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
                _byId.remove(entity.getID());
                _byName.remove(entity.getName());
                removeEntityFromTypeRegistry(entity.getClass(), entity);
                if (_lifeCycleHandler != null)
                {
                    _lifeCycleHandler.entityDeleted(entity);
                }
                entity = null;
                Message msg = _msgFactory.response(req, ResponseCode.OK, null);
                send(msg);
            }
            catch (ManagementException e)
            {
                Message msg = _msgFactory.response(req, e.getCode(), null);
                send(msg);
            }
            for (ManagementEventHandler handler : _mgtEventHandlers)
            {
                handler.onDelete(req, entity);
            }
        }
    }

    @Override
    public void onQuery(Request req)
    {
        for (ManagementEventHandler handler : _mgtEventHandlers)
        {
            handler.onQuery(req);
        }
    }

    @Override
    public void onGetTypes(Request req)
    {
        Map<String, List<String>> results = new HashMap<String, List<String>>();
        if (req.getAppProps().containsKey(ManagementPropertyNames.ENTITY_TYPE)
                && !((String) req.getAppProps().get(ManagementPropertyNames.ENTITY_TYPE)).isEmpty())
        {
            String givenTypeName = (String) req.getAppProps().get(ManagementPropertyNames.ENTITY_TYPE);
            if (_typeRegistry.containsKey(givenTypeName))
            {
                List<String> superTypes = new ArrayList<String>();
                for (Class<?> superType : _typeRegistry.get(givenTypeName).getSuperTypes())
                {
                    String superTypeName = superType.getAnnotation(ManageableEntityType.class).value();
                    superTypes.add(superTypeName);
                }
                results.put(givenTypeName, superTypes);
            }
        }
        else
        {
            for (String typeName : _typeRegistry.keySet())
            {
                List<String> superTypes = new ArrayList<String>();
                for (Class<?> superType : _typeRegistry.get(typeName).getSuperTypes())
                {
                    String superTypeName = superType.getAnnotation(ManageableEntityType.class).value();
                    superTypes.add(superTypeName);
                }
                results.put(typeName, superTypes);
            }
        }
        Message msg = _msgFactory.response(req, ResponseCode.OK, results);
        send(msg);

        for (ManagementEventHandler handler : _mgtEventHandlers)
        {
            handler.onGetTypes(req);
        }
    }

    @Override
    public void onGetAttributes(Request req)
    {
        Map<String, List<String>> results = new HashMap<String, List<String>>();
        if (req.getAppProps().containsKey(ManagementPropertyNames.ENTITY_TYPE)
                && !((String) req.getAppProps().get(ManagementPropertyNames.ENTITY_TYPE)).isEmpty())
        {
            String givenTypeName = (String) req.getAppProps().get(ManagementPropertyNames.ENTITY_TYPE);
            if (_typeRegistry.containsKey(givenTypeName))
            {
                List<String> attributes = new ArrayList<String>();
                attributes.addAll(STD_ATTR);
                attributes.addAll(getManagementAttributes(_typeRegistry.get(givenTypeName).type()));
                for (Class<?> superType : _typeRegistry.get(givenTypeName).getSuperTypes())
                {
                    attributes.addAll(getManagementAttributes(superType));
                }
                results.put(givenTypeName, attributes);
            }
        }
        else
        {
            for (String typeName : _typeRegistry.keySet())
            {
                List<String> attributes = new ArrayList<String>();
                attributes.addAll(STD_ATTR);
                attributes.addAll(getManagementAttributes(_typeRegistry.get(typeName).type()));
                for (Class<?> superType : _typeRegistry.get(typeName).getSuperTypes())
                {
                    attributes.addAll(getManagementAttributes(superType));
                }
                results.put(typeName, attributes);
            }
        }
        Message msg = _msgFactory.response(req, ResponseCode.OK, results);
        send(msg);

        for (ManagementEventHandler handler : _mgtEventHandlers)
        {
            handler.onGetAttributes(req);
        }
    }

    @Override
    public void onGetOperations(Request req)
    {
        Map<String, List<String>> results = new HashMap<String, List<String>>();
        if (req.getAppProps().containsKey(ManagementPropertyNames.ENTITY_TYPE)
                && !((String) req.getAppProps().get(ManagementPropertyNames.ENTITY_TYPE)).isEmpty())
        {
            String givenTypeName = (String) req.getAppProps().get(ManagementPropertyNames.ENTITY_TYPE);
            if (_typeRegistry.containsKey(givenTypeName))
            {
                List<String> methods = new ArrayList<String>();
                methods.addAll(STD_OPS);
                methods.addAll(getManagementMethods(_typeRegistry.get(givenTypeName).type()));
                for (Class<?> superType : _typeRegistry.get(givenTypeName).getSuperTypes())
                {
                    methods.addAll(getManagementMethods(superType));
                }
                results.put(givenTypeName, methods);
            }
        }
        else
        {
            for (String typeName : _typeRegistry.keySet())
            {
                List<String> methods = new ArrayList<String>();
                methods.addAll(STD_OPS);
                methods.addAll(getManagementMethods(_typeRegistry.get(typeName).type()));
                for (Class<?> superType : _typeRegistry.get(typeName).getSuperTypes())
                {
                    methods.addAll(getManagementMethods(superType));
                }
                results.put(typeName, methods);
            }
        }
        Message msg = _msgFactory.response(req, ResponseCode.OK, results);
        send(msg);
        for (ManagementEventHandler handler : _mgtEventHandlers)
        {
            handler.onGetOperations(req);
        }
    }

    @Override
    public void onGetManagementNodes(Request req)
    {
        for (ManagementEventHandler handler : _mgtEventHandlers)
        {
            handler.onGetManagementNodes(req);
        }
    }

    @Override
    public void onRegister(Request req)
    {
        for (ManagementEventHandler handler : _mgtEventHandlers)
        {
            handler.onRegister(req);
        }
    }

    @Override
    public void onDeregister(Request req)
    {
        for (ManagementEventHandler handler : _mgtEventHandlers)
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

    @Override
    public void registerType(Class<?> type, ManageableEntityFactory factory) throws MessagingServiceException
    {
        if (!type.isAnnotationPresent(ManageableEntityType.class))
        {
            throw new MessagingServiceException("Only types annotated with ManageableEntityType are accepted");
        }
        String typeName = type.getAnnotation(ManageableEntityType.class).value();
        TypeRegistryEntry entry = new TypeRegistryEntry(typeName, type, getTypes(type), factory);
        _typeRegistry.put(typeName, entry);
    }

    void addEntityToTypeRegistry(Class<?> clazz, ManageableEntity entity)
    {
        List<Class<?>> types = new ArrayList<Class<?>>();
        String mainType = clazz.getAnnotation(ManageableEntityType.class).value();
        types.add(clazz);
        types.addAll(_typeRegistry.get(mainType).getSuperTypes());

        for (Class<?> type : types)
        {
            String typeName = type.getAnnotation(ManageableEntityType.class).value();
            if (_typeToEntity.containsKey(typeName))
            {
                _typeToEntity.get(typeName).addEnitity(entity);
            }
            else
            {
                TypeToEntityEntry entry = new TypeToEntityEntry(typeName);
                entry.addEnitity(entity);
                _typeToEntity.put(typeName, entry);
            }
        }
    }

    void removeEntityFromTypeRegistry(Class<?> clazz, ManageableEntity entity)
    {
        List<Class<?>> types = new ArrayList<Class<?>>();
        String mainType = clazz.getAnnotation(ManageableEntityType.class).value();
        types.add(clazz);
        types.addAll(_typeRegistry.get(mainType).getSuperTypes());

        for (Class<?> type : types)
        {
            String typeName = type.getAnnotation(ManageableEntityType.class).value();
            if (_typeToEntity.containsKey(typeName))
            {
                _typeToEntity.get(typeName).removeEnitity(entity);
            }
        }
    }

    public class TypeToEntityEntry
    {
        final String _type;

        final List<ManageableEntity> _entities;

        TypeToEntityEntry(String type)
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

        public String type()
        {
            return _type;
        }
    }

    public class TypeRegistryEntry
    {
        final String _typeName;

        final Class<?> _type;

        final ManageableEntityFactory _factory;

        final List<Class<?>> _superTypes;

        TypeRegistryEntry(String typeName, Class<?> type, List<Class<?>> superTypes, ManageableEntityFactory factory)
        {
            _typeName = typeName;
            _type = type;
            _superTypes = superTypes;
            _factory = factory;
        }

        public String typeName()
        {
            return _typeName;
        }

        public Class<?> type()
        {
            return _type;
        }

        List<Class<?>> getSuperTypes()
        {
            return _superTypes;
        }

        ManageableEntityFactory factory()
        {
            return _factory;
        }
    }

    List<Class<?>> getTypes(Class<?> type)
    {
        List<Class<?>> list = new ArrayList<Class<?>>();
        getInterfaceTypes(type, list);
        Class<?> superClass = type.getSuperclass();
        while (superClass != null)
        {
            if (superClass.isAnnotationPresent(ManageableEntityType.class))
            {
                list.add(superClass);
            }
            // Introspec the interfaces of the super class.
            getInterfaceTypes(superClass, list);
            superClass = superClass.getSuperclass();
        }

        return list;
    }

    void getInterfaceTypes(Class<?> type, List<Class<?>> list)
    {
        for (Class<?> interfaze : type.getInterfaces())
        {
            if (interfaze.isAnnotationPresent(ManageableEntityType.class))
            {
                list.add(interfaze);
                Class<?> superClass = interfaze.getSuperclass();
                while (superClass != null)
                {
                    if (superClass.isAnnotationPresent(ManageableEntityType.class))
                    {
                        list.add(superClass);
                    }
                    superClass = superClass.getSuperclass();
                }
            }
        }
    }

    List<String> getManagementMethods(Class<?> type)
    {
        List<String> methods = new ArrayList<String>();
        for (Method method : type.getMethods())
        {
            if (method.isAnnotationPresent(ManagementMethod.class))
            {
                methods.add(method.getAnnotation(ManagementMethod.class).value());
            }
        }
        return methods;
    }

    List<String> getManagementAttributes(Class<?> type)
    {
        List<String> attrs = new ArrayList<String>();
        for (Field field : type.getFields())
        {
            if (field.isAnnotationPresent(ManagementAttribute.class))
            {
                attrs.add(field.getAnnotation(ManagementAttribute.class).value());
            }
        }
        return attrs;
    }
}