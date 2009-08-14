/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.smtpserver.core.filter.fastfail;

import org.apache.james.smtpserver.SMTPSession;


import java.net.UnknownHostException;


public class ReverseEqualsEhloHeloHandler extends ResolvableEhloHeloHandler {

    /**
     * @see org.apache.james.smtpserver.core.filter.fastfail.ResolvableEhloHeloHandler#isBadHelo(org.apache.james.smtpserver.SMTPSession, java.lang.String)
     */
    protected boolean isBadHelo(SMTPSession session, String argument) {
        try {
            // get reverse entry
            String reverse = dnsServer.getHostName(dnsServer.getByName(
                    session.getRemoteIPAddress()));
            if (!argument.equals(reverse)) {
                return true;
            }
        } catch (UnknownHostException e) {
            return true;
        }
        return false;
    }
    
}
