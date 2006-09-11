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
package org.codehaus.jremoting.server.transports.direct;

import org.codehaus.jremoting.api.DefaultThreadPool;
import org.codehaus.jremoting.api.ThreadPool;
import org.codehaus.jremoting.requests.AbstractRequest;
import org.codehaus.jremoting.responses.AbstractResponse;
import org.codehaus.jremoting.responses.InvocationExceptionThrown;
import org.codehaus.jremoting.server.ServerMarshalledInvocationHandler;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideClientContextFactory;
import org.codehaus.jremoting.server.adapters.InvocationHandlerAdapter;
import org.codehaus.jremoting.server.adapters.MarshalledInvocationHandlerAdapter;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.NoClassRetriever;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.transports.AbstractServer;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;

/**
 * Class DirectMarshalledServer
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class DirectMarshalledServer extends AbstractServer implements ServerMarshalledInvocationHandler {

    private final MarshalledInvocationHandlerAdapter marshalledInvocationHandlerAdapter;

    /**
     * Constructor DirectMarshalledServer for use with pre-exiting InvocationHandlerAdapter and MarshalledInvocationHandler
     *
     * @param invocationHandlerAdapter
     * @param serverMonitor
     * @param threadPool
     * @param contextFactory
     * @param marshalledInvocationHandlerAdapter
     *
     */
    public DirectMarshalledServer(InvocationHandlerAdapter invocationHandlerAdapter, ServerMonitor serverMonitor, ThreadPool threadPool, ServerSideClientContextFactory contextFactory, MarshalledInvocationHandlerAdapter marshalledInvocationHandlerAdapter) {
        super(invocationHandlerAdapter, serverMonitor, threadPool, contextFactory);
        this.marshalledInvocationHandlerAdapter = marshalledInvocationHandlerAdapter;
    }

    public DirectMarshalledServer(InvocationHandlerAdapter invocationHandlerAdapter, MarshalledInvocationHandlerAdapter marshalledInvocationHandlerAdapter) {
        this(invocationHandlerAdapter, new NullServerMonitor(), new DefaultThreadPool(), new DefaultServerSideClientContextFactory(), marshalledInvocationHandlerAdapter);
    }

    public DirectMarshalledServer(InvocationHandlerAdapter invocationHandlerAdapter) {
        this(invocationHandlerAdapter, new MarshalledInvocationHandlerAdapter(invocationHandlerAdapter));
    }

    public DirectMarshalledServer() {
        this(new InvocationHandlerAdapter(new NoClassRetriever(), new DefaultAuthenticator(), new NullServerMonitor(), new DefaultServerSideClientContextFactory()));
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

    /**
     * Method handleInvocation
     *
     * @param request
     * @return
     */
    public byte[] handleInvocation(byte[] request, Object connectionDetails) {
        return marshalledInvocationHandlerAdapter.handleInvocation(request, connectionDetails);
    }

    /**
     * Method handleInvocation
     *
     * @param request
     * @return
     */
    public AbstractResponse handleInvocation(AbstractRequest request, Object connectionDetails) {

        if (getState() == STARTED) {
            return super.handleInvocation(request, connectionDetails);
        } else {
            return new InvocationExceptionThrown("Service is not started");
        }
    }
}
