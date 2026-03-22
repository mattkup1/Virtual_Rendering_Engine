package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link primitives.Point}
 * These tests verify:
 * <ul>
 * <li>Point constructor validity</li>
 * <li>{@link Point#subtract(Point)}</li>
 * <li>{@link Point#add(Vector)}</li>
 * <li>{@link Point#distanceSquared(Point)} (Point)}</li>
 * <li>{@link Point#distance(Point)}</li>
 * </ul>
 * <p>
 * Tests follow the methodology of
 * Equivalence Partitioning (EP)
 * and Boundary Values (BVA)
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class PointTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    PointTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Point (1,0,0) used in point tests
     */
    private static final Point POINT_X = new Point(1, 0, 0);
    /**
     * Point (0,1,0) used in point tests
     */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /**
     * Vector -> (0,1,0) used in point tests
     */
    private static final Vector VECTOR_Y = new Vector(0, 1, 0);
    /**
     * Vector -> (-1,0,0) used in point tests
     */
    private static final Vector NEG_VECTOR_X = new Vector(-1, 0, 0);
    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;
    /**
     * Error message - Failed Point construction
     */
    private static final String ERROR_CONSTRUCTOR = "Failed to construct a valid Point";
    /**
     * Error message - Exception expected
     */
    private static final String ERROR_EXPECTED_EXCEPTION = "ERROR: Expected exception";
    /**
     * Error message - Incorrect result
     */
    private static final String ERROR_INCORRECT_RESULT = "ERROR: Incorrect result";

    /**
     * Test method for {@link Point} constructors
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct point construction for ctor with 3 doubles arguments
        assertDoesNotThrow(() -> new Point(1, 2, 3), ERROR_CONSTRUCTOR);

        // EP02: Correct point construction for ctor with Double3 argument
        assertDoesNotThrow(() -> new Point(new Double3(1, 2, 3)), ERROR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Point#subtract(Point)}
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Ensure subtraction returns
        assertEquals(new Vector(1, -1, 0), POINT_X.subtract(POINT_Y), ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Subtracting a point from itself should throw exception (Zero Vector)
        assertThrows(IllegalArgumentException.class, () -> POINT_X.subtract(POINT_X), ERROR_EXPECTED_EXCEPTION);
    }

    /**
     * Test method for {@link Point#add(Vector)}
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard addition of a vector to a point
        assertEquals(new Point(1, 1, 0), POINT_X.add(VECTOR_Y), ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Add negation vector to the point (resulting in ZERO)
        assertEquals(Point.ZERO, POINT_X.add(NEG_VECTOR_X), ERROR_INCORRECT_RESULT);
    }

    /**
     * Test method for {@link Point#distanceSquared(Point)}
     */
    @Test
    void testDistanceSquared() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard distance squared between two points
        assertEquals(2.0, POINT_X.distanceSquared(POINT_Y), DELTA, ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Distance from a point to itself should be 0
        assertEquals(0, POINT_X.distanceSquared(POINT_X), DELTA, ERROR_INCORRECT_RESULT);
    }

    /**
     * Test method for {@link Point#distance(Point)}
     */
    @Test
    void testDistance() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard distance (3-4-5 triangle logic)
        Point p1 = new Point(0, 3, 0);
        Point p2 = new Point(4, 0, 0);
        assertEquals(5.0, p1.distance(p2), DELTA, ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Distance to itself
        assertEquals(0, POINT_X.distance(POINT_X), DELTA, ERROR_INCORRECT_RESULT);
    }
}
