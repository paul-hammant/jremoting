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
package org.codehaus.jremoting.tools.generator.mojo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.jremoting.server.StubGenerator;
import org.codehaus.jremoting.server.PublicationDescriptionItem;


/**
 * Mojo to generate proxies
 *
 * @author Paul Hammant
 * @author Mauro Talevi
 * @goal generate
 * @phase compile
 * @requiresDependencyResolution test
 */
public class StubGeneratorMojo
        extends AbstractMojo
{

    private static final String COMMA = ",";

    private String generatorClass = "org.codehaus.jremoting.tools.generator.BcelStubGenerator";

    /**
     * Test classpath.
     *
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    protected List classpathElements;

    /**
     * The name of the service used to generate stub class names
     * @parameter
     * @required
     */
    protected String genName;

    /**
     * The principal facade that is being published
     * @parameter
     * @required
     */
    protected String interfaces;

    /**
     * The directory to put generated classes into
     * @parameter
     * @required
     */
    protected File classGenDir;

    /**
     * Additional Facades. When encounted in an object tree, they are passed by ref not value to the client
     * @parameter
     */
    protected String additionalFacades;


    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().debug( "Generating JRemoting Stubs.");


        if (genName == null) {
            throw new MojoExecutionException(
                    "Specify the name to use for lookup");
        }

        if (interfaces == null) {
            throw new MojoExecutionException(
                    "Specify at least one interface to expose");
        }

        if (classGenDir == null) {
            throw new MojoExecutionException(
                    "Specify the directory to generate Java classes in");
        }

        StubGenerator stubGenerator;

        try {
            stubGenerator = (StubGenerator)Class.forName(generatorClass).newInstance();
        } catch (Exception e) {
            throw new MojoExecutionException(
                    "Failed to create StubGenerator "+generatorClass, e);
        }

        try {
            stubGenerator.setGenName(genName);
            stubGenerator.setClassGenDir(classGenDir.getAbsolutePath());
            String classpath = toCSV(classpathElements);
            stubGenerator.setClasspath(classpath);
            getLog().debug("StubGenerator classpath: " + classpath);

            ClassLoader classLoader = createClassLoader(classpathElements);

            stubGenerator.setPrimaryFacades(createPublicationDescriptionItems(fromCSV(interfaces), classLoader));

            if (additionalFacades != null) {
                stubGenerator.setAdditionalFacades(createPublicationDescriptionItems(fromCSV(additionalFacades), classLoader));
            }

            stubGenerator.generateClass(classLoader);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Class not found: "
                    + e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Malformed classpath URLs: "
                    + e.getMessage(), e);
        }
    }

    private PublicationDescriptionItem[] createPublicationDescriptionItems(String[] classNames, ClassLoader classLoader) throws ClassNotFoundException {
        PublicationDescriptionItem[] items = new PublicationDescriptionItem[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            items[i] = new PublicationDescriptionItem(
                    classLoader.loadClass(classNames[i]));
        }
        return items;
    }

    private ClassLoader createClassLoader(List classpathElements) throws MalformedURLException {
        return new URLClassLoader(toClasspathURLs(classpathElements));
    }

    private URL[] toClasspathURLs(List classpathElements)
            throws MalformedURLException {
        List urls = new ArrayList();
        if (classpathElements != null) {
            for (Iterator i = classpathElements.iterator(); i.hasNext();) {
                String classpathElement = (String) i.next();
                urls.add(new File(classpathElement).toURL());
            }
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    private String[] fromCSV(String csv) {
        if ( csv == null ) {
            return new String[0];
        }
        return csv.split(COMMA);
    }

    private String toCSV(List classpathElements) {
        StringBuffer sb = new StringBuffer();
        for (Iterator i = classpathElements.iterator(); i.hasNext(); ){
            String path = (String)i.next();
            sb.append(path);
            if ( i.hasNext() ){
                sb.append(COMMA);
            }
        }
        return sb.toString();
    }

}