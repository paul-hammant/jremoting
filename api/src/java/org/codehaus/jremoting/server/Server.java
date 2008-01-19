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
package org.codehaus.jremoting.server;


/**
 * Interface Server
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public interface Server extends Publisher, InvocationHandler {

    /**
     * The state of the system while shutting down.
     */
    String STOPPING = "SHUTTING_DOWN";

    /**
     * The state of the starting system.
     */
    String STARTING = "STARTING";

    /**
     * The state of the starting system.
     */
    String STARTED = "STARTED";

    /**
     * The state of the system when stopped
     */
    String STOPPED = "STOPPED";

    /**
     * The state of the un started system.
     */
    String UNSTARTED = "UNSTARTED";

    /**
     * Suspend publishing
     */
    void suspend();

    /**
     * Resume publishing
     */
    void resume();

    /**
     * Start publishing
     *
     */
    void start();

    /**
     * Stop publishing
     */
    void stop();
    
    /**
     * Get the state for teh server.
     *
     * @return the state.
     */
    public String getState();


}
