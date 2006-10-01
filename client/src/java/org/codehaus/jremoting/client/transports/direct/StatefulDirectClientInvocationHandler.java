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

import java.util.concurrent.ExecutorService;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionPinger;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.client.NoSuchReferenceException;
import org.codehaus.jremoting.client.NotPublishedException;
import org.codehaus.jremoting.client.transports.StatefulClientInvocationHandler;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.ServiceRequest;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.RequestConstants;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.NoSuchReference;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.ProblemResponse;
import org.codehaus.jremoting.responses.TryLater;

/**
 * Class StatefulDirectClientInvocationHandler
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class StatefulDirectClientInvocationHandler extends StatefulClientInvocationHandler {

    protected boolean methodLogging = false;
    protected long lastRealRequest = System.currentTimeMillis();


    public StatefulDirectClientInvocationHandler(ClientMonitor clientMonitor, ExecutorService executorService,
                                           ConnectionPinger connectionPinger, ClassLoader facadesClassLoader) {
        super(clientMonitor, executorService, connectionPinger, facadesClassLoader);
    }

    public Response handleInvocation(Request request) {

        if (request.getRequestCode() != RequestConstants.PINGREQUEST) {
            lastRealRequest = System.currentTimeMillis();
        }

        boolean again = true;
        Response response = null;
        int tries = 0;
        long start = 0;

        if (methodLogging) {
            start = System.currentTimeMillis();
        }

        while (again) {
            tries++;

            again = false;

            response = performInvocation(request);

            if (response instanceof ProblemResponse) {
                if (response instanceof TryLater) {
                    int millis = ((TryLater) response).getSuggestedDelayMillis();

                    clientMonitor.serviceSuspended(this.getClass(), request, tries, millis);

                    again = true;
                } else if (response instanceof NoSuchReference) {
                    throw new NoSuchReferenceException(((NoSuchReference) response).getReferenceID());
                } else if (response instanceof NotPublished) {
                    ServiceRequest pnr = (ServiceRequest) request;

                    throw new NotPublishedException(pnr.getService(), pnr.getObjectName());
                }
            }
        }

        if (methodLogging) {
            if (request instanceof InvokeMethod) {
                clientMonitor.methodCalled(this.getClass(), ((InvokeMethod) request).getMethodSignature(), System.currentTimeMillis() - start, "");
            }
        }

        return response;
    }

    protected boolean tryReconnect() {

        // blimey how do we reconnect this?
        throw new InvocationException("Direct connection broken, unable to reconnect.");
    }

    public long getLastRealRequest() {
        return lastRealRequest;
    }

    protected abstract Response performInvocation(Request request);
}