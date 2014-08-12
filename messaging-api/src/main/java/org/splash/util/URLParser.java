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
package org.splash.util;

import org.splash.messaging.ConnectionSettings;

public class URLParser
{
    public static ConnectionSettings parse(String url)
    {
        ConnectionSettings settings = new ConnectionSettings();
        int start = 0;
        int schemeEnd = url.indexOf("://", start);
        if (schemeEnd >= 0)
        {
            String scheme = url.substring(start, schemeEnd);
            settings.setScheme(scheme);
            start = schemeEnd + 3;
        }

        String uphp = url.substring(start);

        String hp;
        int at = uphp.indexOf('@');
        if (at >= 0)
        {
            String up = uphp.substring(0, at);
            hp = uphp.substring(at + 1);

            int colon = up.indexOf(':');
            if (colon >= 0)
            {
                String user = up.substring(0, colon);
                String pass = up.substring(colon + 1);
                settings.setUser(user);
                settings.setPass(pass);
            }
            else
            {
                String user = up;
                settings.setUser(user);
            }
        }
        else
        {
            hp = uphp;
        }

        String host = null;
        int port = 5672;
        if (hp.startsWith("["))
        {
            int close = hp.indexOf(']');
            if (close >= 0)
            {
                host = hp.substring(1, close);
                if (hp.substring(close + 1).startsWith(":"))
                {
                    port = Integer.parseInt(hp.substring(close + 2));
                }
            }
        }

        if (host == null)
        {
            int colon = hp.indexOf(':');
            if (colon >= 0)
            {
                host = hp.substring(0, colon);
                port = Integer.parseInt(hp.substring(colon + 1));
            }
            else
            {
                host = hp;
            }
        }
        settings.setHost(host);
        settings.setPort(port);
        return settings;
    }
}
