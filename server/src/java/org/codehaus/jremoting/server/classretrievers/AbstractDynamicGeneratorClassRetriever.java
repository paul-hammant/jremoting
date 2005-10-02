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

import org.codehaus.jremoting.server.ClassRetrievalException;
import org.codehaus.jremoting.server.ClassRetriever;
import org.codehaus.jremoting.server.DynamicProxyGenerator;
import org.codehaus.jremoting.server.ProxyGenerationEnvironmentException;
import org.codehaus.jremoting.server.PublicationDescription;
import org.codehaus.jremoting.server.PublicationDescriptionItem;
import org.codehaus.jremoting.server.PublicationException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Class JarFileClassRetriever
 *
 * @author Paul Hammant
 * @version $Revision: 1.2 $
 */
public class AbstractDynamicGeneratorClassRetriever implements DynamicProxyGenerator, ClassRetriever {

    private String classpath;
    private String classGenDir = ".";
    private String srcGenDir;
    private Class generatorClass;

    /**
     * @param classLoader        the classloader in which the proxy generater will be found.
     * @param generatorClassName the name of teh proxy gen class
     */
    public AbstractDynamicGeneratorClassRetriever(ClassLoader classLoader, String generatorClassName) {
        try {
            generatorClass = classLoader.loadClass(generatorClassName);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(generatorClassName);
        }
    }

    /**
     * Method generate
     *
     * @param asName            the name to generate as
     * @param interfaceToExpose the interfaces to expose.
     * @param classLoader       the classloader to use during generation.
     * @throws PublicationException if the generation failed.
     */
    public void generate(String asName, Class interfaceToExpose, ClassLoader classLoader) throws PublicationException {
        generateProxy(asName, new PublicationDescription(interfaceToExpose), classLoader, false);
    }

    /**
     * Method generate
     *
     * @param asName                 the name to generate as
     * @param publicationDescription the description of the publication
     * @param classLoader            the class loader to use.
     * @throws PublicationException if the generation failed.
     */
    public void generate(String asName, PublicationDescription publicationDescription, ClassLoader classLoader) throws PublicationException {
        generateProxy(asName, publicationDescription, classLoader, false);
    }

    /**
     * Method deferredGenerate
     *
     * @param asName                 the name of the clas to generate
     * @param publicationDescription the description of the publication
     * @param classLoader            the class loader to use.
     * @throws PublicationException if the generation failed.
     */
    public void deferredGenerate(String asName, PublicationDescription publicationDescription, ClassLoader classLoader) throws PublicationException {
        generateProxy(asName, publicationDescription, classLoader, true);
    }

    /**
     * Use this classpath during retrieval.
     *
     * @param classpath the classpath
     */
    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    /**
     * Method addToClasspath
     *
     * @param classpathElement an element for the classpath
     */
    public void addToClasspath(String classpathElement) {
        classpath = classpath + File.pathSeparator + classpathElement;
    }

    /**
     * Method setClassGenDir
     *
     * @param classGenDir the class generation directory
     */
    public void setClassGenDir(String classGenDir) {
        this.classGenDir = classGenDir;
    }

    /**
     * Method getProxyClassBytes
     *
     * @param publishedName the name to publish as
     * @return the byte array for the proxy class
     * @throws ClassRetrievalException if the class cannot be retrieved.
     */
    public final byte[] getProxyClassBytes(String publishedName) throws ClassRetrievalException {
        return getThingBytes("JRemotingGenerated" + publishedName);
    }

