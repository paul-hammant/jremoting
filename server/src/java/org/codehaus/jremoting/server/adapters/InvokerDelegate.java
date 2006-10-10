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
package org.codehaus.jremoting.server.adapters;


import java.util.Iterator;
import java.util.Vector;

import org.codehaus.jremoting.Contextualizable;
import org.codehaus.jremoting.client.Context;
import org.codehaus.jremoting.requests.Request;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.requests.CollectGarbage;
import org.codehaus.jremoting.requests.GroupedMethodRequest;
import org.codehaus.jremoting.requests.InvokeAsyncMethod;
import org.codehaus.jremoting.requests.InvokeFacadeMethod;
import org.codehaus.jremoting.requests.InvokeMethod;
import org.codehaus.jremoting.requests.ListInvokableMethods;
import org.codehaus.jremoting.requests.LookupService;
import org.codehaus.jremoting.requests.RequestConstants;
import org.codehaus.jremoting.requests.RetrieveStub;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.AuthenticationFailed;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.responses.FacadeArrayMethodInvoked;
import org.codehaus.jremoting.responses.FacadeMethodInvoked;
import org.codehaus.jremoting.responses.GarbageCollected;
import org.codehaus.jremoting.responses.InvokableMethods;
import org.codehaus.jremoting.responses.NoSuchSession;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.Ping;
import org.codehaus.jremoting.responses.ProblemResponse;
import org.codehaus.jremoting.responses.RequestFailed;
import org.codehaus.jremoting.responses.Service;
import org.codehaus.jremoting.responses.ServicesList;
import org.codehaus.jremoting.responses.ServicesSuspended;
import org.codehaus.jremoting.responses.MethodInvoked;
import org.codehaus.jremoting.responses.StubClass;
import org.codehaus.jremoting.responses.StubRetrievalFailed;
import org.codehaus.jremoting.server.Authenticator;
import org.codehaus.jremoting.server.MethodInvoker;
import org.codehaus.jremoting.server.ServerInvoker;
import org.codehaus.jremoting.server.ServerMonitor;
import org.codehaus.jremoting.server.ServerSideContextFactory;
import org.codehaus.jremoting.server.Session;
import org.codehaus.jremoting.server.StubRetrievalException;
import org.codehaus.jremoting.server.StubRetriever;
import org.codehaus.jremoting.server.monitors.ConsoleServerMonitor;
import org.codehaus.jremoting.server.transports.DefaultServerSideContextFactory;
import org.codehaus.jremoting.util.StubHelper;
import org.codehaus.jremoting.util.MethodNameHelper;

/**
 * Class InvokerDelegate
 *
 * @author Paul Hammant
 */
public class InvokerDelegate extends SessionAdapter implements ServerInvoker {

    private boolean suspended = false;
    private final StubRetriever stubRetriever;
    private final Authenticator authenticator;
    private final ServerMonitor serverMonitor;

    private final ServerSideContextFactory contextFactory;

    public InvokerDelegate(ServerMonitor serverMonitor, StubRetriever stubRetriever, Authenticator authenticator,
                                    ServerSideContextFactory contextFactory) {
        this.stubRetriever = stubRetriever;
        stubRetriever.setPublisher(this);
        this.authenticator = authenticator;
        this.serverMonitor = serverMonitor != null ? serverMonitor : new ConsoleServerMonitor();
        this.contextFactory = contextFactory != null ? contextFactory : new DefaultServerSideContextFactory();
    }

