package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Point}
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
public class PointTests {
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

    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct point construction
        assertDoesNotThrow(() -> new Point(1, 2, 3), "Failed to construct a valid Point");
    }

    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Ensure subtraction returns
        assertEquals(new Vector(1, -1, 0), POINT_X.subtract(POINT_Y),
                "subtract(Point) produced wrong vector");

        // =============== Boundary Values Tests ==================
        // BV01: Subtracting a point from itself should throw exception (Zero Vector)
        assertThrows(IllegalArgumentException.class, () -> POINT_X.subtract(POINT_X),
                "Subtracting a point from itself must throw an exception");
    }

    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard addition of a vector to a point
        assertEquals(new Point(1, 1, 0), POINT_X.add(VECTOR_Y),
                "add() produced wrong coordinates");

        // =============== Boundary Values Tests ==================
        // BV01: Add negation vector to the point (resulting in ZERO)
        assertEquals(Point.ZERO, POINT_X.add(NEG_VECTOR_X),
                "Failed to add negation vector to point");
    }

    @Test
    void testDistanceSquared() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard distance squared between two points
        assertEquals(2.0, POINT_X.distanceSquared(POINT_Y), DELTA,
                "distanceSquared() between (1,0,0) and (0,1,0) should be 2");

        // =============== Boundary Values Tests ==================
        // BV01: Distance from a point to itself should be 0
        assertEquals(0, POINT_X.distanceSquared(POINT_X), DELTA,
                "distanceSquared() from a point to itself should be 0");
    }

    @Test
    void testDistance() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard distance (3-4-5 triangle logic)
        Point p1 = new Point(0, 3, 0);
        Point p2 = new Point(4, 0, 0);
        assertEquals(5.0, p1.distance(p2), DELTA,
                "distance() between (0,3,0) and (4,0,0) should be 5");

        // =============== Boundary Values Tests ==================
        // BV01: Distance to itself
        assertEquals(0, POINT_X.distance(POINT_X), DELTA,
                "distance() from a point to itself should be 0");
    }
}
