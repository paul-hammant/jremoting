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
package org.codehaus.jremoting.transports;

import org.codehaus.jremoting.client.factories.ClientSideStubFactory;
import org.codehaus.jremoting.client.factories.NullContextFactory;
import org.codehaus.jremoting.client.transports.direct.DirectClientInvocationHandler;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.transports.direct.DirectServer;
import org.codehaus.jremoting.test.TestInterface;
import org.codehaus.jremoting.test.TestInterface2;
import org.codehaus.jremoting.test.TestInterface3;
import org.codehaus.jremoting.test.TestInterfaceImpl;


/**
 * Test Direct Transport
 *
 * @author Paul Hammant
 */
public class DirectTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {

        // server side setup.
        server = new DirectServer();
        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        server.publish(testServer, "Hello", pd);
        server.start();

        // Client side setup
        factory = new ClientSideStubFactory(new DirectClientInvocationHandler(new ConsoleClientMonitor(), server), this.getClass().getClassLoader(), new NullContextFactory());
        testClient = (TestInterface) factory.lookupService("Hello");

    }

    public void testHelloCall() throws Exception {
        super.testHelloCall();  
    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        factory.close();
        server.stop();
    }


}