    public Response invoke(Request request, Object connectionDetails) {

        try {
            if (suspended) {
                return new ServicesSuspended();
            }

            // Method request is positioned first as
            // it is the one we want to be most speedy.
            if (request.getRequestCode() == RequestConstants.METHODREQUEST) {

                InvokeMethod invokeMethod = (InvokeMethod) request;
                setClientContext(invokeMethod);
                return doMethodRequest(invokeMethod, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.METHODFACADEREQUEST) {
                InvokeFacadeMethod invokeFacadeMethod = (InvokeFacadeMethod) request;
                setClientContext(invokeFacadeMethod);
                return doMethodFacadeRequest(invokeFacadeMethod, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.METHODASYNCREQUEST) {
                InvokeAsyncMethod invokeAsyncMethod = (InvokeAsyncMethod) request;
                setClientContext(invokeAsyncMethod);
                return doMethodAsyncRequest(invokeAsyncMethod, connectionDetails);

            } else if (request.getRequestCode() == RequestConstants.GCREQUEST) {

                return doGarbageCollectionRequest(request);

            } else if (request.getRequestCode() == RequestConstants.LOOKUPREQUEST) {
                return doLookupRequest(request);

            } else if (request.getRequestCode() == RequestConstants.CLASSREQUEST) {
                return doClassRequest(request);

            } else if (request.getRequestCode() == RequestConstants.OPENCONNECTIONREQUEST) {
                return doOpenConnectionRequest();

            } else if (request.getRequestCode() == RequestConstants.CLOSECONNECTIONREQUEST) {
                CloseConnection closeConnection = (CloseConnection) request;
                return doCloseConnectionRequest(closeConnection.getSessionID());

            } else if (request.getRequestCode() == RequestConstants.PINGREQUEST) {
                return doPing(request);

            } else if (request.getRequestCode() == RequestConstants.LISTSERVICESREQUEST) {
                return doServiceListRequest();

            } else if (request.getRequestCode() == RequestConstants.LISTMETHODSREQUEST) {
                return doListMethodsRequest(request);

            } else {
                return new RequestFailed("Unknown Request Type: " + request.getClass().getName());
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            if (request instanceof InvokeMethod) {
                String methd = ((InvokeMethod) request).getMethodSignature();
                getServerMonitor().unexpectedException(InvokerDelegate.class, "InvokerDelegate.invoke() NPE processing method " + methd, npe);
                throw new NullPointerException("Null pointer exception, processing method " + methd);
            } else {
                getServerMonitor().unexpectedException(InvokerDelegate.class, "InvokerDelegate.invoke() NPE", npe);
                throw npe;
            }
        }
    }

    protected synchronized ServerSideContextFactory getClientContextFactory() {
        return contextFactory;
    }

    private void setClientContext(Contextualizable request) {
        Long session = request.getSessionID();
        Context clientSideContext = request.getContext();

        // *always* happens before method invocations.
        getClientContextFactory().set(session, clientSideContext);

    }

    private Response doMethodFacadeRequest(InvokeFacadeMethod facadeRequest, Object connectionDetails) {

        if (!doesSessionExistAndRefreshItIfItDoes(facadeRequest.getSessionID()) && (connectionDetails == null || !connectionDetails.equals("callback"))) {
            return new NoSuchSession(facadeRequest.getSessionID());
        }

        String publishedThing = facadeRequest.getService() + "_" + facadeRequest.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }

        //if( !doesSessionExistAndRefreshItIfItDoes( facadeRequest.getSession() ) )
        //{
        //    return new ExceptionThrown(
        //        new InvocationException( "TODO - you dirty rat/hacker" ) );
        //}

        MethodInvoker methodInvoker = getMethodInvoker(publishedThing);
        Response response = methodInvoker.handleMethodInvocation(facadeRequest, connectionDetails);

        if (response instanceof ExceptionThrown) {
            return response;
        } else if (response instanceof ProblemResponse) {
            return response;
        } else if (response instanceof MethodInvoked) {
            Object methodResponse = ((MethodInvoked) response).getResponseObject();

            if (methodResponse == null) {
                return new FacadeMethodInvoked(null, null);    // null passing
            } else if (!methodResponse.getClass().isArray()) {
                return doMethodFacadeRequestNonArray(methodResponse, facadeRequest);
            } else {
                return doMethodFacadeRequestArray(methodResponse, facadeRequest);

            }
        } else {
            // unknown reply type from
            return new RequestFailed("Unknown Request Type: " + response.getClass().getName());
        }
    }

    /**
     * Do a method facade request, returning an array
     *
     * @param methodResponse     The array to process.
     * @param invokeFacadeMethod The request
     * @return The reply
     */
    private Response doMethodFacadeRequestArray(Object methodResponse, InvokeFacadeMethod invokeFacadeMethod) {
        Object[] beanImpls = (Object[]) methodResponse;
        Long[] refs = new Long[beanImpls.length];
        String[] objectNames = new String[beanImpls.length];

        if (!doesSessionExistAndRefreshItIfItDoes(invokeFacadeMethod.getSessionID())) {
            return new NoSuchSession(invokeFacadeMethod.getSessionID());
        }

        for (int i = 0; i < beanImpls.length; i++) {
            Object impl = beanImpls[i];
            MethodInvoker mainMethodInvoker = getMethodInvoker(StubHelper.formatServiceName(invokeFacadeMethod.getService()));

            objectNames[i] = MethodNameHelper.encodeClassName(mainMethodInvoker.getMostDerivedType(beanImpls[i]).getName());

            MethodInvoker methodInvoker2 = getMethodInvoker(StubHelper.formatServiceName(invokeFacadeMethod.getService(), objectNames[i]));

            if (methodInvoker2 == null) {
                return new NotPublished();
            }

            //TODO a decent ref number for main?
            if (beanImpls[i] == null) {
                refs[i] = null;
            } else {
                refs[i] = methodInvoker2.getOrMakeReferenceIDForBean(beanImpls[i]);

                Session session = getSession(invokeFacadeMethod.getSessionID());

                session.addBeanInUse(refs[i], beanImpls[i]);
            }
        }

        return new FacadeArrayMethodInvoked(refs, objectNames);
    }


    /**
     * Do a method facade request, returning things other that an array
     *
     * @param beanImpl           The returned object to process.
     * @param invokeFacadeMethod The request
     * @return The reply
     */
    private Response doMethodFacadeRequestNonArray(Object beanImpl, InvokeFacadeMethod invokeFacadeMethod) {

        if (!doesSessionExistAndRefreshItIfItDoes(invokeFacadeMethod.getSessionID())) {
            return new NoSuchSession(invokeFacadeMethod.getSessionID());
        }

        MethodInvoker mainMethodInvoker = getMethodInvoker(StubHelper.formatServiceName(invokeFacadeMethod.getService()));

        String objectName = MethodNameHelper.encodeClassName(mainMethodInvoker.getMostDerivedType(beanImpl).getName());

        MethodInvoker methodInvoker = getMethodInvoker(invokeFacadeMethod.getService() + "_" + objectName);

        if (methodInvoker == null) {
            return new NotPublished();
        }

        //if( !doesSessionExistAndRefreshItIfItDoes( invokeFacadeMethod.getSession() ) )
        //{
        //    return new ExceptionThrown(
        //        new InvocationException( "TODO - you dirty rat/hacker" ) );
        //}

        //TODO a decent ref number for main?
        Long newRef = methodInvoker.getOrMakeReferenceIDForBean(beanImpl);

        // make sure the bean is not garbage collected.
        Session sess = getSession(invokeFacadeMethod.getSessionID());

        sess.addBeanInUse(newRef, beanImpl);

        //long newRef2 = asih2.getOrMakeReferenceIDForBean(beanImpl);
        return new FacadeMethodInvoked(newRef, objectName);
    }

    private Response doMethodRequest(InvokeMethod invokeMethod, Object connectionDetails) {

        if (!doesSessionExistAndRefreshItIfItDoes(invokeMethod.getSessionID()) && (connectionDetails == null || !connectionDetails.equals("callback"))) {
            return new NoSuchSession(invokeMethod.getSessionID());
        }

        String publishedThing = invokeMethod.getService() + "_" + invokeMethod.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }

        MethodInvoker methodInvoker = getMethodInvoker(publishedThing);

        return methodInvoker.handleMethodInvocation(invokeMethod, connectionDetails);
    }

    private Response doMethodAsyncRequest(InvokeAsyncMethod methodRequest, Object connectionDetails) {

        Long session = methodRequest.getSessionID();
        if (!doesSessionExistAndRefreshItIfItDoes(session)) {
            return new NoSuchSession(session);
        }


        String publishedThing = methodRequest.getService() + "_" + methodRequest.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }


        Session sess = getSession(session);

        MethodInvoker methodInvoker = getMethodInvoker(publishedThing);

        GroupedMethodRequest[] requests = methodRequest.getGroupedRequests();
        for (GroupedMethodRequest rawRequest : requests) {
            methodInvoker.handleMethodInvocation(new InvokeMethod(methodRequest.getService(), methodRequest.getObjectName(), rawRequest.getMethodSignature(), rawRequest.getArgs(), methodRequest.getReferenceID(), methodRequest.getSessionID()), connectionDetails);
        }

        return new MethodInvoked();

    }


