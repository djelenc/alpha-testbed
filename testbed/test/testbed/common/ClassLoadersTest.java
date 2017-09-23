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
import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import testbed.interfaces.Accuracy;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;

public class ClassLoadersTest {

    @Test
    public <T extends Comparable<T>> void testLookUp() {
	ClassLoader classLoader = Thread.currentThread()
		.getContextClassLoader();

	@SuppressWarnings("rawtypes")
	List<Scenario> generators = ClassLoaderUtils.lookUp(Scenario.class,
		classLoader);
	List<Accuracy> metrics = ClassLoaderUtils.lookUp(Accuracy.class,
		classLoader);
	List<TrustModel<?>> trustModels = ClassLoaderUtils
		.lookUpTrustModels(classLoader);
	List<DeceptionModel> deceptionModels = ClassLoaderUtils
		.lookUp(DeceptionModel.class, classLoader);

	Assert.assertFalse(generators.isEmpty());
	Assert.assertFalse(metrics.isEmpty());
	Assert.assertFalse(trustModels.isEmpty());
	Assert.assertFalse(deceptionModels.isEmpty());
    }

    @Ignore
    @Test
    public void testServiceLoaderFromExternalJar() throws Exception {
	ClassLoader cl = ClassLoader.getSystemClassLoader();
	// ensure the jar exists before running
	ClassLoaderUtils.addURL(new File("c:/testlib/some.jar").toURI().toURL(),
		cl);

	for (Accuracy dg : ServiceLoader.load(Accuracy.class)) {
	    System.out.println(dg);
	}
    }

    @Ignore
    @Test
    public void testServiceLoaderFromExternalFolder() throws Exception {
	ClassLoader cl = ClassLoader.getSystemClassLoader();
	// ensure the folder/jar exists before running
	ClassLoaderUtils.addDirToClasspath(new File("c:/testlib"), cl);

	for (Accuracy dg : ServiceLoader.load(Accuracy.class)) {
	    System.out.println(dg);
	}
    }

    @Test
    public void testServiceLoaderIDataGenerator() {
	@SuppressWarnings("rawtypes")
	ServiceLoader<Scenario> all = ServiceLoader.load(Scenario.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof Scenario);
    }

    @Test
    public void testServiceLoaderIMetric() {
	ServiceLoader<Accuracy> all = ServiceLoader.load(Accuracy.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof Accuracy);
    }

    @Test
    public void testServiceLoaderIDeceptionModel() {
	ServiceLoader<DeceptionModel> all = ServiceLoader
		.load(DeceptionModel.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof DeceptionModel);
    }
}
