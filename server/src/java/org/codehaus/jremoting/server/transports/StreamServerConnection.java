/* ====================================================================
 * Copyright 2005 JRemoting Committers
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
package org.codehaus.jremoting.server.transports;

import org.codehaus.jremoting.api.BadConnectionException;
import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.commands.EndConnectionResponse;
import org.codehaus.jremoting.commands.InvocationExceptionResponse;
import org.codehaus.jremoting.commands.Request;
import org.codehaus.jremoting.commands.Response;
import org.codehaus.jremoting.server.ServerConnection;
import org.codehaus.jremoting.server.ServerMonitor;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;

/**
 * Class StreamServerConnection
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class StreamServerConnection implements Runnable, ServerConnection {

    /**
     * The Abstract Server
     */
    private AbstractServer abstractServer;

    /**
     * End connections
     */
    private boolean endConnection = false;

    /**
     * The Sever Stream Read Writer.
     */
    private AbstractServerStreamReadWriter readWriter;

    private ServerMonitor serverMonitor;

    /**
     * Construct a StreamServerConnection
     *
     * @param abstractServer The Abstract Server handling requests
     * @param readWriter     The Read Writer.
     */
    public StreamServerConnection(AbstractServer abstractServer, AbstractServerStreamReadWriter readWriter) {
        this.abstractServer = abstractServer;
        this.readWriter = readWriter;
    }


    public void setServerMonitor(ServerMonitor serverMonitor) {
        this.serverMonitor = serverMonitor;
    }

    public ServerMonitor getServerMonitor() {
        return serverMonitor;
    }

    /**
     * Method run
     */
    public void run() {

        abstractServer.connectionStart(this);

        try {
            readWriter.initialize();

            boolean more = true;
            Request request = null;
            Response response = null;

            while (more) {
                try {
                    if (request != null) {
                        response = abstractServer.handleInvocation(request, readWriter.getConnectionDetails());
                    }

                    request = readWriter.writeReplyAndGetRequest(response);

                    // http://developer.java.sun.com/developer/bugParade/bugs/4499841.html
                    // halves the performance though.
                    //oOS.reset();
                    if (endConnection) {
                        response = new EndConnectionResponse();
                        more = false;
                    }
                } catch (BadConnectionException bce) {
                    more = false;
                    serverMonitor.badConnection(this.getClass(), "StreamServerConnection.run(): Bad connection #0", bce);
                    readWriter.close();
                } catch (ConnectionException ace) {
                    more = false;
                    serverMonitor.unexpectedException(this.getClass(), "StreamServerConnection.run(): Unexpected ConnectionException #0", ace);
                    readWriter.close();
                } catch (IOException ioe) {
                    more = false;

                    if (ioe instanceof EOFException) {
                        readWriter.close();
                    } else if (isSafeEnd(ioe)) {

                        // TODO implement implementation indepandant logger
                        readWriter.close();
                    } else {
                        serverMonitor.unexpectedException(this.getClass(), "StreamServerConnection.run(): Unexpected IOE #1", ioe);
                        readWriter.close();
                    }
                } catch (NullPointerException npe) {
                    serverMonitor.unexpectedException(this.getClass(), "StreamServerConnection.run(): Unexpected NPE", npe);
                    response = new InvocationExceptionResponse("NullPointerException on server: " + npe.getMessage());
                }
            }
        } catch (IOException e) {
            serverMonitor.unexpectedException(this.getClass(), "StreamServerConnection.run(): Unexpected IOE #2", e);
        } catch (ClassNotFoundException e) {
            serverMonitor.classNotFound(this.getClass(), e);
        }

        abstractServer.connectionCompleted(this);
    }

    private boolean isSafeEnd(IOException ioe) {
        if ((ioe instanceof SocketException) || ioe.getClass().getName().equals("java.net.SocketTimeoutException") // 1.3 safe
                || (ioe instanceof InterruptedIOException)) {
            return true;
        }

        if (ioe.getMessage() != null) {
            String msg = ioe.getMessage();
            if (msg.equals("Write end dead") | msg.equals("Pipe broken") | msg.equals("Pipe closed")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method endConnection
     */
    public void endConnection() {
        endConnection = true;
        readWriter.close();
    }

    /**
     * Method killConnection
     */
    protected abstract void killConnection();

}