    private Response doLookupRequest(Request request) {
        LookupService lr = (LookupService) request;
        String publishedServiceName = lr.getService();

        if (!authenticator.checkAuthority(lr.getAuthentication(), publishedServiceName)) {
            return new AuthenticationFailed();
        }

        if (!isPublished(StubHelper.formatServiceName(publishedServiceName))) {
            return new NotPublished();
        }

        //TODO a decent ref number for main?
        return new Service((long) 0);
    }

    /**
     * Do a class request
     *
     * @param request The request
     * @return The reply
     */
    private Response doClassRequest(Request request) {
        RetrieveStub cr = (RetrieveStub) request;
        String publishedThing = cr.getService() + "_" + cr.getObjectName();

        try {
            return new StubClass(stubRetriever.getStubClassBytes(publishedThing));
        } catch (StubRetrievalException e) {
            return new StubRetrievalFailed(e.getMessage());
        }
    }

    /**
     * Do an OpenConnection request
     *
     * @return The reply.
     */
    private Response doOpenConnectionRequest() {
        Long session = newSession();
        String textToSign = authenticator == null ? "" : authenticator.getTextToSign();
        return new ConnectionOpened(textToSign, session);
    }


    private Response doCloseConnectionRequest(Long sessionID) {
        if (!sessionExists(sessionID)) {
            return new NoSuchSession(sessionID);
        } else {
            removeSession(sessionID);
            return new ConnectionClosed(sessionID);
        }
    }


