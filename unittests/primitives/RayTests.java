package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for class {@link Ray}
 * These tests verify:
 * <ul>
 * <li>Ray constructor validity and direction normalization</li>
 * <li>{@link Ray#equals(Object)}</li>
 * </ul>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class RayTests {

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link primitives.Ray#Ray(Point, Vector)}.
     */
    @Test
    void testConstructor() {
        Point p = new Point(1, 2, 3);
        Vector v = new Vector(1, 0, 0);
        Vector vNonUnit = new Vector(10, 0, 0);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct ray construction with unit vector
        Ray ray1 = new Ray(p, v);
        assertDoesNotThrow(() -> new Ray(p, v), "Failed to construct a valid Ray");
        assertEquals(v, ray1.getDirection(), "Ray direction should be the same as the unit vector provided");

        // TC02: Ray construction with non-unit vector (should normalize)
        Ray ray2 = new Ray(p, vNonUnit);
        assertEquals(1.0, ray2.getDirection().length(), DELTA,
                "Ray constructor must normalize the direction vector");
        assertEquals(v, ray2.getDirection(),
                "Ray direction after normalization is incorrect");

        // =============== Boundary Values Tests ==================
        // Note: Vector(0,0,0) is handled by the Vector constructor,
        // so Ray doesn't need to test for a zero-vector input explicitly.
    }

    /**
     * Test method for {@link primitives.Ray#equals(Object)}.
     */
    @Test
    void testEquals() {
        Point p1 = new Point(1, 2, 3);
        Point p2 = new Point(4, 5, 6);
        Vector v1 = new Vector(1, 0, 0);
        Vector v2 = new Vector(0, 1, 0);

        Ray ray1 = new Ray(p1, v1);
        Ray ray1Same = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));
        Ray ray2 = new Ray(p1, v2);
        Ray ray3 = new Ray(p2, v1);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Rays with same origin and direction should be equal
        assertEquals(ray1, ray1Same, "Rays with identical values should be equal");

        // TC02: Rays with different directions should not be equal
        assertNotEquals(ray1, ray2, "Rays with different directions should not be equal");

        // TC03: Rays with different origins should not be equal
        assertNotEquals(ray1, ray3, "Rays with different origins should not be equal");

        // TC04: Comparison with null
        assertNotEquals(null, ray1, "Ray should not be equal to null");
    }
}