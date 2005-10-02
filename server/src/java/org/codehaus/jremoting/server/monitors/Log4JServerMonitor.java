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
package org.codehaus.jremoting.server.monitors;

import org.apache.log4j.Logger;
import org.codehaus.jremoting.api.BadConnectionException;
import org.codehaus.jremoting.server.ServerMonitor;

import java.io.IOException;

/**
 * Class Log4JServerMonitor
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class Log4JServerMonitor implements ServerMonitor {

    public void closeError(Class clazz, String s, IOException e) {
        Logger logger = Logger.getLogger(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("<closeError>" + s, e);
        }
    }

    public void badConnection(Class clazz, String s, BadConnectionException bce) {
        Logger logger = Logger.getLogger(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("<badConnection>" + s, bce);
        }
    }

    public void classNotFound(Class clazz, ClassNotFoundException e) {
        Logger logger = Logger.getLogger(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("<classNotFound>", e);
        }
    }

    public void unexpectedException(Class clazz, String s, Exception e) {
        Logger logger = Logger.getLogger(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("<unexpectedException>" + s, e);
        }
    }

    public void stopServerError(Class clazz, String s, Exception e) {
        Logger logger = Logger.getLogger(clazz);
        logger.error("<stopServerError>" + s, e);
    }
}