    /**
     * Do a ListServices
     *
     * @return The reply
     */
    private Response doServiceListRequest() {
        Iterator iterator = getIteratorOfServices();
        Vector<String> vecOfServices = new Vector<String>();

        while (iterator.hasNext()) {
            final String item = (String) iterator.next();

            if (StubHelper.isService(item)) {
                vecOfServices.add(StubHelper.getServiceName(item));
            }
        }

        String[] listOfServices = new String[vecOfServices.size()];

        System.arraycopy(vecOfServices.toArray(), 0, listOfServices, 0, vecOfServices.size());

        return new ServicesList(listOfServices);
    }

    /**
     * Do a GarbageCollection Request
     *
     * @param request The request
     * @return The reply
     */
    private Response doGarbageCollectionRequest(Request request) {
        CollectGarbage gcr = (CollectGarbage) request;
        String publishedThing = gcr.getService() + "_" + gcr.getObjectName();

        if (!isPublished(publishedThing)) {
            return new NotPublished();
        }

        Long sessionID = gcr.getSessionID();
        if (doesSessionExistAndRefreshItIfItDoes(sessionID)) {
            Session sess = getSession(sessionID);
            if (gcr.getReferenceID() == null) {
                System.err.println("DEBUG- GC on missing referenceID -" + gcr.getReferenceID());
            } else {
                sess.removeBeanInUse(gcr.getReferenceID());
            }
        }
        return new GarbageCollected();
    }

    private Response doPing(Request request) {
        org.codehaus.jremoting.requests.Ping ping = (org.codehaus.jremoting.requests.Ping) request;
        super.doesSessionExistAndRefreshItIfItDoes(ping.getSession());
        return new Ping();
    }

    /**
     * Do a ListMethods Request
     *
     * @param request The request
     * @return The reply
     */
    private Response doListMethodsRequest(Request request) {
        ListInvokableMethods lReq = (ListInvokableMethods) request;
        String publishedThing = StubHelper.formatServiceName(lReq.getService());

        if (!isPublished(publishedThing)) {
            //Should it throw an exception back?
            return new InvokableMethods(new String[0]);
        }

        return new InvokableMethods(getMethodInvoker(publishedThing).getListOfMethods());
    }


    /**
     * Suspend an service
     */
    public void suspend() {
        suspended = true;
    }

    /**
     * Resume an service
     */
    public void resume() {
        suspended = false;
    }

    public synchronized ServerMonitor getServerMonitor() {
        return serverMonitor;
    }

}