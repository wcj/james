/***********************************************************************
 * Copyright (c) 1999-2006 The Apache Software Foundation.             *
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
package org.apache.james.util;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.StringTokenizer;

/**
 * Sends the message through daemonized SpamAssassin (spamd), visit <a
 * href="SpamAssassin.org">SpamAssassin.org</a> for info on configuration.
 */
public class SpamAssassinInvoker {

    String spamdHost;

    int spamdPort;

    String subjectPrefix;

    String hits = "?";

    String required = "?";

    public SpamAssassinInvoker(String spamdHost, int spamdPort) {
        this.spamdHost = spamdHost;
        this.spamdPort = spamdPort;
    }

    /**
     * Scan a MimeMessage for spam by passing it to spamd. 
     * 
     * @param message The MimeMessage to scan 
     * @return true if spam otherwise false
     * @throws MessagingException if an error on scanning is detected
     */
    public boolean scanMail(MimeMessage message) throws MessagingException {
        Socket socket = null;
        OutputStream out = null;
        BufferedReader in = null;

        try {
            socket = new Socket(spamdHost, spamdPort);

            out = socket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            out.write("CHECK SPAMC/1.2\r\n\r\n".getBytes());

            message.writeTo(out);
            out.flush();
            socket.shutdownOutput();
            String s = null;
            while ((s = in.readLine()) != null) {
                if (s.startsWith("Spam:")) {
                    StringTokenizer t = new StringTokenizer(s, " ");
                    boolean spam;
                    try {
                        t.nextToken();
                        spam = Boolean.valueOf(t.nextToken()).booleanValue();
                    } catch (Exception e) {
                        // On exception return flase
                        return false;
                    }
                    t.nextToken();
                    hits = t.nextToken();
                    t.nextToken();
                    required = t.nextToken();

                    if (spam) {
                        // spam detected
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        } catch (UnknownHostException e1) {
            throw new MessagingException(
                    "Error communicating with spamd. Unknown host: "
                            + spamdHost);
        } catch (IOException e1) {
            throw new MessagingException("Error communicating with spamd on "
                    + spamdHost + ":" + spamdPort + "Exception:" + e1);
        } catch (MessagingException e1) {
            throw new MessagingException("Error communicating with spamd on "
                    + spamdHost + ":" + spamdPort + "Exception:" + e1);
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
            }

        }
    }

    /**
     * Return the hits which was returned by spamd
     * 
     * @return hits
     */
    public String getHits() {
        return hits;
    }

    /**
     * Return the required hits
     * 
     * @return required
     */
    public String getRequiredHits() {
        return required;
    }
}
