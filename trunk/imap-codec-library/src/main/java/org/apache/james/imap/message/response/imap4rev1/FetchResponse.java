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
package org.apache.james.imap.message.response.imap4rev1;

import javax.mail.Flags;

import org.apache.james.api.imap.message.response.ImapResponseMessage;

public final class FetchResponse implements ImapResponseMessage {

        private final int messageNumber;
        private final Flags flags;
        private final Long uid;
        
        public FetchResponse(final int messageNumber, final Flags flags, final Long uid) {
            super();
            this.messageNumber = messageNumber;
            this.flags = flags;
            this.uid = uid;
        }
        
        /**
         * Gets the number of the message whose details 
         * have been fetched.
         * @return message number
         */
        public final int getMessageNumber() {
            return messageNumber;
        }
        
        /**
         * Gets the fetched flags.
         * @return {@link Flags} fetched,
         * or null if the <code>FETCH</code> did not include <code>FLAGS</code>
         */
        public Flags getFlags() {
            return flags;
        }
        
        /**
         * Gets the unique id for the fetched message.
         * @return message uid, 
         * or null if the <code>FETCH</code> did not include <code>UID</code>
         */
        public Long getUid() {
            return uid;
        }
}
