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
package org.codehaus.jremoting.client.factories;

import org.codehaus.jremoting.client.ClientInvocationHandler;
import org.codehaus.jremoting.client.HostContext;

/**
 * Class AbstractHostContext
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractHostContext implements HostContext {

    protected final ClientInvocationHandler invocationHandler;

    /**
     * Constructor AbstractHostContext
     *
     * @param clientInvocationHandler
     */
    public AbstractHostContext(ClientInvocationHandler clientInvocationHandler) {
        invocationHandler = clientInvocationHandler;
    }

    /**
     * Method getInvocationHandler
     *
     * @return
     */
    public ClientInvocationHandler getInvocationHandler() {
        return invocationHandler;
    }


    public HostContext makeSameVmHostContext(String key) {
        throw new UnsupportedOperationException();
    }

}