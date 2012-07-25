package testbed.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UtilsTest.class, DeceptionModelsTest.class,
	ClassLoadersTest.class, AlphaTestbedTest.class, MetricsTest.class,
	RandomScenarioTest.class, EigenTrustContinuousTMTest.class,
	ARHTMTest.class, OnlyExperiencesTMTest.class, SimpleTMTest.class,
	OnlyOpinionsTMTest.class, BetaReputationTMTest.class,
	BRSWithFilteringTMTest.class, TravosTMTest.class,
	YuSinghSycaraTMTest.class, QADTMTest.class, QTMTest.class,
	OscillationScenarioTest.class, RandomGeneratorTest.class })
public class AllTests {

}
