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
package org.codehaus.jremoting.server;

/**
 * Interface ProxyGenerator
 *
 * @author Paul Hammant
 * @version * $Revision: 1.2 $
 */
public interface DynamicProxyGenerator {

    /**
     * Generate a proxy.
     *
     * @param asName            the name of the generated proxy.  As used in lookup.
     * @param interfaceToExpose the princial lookupable interface
     * @param classLoader       - classloader containing all needed for proxy generation
     * @throws PublicationException if there is a problem publishing
     */
    void generate(String asName, Class interfaceToExpose, ClassLoader classLoader) throws PublicationException;

    /**
     * Generate a proxy.
     *
     * @param asName                 the name of the generated proxy.  As used in lookup.
     * @param publicationDescription a descriptor detailing complex cases.
     * @param classLoader            - classloader containing all needed for proxy generation
     * @throws PublicationException if there is a problem publishing
     */
    void generate(String asName, PublicationDescription publicationDescription, ClassLoader classLoader) throws PublicationException;

    /**
     * Generate a proxy.  Deferred till a later moment for performance reasons (most of
     * use with javac generator)
     *
     * @param asName                 the name of the generated proxy.  As used in lookup.
     * @param publicationDescription a descriptor detailing complex cases.
     * @param classLoader            - classloader containing all needed for proxy generation
     * @throws PublicationException if there is a problem publishing
     */
    void deferredGenerate(String asName, PublicationDescription publicationDescription, ClassLoader classLoader) throws PublicationException;

    /**
     * Generate the deferred proxies.
     *
     * @param classLoader the class loader.
     */
    void generateDeferred(ClassLoader classLoader);
}