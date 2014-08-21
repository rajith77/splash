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

import java.util.HashMap;
import java.util.Map;

import org.splash.messaging.Message;
import org.splash.messaging.management.ManagementException;
import org.splash.messaging.management.ManagementMessageFactory;
import org.splash.messaging.management.ManagementPropertyNames;
import org.splash.messaging.management.Operation;
import org.splash.messaging.management.Request;
import org.splash.messaging.management.Response;
import org.splash.messaging.management.ResponseCode;

public class ManagementMessageFactoryImpl implements ManagementMessageFactory
{
    String _locales;

    @Override
    public void setLocales(String locales)
    {
        _locales = locales;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Request parseRequest(final Message m) throws ManagementException
    {
        return new Request()
        {
            Map<String, Object> _props = m.getApplicationProperties();

            Object _msgId = m.getMessageId();

            Object _correlationId = m.getCorrelationId();

            Operation _opCode = Operation.get((String) _props.get(ManagementPropertyNames.OP));

            String _type = (String) _props.get(ManagementPropertyNames.TYPE);

            String _name = (String) _props.get(ManagementPropertyNames.NAME);

            String _id = (String) _props.get(ManagementPropertyNames.IDENTITY);

            Map<String, Object> _args = (Map<String, Object>) m.getContent();

            @Override
            public Object getMessageId()
            {
                return _msgId;
            }

            @Override
            public Object getCorrelationId()
            {
                return _correlationId;
            }

            @Override
            public Operation getOperation()
            {
                return _opCode;
            }

            @Override
            public String getType()
            {
                return _type;
            }

            @Override
            public String getName()
            {
                return _name;
            }

            @Override
            public String getId()
            {
                return _id;
            }

            @Override
            public Map<String, Object> getAppProps()
            {
                return _props;
            }

            @Override
            public Map<String, Object> getBody()
            {
                return _args;
            }

        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Response parseResponse(final Message m) throws ManagementException
    {
        return new Response()
        {
            Map<String, Object> _props = m.getApplicationProperties();

            Operation _opCode = Operation.get((String) _props.get(ManagementPropertyNames.OP));

            Object _msgId = m.getMessageId();

            Object _correlationId = m.getCorrelationId();

            ResponseCode _resCode = ResponseCode.get((Integer) _props.get(ManagementPropertyNames.STATUS_CODE));

            String _desc = (String) _props.get(ManagementPropertyNames.STATUS_DESC);

            Map<String, Object> _args = (Map<String, Object>) m.getContent();

            @Override
            public Object getMessageId()
            {
                return _msgId;
            }

            @Override
            public Object getCorrelationId()
            {
                return _correlationId;
            }

            @Override
            public Operation getOperation()
            {
                return _opCode;
            }

            @Override
            public ResponseCode getResponseCode()
            {
                return _resCode;
            }

            @Override
            public String getDesc()
            {
                return _desc;
            }

            @Override
            public Map<String, Object> getAppProps()
            {
                return _props;
            }

            @Override
            public Map<String, Object> getBody()
            {
                return _args;
            }

        };
    }

    @Override
    public Message response(Request req, ResponseCode code, Map<String, ? extends Object> attributes)
    {
        return response(req, code, null, attributes);
    }

    @Override
    public Message response(Request req, ResponseCode code, String desc, Map<String, ? extends Object> attributes)
    {
        Message msg = new MessageImpl();
        msg.setCorrelationId(req.getCorrelationId() == null ? req.getMessageId() : req.getCorrelationId());
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ManagementPropertyNames.STATUS_CODE, code.getCode());
        props.put(ManagementPropertyNames.STATUS_DESC, desc == null ? code.getDesc() : code.getDesc() + ":" + desc);
        props.put(ManagementPropertyNames.OP, req.getOperation().getDesc());
        msg.setApplicationProperties(props);
        msg.setContent(attributes);
        return msg;
    }

    @Override
    public Message create(String correlationId, String name, String type, Map<String, ? extends Object> attributes)
    {
        Message msg = new MessageImpl();
        msg.setCorrelationId(correlationId);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ManagementPropertyNames.NAME, name);
        props.put(ManagementPropertyNames.TYPE, type);
        props.put(ManagementPropertyNames.OP, Operation.CREATE.getDesc());
        msg.setApplicationProperties(props);
        msg.setContent(attributes);
        return msg;
    }

    @Override
    public Message updateByName(String correlationId, String name, String type, Map<String, ? extends Object> attributes)
    {
        Message msg = new MessageImpl();
        msg.setCorrelationId(correlationId);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ManagementPropertyNames.NAME, name);
        props.put(ManagementPropertyNames.TYPE, type);
        props.put(ManagementPropertyNames.OP, Operation.UPDATE.getDesc());
        msg.setApplicationProperties(props);
        msg.setContent(attributes);
        return msg;
    }

    @Override
    public Message updateByID(String correlationId, String id, String type, Map<String, ? extends Object> attributes)
    {
        Message msg = new MessageImpl();
        msg.setCorrelationId(correlationId);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ManagementPropertyNames.IDENTITY, id);
        props.put(ManagementPropertyNames.TYPE, type);
        props.put(ManagementPropertyNames.OP, Operation.UPDATE.getDesc());
        msg.setApplicationProperties(props);
        msg.setContent(attributes);
        return msg;
    }

    @Override
    public Message readByName(String correlationId, String name, String type)
    {
        Message msg = new MessageImpl();
        msg.setCorrelationId(correlationId);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ManagementPropertyNames.NAME, name);
        props.put(ManagementPropertyNames.TYPE, type);
        props.put(ManagementPropertyNames.OP, Operation.READ.getDesc());
        msg.setApplicationProperties(props);
        return msg;
    }

    @Override
    public Message readByID(String correlationId, String id, String type)
    {
        Message msg = new MessageImpl();
        msg.setCorrelationId(correlationId);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ManagementPropertyNames.IDENTITY, id);
        props.put(ManagementPropertyNames.TYPE, type);
        props.put(ManagementPropertyNames.OP, Operation.READ.getDesc());
        msg.setApplicationProperties(props);
        return msg;
    }

    @Override
    public Message deleteByName(String correlationId, String name, String type)
    {
        Message msg = new MessageImpl();
        msg.setCorrelationId(correlationId);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ManagementPropertyNames.NAME, name);
        props.put(ManagementPropertyNames.TYPE, type);
        props.put(ManagementPropertyNames.OP, Operation.DELETE.getDesc());
        msg.setApplicationProperties(props);
        return msg;
    }

    @Override
    public Message deleteByID(String correlationId, String id, String type)
    {
        Message msg = new MessageImpl();
        msg.setCorrelationId(correlationId);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(ManagementPropertyNames.IDENTITY, id);
        props.put(ManagementPropertyNames.TYPE, type);
        props.put(ManagementPropertyNames.OP, Operation.DELETE.getDesc());
        msg.setApplicationProperties(props);
        return msg;
    }

}