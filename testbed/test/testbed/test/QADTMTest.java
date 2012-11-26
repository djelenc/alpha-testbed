package testbed.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static testbed.trustmodel.qad.Omega.D;
import static testbed.trustmodel.qad.Omega.PD;
import static testbed.trustmodel.qad.Omega.PT;
import static testbed.trustmodel.qad.Omega.T;
import static testbed.trustmodel.qad.Omega.U;
import static testbed.trustmodel.qad.Omega.fromNumeric;
import static testbed.trustmodel.qad.Omega.normalizedNumeric;
import static testbed.trustmodel.qad.Operator.CENTRALIST;
import static testbed.trustmodel.qad.Operator.EXTREME_OPTIMIST;
import static testbed.trustmodel.qad.Operator.EXTREME_PESSIMIST;
import static testbed.trustmodel.qad.Operator.MODERATE_OPTIMIST;
import static testbed.trustmodel.qad.Operator.MODERATE_PESSIMIST;
import static testbed.trustmodel.qad.Operator.NON_CENTRALIST;
import static testbed.trustmodel.qad.Operator.STABLE;

import java.util.ArrayList;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import testbed.interfaces.Experience;
import testbed.interfaces.Opinion;
import testbed.trustmodel.qad.Omega;
import testbed.trustmodel.qad.Operator;
import testbed.trustmodel.qad.QAD;

public class QADTMTest {

    @Test
    public void testOrdinas() {
	Assert.assertEquals(T.ordinal() - D.ordinal(), 4);

	Assert.assertTrue(D.ordinal() == 0);
	Assert.assertTrue(D.ordinal() < PD.ordinal());
	Assert.assertTrue(PD.ordinal() < U.ordinal());
	Assert.assertTrue(U.ordinal() < PT.ordinal());
	Assert.assertTrue(PT.ordinal() < T.ordinal());
    }

    @Test
    public void testOperators() {
	Operator o = null;
	Omega[][] opinions = new Omega[][] { { D, D, D }, { U, U, U },
		{ T, T, T } };

	Omega[] experiences = new Omega[] { PT, PD, null };
	Map<Integer, Omega> trust = null;

	o = EXTREME_OPTIMIST;
	trust = o.compute(experiences, opinions);
	assertEquals((trust.get(0)), T);
	assertEquals((trust.get(1)), T);
	assertEquals((trust.get(2)), T);

	o = EXTREME_PESSIMIST;
	trust = o.compute(experiences, opinions);
	assertEquals((trust.get(0)), D);
	assertEquals((trust.get(1)), D);
	assertEquals((trust.get(2)), D);

	o = CENTRALIST;
	trust = o.compute(experiences, opinions);
	assertEquals((trust.get(0)), U);
	assertEquals((trust.get(1)), U);
	assertEquals((trust.get(2)), U);

	o = NON_CENTRALIST;
	trust = o.compute(experiences, opinions);
	assertEquals((trust.get(0)), PT);
	assertEquals((trust.get(1)), PD);
	assertEquals((trust.get(2)), U);

	o = MODERATE_OPTIMIST;
	trust = o.compute(experiences, opinions);
	assertEquals((trust.get(0)), PT);
	assertEquals((trust.get(1)), U);
	assertEquals((trust.get(2)), PD);

	o = MODERATE_PESSIMIST;
	trust = o.compute(experiences, opinions);
	assertEquals((trust.get(0)), U);
	assertEquals((trust.get(1)), PD);
	assertEquals((trust.get(2)), PT);

	o = STABLE;
	trust = o.compute(experiences, opinions);
	assertEquals((trust.get(0)), PT);
	assertEquals((trust.get(1)), PD);
	assertNull(trust.get(2));
    }

    @Test
    public void testOmegaFromNormalizedNumeric() {
	assertEquals(D, normalizedNumeric(0));
	assertEquals(D, normalizedNumeric(0.1));
	assertEquals(D, normalizedNumeric(0.19));

	assertEquals(PD, normalizedNumeric(0.2));
	assertEquals(PD, normalizedNumeric(0.3));
	assertEquals(PD, normalizedNumeric(0.39));
	assertEquals(U, normalizedNumeric(0.4));
	assertEquals(U, normalizedNumeric(0.5));
	assertEquals(U, normalizedNumeric(0.59));

	assertEquals(PT, normalizedNumeric(0.6));
	assertEquals(PT, normalizedNumeric(0.75));
	assertEquals(PT, normalizedNumeric(0.79));

	assertEquals(T, normalizedNumeric(0.8));
	assertEquals(T, normalizedNumeric(1));

	try {
	    assertEquals(PT, normalizedNumeric(-1));
	    fail();
	} catch (Exception e) {
	    assertTrue(true);
	}

	try {
	    assertEquals(PT, normalizedNumeric(1.5));
	    fail();
	} catch (Exception e) {
	    assertTrue(true);
	}
    }

    @Test
    public void testExpandArray() {
	QAD tm = new QAD();
	tm.initialize(Operator.CENTRALIST);

	ArrayList<Experience> exps = new ArrayList<Experience>();
	ArrayList<Opinion> opinions = new ArrayList<Opinion>();

	opinions.add(new Opinion(1, 0, 0, 0, 0.1));
	opinions.add(new Opinion(2, 0, 0, 0, 0.1));
	opinions.add(new Opinion(0, 1, 0, 0, 0.1));
	opinions.add(new Opinion(1, 0, 0, 0, 0.1));
	tm.processExperiences(exps);
	tm.processOpinions(opinions);
	tm.calculateTrust();

	opinions.clear();
	exps.clear();
	exps.add(new Experience(3, 0, 0, 0.8));
	tm.processExperiences(exps);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	assertEquals(4, tm.row.length);
	assertEquals(4, tm.op.length);

	opinions.clear();
	exps.clear();
	exps.add(new Experience(5, 0, 0, 0.8));
	tm.processExperiences(exps);
	tm.processOpinions(opinions);
	tm.calculateTrust();
	assertEquals(6, tm.row.length);
	assertEquals(6, tm.op.length);
    }

    @Test
    public void testOmegaFromNumeric() {
	assertEquals(D, fromNumeric(-2));
	assertEquals(PD, fromNumeric(-1));
	assertEquals(U, fromNumeric(0));
	assertEquals(PT, fromNumeric(1));
	assertEquals(T, fromNumeric(2));

	try {
	    assertEquals(PT, fromNumeric(2.5));
	    fail();
	} catch (Exception e) {
	    assertTrue(true);
	}

	try {
	    assertEquals(PT, fromNumeric(-3));
	    fail();
	} catch (Exception e) {
	    assertTrue(true);
	}

	try {
	    assertEquals(PT, fromNumeric(0.1));
	    fail();
	} catch (Exception e) {
	    assertTrue(true);
	}
    }

}
