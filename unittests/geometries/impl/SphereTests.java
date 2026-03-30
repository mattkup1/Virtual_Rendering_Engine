package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link Sphere}.
 * The tests verify:
 * <ul>
 * <li>Sphere constructor validity</li>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
public class SphereTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    SphereTests() {/* to satisfy JavaDoc generator */ }

    /**
     * Center point of the test sphere
     */
    private static final Point P111 = new Point(1, 1, 1);
    /**
     * Radius of the test sphere
     */
    private static final double RADIUS1 = 1.0;
    /**
     * Sphere defined by the test center and radius
     */
    private static final Sphere SPHERE1 = new Sphere(P111, RADIUS1);
    /**
     * Point on the surface of the test sphere
     */
    private static final Point POINT_ON_SPHERE = new Point(2, 1, 1);
    /**
     * Normal vector at the test point on the sphere
     */
    private static final Vector NORMAL_VECTOR = new Vector(1, 0, 0);
    /**
     * Error message for failed sphere construction
     */
    private static final String FAILED_CONSTRUCTOR_ERROR = "Failed to construct a sphere";
    /**
     * Error message for an unexpected normal vector
     */
    private static final String UNMATCH_VECTOR_NORMAL = "getNormal should return the right normal";

    /**
     * Point (0,0,1) used in some tests
     */
    private static final Point P001 = new Point(0, 0, 1);
    /**
     * Point (1,0,0) used in some tests
     */
    private static final Point P100 = new Point(1, 0, 0);
    /**
     * Point (-1,0,0) used in some tests
     */
    private static final Point P01 = new Point(-1, 0, 0);
    /**
     * Vector (0,0,1) used in some tests
     */
    private static final Vector V001 = new Vector(0, 0, 1);
    /**
     * Vector (3,1,0) used in some tests
     */
    private static final Vector V310 = new Vector(3, 1, 0);
    /**
     * Vector (1,1,0) used in some tests
     */
    private static final Vector V110 = new Vector(1, 1, 0);
    /**
     * Point used as intersection in some tests
     */
    private static final Point INTERSECTION1 = new Point(0.0651530771650466, 0.355051025721682, 0);
    /**
     * Point used as intersection in some tests
     */
    private static final Point INTERSECTION2 = new Point(1.53484692283495, 0.844948974278318, 0);
    /**
     * Expected list of intersections in some tests
     */
    //private static final var EXPECTED1 = List.of(INTERSECTION1, INTERSECTION2);
    /**
     * Sphere used in some tests
     */
    private static final Sphere SPHERE2 = new Sphere(P100, RADIUS1);
    /**
     * Error message for sphere intersection failures
     */
    private static final String ERROR_SPHERE_INTERSECTION = "Wrong sphere intersection result";

    /**
     * Test method for {@link Sphere} constructor
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct plane defined by center and radius
        assertDoesNotThrow(() -> new Sphere(P111, RADIUS1), FAILED_CONSTRUCTOR_ERROR);
    }

    /**
     * Test method for {@link Sphere#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        assertEquals(NORMAL_VECTOR, SPHERE1.getNormal(POINT_ON_SPHERE), UNMATCH_VECTOR_NORMAL);
    }

    /**
     * Test method for {@link Sphere#findIntersections(primitives.Ray)}.
     */
    /*
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ============== //
        // EP01: Ray's straight is outside the sphere (0 points)
        assertNull(SPHERE1.findIntersections(new Ray(P01, V110)), ERROR_SPHERE_INTERSECTION);
        // EP02: Ray starts before and crosses the sphere (2 points)
        final var result1 = sphere.findIntersections(new Ray(P01, V310));
        assertNotNull(result1, ERROR_SPHERE_INTERSECTION); // not necessary
        assertEquals(2, result1.size(), ERROR_SPHERE_INTERSECTION); // not necessary
        assertEquals(EXPECTED1, result1, ERROR_SPHERE_INTERSECTION); // works also for null or wrong size

        // EP03: Ray starts inside the sphere (1 point)
        // TODO
        // EP04: Ray starts after the sphere (0 points)
        // TODO
        // =============== Boundary Values Tests ==================
        // **** Group 1: Ray's line crosses the sphere (but not the center)
        // BV11: Ray starts at sphere and goes inside (1 points)
        // BV12: Ray starts at sphere and goes outside (0 points)
        // **** Group 2: Ray's line goes through the center
        // BV21: Ray starts before the sphere (2 points)
        // BV22: Ray starts at sphere and goes inside (1 points)
        // BV23: Ray starts inside (1 points)
        // BV24: Ray starts at the center (1 points)
        // BV25: Ray starts at sphere and goes outside (0 points)
        // BV26: Ray starts after sphere (0 points)
        // **** Group 3: Ray's line is tangent to the sphere (all tests 0 points)
        // BV31: Ray starts before the tangent point
        // BV32: Ray starts at the tangent point
        // BV33: Ray starts after the tangent point
        // **** Group 4: Special cases
        // BV41: Ray's line is outside sphere, ray is orthogonal to ray start to sphere's center line
        // BV42: Ray's starts inside, ray is orthogonal to ray start to sphere's center line
    }
*/
}