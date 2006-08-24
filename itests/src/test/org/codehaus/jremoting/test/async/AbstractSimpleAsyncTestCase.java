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
package org.codehaus.jremoting.test.async;

import junit.framework.TestCase;
import org.codehaus.jremoting.api.DefaultThreadPool;
import org.codehaus.jremoting.client.Factory;
import org.codehaus.jremoting.client.factories.ServerSideClassFactory;
import org.codehaus.jremoting.client.transports.socket.SocketCustomStreamHostContext;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.server.authenticators.DefaultAuthenticator;
import org.codehaus.jremoting.server.classretrievers.AbstractDynamicGeneratorClassRetriever;
import org.codehaus.jremoting.server.monitors.NullServerMonitor;
import org.codehaus.jremoting.server.transports.DefaultServerSideClientContextFactory;
import org.codehaus.jremoting.server.transports.socket.CompleteSocketCustomStreamServer;

import java.io.File;

public abstract class AbstractSimpleAsyncTestCase extends TestCase {

    AsyncTestImpl asyncTestImpl;
    AsyncTest testClient;
    Factory factory;
    CompleteSocketCustomStreamServer server;

    public AbstractSimpleAsyncTestCase(String name) {
        super(name);
    }

    protected abstract AbstractDynamicGeneratorClassRetriever getAbstractDynamicGeneratorClassRetriever(ClassLoader cl);

    /**
     * Fetch the directory to store the classes and java source generated by the dynamic class retrievers
     */
    private String getClassGenDir() {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        return path;
    }

    protected void setUp() throws Exception {
        super.setUp();

        // server side setup.
        AbstractDynamicGeneratorClassRetriever cr = getAbstractDynamicGeneratorClassRetriever(this.getClass().getClassLoader());

        // Fetch the dir to place the generated classes from the System properties
        String class_gen_dir = getClassGenDir();

        cr.setClassGenDir(class_gen_dir);
        cr.setSrcGenDir(class_gen_dir);

        DefaultServerSideClientContextFactory ccf = new DefaultServerSideClientContextFactory();
        server = new CompleteSocketCustomStreamServer(cr, new DefaultAuthenticator(), new NullServerMonitor(), new DefaultThreadPool(), ccf, 11003);
        asyncTestImpl = new AsyncTestImpl();
        PublicationDescription pd = new PublicationDescription();
        pd.addInterfaceToExpose(new PublicationDescriptionItem(AsyncTest.class, new String[]{"setOne(java.lang.String)", "setTwo(java.lang.String)", "setThree(java.lang.String)", }, new String[]{"fire()"}, new String[]{"whoa()"}));
        cr.generate("AsyncTest", pd, this.getClass().getClassLoader());
        server.publish(asyncTestImpl, "AsyncTest", pd);
        server.start();

        // Client side setup
        factory = new ServerSideClassFactory(new SocketCustomStreamHostContext("127.0.0.1", 11003), false);
        testClient = (AsyncTest) factory.lookup("AsyncTest");

        // just a kludge for unit testing given we are intrinsically dealing with
        // threads, JRemoting being a client/server thing
        Thread.yield();
    }

    public void testSimpleAsync() throws Exception {

        testClient.setOne("one");
        testClient.setTwo("two");
        testClient.setThree("three");

        assertNull("Field 'One' should be null", asyncTestImpl.one);
        assertNull("Field 'Two' should be null", asyncTestImpl.two);
        assertNull("Field 'Tree' should be null", asyncTestImpl.three);
        assertFalse("Field 'Fire' should be false", asyncTestImpl.fired);

        testClient.fire();

        assertNotNull("Field 'One' should not be null", asyncTestImpl.one);
        assertNotNull("Field 'Two' should not be null", asyncTestImpl.two);
        assertNotNull("Field 'Tree' should not be null", asyncTestImpl.three);
        assertTrue("Field 'Fire' should not be false", asyncTestImpl.fired);

    }

    public void testRollback() throws Exception {

        testClient.setOne("111");

        assertNull("Field 'One' should be null #1", asyncTestImpl.one);

        testClient.whoa();
        testClient.fire();

        assertNull("Field 'One' should be null #2", asyncTestImpl.one);
        assertTrue("Field 'Whoa' should not be false", asyncTestImpl.whoa);
        assertTrue("Field 'Fire' should not be false", asyncTestImpl.fired);

        testClient.setOne("222");
        testClient.fire();

        assertNotNull("Field 'One' should not be null", asyncTestImpl.one);

    }


    protected void tearDown() throws Exception {
        testClient = null;
        System.gc();
        Thread.yield();
        factory.close();
        Thread.yield();
        server.stop();
        Thread.yield();
        server = null;
        asyncTestImpl = null;
        super.tearDown();
    }


}
