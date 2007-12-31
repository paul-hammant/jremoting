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
package org.codehaus.jremoting.itests.transports;

import org.codehaus.jremoting.client.ContextFactory;
import org.codehaus.jremoting.client.factories.JRemotingServiceResolver;
import org.codehaus.jremoting.client.factories.StubsFromServer;
import org.codehaus.jremoting.client.monitors.ConsoleClientMonitor;
import org.codehaus.jremoting.client.transports.socket.SocketTransport;
import org.codehaus.jremoting.itests.TestInterface;
import org.codehaus.jremoting.itests.TestInterface2;
import org.codehaus.jremoting.itests.TestInterface3;
import org.codehaus.jremoting.itests.TestInterfaceImpl;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.factories.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.stubretrievers.BcelDynamicStubRetriever;
import org.codehaus.jremoting.server.transports.ByteStreamEncoding;
import org.codehaus.jremoting.server.transports.socket.SocketStreamServer;
import org.jmock.Mock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Test case which tests the proxies generated using BCEL generator
 *
 * @author Vinay Chandrasekharan
 */
public class BcelTestCase extends AbstractHelloTestCase {

    /**
     * Fetch the directory to store the classes and java source generated by the dynamic class retrievers
     */
    private String getClassGenDir()
    {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        return path;
    }

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        BcelDynamicStubRetriever stubRetriever = new BcelDynamicStubRetriever(this.getClass().getClassLoader());

        String class_gen_dir = getClassGenDir();
        stubRetriever.setClassGenDir(class_gen_dir);
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        server = new SocketStreamServer((ServerMonitor) mockServerMonitor.proxy(), stubRetriever, new NullAuthenticator(), new ByteStreamEncoding(), executorService, new ThreadLocalServerContextFactory(), 10201);

        testServer = new TestInterfaceImpl();
        PublicationDescription pd = new PublicationDescription(TestInterface.class, new Class[]{TestInterface3.class, TestInterface2.class});
        stubRetriever.generate("Hello223", pd, this.getClass().getClassLoader());
        server.publish(testServer, "Hello223", pd);
        server.start();

        // Client side setup
        Mock mock = mock(ContextFactory.class);
        mock.expects(atLeastOnce()).method("getClientContext").withNoArguments().will(returnValue(null));
        serviceResolver = new JRemotingServiceResolver(new SocketTransport(new ConsoleClientMonitor(),
                new org.codehaus.jremoting.client.transports.ByteStreamEncoding(), "127.0.0.1", 10201), (ContextFactory) mock.proxy(), new StubsFromServer());
        testClient = (TestInterface) serviceResolver.lookupService(TestInterface.class, "Hello223");

    }

    public void testHelloCall() throws Exception {
        super.testHelloCall();
    }

    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.sleep(300);
        serviceResolver.close();
        server.stop();
    }


}
