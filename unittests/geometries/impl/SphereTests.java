package geometries.impl;


import java.util.List;

import org.junit.jupiter.api.Test;

import primitives.Point;

import primitives.Ray;

import primitives.Vector;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Unit tests for class {@link Sphere}.
 * <p>
 * The tests verify:
 *
 * <ul>
 *
 * <li>Sphere constructor validity</li>
 *
 * <li>{@link Sphere#getNormal(Point)}</li>
 *
 * </ul>
 * <p>
 * Tests follow the methodology of
 * <p>
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */

public class SphereTests {


    /**
     * Default constructor to satisfy Javadoc generator
     */

    SphereTests() {/* to satisfy Javadoc generator */ }


    /**
     * {@link Point} used in some tests
     */

    private static final Point P111 = new Point(1, 1, 1);

    /**
     * {@link Point} (-1,0,1) used in some tests
     */

    private static final Point Pn101 = new Point(-1, 0, 1);

    /**
     * {@link Point} (1,0,0) used in some tests
     */

    private static final Point P100 = new Point(1, 0, 0);

    /**
     * {@link Point} (-1,0,0) used in some tests
     */

    private static final Point P112 = new Point(1, 1, 2);

    /**
     * {@link Point} (1,1,2) used in some tests
     */

    private static final Point Pn100 = new Point(-1, 0, 0);

    /**
     * Radius of the test sphere
     */

    private static final double RADIUS1 = 1.0;

    /**
     * {@link Sphere} defined by the test center and radius
     */

    private static final Sphere SPHEREp111r1 = new Sphere(P111, RADIUS1);

    /**
     * {@link Point} on the surface of the test sphere
     */

    private static final Point P211 = new Point(2, 1, 1);

    /**
     * Normal vector at the test point on the sphere
     */

    private static final Vector V100 = new Vector(1, 0, 0);

    /**
     * Error message for failed sphere construction
     */

    private static final String FAILED_CONSTRUCTOR_ERROR = "Failed to construct a sphere";

    /**
     * Error message for an unexpected normal vector
     */

    private static final String UNMATCH_VECTOR_NORMAL = "getNormal should return the right normal";

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

    private static final List<Point> EXPECTED1 = List.of(INTERSECTION1, INTERSECTION2);

    private static final List<Point> EXPECTED_L112 = List.of(P112);

    /**
     * Sphere used in some tests
     */

    private static final Sphere SPHEREp100r1 = new Sphere(P100, RADIUS1);

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

        assertEquals(V100, SPHEREp111r1.getNormal(P211), UNMATCH_VECTOR_NORMAL);

    }


    /**
     * Test method for {@link Sphere#findIntersections(primitives.Ray)}.
     */

    @Test
    void testFindIntersections() {

// ============ Equivalence Partitions Tests ============== //

// EP01: Ray's straight is outside the sphere (0 points)

        assertNull(SPHEREp111r1.findIntersections(new Ray(Pn100, V110)), ERROR_SPHERE_INTERSECTION);

// EP02: Ray starts before and crosses the sphere (2 points)

        final var result1 = SPHEREp100r1.findIntersections(new Ray(Pn100, V310));

        assertEquals(EXPECTED1, result1, ERROR_SPHERE_INTERSECTION); // works also for null or wrong size


// EP03: Ray starts inside the sphere (1 point)

        final Ray ray1 = new Ray(new Point(1, 1, 0.5), Vector.AXIS_Z);

        final var result2 = SPHEREp111r1.findIntersections(ray1);

        final var P112 = new Point(1, 1, 2);

        final var expected2 = List.of(P112);

        assertEquals(expected2, result2, ERROR_SPHERE_INTERSECTION);


// EP04: Ray starts after the sphere (0 points)

// Use opposite direction vector from EP02

        assertNull(SPHEREp111r1.findIntersections(new Ray(Pn100, V310.scale(-1))), ERROR_SPHERE_INTERSECTION);


// =============== Boundary Values Tests ==================

// **** Group 1: Ray's line crosses the sphere (but not the center)

// BV11: Ray starts at sphere and goes inside (1 point)

        final Point P110 = new Point(1, 1, 0);

        final Vector vector1 = new Vector(-1, 0, 1);

        final Ray ray2 = new Ray(P110, vector1);

        final var result3 = SPHEREp111r1.findIntersections(ray2);

        final var expected3 = List.of(new Point(0, 1, 1));

        assertEquals(expected3, result3, ERROR_SPHERE_INTERSECTION);

// BV12: Ray starts at sphere and goes outside (0 points)

        final Ray ray3 = new Ray(P110, vector1.scale(-1));

        assertNull(SPHEREp100r1.findIntersections(ray3), ERROR_SPHERE_INTERSECTION);


// **** Group 2: Ray's line goes through the center

// BV21: Ray starts before the sphere (2 points)

        final Ray ray4 = new Ray(new Point(1, 1, -1), Vector.AXIS_Z);

        final var result4 = SPHEREp111r1.findIntersections(ray4);

        final var expected4 = List.of(P110, P112);

        assertEquals(expected4, result4, ERROR_SPHERE_INTERSECTION);

// BV22: Ray starts at sphere and goes inside (1 point)

        final Ray ray5 = new Ray(P110, Vector.AXIS_Z);

        final var result5 = SPHEREp111r1.findIntersections(ray5);

        assertEquals(EXPECTED_L112, result5, ERROR_SPHERE_INTERSECTION);

// BV23: Ray starts inside (1 point)

        final Ray ray6 = new Ray(new Point(1, 1, 1.5), Vector.AXIS_Z);

        final var result6 = SPHEREp111r1.findIntersections(ray6);

        assertEquals(EXPECTED_L112, result6, ERROR_SPHERE_INTERSECTION);

// BV24: Ray starts at the center (1 point)

        final Ray ray7 = new Ray(P111, Vector.AXIS_Z);

        final var result7 = SPHEREp111r1.findIntersections(ray7);

        assertEquals(EXPECTED_L112, result7, ERROR_SPHERE_INTERSECTION);

// BV25: Ray starts at sphere and goes outside (0 points)

        final Ray ray8 = new Ray(P112, Vector.AXIS_Z);

        assertNull(SPHEREp111r1.findIntersections(ray8), ERROR_SPHERE_INTERSECTION);

// BV26: Ray starts after sphere (0 points)

        final Ray ray9 = new Ray(new Point(1, 1, 3), Vector.AXIS_Z);

        assertNull(SPHEREp111r1.findIntersections(ray9), ERROR_SPHERE_INTERSECTION);


// **** Group 3: Ray's line is tangent to the sphere (all tests 0 points)

// BV31: Ray starts before the tangent point

        final Ray ray10 = new Ray(P100, Vector.AXIS_Z);

        assertNull(SPHEREp111r1.findIntersections(ray10), ERROR_SPHERE_INTERSECTION);

// BV32: Ray starts at the tangent point

        final Ray ray11 = new Ray(new Point(1, 0, 1), Vector.AXIS_Z);

        assertNull(SPHEREp111r1.findIntersections(ray11), ERROR_SPHERE_INTERSECTION);

// BV33: Ray starts after the tangent point

        final Ray ray12 = new Ray(new Point(1, 0, 2), Vector.AXIS_Z);

        assertNull(SPHEREp111r1.findIntersections(ray12), ERROR_SPHERE_INTERSECTION);


// **** Group 4: Special cases

// BV41: Ray's line is outside sphere, ray is orthogonal to ray start to sphere's center line

        final Ray ray13 = new Ray(new Point(1, -1, 1), Vector.AXIS_Z);

        assertNull(SPHEREp111r1.findIntersections(ray13), ERROR_SPHERE_INTERSECTION);

// BV42: Ray's starts inside, ray is orthogonal to ray start to sphere's center line

        final Ray ray14 = new Ray(new Point(1, 0.5, 1), Vector.AXIS_Z);

        assertNull(SPHEREp100r1.findIntersections(ray14), ERROR_SPHERE_INTERSECTION);

    }

}