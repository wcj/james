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

package org.apache.james.mailboxmanager.repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.james.test.mock.avalon.MockLogger;
import org.xml.sax.SAXException;

public class MailboxManagerMailRepositoryTest extends TestCase {

    protected MailboxManagerMailRepository mailboxManagerMailRepository;

    public void setUp() {
        mailboxManagerMailRepository = new MailboxManagerMailRepository();
        ContainerUtil.enableLogging(mailboxManagerMailRepository,
                new MockLogger());
    }

    public void testConfigurePostfix() throws ConfigurationException,
            SAXException, IOException {
        mailboxManagerMailRepository.configure(get(
                "mailboxmanager://#mail/tuser/", ".INBOX", true));
        assertEquals("#mail.tuser.INBOX", mailboxManagerMailRepository
                .getMailboxName());
        
        mailboxManagerMailRepository.configure(get(
                "mailboxmanager://#mail/tuser", ".NEWBOX", true));
        assertEquals("#mail.tuser.NEWBOX", mailboxManagerMailRepository
                .getMailboxName());
        
        mailboxManagerMailRepository.configure(get(
                "mailboxmanager://#mail/tuser", ".NEWBOX", false));
        assertEquals("#mail/tuser.NEWBOX", mailboxManagerMailRepository
                .getMailboxName());
        
        mailboxManagerMailRepository.configure(get(
                "mailboxmanager://#mail/tuser/", ".NEWBOX", false));
        assertEquals("#mail/tuser/.NEWBOX", mailboxManagerMailRepository
                .getMailboxName());
    }

    public void testConfigure() throws ConfigurationException,
            SAXException, IOException {
        mailboxManagerMailRepository.configure(get(
                "mailboxmanager://#system/tuser/", null, true));
        assertEquals("#system.tuser", mailboxManagerMailRepository
                .getMailboxName());
        
        mailboxManagerMailRepository.configure(get(
                "mailboxmanager://#system/tuser", null, true));
        assertEquals("#system.tuser", mailboxManagerMailRepository
                .getMailboxName());
        
        mailboxManagerMailRepository.configure(get(
                "mailboxmanager://#system/tuser", null, false));
        assertEquals("#system/tuser", mailboxManagerMailRepository
                .getMailboxName());
        
        mailboxManagerMailRepository.configure(get(
                "mailboxmanager://#system/tuser/", null, false));
        assertEquals("#system/tuser/", mailboxManagerMailRepository
                .getMailboxName());
    }

    protected Configuration get(String url, String postfix,
            boolean translateDelimiter) throws ConfigurationException,
            SAXException, IOException {
        String trans = "";
        if (translateDelimiter) {
            trans = "translateDelimiters=\"true\" ";
        }
        if (postfix != null) {
            postfix = "postfix=\"" + postfix + "\" ";
        } else {
            postfix = "";
        }
        String configXml = "<repository destinationURL=\"" + url + "\" " + postfix
                + trans + "type=\"MAIL\" />";
        InputStream stream=new ByteArrayInputStream(configXml.getBytes());
        return new DefaultConfigurationBuilder().build(stream);
    }

}
