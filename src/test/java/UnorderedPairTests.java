/**
 * Created by sandulmv on 31.07.17.
 */

import org.junit.Assert;
import org.junit.Test;

public class UnorderedPairTests {

  @Test
  public void testInPair_DifferentObjects() {
    Object o1InPair = new Object();
    Object o2InPair = new Object();
    Object o3NotInPair = new Object();
    UnorderedPair unorderedPair = new UnorderedPairHash(o1InPair, o2InPair);

    Assert.assertTrue(unorderedPair.inPair(o1InPair));
    Assert.assertTrue(unorderedPair.inPair(o2InPair));
    Assert.assertFalse(unorderedPair.inPair(o3NotInPair));
    Assert.assertFalse(unorderedPair.inPair(null));
  }

  @Test
  public void testInPair_EqualObjects() {
    Object o1InPair = new Object();
    Object o2NotInPair = new Object();
    UnorderedPair unorderedPair = new UnorderedPairHash(o1InPair, o1InPair);

    Assert.assertTrue(unorderedPair.inPair(o1InPair));
    Assert.assertTrue(unorderedPair.inPair(o1InPair));
    Assert.assertFalse(unorderedPair.inPair(o2NotInPair));
    Assert.assertFalse(unorderedPair.inPair(null));
  }

  @Test
  public void testGetNotEqualTo_DifferentObjects() {
    Object o1InPair = new Object();
    Object o2InPair = new Object();
    Object o3NotInPair = new Object();

    UnorderedPair uPair = new UnorderedPairHash(o1InPair, o2InPair);

    Assert.assertTrue(o1InPair.equals(uPair.getNotEqualTo(o2InPair)));
    Assert.assertTrue(o2InPair.equals(uPair.getNotEqualTo(o1InPair)));

    Object someObjectInPair = uPair.getNotEqualTo(o3NotInPair);
    Assert.assertTrue(someObjectInPair.equals(o2InPair) || someObjectInPair.equals(o1InPair));

    someObjectInPair = uPair.getNotEqualTo(null);
    Assert.assertTrue(someObjectInPair.equals(o2InPair) || someObjectInPair.equals(o1InPair));
  }

  @Test
  public void testGetNotEqualTo_EqualObjects() {
    Object oInPair = new Object();
    Object oNotInPair = new Object();

    UnorderedPair uPair = new UnorderedPairHash(oInPair, oInPair);

    Assert.assertNull(uPair.getNotEqualTo(oInPair));
    Assert.assertTrue(oInPair.equals(uPair.getNotEqualTo(oNotInPair)));
    Assert.assertTrue(oInPair.equals(uPair.getNotEqualTo(null)));
  }

  @Test
  public void testEquals() {
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();

    UnorderedPair uPair1 = new UnorderedPairHash(o1, o2);
    UnorderedPair uPair2 = new UnorderedPairHash(o1, o2);
    UnorderedPair uPair3 = new UnorderedPairHash(o2, o1);
    UnorderedPair uPair4 = new UnorderedPairHash(o1, o1);
    UnorderedPair uPair5 = new UnorderedPairHash(o1, o1);
    UnorderedPair uPair6 = new UnorderedPairHash(o2, o3);

    Assert.assertTrue(uPair1.equals(uPair2));
    Assert.assertTrue(uPair1.equals(uPair3));
    Assert.assertTrue(uPair3.equals(uPair2));
    Assert.assertFalse(uPair1.equals(uPair4));
    Assert.assertFalse(uPair1.equals(uPair6));
    Assert.assertTrue(uPair4.equals(uPair5));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStoreNull() {
    new UnorderedPairHash<>(null, new Object());
  }
}
