package testbed.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import testbed.trustmodel.qad.QADTMTest;
import testbed.trustmodel.qad.QTMTest;

@RunWith(Suite.class)
@SuiteClasses({ UtilsTest.class, DeceptionModelsTest.class,
	ClassLoadersTest.class, AlphaTestbedTest.class, MetricsTest.class,
	RandomScenarioTest.class, ARHTMTest.class, OnlyExperiencesTMTest.class,
	OnlyOpinionsTMTest.class, BetaReputationTMTest.class,
	BRSWithFilteringTMTest.class, TravosTMTest.class,
	YuSinghSycaraTMTest.class, QADTMTest.class, QTMTest.class,
	OscillationScenarioTest.class, DefaultRandomGeneratorTest.class,
	ExampleGeneratorTest.class, OpinionRequestTest.class,
	EvaluationProtocolTests.class, TargetedAttackScenarioTest.class })
public class AllTests {

}
