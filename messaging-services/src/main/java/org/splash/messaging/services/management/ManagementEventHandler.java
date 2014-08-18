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
package org.splash.messaging.services.management;

import org.splash.messaging.management.Request;
import org.splash.messaging.management.Response;

public interface ManagementEventHandler
{
    void onRequest(Request req);

    void onResponse(Response res);

    void onCreate(Request req);

    void onRead(Request req, ManageableEntity entity);

    void onUpdate(Request req, ManageableEntity entity);

    void onDelete(Request req, ManageableEntity entity);

    void onQuery(Request req);

    void onGetTypes(Request req);

    void onGetAttributes(Request req);

    void onGetOperations(Request req);

    void onGetManagementNodes(Request req);

    void onRegister(Request req);

    void onDeregister(Request req);
}