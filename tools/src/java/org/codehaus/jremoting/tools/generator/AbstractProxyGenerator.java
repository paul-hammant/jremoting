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
package org.codehaus.jremoting.tools.generator;

import org.codehaus.jremoting.server.ProxyGenerationException;
import org.codehaus.jremoting.server.ProxyGenerator;
import org.codehaus.jremoting.server.PublicationDescriptionItem;

import java.lang.reflect.Method;


/**
 * Abstract parent for Proxy Generators
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $*
 */

public abstract class AbstractProxyGenerator implements ProxyGenerator {

    private String classGenDir;
    private String genName;
    private String srcGenDir;
    private String classpath;
    private boolean verbose;
    private PublicationDescriptionItem[] additionalFacades;
    private PublicationDescriptionItem[] callbackFacades;
    private PublicationDescriptionItem[] interfacesToExpose;

    /**
     * Get the directory name of the class generation directory.
     *
     * @return the dir name.
     */
    public String getClassGenDir() {
        return classGenDir;
    }

    /**
     * Get the generation name of the class
     *
     * @return the name.
     */

    public String getGenName() {
        return genName;
    }

    /**
     * Get the source directory name
     *
     * @return the dir name.
     */

    public String getSrcGenDir() {
        return srcGenDir;
    }


    /**
     * Get the classpath used during creation
     *
     * @return classpath
     */

    public String getClasspath() {
        return classpath;
    }

    /**
     * Is verbose debugging level
     *
     * @return verbose or not
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Get the additional facades
     *
     * @return the additional facades
     */
    public PublicationDescriptionItem[] getAdditionalFacades() {
        return additionalFacades;
    }

    public PublicationDescriptionItem[] getCallbackFacades() {
        return callbackFacades;
    }

    /**
     * Get the interfaces to expose.
     *
     * @return the interfaces
     */
    public PublicationDescriptionItem[] getInterfacesToExpose() {
        return interfacesToExpose;
    }

    /**
     * Set the verbose logging level for class generation.
     *
     * @param trueFalse set the verbose level
     */
    public void verbose(boolean trueFalse) {
        verbose = trueFalse;
    }


    /**
     * Set the interfaces to expose.
     *
     * @param interfacesToExpose the interfaces.
     */
    public void setInterfacesToExpose(PublicationDescriptionItem[] interfacesToExpose) {
        this.interfacesToExpose = interfacesToExpose;
    }


    /**
     * Set the additional facades
     *
     * @param additionalFacades the facades.
     */
    public void setAdditionalFacades(PublicationDescriptionItem[] additionalFacades) {
        this.additionalFacades = additionalFacades;
    }

    public void setCallbackFacades(PublicationDescriptionItem[] callbackFacades) {
        this.callbackFacades = callbackFacades;
    }

    /**
     * Set the clas generation dorectory
     *
     * @param classGenDir the dir.
     */
    public void setClassGenDir(String classGenDir) {
        this.classGenDir = classGenDir;
    }

    /**
     * Set the generation name
     *
     * @param genName the name
     */
    public void setGenName(String genName) {
        this.genName = genName;
    }

    /**
     * Set the source generation directory.
     *
     * @param srcGenDir the dir name.
     */
    public void setSrcGenDir(String srcGenDir) {
        this.srcGenDir = srcGenDir;
    }

    /**
     * Set the classpath to generate with
     *
     * @param classpath the classpath.
     */
    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    /**
     * Is the param one of the additional facades?
     *
     * @param clazz the class
     * @return true if the class is one of the designated facades
     */
    protected boolean isAdditionalFacade(Class clazz) {

        if (additionalFacades == null) {
            return false;
        }

        for (int p = 0; p < additionalFacades.length; p++) {
            if (clazz.getName().equals(additionalFacades[p].getFacadeClass().getName())) {
                return true;
            } else if (clazz.getName().equals("[L" + additionalFacades[p].getFacadeClass().getName() + ";")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates the Java-Source type of the string.
     *
     * @param rClass the class to get a type for
     * @return the class type
     */
    protected String getClassType(Class rClass) {

        String cn = rClass.getName();

        if (rClass.getName().startsWith("[L")) {
            return cn.substring(2, cn.length() - 1) + "[]";
        } else {
            return cn;
        }
    }

    /**
     * Gernerate the source for the proxy class.
     *
     * @param classLoader the classloader to use while making the source.
     * @throws ProxyGenerationException if an error during generation
     */
    public void generateSrc(ClassLoader classLoader) throws ProxyGenerationException {
        // default impl
    }

    /**
     * Generate the deferred classes.
     */
    public void generateDeferredClasses() {
        // default impl
    }

    /**
     * @param publicationDescriptionItemses
     * @return
     */
    protected boolean needsAsyncBehavior(PublicationDescriptionItem[] publicationDescriptionItemses) {
        for (int i = 0; i < publicationDescriptionItemses.length; i++) {
            PublicationDescriptionItem publicationDescriptionItem = publicationDescriptionItemses[i];
            if (publicationDescriptionItem.hasAsyncBehavior()) {
                return true;
            }
        }
        return false;
    }

    protected Method[] getGeneratableMethods(Class clazz) {

        Method[] methods = null;
        try {
            Method ts = Object.class.getMethod("toString", new Class[0]);
            Method hc = Object.class.getMethod("hashCode", new Class[0]);
            Method[] interfaceMethods = clazz.getMethods();
            methods = new Method[interfaceMethods.length + 2];
            System.arraycopy(interfaceMethods, 0, methods, 0, interfaceMethods.length);
            methods[interfaceMethods.length] = ts;
            methods[interfaceMethods.length + 1] = hc;
        } catch (NoSuchMethodException e) {
            // never!
        }
        return methods;

    }


}