    /**
     * @param thingName the thing name
     * @return the byte array of the thing.
     * @throws org.codehaus.jremoting.server.ClassRetrievalException
     *          if getting the bytes was a problem.
     */
    protected byte[] getThingBytes(String thingName) throws ClassRetrievalException {

        thingName = thingName.replace('.', '\\') + ".class";

        FileInputStream fis;

        try {
            fis = new FileInputStream(new File(classGenDir, thingName));
        } catch (Exception e) {
            e.printStackTrace();

            throw new ClassRetrievalException("Generated class not found in classloader specified : " + e.getMessage());
        }

        if (fis == null) {
            throw new ClassRetrievalException("Generated class not found in classloader specified.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;

        try {
            while (-1 != (i = fis.read())) {
                baos.write(i);
            }

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();

            throw new ClassRetrievalException("Error retrieving generated class bytes : " + e.getMessage());
        }

        byte[] bytes = baos.toByteArray();

        return bytes;
    }

    /**
     * Method setSrcGenDir
     *
     * @param srcGenDir the source generaton directory.
     */
    public void setSrcGenDir(String srcGenDir) {
        this.srcGenDir = srcGenDir;
    }

    private void generateProxy(String asName, PublicationDescription publicationDescription, ClassLoader classLoader, boolean deferred) throws PublicationException {

        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        PublicationDescriptionItem[] interfacesToExpose = new PublicationDescriptionItem[0];
        PublicationDescriptionItem[] addInfs = new PublicationDescriptionItem[0];

        interfacesToExpose = publicationDescription.getInterfacesToExpose();
        addInfs = publicationDescription.getAdditionalFacades();

        org.codehaus.jremoting.server.ProxyGenerator proxyGenerator;

        try {
            proxyGenerator = (org.codehaus.jremoting.server.ProxyGenerator) generatorClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("ProxyGenerator cannot be instantiated.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ProxyGenerator was illegally accessed");
        }

        proxyGenerator.setSrcGenDir(srcGenDir);
        proxyGenerator.setClassGenDir(classGenDir);
        proxyGenerator.setGenName(asName);
        proxyGenerator.setClasspath(classpath);
        proxyGenerator.setInterfacesToExpose(interfacesToExpose);
        proxyGenerator.setAdditionalFacades(addInfs);

        try {
            proxyGenerator.generateSrc(classLoader);
        } catch (Throwable t) {
            System.err.println("******");
            System.err.println("** Exception while making source : ");
            System.err.flush();
            t.printStackTrace();
            System.err.println("** Name=" + asName);
            System.err.println("** Classes/Interfaces to Expose..");

            for (int i = 0; i < interfacesToExpose.length; i++) {
                String aString = interfacesToExpose[i].getFacadeClass().getName();

                System.err.println("** .." + aString);
            }

            System.err.println("******");
            System.err.flush();
        }

        if (!deferred) {
            try {
                proxyGenerator.generateClass(classLoader);
            } catch (Throwable t) {
                if ((t instanceof NoClassDefFoundError) && t.getMessage().equals("sun/tools/javac/Main")) {
                    System.err.println("********************************************");
                    System.err.println("*                                          *");
                    System.err.println("* JRemoting Remoting problem......     *");
                    System.err.println("* Please copy JAVA_HOME/lib/tools.jar      *");
                    System.err.println("* to your applications classpath so      *");
                    System.err.println("* that proxys can be compiled.             *");
                    System.err.println("*                                          *");
                    System.err.println("********************************************");

                    throw new ProxyGenerationEnvironmentException("tools.jar not found in classpath.");
                }

                System.err.println("******");
                System.err.println("** Exception while making String : ");
                System.err.flush();
                t.printStackTrace();
                System.err.println("** SrcDir=" + srcGenDir);
                System.err.println("** ClassDir=" + classGenDir);
                System.err.println("** Name=" + asName);
                System.err.println("** CLasspath=" + classpath);
                System.err.println("** Classes/Interfaces to Expose..");

                for (int i = 0; i < interfacesToExpose.length; i++) {
                    String aString = interfacesToExpose[i].getFacadeClass().getName();

                    System.err.println("** .." + aString);
                }

                System.err.println("******");
                System.err.flush();
            }
        }
    }

    /**
     * Method generateDeferred
     *
     * @param classLoader the classloader to use.
     */
    public void generateDeferred(ClassLoader classLoader) {

        org.codehaus.jremoting.server.ProxyGenerator proxyGenerator;

        try {
            proxyGenerator = (org.codehaus.jremoting.server.ProxyGenerator) generatorClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("ProxyGenerator cannot be instantiated.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ProxyGenerator was illegally accessed");
        }
        proxyGenerator.generateDeferredClasses();
    }
}