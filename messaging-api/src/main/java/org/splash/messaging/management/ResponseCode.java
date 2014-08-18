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

public enum ResponseCode
{
    OK(200, "OK"),
    CREATED(201, "Created"),
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented");

    private int _code;

    private String _description;

    ResponseCode(int code, String description)
    {
        _code = code;
        _description = description;
    }

    public int getCode()
    {
        return _code;
    }

    public String getDesc()
    {
        return _description;
    }
    
    public static ResponseCode get(int code) throws ManagementException
    {
        switch(code)
        {
        case 200:
            return OK;
        case 201:
            return CREATED;
        case 204:
            return NO_CONTENT;
        case 400:
            return BAD_REQUEST;
        case 404:
            return NOT_FOUND;
        case 500:
            return INTERNAL_ERROR;
        case 501:
            return NOT_IMPLEMENTED;
        default:
            throw new ManagementException(null, "Invalid response code");
        }
    }
}