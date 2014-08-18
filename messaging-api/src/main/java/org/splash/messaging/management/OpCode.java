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
package org.splash.messaging.management;

public enum OpCode
{
    CREATE("CREATE"),
    READ("READ"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    QUERY("QUERY"),
    GET_TYPES("GET-TYPES"),
    GET_ATTRIBUTES("GET-ATTRIBUTES"),
    GET_OPERATIONS("GET-OPERATIONS"),
    GET_MGMT_NODES("GET-MGMT-NODES"),
    REGISTER("REGISTER"),
    DEREGISTER("DEREGISTER");
    
    private String _description;

    OpCode(String description)
    {
        _description = description;
    }

    String getDesc()
    {
        return _description;
    }
    
    public static OpCode get(String code)
    {
        return Enum.valueOf(OpCode.class, code);
    }
}