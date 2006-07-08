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

import java.util.Iterator;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.james.util.SpamAssassinInvoker;
import org.apache.james.util.mail.dsn.DSNStatus;

/**
 * This MessageHandler could be used to check message against spamd before
 * accept the email. So its possible to reject a message on smtplevel if a
 * configured hits amount is reached.<br>
 * 
 * Sample Configuration: <br>
 * <br>
 * &lt;handler class="org.apache.james.smtpserver.SpamAssassinHandler"&gt;
 * &lt;spamdHost&gt;localhost&lt;/spamdHost&gt;
 * &lt;spamdPort&gt;783&lt;/spamdPort&gt; <br>
 * &lt;spamdRejectionHits&gt;15.0&lt;/spamdRejectionHits&gt; &lt;/handler&gt;
 */
public class SpamAssassinHandler extends AbstractLogEnabled implements
        MessageHandler, Configurable {

    /**
     * The port spamd is listen on
     */
    private int spamdPort = 783;

    /**
     * The host spamd is runnin on
     */
    private String spamdHost = "localhost";

    /**
     * The hits on which the message get rejected
     */
    private double spamdRejectionHits = 0.0;

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration arg0) throws ConfigurationException {
        Configuration spamdHostConf = arg0.getChild("spamdHost", false);
        if (spamdHostConf != null) {
            setSpamdHost(spamdHostConf.getValue("localhost"));
        }

        Configuration spamdPortConf = arg0.getChild("spamdPort", false);
        if (spamdPortConf != null) {
            setSpamdPort(spamdPortConf.getValueAsInteger(783));
        }

        Configuration spamdRejectionHitsConf = arg0.getChild(
                "spamdRejectionHits", false);
        if (spamdRejectionHitsConf != null) {
            setSpamdRejectionHits(spamdRejectionHitsConf.getValueAsDouble(0.0));
        }

    }

    /**
     * Set the host the spamd daemon is running at
     * 
     * @param spamdHost
     *            The spamdHost
     */
    public void setSpamdHost(String spamdHost) {
        this.spamdHost = spamdHost;
    }

    /**
     * Set the port the spamd damon is listen on
     * 
     * @param spamdPort
     *            the spamdPort
     */
    public void setSpamdPort(int spamdPort) {
        this.spamdPort = spamdPort;
    }

    /**
     * Set the hits on which the message will be rejected.
     * 
     * @param spamdRejectionHits
     *            The hits
     */
    public void setSpamdRejectionHits(double spamdRejectionHits) {
        this.spamdRejectionHits = spamdRejectionHits;

    }

    /**
     * @see org.apache.james.smtpserver.MessageHandler#onMessage(SMTPSession)
     */
    public void onMessage(SMTPSession session) {

        // Not scan the message if relaying allowed
        if (session.isRelayingAllowed()) {
            return;
        }

        try {
            MimeMessage message = session.getMail().getMessage();
            SpamAssassinInvoker sa = new SpamAssassinInvoker(spamdHost,
                    spamdPort);
            sa.scanMail(message);

            Iterator headers = sa.getHeaders().keySet().iterator();

            // Add the headers
            while (headers.hasNext()) {
                String key = headers.next().toString();

                message.setHeader(key, (String) sa.getHeaders().get(key));
            }

            // Check if rejectionHits was configured
            if (spamdRejectionHits > 0) {
                try {
                    double hits = Double.parseDouble(sa.getHits());

                    // if the hits are bigger the rejectionHits reject the
                    // message
                    if (spamdRejectionHits <= hits) {
                        String responseString = "554 "
                                + DSNStatus.getStatus(DSNStatus.PERMANENT,
                                        DSNStatus.SECURITY_OTHER)
                                + " This message smells like SPAM. Message rejected";
                        StringBuffer buffer = new StringBuffer(256).append(
                                "Rejected message from ").append(
                                session.getState().get(SMTPSession.SENDER)
                                        .toString()).append(" from host ")
                                .append(session.getRemoteHost()).append(" (")
                                .append(session.getRemoteIPAddress()).append(
                                        ") " + responseString).append(
                                        ". Required rejection hits: "
                                                + spamdRejectionHits
                                                + " hits: " + hits);
                        getLogger().info(buffer.toString());
                        session.writeResponse(responseString);

                        // Message reject .. abort it!
                        session.abortMessage();
                    }
                } catch (NumberFormatException e) {
                    // hits unknown
                }
            }
        } catch (MessagingException e) {
            getLogger().error(e.getMessage());
        }

    }
}
