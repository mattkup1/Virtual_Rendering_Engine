package primitives;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
class RayTests {

    /**
     * Default constructor to satisfy JavaDoc generator
     */
    RayTests() { /* To satisfy Javadoc generator */ }

    /**
     * {@link Point} (1,0,0) used in some tests
     */
    private static final Point P100 = new Point(1, 0, 0);
    /**
     * {@link Point} (1,2,3) used in some tests
     */
    private static final Point P123 = new Point(1, 2, 3);
    /**
     * {@link Point} (4,5,6) used in some tests
     */
    private static final Point P456 = new Point(4, 5, 6);
    /**
     * Non-unit {@link Vector} (10,0,0) used in some tests
     */
    private static final Vector vNonUnit = new Vector(10, 0, 0);
    /**
     * {@link Ray} with origin (1,2,3) and direction -> (1,0,0) used in some tests
     */
    private static final Ray RAYp123v100 = new Ray(P123, Vector.AXIS_X);
    /**
     * {@link Ray} with origin (1,2,3) and direction -> (1,0,0)
     * (same values as {@link Vector#AXIS_X})
     * used in some tests
     */
    private static final Ray ray1Same = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));
    /**
     * {@link Ray} with origin (1,2,3) and direction -> (0,1,0) used in some tests
     */
    private static final Ray RAYp123v010 = new Ray(P123, Vector.AXIS_Y);
    /**
     * {@link Ray} with origin (4,5,6) and direction -> (1,0,0) used in some tests
     */
    private static final Ray RAYp456v100 = new Ray(P456, Vector.AXIS_X);
    /**
     * {@link Ray} with origin (1,0,0) and direction -> (1,0,0) used in some tests
     */
    private static final Ray RAYp100v100 = new Ray(P100, Vector.AXIS_X);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for failed ray construction
     */
    private static final String ERROR_CTOR = "ERROR: Failed to construct a valid Ray";
    /**
     * Error message for incorrect ray direction
     */
    private static final String ERROR_CTOR_DIRECTION =
            "ERROR: Ray direction should be the same as the unit vector provided";
    /**
     * Error message for non-normalized direction vector
     */
    private static final String ERROR_CTOR_NORMALIZE_DIRECTION =
            "ERROR: Ray constructor must normalize the direction vector";
    /**
     * Error message for ray construction with zero vector as direction
     */
    private static final String ERROR_CTOR_ZERO_DIRECTION =
            "ERROR: Construction of ray with zero vector as direction should throw exception";
    /**
     * Error message for ray comparison
     */
    private static final String ERROR_EQUAL = "ERROR: Rays should be equal";
    /**
     * Error message for ray comparison
     */
    private static final String ERROR_NOT_EQUAL = "ERROR: Rays should not be equal";
    /**
     * Error message for {@link RayTests#testGetPoint()} method
     */
    private static final String ERROR_WRONG_RESULT_GET_POINT = "ERROR: GetPoint() produced wrong result";
    /**
     * Error message for {@link RayTests#testFindClosestPoint()}
     */
    private static final String ERR_CLOSEST_POINT = "ERROR: FindClosestPoint() produced wrong result";

    /**
     * Test method for {@link primitives.Ray#Ray(Point, Vector)}.
     */
    @Test
    void testConstructor() {
        final Ray ray = new Ray(P100, Vector.AXIS_Z);

        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct ray construction with unit vector
        assertDoesNotThrow(() -> new Ray(P123, Vector.AXIS_X), ERROR_CTOR);
        assertEquals(Vector.AXIS_X, RAYp123v100.getDirection(), ERROR_CTOR_DIRECTION);

        // EP02: Ray construction with non-unit vector (should normalize)
        Ray ray2 = new Ray(P123, vNonUnit);
        assertEquals(1.0, ray2.getDirection().length(), DELTA, ERROR_CTOR_NORMALIZE_DIRECTION);

        // =============== Boundary Values Tests ==================

        // BV01: Ray construction with zero vector as direction (should throw exception)
        assertThrows(IllegalArgumentException.class,
                () -> new Ray(P123, new Vector(0, 0, 0)), ERROR_CTOR_ZERO_DIRECTION);
    }

    /**
     * Test method for {@link primitives.Ray#getPoint(double)}
     */
    @Test
    void testGetPoint() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: t < 0 - return a point
        assertEquals(new Point(-1, 0, 0), RAYp100v100.getPoint(-2), ERROR_WRONG_RESULT_GET_POINT);
        // EP02: t > 0 - return a point
        assertEquals(new Point(2, 0, 0), RAYp100v100.getPoint(1), ERROR_WRONG_RESULT_GET_POINT);

        // =============== Boundary Values Tests ==================
        // BV11: t = 0 - return the origin
        assertEquals(RAYp100v100.getOrigin(), RAYp100v100.getPoint(0), ERROR_WRONG_RESULT_GET_POINT);
    }

    /**
     * Test method for {@link Ray#findClosestPoint(List)}
     */
    @Test
    void testFindClosestPoint() {

        final Point closest = new Point(2, 0, 0); // Closest point
        final Point pn300 = new Point(-3, 0, 0);
        final Point pn200 = new Point(-2, 0, 0);
        final Point p500 = new Point(5, 0, 0);
        final Point p600 = new Point(6, 0, 0);
        final Ray ray = new Ray(P100, Vector.AXIS_Z);

        // ============ Equivalence Partitions Tests ==============

        // EP01: Middle point is the closest to ray origin
        final var points1 = List.of(pn300, pn200, closest, p500, p600);
        assertEquals(closest, ray.findClosestPoint(points1), ERR_CLOSEST_POINT);

        // =============== Boundary Values Tests ==================

        // BV11: Points list is null
        assertNull(ray1Same.findClosestPoint(null), ERR_CLOSEST_POINT);

        // BV12: First point in the list is the closest to the ray origin
        final var points2 = List.of(closest, pn300, pn200, p600, p500);
        assertEquals(closest, ray.findClosestPoint(points2), ERR_CLOSEST_POINT);

        // BV13: Last point in the list is the closest to the ray origin
        final var points3 = List.of(pn300, pn200, p500, p600, closest);
        assertEquals(closest, ray.findClosestPoint(points3), ERR_CLOSEST_POINT);
    }


    /**
     * Test method for {@link primitives.Ray#equals(Object)}.
     */
    @Test
    void testEquals() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Rays with same origin and direction should be equal
        assertEquals(RAYp123v100, ray1Same, ERROR_EQUAL);

        // EP02: Rays with different directions should not be equal
        assertNotEquals(RAYp123v100, RAYp123v010, ERROR_NOT_EQUAL);

        // EP03: Rays with different origins should not be equal
        assertNotEquals(RAYp123v100, RAYp456v100, ERROR_NOT_EQUAL);

        // EP04: Comparison with null
        assertNotEquals(null, RAYp123v100, ERROR_NOT_EQUAL);
    }
}