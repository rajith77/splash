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

/**
 * 
 * Represents a logical <i>link</i> where messages travel <u>inwards</u> from
 * the containers perspective.
 * 
 * <h4>Message Credits And CreditMode</h4> <b><i>Message Credits</i></b> and
 * <b><i>CreditMode</i></b> together determines how message credits are managed
 * by the container.
 * 
 * <h4>How To Set CreditMode</h4> The CreditMode can be specified using
 * {@link Session#createInboundLink(address, InboundLinkMode, CreditMode)}.
 * 
 * <h4>How To Set Message Credits</h4> When the InboundLink is created the
 * default message credits is determined based on it's CreditMode. <br>
 * If {@link CreditMode#AUTO} is used it will be set to "1". This can be changed
 * via the system property "splash.default.credits". <br>
 * If {@link CreditMode#EXPLICT} is used it will be set to "0".
 * 
 * Once the InboundLink is created it can explicitly specify message credits
 * using {@link InboindLink#setCredits(int)}. The capacity can be any non
 * negative integer including zero.
 * 
 * <h4>How Message Credits Work</h4> When the InboundLink is created, <br>
 * If CreditMode is {@link CreditMode#AUTO}, "N" message credits will be issued
 * immediately, where "N" is the default as determined above. <br>
 * If CreditMode is {@link CreditMode#EXPLICT} no message credits are issued.
 * 
 * When the application sets the credits using
 * {@link InboudLink#setCredits(int)}, the container will issue a cancel for any
 * previously issued credits and re-issue credits as specified by the method. If
 * any messages were in flight before the peer sees the 'cancel' the InboundLink
 * will end up getting extra messages than intended.
 * 
 * If CreditMode is {@link CreditMode#AUTO}, the container will automatically
 * re-issue credits after a certain number of messages have been marked as
 * either accepted, rejected or released by the application. The container will
 * determine the optimum threshold for when the re-issue happens.
 * 
 * If CreditMode is {@link CreditMode#EXPLICT} the application needs to
 * explicitly manage it's message credits and use
 * {@link InboundLink#setCredits(int)} to issue credits when it is ready to
 * process messages.
 * 
 * <h4>Exceptions</h4>
 * <ul>
 * <li>NetworkException : Thrown when the underlying network connection fails.
 * </li>
 * <li>MessagingException : Thrown when the inbound-link gets to an erroneous
 * state.</li>
 * </ul>
 */
public interface InboundLink extends ContextAware
{
    /**
     * @return The address used for establishing the Link
     */
    String getAddress();

    /**
     * @return The CreditMode used by the InboundLink
     * @see CreditMode
     */
    CreditMode getCreditMode();

    /**
     * @return The number of messages received by the application but has not
     *         yet been settled.
     */
    int getUnsettled() throws MessagingException;

    /**
     * Sets the message credits for the InboundLink. The credits should be a non
     * negative value.
     * 
     * If CreditMode is EXPLICIT, the application needs to set the credits to a
     * non zero value to begin receiving messages.
     * 
     */
    void setCredits(int credits) throws MessagingException, NetworkException;

    /**
     * Close the Link and free any resources associated with it.
     */
    void close() throws MessagingException, NetworkException;
}