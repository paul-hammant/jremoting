/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
 * Portions copyright 2001 - 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.codehaus.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.jremoting.client.transports.direct;

import org.codehaus.jremoting.JRemotingException;

import java.util.concurrent.ScheduledExecutorService;
import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.server.ServerMarshalledInvocationHandler;
import org.codehaus.jremoting.util.SerializationHelper;

/**
 * Class DirectMarshalledClientInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public final class DirectMarshalledClientInvocationHandler extends StatefulDirectClientInvocationHandler {

    private ServerMarshalledInvocationHandler invocationHandler;

    public DirectMarshalledClientInvocationHandler(ClientMonitor clientMonitor, ScheduledExecutorService executorService,
                                             ConnectionPinger connectionPinger, ServerMarshalledInvocationHandler invocationHandler,
                                             ClassLoader facadesClassLoader) {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader);
        this.invocationHandler = invocationHandler;
    }

    protected Response performInvocation(Request request) {

        try {
            byte[] serRequest = SerializationHelper.getBytesFromInstance(request);
            byte[] serResponse = invocationHandler.handleInvocation(serRequest, null);

            Object instanceFromBytes = SerializationHelper.getInstanceFromBytes(serResponse, getFacadesClassLoader());
            return (Response) instanceFromBytes;
        } catch (ClassNotFoundException cnfe) {
            String msg = "Some ClassNotFoundException on client side";
            clientMonitor.classNotFound(DirectMarshalledClientInvocationHandler.class, msg, cnfe);
            throw new JRemotingException(msg, cnfe);
        }
    }

}
