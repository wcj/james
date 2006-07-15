/***********************************************************************
 * Copyright (c) 2006 The Apache Software Foundation.                  *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.apache.james.smtpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.james.services.DNSServer;
import org.apache.james.smtpserver.core.DNSRBLHandler;
import org.apache.james.test.mock.avalon.MockLogger;
import org.apache.james.util.watchdog.Watchdog;
import org.apache.mailet.Mail;

import junit.framework.TestCase;

public class DNSRBLHandlerTest extends TestCase {

    private DNSServer mockedDnsServer;

    private SMTPSession mockedSMTPSession;

    private String remoteIp = "127.0.0.2";

    private boolean relaying = false;

    protected void setUp() throws Exception {
        super.setUp();
        setupMockedDnsServer();
        setRelayingAllowed(false);
    }

    /**
     * Set the remoteIp
     * 
     * @param remoteIp The remoteIP to set
     */
    private void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    /**
     * Set relayingAllowed
     * 
     * @param relaying true or false
     */
    private void setRelayingAllowed(boolean relaying) {
        this.relaying = relaying;
    }

    /**
     * Setup the mocked dnsserver
     *
     */
    private void setupMockedDnsServer() {
        mockedDnsServer = new DNSServer() {

            public Collection findMXRecords(String hostname) {
                throw new UnsupportedOperationException("Unimplemented in mock");
            }

            public Collection findTXTRecords(String hostname) {
                List res = new ArrayList();
                if (hostname == null) {
                    return res;
                }
                ;
                if ("2.0.0.127.bl.spamcop.net".equals(hostname)) {
                    res.add("Blocked - see http://www.spamcop.net/bl.shtml?127.0.0.2");
                }
                return res;
            }

            public Iterator getSMTPHostAddresses(String domainName) {
                throw new UnsupportedOperationException("Unimplemented in mock");
            }

            public InetAddress[] getAllByName(String host)
                    throws UnknownHostException {
                throw new UnsupportedOperationException("Unimplemented in mock");
            }

            public InetAddress getByName(String host)
                    throws UnknownHostException {
                if ("2.0.0.127.bl.spamcop.net".equals(host)) {
                    return InetAddress.getByName("127.0.0.1");
                } else if ("3.0.0.127.bl.spamcop.net".equals(host)) {
                    return InetAddress.getByName("127.0.0.1");
                } else if ("192.168.0.1.bl.spamcop.net".equals(host)) {
                    return InetAddress.getByName("fesdgaeg.deger");
                }
                return InetAddress.getByName(host);
            }
        };
    }

    /**
     * Setup mocked smtpsession
     */
    private void setupMockedSMTPSession() {
        mockedSMTPSession = new SMTPSession() {

            private String blockListedDetail = null;

            private boolean blocklisted = false;

            public void writeResponse(String respString) {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public String readCommandLine() throws IOException {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public StringBuffer getResponseBuffer() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public String clearResponseBuffer() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public InputStream getInputStream() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public String getCommandName() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public String getCommandArgument() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public Mail getMail() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public void setMail(Mail mail) {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public String getRemoteHost() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public String getRemoteIPAddress() {
                return remoteIp;
            }

            public void abortMessage() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public void endSession() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public boolean isSessionEnded() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public HashMap getState() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public void resetState() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public SMTPHandlerConfigurationData getConfigurationData() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public void setBlockListed(boolean blocklisted) {
                this.blocklisted = blocklisted;
            }

            public boolean isBlockListed() {
                return blocklisted;
            }

            public void setBlockListedDetail(String detail) {
                this.blockListedDetail = detail;
            }

            public String getBlockListedDetail() {
                return blockListedDetail;
            }

            public boolean isRelayingAllowed() {
                return relaying;
            }

            public boolean isAuthRequired() {
                return false;
            }

            public boolean useHeloEhloEnforcement() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public String getUser() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public void setUser(String user) {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public Watchdog getWatchdog() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public String getSessionID() {
                throw new UnsupportedOperationException(
                        "Unimplemented mock service");
            }

            public int getRcptCount() {
                // TODO Auto-generated method stub
                return 0;
            }

            public void setStopHandlerProcessing(boolean b) {
                // TODO Auto-generated method stub
                
            }

            public boolean getStopHandlerProcessing() {
                // TODO Auto-generated method stub
                return false;
            }

        };
    }

    // ip is blacklisted and has txt details
    public void testBlackListedTextPresent() {
        DNSRBLHandler rbl = new DNSRBLHandler();

        ContainerUtil.enableLogging(rbl, new MockLogger());

        setupMockedSMTPSession();
        rbl.setDNSServer(mockedDnsServer);

        rbl.setBlacklist(new String[] { "bl.spamcop.net" });
        rbl.setGetDetail(true);
        rbl.onConnect(mockedSMTPSession);
        assertEquals("Blocked - see http://www.spamcop.net/bl.shtml?127.0.0.2",
                mockedSMTPSession.getBlockListedDetail());
        assertTrue(mockedSMTPSession.isBlockListed());
    }

    // ip is blacklisted and has txt details but we don'T want to retrieve the txt record
    public void testGetNoDetail() {
        DNSRBLHandler rbl = new DNSRBLHandler();

        ContainerUtil.enableLogging(rbl, new MockLogger());

        setupMockedSMTPSession();
        rbl.setDNSServer(mockedDnsServer);

        rbl.setBlacklist(new String[] { "bl.spamcop.net" });
        rbl.setGetDetail(false);
        rbl.onConnect(mockedSMTPSession);
        assertNull(mockedSMTPSession.getBlockListedDetail());
        assertTrue(mockedSMTPSession.isBlockListed());
    }

    // ip is allowed to relay
    public void testRelayAllowed() {
        DNSRBLHandler rbl = new DNSRBLHandler();

        ContainerUtil.enableLogging(rbl, new MockLogger());

        setRelayingAllowed(true);
        setupMockedSMTPSession();

        rbl.setDNSServer(mockedDnsServer);

        rbl.setBlacklist(new String[] { "bl.spamcop.net" });
        rbl.setGetDetail(true);
        rbl.onConnect(mockedSMTPSession);
        assertNull(mockedSMTPSession.getBlockListedDetail());
        assertFalse(mockedSMTPSession.isBlockListed());
    }

    // ip not on blacklist
    public void testNotBlackListed() {
        DNSRBLHandler rbl = new DNSRBLHandler();

        ContainerUtil.enableLogging(rbl, new MockLogger());
        setRemoteIp("192.168.0.1");
        setupMockedSMTPSession();

        rbl.setDNSServer(mockedDnsServer);

        rbl.setBlacklist(new String[] { "bl.spamcop.net" });
        rbl.setGetDetail(true);
        rbl.onConnect(mockedSMTPSession);
        assertNull(mockedSMTPSession.getBlockListedDetail());
        assertFalse(mockedSMTPSession.isBlockListed());
    }

    // ip on blacklist without txt details
    public void testBlackListedNoTxt() {
        DNSRBLHandler rbl = new DNSRBLHandler();

        ContainerUtil.enableLogging(rbl, new MockLogger());
        setRemoteIp("127.0.0.3");
        setupMockedSMTPSession();

        rbl.setDNSServer(mockedDnsServer);

        rbl.setBlacklist(new String[] { "bl.spamcop.net" });
        rbl.setGetDetail(true);
        rbl.onConnect(mockedSMTPSession);
        assertNull(mockedSMTPSession.getBlockListedDetail());
        assertTrue(mockedSMTPSession.isBlockListed());
    }

    // ip on whitelist
    public void testWhiteListed() {
        DNSRBLHandler rbl = new DNSRBLHandler();

        ContainerUtil.enableLogging(rbl, new MockLogger());
        setRemoteIp("127.0.0.2");
        setupMockedSMTPSession();

        rbl.setDNSServer(mockedDnsServer);

        rbl.setWhitelist(new String[] { "bl.spamcop.net" });
        rbl.setGetDetail(true);
        rbl.onConnect(mockedSMTPSession);
        assertNull(mockedSMTPSession.getBlockListedDetail());
        assertFalse(mockedSMTPSession.isBlockListed());
    }

}
