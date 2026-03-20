package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
    RayTests() { /* To satisfy JavaDoc generator */ }

    /**
     * Point (1,2,3) used in ray tests
     */
    private static final Point p1 = new Point(1, 2, 3);
    /**
     * Point (4,5,6) used in ray tests
     */
    private static final Point p2 = new Point(4, 5, 6);
    /**
     * Vector -> (1,0,0) used in ray tests
     */
    private static final Vector v1 = new Vector(1, 0, 0);
    /**
     * Vector -> (0,1,0) used in ray tests
     */
    private static final Vector v2 = new Vector(0, 1, 0);
    /**
     * Non-unit vector (10,0,0) used in ray tests
     */
    private static final Vector vNonUnit = new Vector(10, 0, 0);
    /**
     * Ray with origin (1,2,3) and direction -> (1,0,0) used in ray tests
     */
    private static final Ray ray1 = new Ray(p1, v1);
    /**
     * Ray with origin (1,2,3) and direction -> (1,0,0)
     * (same values as {@link RayTests#v1})
     * used in ray tests
     */
    private static final Ray ray1Same = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));
    /**
     * Ray with origin (1,2,3) and direction -> (0,1,0) used in ray tests
     */
    private static final Ray ray2 = new Ray(p1, v2);
    /**
     * Ray with origin (4,5,6) and direction -> (1,0,0) used in ray tests
     */
    private static final Ray ray3 = new Ray(p2, v1);

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
            "Construction of ray with zero vector as direction should throw exception";
    /**
     * Error message for ray comparison
     */
    private static final String ERROR_EQUAL = "Rays should be equal";
    /**
     * Error message for ray comparison
     */
    private static final String ERROR_NOT_EQUAL = "Rays should not be equal";


    /**
     * Test method for {@link primitives.Ray#Ray(Point, Vector)}.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct ray construction with unit vector
        assertDoesNotThrow(() -> new Ray(p1, v1), ERROR_CTOR);
        assertEquals(v1, ray1.getDirection(), ERROR_CTOR_DIRECTION);

        // EP02: Ray construction with non-unit vector (should normalize)
        Ray ray2 = new Ray(p1, vNonUnit);
        assertEquals(1.0, ray2.getDirection().length(), DELTA, ERROR_CTOR_NORMALIZE_DIRECTION);

        // =============== Boundary Values Tests ==================
        // BV01: Ray construction with zero vector as direction (should throw exception)
        assertThrows(IllegalArgumentException.class,
                () -> new Ray(p1, new Vector(0, 0, 0)), ERROR_CTOR_ZERO_DIRECTION);
    }

    /**
     * Test method for {@link primitives.Ray#equals(Object)}.
     */
    @Test
    void testEquals() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Rays with same origin and direction should be equal
        assertEquals(ray1, ray1Same, ERROR_EQUAL);

        // EP02: Rays with different directions should not be equal
        assertNotEquals(ray1, ray2, ERROR_NOT_EQUAL);

        // EP03: Rays with different origins should not be equal
        assertNotEquals(ray1, ray3, ERROR_NOT_EQUAL);

        // EP04: Comparison with null
        assertNotEquals(null, ray1, ERROR_NOT_EQUAL);
    }
}