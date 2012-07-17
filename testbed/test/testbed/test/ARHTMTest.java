package testbed.test;

import java.util.ArrayList;

import junit.framework.Assert;

import org.junit.Test;

import testbed.trustmodel.AbdulRahmanHailes;
import testbed.trustmodel.TD;

public class ARHTMTest {

    @Test
    public void testTDFromDouble() {
	Assert.assertEquals(TD.VB, TD.fromDouble(0d));
	Assert.assertEquals(TD.VB, TD.fromDouble(0.1));
	Assert.assertEquals(TD.B, TD.fromDouble(0.25));
	Assert.assertEquals(TD.B, TD.fromDouble(0.35));
	Assert.assertEquals(TD.G, TD.fromDouble(0.5));
	Assert.assertEquals(TD.G, TD.fromDouble(0.6));
	Assert.assertEquals(TD.VG, TD.fromDouble(0.75));
	Assert.assertEquals(TD.VG, TD.fromDouble(0.9));
	Assert.assertEquals(TD.VG, TD.fromDouble(1d));
    }

    @Test
    public void testModeExperience() {
	int[] stats = new int[] { 0, 0, 0, 0 };

	Assert.assertNull(AbdulRahmanHailes.modeTD(stats));

	stats[0] = 3;
	Assert.assertSame(TD.VG, AbdulRahmanHailes.modeTD(stats));
	stats[1] = 3;
	Assert.assertSame(TD.G, AbdulRahmanHailes.modeTD(stats));
	stats[3] = 3;
	Assert.assertSame(TD.VB, AbdulRahmanHailes.modeTD(stats));
    }

    @Test
    public void testModeSDAbs() {
	ArrayList<Integer> list = new ArrayList<Integer>();

	// empty
	Assert.assertEquals(-1, AbdulRahmanHailes.modeSDAbs(list));

	// multi-modal
	list.add(1);
	list.add(2);
	list.add(3);
	Assert.assertEquals(3, AbdulRahmanHailes.modeSDAbs(list));

	// valid
	list.add(-2);
	Assert.assertEquals(2, AbdulRahmanHailes.modeSDAbs(list));

	// out of scope
	list.add(4);
	list.add(-4);

	try {
	    AbdulRahmanHailes.modeSDAbs(list);
	    Assert.fail();
	} catch (IllegalArgumentException e) {
	    Assert.assertTrue(true);
	    list.remove(new Integer(4));
	}

	try {
	    AbdulRahmanHailes.modeSDAbs(list);
	    Assert.fail();
	} catch (IllegalArgumentException e) {
	    Assert.assertTrue(true);
	    list.remove(new Integer(-4));
	}

	// valid again
	Assert.assertEquals(2, AbdulRahmanHailes.modeSDAbs(list));
    }

    @Test
    public void testModeSD() {
	ArrayList<Integer> list = new ArrayList<Integer>();

	// empty
	Assert.assertEquals(0, AbdulRahmanHailes.modeSD(list));

	// multi-modal
	list.add(1);
	list.add(2);
	list.add(3);
	Assert.assertEquals(0, AbdulRahmanHailes.modeSD(list));

	// valid case: return 3
	list.add(3);
	Assert.assertEquals(3, AbdulRahmanHailes.modeSD(list));

	// out of scope
	list.add(4);
	list.add(-4);

	try {
	    AbdulRahmanHailes.modeSD(list);
	    Assert.fail();
	} catch (IllegalArgumentException e) {
	    Assert.assertTrue(true);
	    list.remove(new Integer(4));
	}

	try {
	    AbdulRahmanHailes.modeSD(list);
	    Assert.fail();
	} catch (IllegalArgumentException e) {
	    Assert.assertTrue(true);
	    list.remove(new Integer(-4));
	}

	// valid again
	Assert.assertEquals(3, AbdulRahmanHailes.modeSD(list));
    }

}
