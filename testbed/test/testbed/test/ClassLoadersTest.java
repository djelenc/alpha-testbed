package testbed.test;

import java.io.File;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import testbed.common.ClassLoaderUtils;
import testbed.interfaces.IDeceptionModel;
import testbed.interfaces.IMetric;
import testbed.interfaces.IScenario;
import testbed.interfaces.ITrustModel;

public class ClassLoadersTest {

    @Test
    public void testLookUp() {

	ClassLoader classLoader = Thread.currentThread()
		.getContextClassLoader();

	List<IScenario> generators = ClassLoaderUtils.lookUp(IScenario.class,
		classLoader);
	List<IMetric> metrics = ClassLoaderUtils.lookUp(IMetric.class,
		classLoader);
	List<ITrustModel> trustModels = ClassLoaderUtils.lookUp(
		ITrustModel.class, classLoader);
	List<IDeceptionModel> deceptionModels = ClassLoaderUtils.lookUp(
		IDeceptionModel.class, classLoader);

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
	ClassLoaderUtils.addURL(
		new File("c:/testlib/some.jar").toURI().toURL(), cl);

	for (IMetric dg : ServiceLoader.load(IMetric.class)) {
	    System.out.println(dg.getName());
	}
    }

    @Ignore
    @Test
    public void testServiceLoaderFromExternalFolder() throws Exception {
	ClassLoader cl = ClassLoader.getSystemClassLoader();
	// ensure the folder/jar exists before running
	ClassLoaderUtils.addDirToClasspath(new File("c:/testlib"), cl);

	for (IMetric dg : ServiceLoader.load(IMetric.class)) {
	    System.out.println(dg.getName());
	}
    }

    @Test
    public void testServiceLoaderIDataGenerator() {
	ServiceLoader<IScenario> all = ServiceLoader.load(IScenario.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof IScenario);
    }

    @Test
    public void testServiceLoaderIMetric() {
	ServiceLoader<IMetric> all = ServiceLoader.load(IMetric.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof IMetric);
    }

    @Test
    public void testServiceLoaderITrustModel() {
	ServiceLoader<ITrustModel> all = ServiceLoader.load(ITrustModel.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof ITrustModel);
    }

    @Test
    public void testServiceLoaderIDeceptionModel() {
	ServiceLoader<IDeceptionModel> all = ServiceLoader
		.load(IDeceptionModel.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof IDeceptionModel);
    }
}
