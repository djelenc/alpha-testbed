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
package atb.trustmodel;

import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class ARHTMTest {

    @Test
    public void compareEnums() {
        assertTrue(TD.VG.compareTo(TD.G) > 0);
        assertTrue(TD.VG.compareTo(TD.B) > 0);
        assertTrue(TD.VG.compareTo(TD.VB) > 0);
        assertTrue(TD.G.compareTo(TD.B) > 0);
        assertTrue(TD.G.compareTo(TD.VB) > 0);
        assertTrue(TD.B.compareTo(TD.VB) > 0);
        assertEquals(TD.VG.compareTo(TD.VG), 0);
        assertEquals(TD.G.compareTo(TD.G), 0);
        assertEquals(TD.B.compareTo(TD.B), 0);
        assertEquals(TD.VB.compareTo(TD.VB), 0);
    }

    @Test
    public void testTDFromIndex() {
        assertEquals(TD.VB, TD.fromIndex(0));
        assertEquals(TD.B, TD.fromIndex(1));
        assertEquals(TD.G, TD.fromIndex(2));
        assertEquals(TD.VG, TD.fromIndex(3));
    }

    @Test
    public void testTDFromDouble() {
        assertEquals(TD.VB, TD.fromDouble(0d));
        assertEquals(TD.VB, TD.fromDouble(0.1));
        assertEquals(TD.B, TD.fromDouble(0.25));
        assertEquals(TD.B, TD.fromDouble(0.35));
        assertEquals(TD.G, TD.fromDouble(0.5));
        assertEquals(TD.G, TD.fromDouble(0.6));
        assertEquals(TD.VG, TD.fromDouble(0.75));
        assertEquals(TD.VG, TD.fromDouble(0.9));
        assertEquals(TD.VG, TD.fromDouble(1d));
    }

    @Test
    public void testModeExperience() {
        int[] stats = new int[]{0, 0, 0, 0};

        assertNull(AbdulRahmanHailes.modeTD(stats));

        stats[0] = 3;
        assertSame(TD.VB, AbdulRahmanHailes.modeTD(stats));
        stats[1] = 3;
        assertSame(TD.B, AbdulRahmanHailes.modeTD(stats));
        stats[3] = 3;
        assertSame(TD.VG, AbdulRahmanHailes.modeTD(stats));
    }

    @Test
    public void testModeSDAbs() {
        ArrayList<Integer> list = new ArrayList<Integer>();

        // empty
        assertEquals(-1, AbdulRahmanHailes.modeSDAbs(list));

        // multi-modal
        list.add(1);
        list.add(2);
        list.add(3);
        assertEquals(3, AbdulRahmanHailes.modeSDAbs(list));

        // valid
        list.add(-2);
        assertEquals(2, AbdulRahmanHailes.modeSDAbs(list));

        // out of scope
        list.add(4);
        list.add(-4);

        try {
            AbdulRahmanHailes.modeSDAbs(list);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
            list.remove(new Integer(4));
        }

        try {
            AbdulRahmanHailes.modeSDAbs(list);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
            list.remove(new Integer(-4));
        }

        // valid again
        assertEquals(2, AbdulRahmanHailes.modeSDAbs(list));
    }

    @Test
    public void testModeSD() {
        ArrayList<Integer> list = new ArrayList<Integer>();

        // empty
        assertEquals(0, AbdulRahmanHailes.modeSD(list));

        // multi-modal
        list.add(1);
        list.add(2);
        list.add(3);
        assertEquals(0, AbdulRahmanHailes.modeSD(list));

        // valid case: return 3
        list.add(3);
        assertEquals(3, AbdulRahmanHailes.modeSD(list));

        // out of scope
        list.add(4);
        list.add(-4);

        try {
            AbdulRahmanHailes.modeSD(list);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
            list.remove(new Integer(4));
        }

        try {
            AbdulRahmanHailes.modeSD(list);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
            list.remove(new Integer(-4));
        }

        // valid again
        assertEquals(3, AbdulRahmanHailes.modeSD(list));
    }

}
