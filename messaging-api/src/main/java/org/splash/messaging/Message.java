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
package org.splash.messaging;

import java.util.Map;

/**
 * Provides a representation of a <i>Message</i>.
 * 
 * <h4>For Sending</h4> The application can use {@link Messaging#message()} to
 * create a message that can be used or sending. To set the content use
 * {@link Message#setContent(Object)} and the various setter method for setting
 * message properties.
 * 
 * <pre>
 * {@code
 * Message m = Messaging.message();
 * m.setAddress("foo/bar");
 * m.setContent("Hello World");
 * }
 * </pre>
 * 
 * <h4>Receiving</h4> The application can use {@link Message#getContent()} for
 * getting the content and the various get methods for accessing the message
 * properties.
 */
public interface Message
{
    short DEFAULT_PRIORITY = 4;

    boolean isDurable();

    long getDeliveryCount();

    short getPriority();

    boolean isFirstAcquirer();

    long getTtl();

    Object getMessageId();

    long getGroupSequence();

    String getReplyToGroupId();

    long getCreationTime();

    String getAddress();

    byte[] getUserId();

    String getReplyTo();

    String getGroupId();

    String getContentType();

    long getExpiryTime();

    Object getCorrelationId();

    String getContentEncoding();

    String getSubject();

    Map getMessageAnnotations();

    Map getDeliveryAnnotations();

    Map getApplicationProperties();

    Object getContent();

    void setDurable(boolean durable);

    void setTtl(long ttl);

    void setDeliveryCount(long deliveryCount);

    void setFirstAcquirer(boolean firstAcquirer);

    void setPriority(short priority);

    void setGroupSequence(long groupSequence);

    void setUserId(byte[] userId);

    void setCreationTime(long creationTime);

    void setSubject(String subject);

    void setGroupId(String groupId);

    void setAddress(String to);

    void setExpiryTime(long absoluteExpiryTime);

    void setReplyToGroupId(String replyToGroupId);

    void setContentEncoding(String contentEncoding);

    void setContentType(String contentType);

    void setReplyTo(String replyTo);

    void setCorrelationId(Object correlationId);

    void setMessageId(Object messageId);

    void setMessageAnnotations(Map map);

    void setDeliveryAnnotations(Map map);

    void setApplicationProperties(Map map);

    void setContent(Object content);
}