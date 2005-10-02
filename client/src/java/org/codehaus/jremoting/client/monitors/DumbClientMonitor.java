/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
package org.codehaus.jremoting.client.monitors;

import org.codehaus.jremoting.client.ClientMonitor;
import org.codehaus.jremoting.client.ConnectionClosedException;
import org.codehaus.jremoting.client.InvocationException;
import org.codehaus.jremoting.commands.Request;

import java.io.IOException;

/**
 * Class DumbClientMonitor
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public class DumbClientMonitor implements ClientMonitor {

    /**
     * Creates a new DumbClientMonitor.
     */
    public DumbClientMonitor() {
    }

    /**
     * Method methodCalled
     *
     * @param methodSignature
     * @param duration
     */
    public void methodCalled(Class clazz, final String methodSignature, final long duration, String annotation) {
        // do mothing in default impl, could do logging.
    }

    /**
     * Method methodLogging tests if the implementing class intends to do method logging.
     *
     * @return
     */
    public boolean methodLogging() {
        return false;
    }

    /**
     * Method serviceSuspended
     *
     * @param request
     * @param attempt
     * @param suggestedWaitMillis
     */
    public void serviceSuspended(Class clazz, final Request request, final int attempt, final int suggestedWaitMillis) {
        throw new InvocationException("Service suspended");
    }

    /**
     * Method serviceAbend
     *
     * @param attempt
     */
    public void serviceAbend(Class clazz, int attempt, IOException cause) {
        throw new InvocationException("JRemoting Service has Abended.");
    }

    public void invocationFailure(Class clazz, String name, InvocationException ie) {
    }

    public void unexpectedClosedConnection(Class clazz, String name, ConnectionClosedException cce) {
    }

    public void unexpectedInterruption(Class clazz, String name, InterruptedException ie) {
    }

}
