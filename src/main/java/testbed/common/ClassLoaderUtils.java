/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.common;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import testbed.interfaces.TrustModel;

public class ClassLoaderUtils {

    private static final Logger logger = Logger
	    .getLogger(ClassLoaderUtils.class.getName());

    @SuppressWarnings("rawtypes")
    private static final Class[] parameters = new Class[] { URL.class };

    /**
     * Returns a List of implementations of a given interface using provided
     * class loader. The implementations have to be first registered via files
     * in META-INF/services directory.
     * 
     * @param clazz
     *            Interface of implementations to look for
     * @param classLoader
     *            The class loader to be used to load provider-configuration
     *            files and provider classes, or null if the system class loader
     *            (or, failing that, the bootstrap class loader) is to be used
     * @return List of instances of classes that implement the given interface
     * @see ServiceLoader
     */
    public static <T> List<T> lookUp(Class<T> clazz, ClassLoader classLoader) {
	List<T> result = new ArrayList<T>();

	for (T impl : ServiceLoader.load(clazz, classLoader)) {
	    result.add(impl);
	}

	return result;
    }

    /**
     * Returns a List of ITrustModel implementations using provided class
     * loader. The implementations have to be registered in
     * META-INF/services/testbed.interfaces.ITrustModel file.
     * 
     * @param clazz
     * @param classLoader
     * @return
     */
    public static <T extends Comparable<T>> List<TrustModel<?>> lookUpTrustModels(
	    ClassLoader classLoader) {

	List<TrustModel<?>> result = new ArrayList<TrustModel<?>>();

	for (TrustModel<?> impl : ServiceLoader.load(TrustModel.class,
		classLoader)) {
	    result.add(impl);
	}

	return result;
    }

    /**
     * Adds the jars in the given directory to class-path
     * 
     * @param directory
     * @param sysLoader
     * @throws IOException
     */
    public static void addDirToClasspath(File directory, ClassLoader sysLoader)
	    throws IOException {
	if (directory.exists()) {
	    for (File file : directory.listFiles()) {
		addURL(file.toURI().toURL(), sysLoader);
	    }
	} else {
	    logger.warning(
		    "The directory \"" + directory + "\" does not exist!");
	}
    }

    /**
     * Adds a given JAR to class-path
     * 
     * @param u
     *            The URL of JAR to be added to class-path
     * @param cl
     *            The ClassLoader to be used for loading
     * @throws IOException
     */
    public static void addURL(URL u, ClassLoader cl) throws IOException {
	URLClassLoader sysLoader = (URLClassLoader) cl;

	for (URL url : sysLoader.getURLs()) {
	    if (url.toString().equalsIgnoreCase(u.toString())) {
		logger.info(
			String.format("URL %s is already in the CLASSPATH", u));
		return;
	    }
	}

	Class<URLClassLoader> sysclass = URLClassLoader.class;

	try {
	    Method method = sysclass.getDeclaredMethod("addURL", parameters);
	    method.setAccessible(true);
	    method.invoke(sysLoader, new Object[] { u });
	} catch (Throwable t) {
	    String message = "Error, could not add URL to system classloader";
	    logger.severe(message);

	    throw new IOException(message, t);
	}
    }
}
