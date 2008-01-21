package org.codehaus.jremoting.server.adapters;

import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.ListServices;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.RetrieveStub;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.RequestFailed;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.ServicesList;
import org.codehaus.jremoting.responses.ServicesSuspended;
import org.codehaus.jremoting.responses.StubRetrievalFailed;
import org.codehaus.jremoting.server.PublicationException;
import org.codehaus.jremoting.server.authenticators.NullAuthenticator;
import org.codehaus.jremoting.server.context.ThreadLocalServerContextFactory;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.stubretrievers.RefusingStubRetriever;
import org.codehaus.jremoting.server.transports.direct.DirectMarshalledServer;
import org.jmock.MockObjectTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class DirectMarshalledTestCase extends MockObjectTestCase {

    private DefaultInvocationHandler invocationHandler;
    private DirectMarshalledServer server;
    HashMap impl = new HashMap();

    protected void setUp() throws Exception {
        invocationHandler = new DefaultInvocationHandler(new ConsoleServerMonitor(), new RefusingStubRetriever(), new NullAuthenticator(), new ThreadLocalServerContextFactory());
        server = new DirectMarshalledServer(new ConsoleServerMonitor(), Executors.newScheduledThreadPool(10), invocationHandler);
    }

    public void testPublishAndUnpublish() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "foo", Map.class);
        putTestEntry();
        server.unPublish(impl, "foo");
        Response resp = serDeSerResponse(putTestEntry());
        assertTrue(resp instanceof NotPublished);
    }

    public void testPublishAndUnpublishDoes() throws PublicationException {
        server.publish(impl, "foo", Map.class);
        server.unPublish(impl, "foo");
        try {
            server.unPublish(impl, "foo");
            fail("should have barfed");
        } catch (PublicationException e) {
        }
    }

    public void testCantUnpublishSomethingThatWasNeverPublished() throws PublicationException {
        try {
            server.unPublish(impl, "foo");
            fail("should have barfed");
        } catch (PublicationException e) {
        }
    }

    public void testCantPublishServiceTwice() throws PublicationException {
        server.publish(impl, "foo", Map.class);
        try {
            server.publish(impl, "foo", Map.class);
            fail("should have barfed");
        } catch (PublicationException e) {
            assertEquals("Service 'foo' already published",e.getMessage());
        }
    }

    public void testPublishAndRePublish() throws PublicationException, IOException, ClassNotFoundException {
        HashMap impl2 = new HashMap();
        server.publish(impl, "foo", Map.class);
        server.replacePublished(impl, "foo", impl2);
        putTestEntry();
        assertEquals("2", impl2.get("1"));
        assertNull(impl.get("1"));
    }

    private Response putTestEntry() throws IOException, ClassNotFoundException {
        ConnectionOpened co = (ConnectionOpened) invocationHandler.invoke(new OpenConnection(), "");
        Request request = new InvokeMethod("foo", "Main", "put(java.lang.Object, java.lang.Object)", new Object[]{"1", "2"}, (long) 0, co.getSessionID());
        return invocationHandler.invoke(serDeSerRequest(request), "");
    }

    private Request serDeSerRequest(Request request) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (Request) ois.readObject();
    }

    private Response serDeSerResponse(Response response) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        oos.flush();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (Response) ois.readObject();
    }

    public void testPublishAndSuspendBlocksOperations() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "foo", Map.class);
        server.suspend();
        assertTrue(serDeSerResponse(invocationHandler.invoke(serDeSerRequest(new OpenConnection()), "")) instanceof ServicesSuspended);
    }

    public void testPublishAndSuspendAndResumeDoesNotBlockOperation() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "foo", Map.class);
        server.suspend();
        server.resume();
        assertTrue(serDeSerResponse(invocationHandler.invoke(serDeSerRequest(new OpenConnection()), "")) instanceof ConnectionOpened);
    }

    public void testStubRetrievalFailsWhenItsAppropriate() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "foo", Map.class);
        Response response = serDeSerResponse(invocationHandler.invoke(serDeSerRequest(new RetrieveStub("foo", "Main")), ""));
        assertTrue(response instanceof StubRetrievalFailed);
    }

    public void testRequestFailsOnUnknownRequestType() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "foo", Map.class);
        Response response = serDeSerResponse(invocationHandler.invoke(serDeSerRequest(new DirectMarshalledTestCase.MyRequest()), ""));
        assertTrue(response instanceof RequestFailed);
        assertEquals("Unknown Request Type: org.codehaus.jremoting.server.adapters.DirectMarshalledTestCase$MyRequest", ((RequestFailed) response).getFailureReason());
    }

    public void testListServicesRespondsAppropriately() throws PublicationException, IOException, ClassNotFoundException {
        server.publish(impl, "foo", Map.class);
        Response response = invocationHandler.invoke(serDeSerRequest(new ListServices()), "");
        assertTrue(serDeSerResponse(response) instanceof ServicesList);
        assertEquals(1, ((ServicesList ) response).getServices().length);
        assertEquals("foo", ((ServicesList ) response).getServices()[0]);
    }

    public void testListServicesRespondsAppropriatelyWhenThereAreNone() throws PublicationException, IOException, ClassNotFoundException {
        Response response = invocationHandler.invoke(serDeSerRequest(new ListServices()), "");
        assertTrue(serDeSerResponse(response) instanceof ServicesList);
        assertEquals(0, ((ServicesList ) response).getServices().length);
    }

    private static class MyRequest extends Request {
        private static final long serialVersionUID = 4012541139227639079L;

        public MyRequest() {
        }

        public int getRequestCode() {
            return 999;
        }
    }
}
