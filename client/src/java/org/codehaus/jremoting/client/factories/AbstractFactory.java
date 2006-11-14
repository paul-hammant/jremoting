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
package org.codehaus.jremoting.client.factories;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.codehaus.jremoting.ConnectionException;
import org.codehaus.jremoting.authentications.Authentication;
import org.codehaus.jremoting.client.*;
import org.codehaus.jremoting.requests.CloseConnection;
import org.codehaus.jremoting.requests.ListServices;
import org.codehaus.jremoting.requests.LookupService;
import org.codehaus.jremoting.requests.OpenConnection;
import org.codehaus.jremoting.responses.Response;
import org.codehaus.jremoting.responses.ConnectionClosed;
import org.codehaus.jremoting.responses.ConnectionOpened;
import org.codehaus.jremoting.responses.ExceptionThrown;
import org.codehaus.jremoting.responses.NotPublished;
import org.codehaus.jremoting.responses.Service;
import org.codehaus.jremoting.responses.ServicesList;
import org.codehaus.jremoting.util.FacadeRefHolder;


/**
 * Class AbstractFactory
 *
 * @author Paul Hammant
 * @author Peter Royal <a href="mailto:proyal@managingpartners.com">proyal@managingpartners.com</a>
 * @author Mauro Talevi
 */
public abstract class AbstractFactory implements Factory {

    private static final int STUB_PREFIX_LENGTH = org.codehaus.jremoting.util.StubHelper.getStubPrefixLength();
    protected final ClientInvoker clientInvoker;
    private final ContextFactory contextFactory;
    protected final HashMap<Long,WeakReference<Object>> refObjs = new HashMap<Long, WeakReference<Object>>();
    private transient String textToSign;
    protected final Long sessionID;
    private StubRegistry stubRegistry;


    public AbstractFactory(final ClientInvoker clientInvoker, ContextFactory contextFactory) throws ConnectionException {
        this.clientInvoker = clientInvoker;
        this.contextFactory = contextFactory;

        clientInvoker.initialize();

        Response response = clientInvoker.invoke(new OpenConnection());

        if (response instanceof ConnectionOpened) {
            textToSign = ((ConnectionOpened) response).getTextToSign();
            sessionID = ((ConnectionOpened) response).getSessionID();
        } else {

            throw new ConnectionException("Setting of host context blocked for reasons of unknown, server-side response: (" + response.getClass().getName() + ")");
        }

        stubRegistry = new StubRegistry() {

            public void registerReferenceObject(Object instance, Long referenceID) {
                synchronized (this) {
                    refObjs.put(referenceID, new WeakReference<Object>(instance));
                }
            }

            public Object getInstance(Long referenceID) {
                WeakReference<Object> wr;
                synchronized (this) {
                    wr = refObjs.get(referenceID);
                }
                if (wr == null) {
                    return null;
                }
                Object obj = wr.get();

                if (obj == null) {
                    refObjs.remove(referenceID);
                }
                return obj;
            }

            public Object getInstance(String publishedServiceName, String objectName, StubHelper stubHelper) throws ConnectionException {
                return AbstractFactory.this.getInstance(publishedServiceName, objectName, stubHelper);
            }

            public void marshallCorrection(String remoteObjectName, String methodSignature, Object[] args, Class[] argClasses) {
                for (int i = 0; i < argClasses.length; i++) {
                    Class argClass = argClasses[i];
                    if (argClass == null) {
                        continue;
                    }
                    //All remote references implement Proxy interface
                    if (args[i] instanceof Proxy) {
                        Proxy proxy = (Proxy) args[i];

                        if (getReferenceID(proxy) != null) {
                            //The stripping STUB_PREFIX from the proxy names generated by StubGenerator
                            String objName = args[i].getClass().getName().substring(STUB_PREFIX_LENGTH);

                            args[i] = makeFacadeRefHolder(proxy, objName);
                        }
                    } else // Let the specific Invokers be given the last chance to modify the arguments.
                    {
                        args[i] = clientInvoker.resolveArgument(remoteObjectName, methodSignature, argClasses[i], args[i]);
                    }
                }
            }
        };

    }

    public Object lookupService(String publishedServiceName, Authentication authentication) throws ConnectionException {

        Response ar = clientInvoker.invoke(new LookupService(publishedServiceName, authentication, sessionID));

        if (ar instanceof NotPublished) {
            throw new ConnectionException("Service '" + publishedServiceName + "' not published");
        } else if (ar instanceof ExceptionThrown) {
            ExceptionThrown er = (ExceptionThrown) ar;
            Throwable t = er.getResponseException();

            if (t instanceof ConnectionException) {
                throw (ConnectionException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new ConnectionException("Problem doing lookup on service [exception: " + t.getMessage() + "]");
            }
        }

        Service lr = (Service) ar;
        DefaultStubHelper baseObj = new DefaultStubHelper(stubRegistry, clientInvoker,
                contextFactory, publishedServiceName, "Main", lr.getReferenceID(), sessionID);
        Object retVal = getInstance(publishedServiceName, "Main", baseObj);

        baseObj.registerInstance(retVal);

        return retVal;
    }

    protected abstract Class getStubClass(String publishedServiceName, String objectName) throws ConnectionException, ClassNotFoundException;

    public final Long getReferenceID(Proxy obj) {
        return obj.codehausRemotingGetReferenceID(this);
    }

    public final Object lookupService(String publishedServiceName) throws ConnectionException {
        return lookupService(publishedServiceName, null);
    }

    public String getTextToSignForAuthentication() {
        return textToSign;
    }

    public String[] listServices() {
        Response ar = clientInvoker.invoke(new ListServices());
        return ((ServicesList) ar).getServices();
    }

    public boolean hasService(String publishedServiceName) {
        final String[] services = listServices();
        for (String service : services) {
            if (service.equals(publishedServiceName)) {
                return true;
            }
        }
        return false;
    }

    private FacadeRefHolder makeFacadeRefHolder(Proxy obj, String objectName) {
        Long refID = getReferenceID(obj);
        return new FacadeRefHolder(refID, objectName);
    }


    protected Object getInstance(String publishedServiceName, String objectName, StubHelper stubHelper) throws ConnectionException {

        try {
            Class stubClass = getStubClass(publishedServiceName, objectName);
            Constructor[] constructors = stubClass.getConstructors();
            return constructors[0].newInstance(new Object[]{stubHelper});
        } catch (InvocationTargetException ite) {
            throw new ConnectionException("Generated class not instantiated : " + ite.getTargetException().getMessage());
        } catch (ClassNotFoundException cnfe) {
            throw new ConnectionException("Generated class not found during lookup : " + cnfe.getMessage());
        } catch (InstantiationException ie) {
            throw new ConnectionException("Generated class not instantiable during lookup : " + ie.getMessage());
        } catch (IllegalAccessException iae) {
            throw new ConnectionException("Illegal access to generated class during lookup : " + iae.getMessage());
        }
    }

    public void close() {
        CloseConnection request = new CloseConnection(sessionID);
        ConnectionClosed closed = (ConnectionClosed) clientInvoker.invoke(request);
        clientInvoker.close();
    }



}
