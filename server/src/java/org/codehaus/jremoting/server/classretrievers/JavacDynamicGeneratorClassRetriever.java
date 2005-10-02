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
package org.codehaus.jremoting.server.classretrievers;

/**
 * Class JavacDynamicGeneratorClassRetriever
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class JavacDynamicGeneratorClassRetriever extends AbstractDynamicGeneratorClassRetriever {
    /**
     * Contruct a Dynamic Class Generator with a classloader pointing to the generator classes.
     *
     * @param classLoader the classloader in which the proxy generater will be found.
     */
    public JavacDynamicGeneratorClassRetriever(ClassLoader classLoader) {
        super(classLoader, "org.codehaus.jremoting.tools.generator.ProxyGeneratorImpl");
    }

    /**
     * Contruct a Dynamic Class Generator using the classes own classloader.
     */
    public JavacDynamicGeneratorClassRetriever() {
        super(JavacDynamicGeneratorClassRetriever.class.getClassLoader(), "org.codehaus.jremoting.tools.generator.ProxyGeneratorImpl");
    }
}