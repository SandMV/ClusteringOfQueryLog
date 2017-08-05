/**
 * Created by sandulmv on 31.07.17.
 */

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class DistanceMatrixTests {
    @Test
    public void testAddDistance() {
        DistanceMatrix dMatrix = new DistanceMatrixTree();

        Object o1 = new Object();
        Object o2 = new Object();

        Assert.assertNull(dMatrix.addDistance(o1, o2, 0));
        Assert.assertEquals(0., dMatrix.addDistance(o1, o2, 1), 1e-8);
        Assert.assertEquals(1., dMatrix.addDistance(o1, o2, 2), 1e-8);

        Assert.assertTrue(dMatrix.containsDistance(o1, o2));
        Assert.assertTrue(dMatrix.containsDistance(o2, o1));

        Assert.assertEquals(2., dMatrix.getDistance(o2, o1), 1e-8);

        Object o3 = new Object();

        Assert.assertNull(dMatrix.addDistance(o1, o3, 2));
        Assert.assertNull(dMatrix.addDistance(o3, o2, 2));

        Assert.assertEquals(2., dMatrix.getDistance(o1, o2), 1e-8);
        Assert.assertEquals(2., dMatrix.getDistance(o2, o3), 1e-8);
        Assert.assertEquals(2., dMatrix.getDistance(o1, o3), 1e-8);
        Assert.assertEquals(2., dMatrix.getDistance(o2, o1), 1e-8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDistance_NegativeDistance() {
        DistanceMatrix distanceMatrix = new DistanceMatrixTree();
        distanceMatrix.addDistance(new Object(), new Object(), -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDistance_Null() {
        DistanceMatrix distanceMatrix = new DistanceMatrixTree();
        distanceMatrix.addDistance(null, new Object(), 1);
    }

    @Test
    public void testDeleteDistance() {
        double val = 1.;
        Object[] points = {new Object(), new Object(), new Object()};
        DistanceMatrix distanceMatrix = makeMatrix(val, points);

        Assert.assertEquals(val, distanceMatrix.deleteDistance(points[0], points[1]), 1e-8);
        Assert.assertNull(distanceMatrix.deleteDistance(points[0], points[1]));
        Assert.assertFalse(distanceMatrix.containsDistance(points[0], points[1]));
        Assert.assertNull(distanceMatrix.deleteDistance(points[1], points[0]));
        Assert.assertFalse(distanceMatrix.containsDistance(points[0], points[1]));
        Assert.assertNull(distanceMatrix.getDistance(points[0], points[1]));
        Assert.assertNull(distanceMatrix.addDistance(points[0], points[1], 0));
        Assert.assertEquals(0., distanceMatrix.deleteDistance(points[1], points[0]), 1e-8);

        Assert.assertNotNull(distanceMatrix.deleteDistance(points[0], points[2]));
        Assert.assertNull(distanceMatrix.deleteDistance(points[0], points[2]));
        Assert.assertNotNull(distanceMatrix.deleteDistance(points[2], points[1]));
        Assert.assertNull(distanceMatrix.deleteDistance(points[2], points[1]));
    }

    @Test
    public void testDeleteRow() {
        double val = 1.;
        Object[] points = {new Object(), new Object(), new Object(), new Object()};
        DistanceMatrix distanceMatrix = makeMatrix(val, points);

        Map<UnorderedPair, Double> deletedRow = distanceMatrix.deleteRow(points[0]);

        Assert.assertNull(distanceMatrix.deleteDistance(points[0], points[1]));
        Assert.assertNull(distanceMatrix.deleteDistance(points[0], points[2]));
        Assert.assertNull(distanceMatrix.deleteDistance(points[0], points[3]));

        for (Object p : deletedRow.keySet()) {
            Assert.assertFalse(distanceMatrix.containsDistance(points[0], p));
        }

        deletedRow = distanceMatrix.deleteRow(points[0]);
        Assert.assertTrue(deletedRow.isEmpty());

        Assert.assertEquals(2, distanceMatrix.deleteRow(points[1]).size());
        Assert.assertEquals(1, distanceMatrix.deleteRow(points[2]).size());
        Assert.assertTrue(distanceMatrix.deleteRow(points[1]).isEmpty());

        Assert.assertFalse(distanceMatrix.containsDistance(points[1], points[3]));
    }

    @Test
    public void testGetPairWithMinDistance() {
        double val = 2;
        Object[] points = {new Object(), new Object(), new Object(), new Object()};
        DistanceMatrix distanceMatrix = makeMatrix(val, points);

        UnorderedPair minPair = distanceMatrix.getPairWithMinDistance();
        Object pt1 = minPair.getNotEqualTo(null);
        Object pt2 = minPair.getNotEqualTo(pt1);

        Assert.assertEquals(val, distanceMatrix.getDistance(pt1, pt2), 1e-8);

        distanceMatrix.addDistance(pt1, pt2, val - 1);
        Assert.assertEquals(new UnorderedPairHash(pt1, pt2), distanceMatrix.getPairWithMinDistance());

        distanceMatrix.deleteDistance(pt1, pt2);
        Assert.assertNotEquals(new UnorderedPairHash(pt1, pt2), distanceMatrix.getPairWithMinDistance());

        distanceMatrix.addDistance(pt1, pt2, val - 1);
        Assert.assertEquals(new UnorderedPairHash(pt1, pt2), distanceMatrix.getPairWithMinDistance());

        distanceMatrix.deleteRow(pt1);
        Assert.assertNotEquals(new UnorderedPairHash(pt1, pt2), distanceMatrix.getPairWithMinDistance());

        minPair = distanceMatrix.getPairWithMinDistance();
        distanceMatrix.addDistance(new Object(), new Object(), val - 1);
        Assert.assertNotEquals(minPair, distanceMatrix.getPairWithMinDistance());

        minPair = distanceMatrix.getPairWithMinDistance();
        Assert.assertEquals(1, distanceMatrix.getDistance(
                minPair.getNotEqualTo(null),
                minPair.getNotEqualTo(minPair.getNotEqualTo(null))), 1e-8);
    }

    @Test
    public void testGetPairWithMaxDistance() {
        double val = 2;
        Object[] points = {new Object(), new Object(), new Object(), new Object()};
        DistanceMatrix distanceMatrix = makeMatrix(val, points);

        UnorderedPair minPair = distanceMatrix.getPairWithMaxDistance();
        Object pt1 = minPair.getNotEqualTo(null);
        Object pt2 = minPair.getNotEqualTo(pt1);

        Assert.assertEquals(val, distanceMatrix.getDistance(pt1, pt2), 1e-8);

        distanceMatrix.addDistance(pt1, pt2, val + 1);
        Assert.assertEquals(new UnorderedPairHash(pt1, pt2), distanceMatrix.getPairWithMaxDistance());

        distanceMatrix.deleteDistance(pt1, pt2);
        Assert.assertNotEquals(new UnorderedPairHash(pt1, pt2), distanceMatrix.getPairWithMaxDistance());

        distanceMatrix.addDistance(pt1, pt2, val + 1);
        Assert.assertEquals(new UnorderedPairHash(pt1, pt2), distanceMatrix.getPairWithMaxDistance());

        distanceMatrix.deleteRow(pt1);
        Assert.assertNotEquals(new UnorderedPairHash(pt1, pt2), distanceMatrix.getPairWithMaxDistance());

        minPair = distanceMatrix.getPairWithMinDistance();
        distanceMatrix.addDistance(new Object(), new Object(), val + 1);
        Assert.assertNotEquals(minPair, distanceMatrix.getPairWithMaxDistance());

        minPair = distanceMatrix.getPairWithMaxDistance();
        Assert.assertEquals(val + 1, distanceMatrix.getDistance(
                minPair.getNotEqualTo(null),
                minPair.getNotEqualTo(minPair.getNotEqualTo(null))), 1e-8);
    }

    @Test
    public void testMinMaxPair_EmptyMatrix() {
        DistanceMatrix distanceMatrix = new DistanceMatrixTree();
        Assert.assertEquals(null, distanceMatrix.getPairWithMinDistance());
        Assert.assertEquals(null, distanceMatrix.getPairWithMaxDistance());
    }

    /**
     * @param val
     * @param points
     * @return distance matrix which contains all possible unordered pairs (subsets of two elements)
     * made from points with distance = val
     */
    private DistanceMatrix makeMatrix(double val, Object... points) {
        DistanceMatrix distanceMatrix = new DistanceMatrixTree();
        for (int i = 0; i < points.length; ++i) {
            for (int j = i + 1; j < points.length; ++j) {
                distanceMatrix.addDistance(points[i], points[j], val);
            }
        }
        return distanceMatrix;
    }
}
