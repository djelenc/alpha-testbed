package testbed.test;

import java.io.File;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import testbed.common.ClassLoaderUtils;
import testbed.interfaces.DeceptionModel;
import testbed.interfaces.RankingMetric;
import testbed.interfaces.Scenario;
import testbed.interfaces.TrustModel;

public class ClassLoadersTest {

    @Test
    public <T extends Comparable<T>> void testLookUp() {
	ClassLoader classLoader = Thread.currentThread()
		.getContextClassLoader();

	List<Scenario> generators = ClassLoaderUtils.lookUp(Scenario.class,
		classLoader);
	List<RankingMetric> metrics = ClassLoaderUtils.lookUp(
		RankingMetric.class, classLoader);
	List<TrustModel<?>> trustModels = ClassLoaderUtils
		.lookUpTrustModels(classLoader);
	List<DeceptionModel> deceptionModels = ClassLoaderUtils.lookUp(
		DeceptionModel.class, classLoader);

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

	for (RankingMetric dg : ServiceLoader.load(RankingMetric.class)) {
	    System.out.println(dg);
	}
    }

    @Ignore
    @Test
    public void testServiceLoaderFromExternalFolder() throws Exception {
	ClassLoader cl = ClassLoader.getSystemClassLoader();
	// ensure the folder/jar exists before running
	ClassLoaderUtils.addDirToClasspath(new File("c:/testlib"), cl);

	for (RankingMetric dg : ServiceLoader.load(RankingMetric.class)) {
	    System.out.println(dg);
	}
    }

    @Test
    public void testServiceLoaderIDataGenerator() {
	ServiceLoader<Scenario> all = ServiceLoader.load(Scenario.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof Scenario);
    }

    @Test
    public void testServiceLoaderIMetric() {
	ServiceLoader<RankingMetric> all = ServiceLoader
		.load(RankingMetric.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof RankingMetric);
    }

    @Test
    public void testServiceLoaderIDeceptionModel() {
	ServiceLoader<DeceptionModel> all = ServiceLoader
		.load(DeceptionModel.class);

	Assert.assertTrue(all.iterator().hasNext());
	Assert.assertTrue(all.iterator().next() instanceof DeceptionModel);
    }
}
