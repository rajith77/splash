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
