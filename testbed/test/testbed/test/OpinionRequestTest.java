package testbed.test;

import org.junit.Assert;
import org.junit.Test;

import testbed.interfaces.OpinionRequest;

public class OpinionRequestTest {

    @Test
    public void opinionRequestOrder() {
	final OpinionRequest r1 = new OpinionRequest(1, 1, 1);
	final OpinionRequest r2 = new OpinionRequest(2, 1, 1);
	final OpinionRequest r3 = new OpinionRequest(1, 2, 1);
	final OpinionRequest r4 = new OpinionRequest(1, 1, 2);
	final OpinionRequest r5 = new OpinionRequest(1, 1, 1);

	Assert.assertTrue(r1.compareTo(r2) < 0);
	Assert.assertTrue(r1.compareTo(r3) < 0);
	Assert.assertTrue(r1.compareTo(r4) < 0);
	Assert.assertTrue(r1.compareTo(r5) == 0);
	Assert.assertTrue(r3.compareTo(r4) > 0);
    }

}
