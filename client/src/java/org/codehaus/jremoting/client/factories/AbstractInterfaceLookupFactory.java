/* ====================================================================
 * Copyright 2005 JRemoting Committers
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

import org.codehaus.jremoting.api.ConnectionException;
import org.codehaus.jremoting.client.InterfaceLookup;
import org.codehaus.jremoting.client.InterfaceLookupFactory;

import java.util.Vector;

/**
 * Class AbstractInterfaceLookupFactory
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public abstract class AbstractInterfaceLookupFactory implements InterfaceLookupFactory {

    private Vector factories = new Vector();

    protected void addFactory(String factoryStringPrefix, InterfaceLookupFactory factory) {
        factories.add(new Factory(factoryStringPrefix, factory));
    }

    /**
     * Method getInterfaceLookup
     *
     * @param factoryString
     * @return
     * @throws ConnectionException
     */
    public final InterfaceLookup getInterfaceLookup(String factoryString, boolean optimize) throws ConnectionException {
        return getInterfaceLookup(factoryString, this.getClass().getClassLoader(), optimize);
    }

    /**
     * Method getInterfaceLookup
     *
     * @param factoryString
     * @param interfacesClassLoader
     * @return
     * @throws ConnectionException
     */
    public InterfaceLookup getInterfaceLookup(String factoryString, ClassLoader interfacesClassLoader, boolean optimize) throws ConnectionException {

        for (int i = 0; i < factories.size(); i++) {
            Factory factory = (Factory) factories.elementAt(i);

            if (factoryString.startsWith(factory.factoryStringPrefix)) {

                InterfaceLookup interfaceLookup = factory.interfaceLookupFactory.getInterfaceLookup(factoryString, interfacesClassLoader, optimize);
                return interfaceLookup;
            }
        }

        return null;
    }

    /**
     * Class Factory
     *
     * @author Paul Hammant
     * @version $Revision: 1.2 $
     */
    private class Factory {

        private String factoryStringPrefix;
        private InterfaceLookupFactory interfaceLookupFactory;

        /**
         * Constructor Factory
         *
         * @param factoryStringPrefix
         * @param interfaceLookupFactory
         */
        public Factory(String factoryStringPrefix, InterfaceLookupFactory interfaceLookupFactory) {
            this.factoryStringPrefix = factoryStringPrefix;
            this.interfaceLookupFactory = interfaceLookupFactory;
        }
    }


}