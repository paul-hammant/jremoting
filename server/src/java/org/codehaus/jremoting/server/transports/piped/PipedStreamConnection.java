/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.server.transports.piped;

import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.ConnectingServer;
import org.codehaus.jremoting.server.transports.StreamConnection;
import org.codehaus.jremoting.server.transports.StreamEncoder;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Class PipedStreamConnection
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class PipedStreamConnection extends StreamConnection {

    /**
     * The piped input stream.
     */
    private PipedInputStream pipedIn;

    /**
     * The piped output stream.
     */
    private PipedOutputStream pipedOut;

    public PipedStreamConnection(ConnectingServer connectingServer, PipedInputStream pipedIn, PipedOutputStream pipedOut, StreamEncoder encoder, ServerMonitor serverMonitor) {

        super(connectingServer, encoder, serverMonitor);

        this.pipedIn = pipedIn;
        this.pipedOut = pipedOut;
    }

    /**
     * Kill connections
     */
    protected void killConnection() {

        try {
            pipedIn.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "PipedStreamConnection.killConnection(): Some problem during closing of Input Stream", e);
        }

        try {
            pipedOut.close();
        } catch (IOException e) {
            serverMonitor.closeError(this.getClass(), "PipedStreamConnection.killConnection(): Some problem during closing of Output Stream", e);
        }
    }
}
