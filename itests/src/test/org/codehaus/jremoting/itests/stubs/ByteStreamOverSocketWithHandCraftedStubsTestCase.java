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
package org.codehaus.jremoting.itests.stubs;


import org.codehaus.jremoting.client.context.ThreadLocalContextFactory;
import org.codehaus.jremoting.client.encoders.ByteStreamEncoding;
import org.codehaus.jremoting.client.factories.JRemotingClient;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestFacade;
import org.codehaus.jremoting.itests.TestFacade2;
import org.codehaus.jremoting.itests.TestFacade3;
import org.codehaus.jremoting.itests.TestFacadeImpl;
import org.codehaus.jremoting.itests.transports.AbstractHelloTestCase;
import org.codehaus.jremoting.itests.stubs.HandCraftedTestFacadeStubFactory;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.transports.socket.SocketServer;

import java.net.InetSocketAddress;


/**
 * Test showing hand crafted stubs.
 * The inner classes of HandCraftedTestFacadeStubFactory are what BCEL and ASM StubGeneraters should make.
 *
 * @author Paul Hammant
 */
public class ByteStreamOverSocketWithHandCraftedStubsTestCase extends AbstractHelloTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        server = new SocketServer((ServerMonitor) mockServerMonitor.proxy(), new InetSocketAddress(10333));
        testServer = new TestFacadeImpl();
        server.publish(testServer, "Hello", TestFacade.class, TestFacade3.class, TestFacade2.class);
        server.start();

        // Client side setup
        jremotingClient = new JRemotingClient(new SocketTransport(new ConsoleClientMonitor(), new ByteStreamEncoding(), new InetSocketAddress("localhost", 10333)),
                new ThreadLocalContextFactory(), new HandCraftedTestFacadeStubFactory());
        testClient = (TestFacade) jremotingClient.lookupService("Hello");

    }



    protected void tearDown() throws Exception {
        super.tearDown();
        testClient = null;
        System.gc();
        Thread.sleep(300);
        jremotingClient.close();
        server.stop();
    }


}