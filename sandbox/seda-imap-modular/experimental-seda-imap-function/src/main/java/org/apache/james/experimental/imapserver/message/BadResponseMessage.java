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

package org.apache.james.experimental.imapserver.message;

import org.apache.james.experimental.imapserver.ImapResponse;
import org.apache.james.experimental.imapserver.ImapSession;

/**
 * Carries the response to a request with bad syntax..
 * 
 */
public class BadResponseMessage implements ImapRequestMessage,
        ImapResponseMessage {

    private final String message;
    private final String tag;
    
    public BadResponseMessage(final String message) {
    	this(message, null);
    }
    
    public BadResponseMessage(final String message, final String tag) {
        super();
        this.message = message;
        this.tag = tag;
    }
    

    public ImapResponseMessage process(ImapSession session) {
        return this;
    }

    public void encode(ImapResponse response, ImapSession session) {
    	if (tag == null) {
    		response.badResponse(message);
    	} else {
    		response.badResponse(message, tag);
    	}
    }

}
