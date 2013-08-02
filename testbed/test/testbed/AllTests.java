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
package testbed;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


import testbed.common.ClassLoadersTest;
import testbed.common.DefaultRandomGeneratorTest;
import testbed.common.ExampleGeneratorTest;
import testbed.common.UtilsTest;
import testbed.core.AlphaTestbedTest;
import testbed.core.EvaluationProtocolTests;
import testbed.deceptiomodel.DeceptionModelsTest;
import testbed.interfaces.OpinionRequestTest;
import testbed.metric.MetricsTest;
import testbed.scenario.OscillationScenarioTest;
import testbed.scenario.RandomScenarioTest;
import testbed.scenario.TargetedAttackScenarioTest;
import testbed.trustmodel.ARHTMTest;
import testbed.trustmodel.BRSWithFilteringTMTest;
import testbed.trustmodel.BetaReputationTMTest;
import testbed.trustmodel.OnlyExperiencesTMTest;
import testbed.trustmodel.OnlyOpinionsTMTest;
import testbed.trustmodel.TravosTMTest;
import testbed.trustmodel.YuSinghSycaraTMTest;
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
