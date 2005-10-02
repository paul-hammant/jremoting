/* ====================================================================
 * Copyright 2005 JRemoting Committers
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

import org.codehaus.jremoting.ThreadContext;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.ClassRetriever;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.transports.AbstractServer;
import org.codehaus.jremoting.server.transports.AbstractServerStreamReadWriter;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Class AbstractPipedServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractPipedServer extends AbstractServer {

    public AbstractPipedServer(ClassRetriever classRetriever, Authenticator authenticator, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory contextFactory) {
        super(new InvocationHandlerAdapter(classRetriever, authenticator, serverMonitor, contextFactory), serverMonitor, threadPool, contextFactory);
    }

    public AbstractPipedServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory contextFactory) {
        super(invocationHandlerAdapter, serverMonitor, threadPool, contextFactory);
    }

    /**
     * Method connect
     *
     * @param in
     * @param out
     * @throws ConnectionException
     */
    public void makeNewConnection(PipedInputStream in, PipedOutputStream out) throws ConnectionException {

        if (getState() == UNSTARTED | getState() == STARTING) {
            throw new ConnectionException("Server not started yet");
        } else if (getState() == SHUTTINGDOWN) {
            throw new ConnectionException("Server is Shutting down");
        }

        try {
            PipedInputStream pIS = new PipedInputStream();
            PipedOutputStream pOS = new PipedOutputStream();

            pIS.connect(out);
            in.connect(pOS);

            AbstractServerStreamReadWriter ssrw = createServerStreamReadWriter();

            ssrw.setStreams(pIS, pOS, "piped");

            PipedStreamServerConnection pssc = new PipedStreamServerConnection(this, pIS, pOS, ssrw, serverMonitor);

            ThreadContext thread = getThreadPool().getThreadContext(pssc);

            thread.start();
        } catch (IOException pe) {
            throw new ConnectionException("Some problem setting up server : " + pe.getMessage());
        }
    }

    /**
     * Method start
     */
    public void start() {
        setState(STARTED);
    }

    /**
     * Method stop
     */
    public void stop() {

        setState(SHUTTINGDOWN);

        killAllConnections();

        setState(STOPPED);
    }

    protected abstract AbstractServerStreamReadWriter createServerStreamReadWriter();
}