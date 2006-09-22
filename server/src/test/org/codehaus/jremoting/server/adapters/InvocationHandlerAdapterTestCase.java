/* ====================================================================
 * Copyright 2005-2006 JRemoting Committers
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

package org.codehaus.jremoting.server.adapters;

import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.NoSuchSession;
import org.codehaus.jremoting.responses.AbstractResponse;
import junit.framework.TestCase;

public class InvocationHandlerAdapterTestCase extends TestCase {

    public void testOpenConnection() {
        InvocationHandlerAdapter invocationHandle = new InvocationHandlerAdapter(null, null, null, null);
        ConnectionOpened connectionOpened = (ConnectionOpened) invocationHandle.handleInvocation(new OpenConnection(), new Object());
        assertNotNull(connectionOpened);
        assertNotNull(connectionOpened.getSessionID());
    }

    public void testCloseConnectionAfterOpenConnection() {
        InvocationHandlerAdapter invocationHandle = new InvocationHandlerAdapter(null, null, null, null);
        ConnectionOpened connectionOpened = (ConnectionOpened) invocationHandle.handleInvocation(new OpenConnection(), new Object());
        ConnectionClosed connectionClosed = (ConnectionClosed) invocationHandle.handleInvocation(new CloseConnection(connectionOpened.getSessionID()), new Object());
        assertNotNull(connectionClosed);
        assertNotNull(connectionClosed.getSessionID());
        assertEquals(connectionClosed.getSessionID(), connectionOpened.getSessionID());

    }

    public void testCloseConnectionErrorsOnBogusSession() {
        InvocationHandlerAdapter invocationHandle = new InvocationHandlerAdapter(null, null, null, null);
        AbstractResponse abstractResponse = invocationHandle.handleInvocation(new CloseConnection(new Long(123)), new Object());
        assertTrue(abstractResponse instanceof NoSuchSession);
        NoSuchSession noSuchSession = (NoSuchSession) abstractResponse;
        assertNotNull(noSuchSession);
        assertNotNull(noSuchSession.getSessionID());
        assertEquals(noSuchSession.getSessionID(), new Long(123));

    }


}