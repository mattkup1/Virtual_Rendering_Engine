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
 * The tests verify:
 * <ul>
 * <li>Sphere constructor validity</li>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * <li>{@link Sphere#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class SphereTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    SphereTests() {
    }

    // ========== Static Constants for Testing ==========

    /**
     * Radius value of 1.0 used for unit sphere tests
     */
    private static final double RADIUS1 = 1.0;

    /**
     * Point at (1,0,0) used as a center or intersection point
     */
    private static final Point P100 = new Point(1, 0, 0);

    /**
     * Point at (1,1,0) used for boundary value tests on the sphere surface
     */
    private static final Point P110 = new Point(1, 1, 0);

    /**
     * Point at (1,1,1) used as the center for the standard test sphere
     */
    private static final Point P111 = new Point(1, 1, 1);

    /**
     * Point at (1,1,2) used as an expected intersection point
     */
    private static final Point P112 = new Point(1, 1, 2);

    /**
     * Point at (-1,0,0) used as a ray origin outside the sphere
     */
    private static final Point Pn100 = new Point(-1, 0, 0);

    /**
     * Point at (-1,0,1) used as a ray origin for intersection tests
     */
    private static final Point Pn101 = new Point(-1, 0, 1);

    /**
     * Point at (2,1,1) used to test the normal on the sphere surface
     */
    private static final Point P211 = new Point(2, 1, 1);

    /**
     * Unit vector in the X direction
     */
    private static final Vector V100 = new Vector(1, 0, 0);

    /**
     * Vector (1,1,0) used for rays missing the sphere
     */
    private static final Vector V110 = new Vector(1, 1, 0);

    /**
     * Vector (3,1,0) used for rays crossing the sphere at an angle
     */
    private static final Vector V310 = new Vector(3, 1, 0);

    /**
     * Vector (-1,0,1) used for boundary intersection tests
     */
    private static final Vector V_n101 = new Vector(-1, 0, 1);

    /**
     * Sphere with center (1,1,1) and radius 1.0
     */
    private static final Sphere SPHERE_P111 = new Sphere(P111, RADIUS1);

    /**
     * Sphere with center (1,0,0) and radius 1.0
     */
    private static final Sphere SPHERE_P100 = new Sphere(P100, RADIUS1);

    // ========== Expected Results ==========

    /**
     * First expected intersection point for EP02
     */
    private static final Point INTERSECTION1 = new Point(0.0651530771650466, 0.355051025721682, 0);

    /**
     * Second expected intersection point for EP02
     */
    private static final Point INTERSECTION2 = new Point(1.53484692283495, 0.844948974278318, 0);

    /**
     * Expected list containing two intersection points
     */
    private static final List<Point> EXP_TWO_PTS = List.of(INTERSECTION1, INTERSECTION2);

    /**
     * Expected list containing a single intersection point at (1,1,2)
     */
    private static final List<Point> EXP_SINGLE_P112 = List.of(P112);

    // ========== Error Messages ==========

    /**
     * Error message for sphere construction failure
     */
    private static final String ERROR_CTOR = "Failed to construct a sphere";

    /**
     * Error message for incorrect normal calculation
     */
    private static final String ERROR_GET_NORMAL = "getNormal should return the right normal";

    /**
     * Error message for incorrect intersection results
     */
    private static final String ERROR_FIND_INTERSECTIONS = "Wrong sphere intersection result";

    /**
     * Test method for {@link Sphere} constructor
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct sphere defined by center and radius
        assertDoesNotThrow(() -> new Sphere(P111, RADIUS1), ERROR_CTOR);
    }

    /**
     * Test method for {@link Sphere#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard normal calculation
        assertEquals(V100, SPHERE_P111.getNormal(P211), ERROR_GET_NORMAL);
    }

    /**
     * Test method for {@link Sphere#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Ray's line is outside the sphere (0 points)
        assertNull(SPHERE_P111.findIntersections(new Ray(Pn100, V110)),
                ERROR_FIND_INTERSECTIONS);

        // EP02: Ray starts before and crosses the sphere (2 points)
        assertEquals(EXP_TWO_PTS,
                SPHERE_P100.findIntersections(new Ray(Pn101, V310)),
                ERROR_FIND_INTERSECTIONS);

        // EP03: Ray starts inside the sphere (1 point)
        assertEquals(EXP_SINGLE_P112,
                SPHERE_P100.findIntersections(new Ray(new Point(1, 1, 0.5), Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // EP04: Ray starts after the sphere (0 points)
        assertNull(SPHERE_P111.findIntersections(new Ray(Pn100, V310.scale(-1))),
                ERROR_FIND_INTERSECTIONS);

        // =============== Boundary Values Tests ==================

        // **** Group 1: Ray's line crosses the sphere (but not through center)
        // BV11: Ray starts at sphere and goes inside (1 point)
        assertEquals(List.of(new Point(0, 1, 1)),
                SPHERE_P100.findIntersections(new Ray(P110, V_n101)),
                ERROR_FIND_INTERSECTIONS);

        // BV12: Ray starts at sphere and goes outside (0 points)
        assertNull(SPHERE_P100.findIntersections(new Ray(P110, V_n101.scale(-1))),
                ERROR_FIND_INTERSECTIONS);

        // **** Group 2: Ray's line goes through the center
        // BV21: Ray starts before the sphere (2 points)
        assertEquals(List.of(P110, P112),
                SPHERE_P100.findIntersections(new Ray(new Point(1, 1, -1), Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // BV22: Ray starts at sphere and goes inside (1 point)
        assertEquals(EXP_SINGLE_P112,
                SPHERE_P100.findIntersections(new Ray(P110, Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // BV23: Ray starts inside (not at center) (1 point)
        assertEquals(EXP_SINGLE_P112,
                SPHERE_P100.findIntersections(new Ray(new Point(1, 1, 1.5), Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // BV24: Ray starts at the center (1 point)
        assertEquals(EXP_SINGLE_P112,
                SPHERE_P100.findIntersections(new Ray(P111, Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // BV25: Ray starts at sphere and goes outside (0 points)
        assertNull(SPHERE_P100.findIntersections(new Ray(P112, Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // BV26: Ray starts after sphere (0 points)
        assertNull(SPHERE_P100.findIntersections(new Ray(new Point(1, 1, 3), Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // **** Group 3: Ray's line is tangent to the sphere (all tests 0 points)
        // BV31: Ray starts before the tangent point
        assertNull(SPHERE_P100.findIntersections(new Ray(P100, Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // BV32: Ray starts at the tangent point
        assertNull(SPHERE_P100.findIntersections(new Ray(Pn101, Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // BV33: Ray starts after the tangent point
        assertNull(SPHERE_P100.findIntersections(new Ray(new Point(1, 0, 2), Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // **** Group 4: Special cases
        // BV41: Ray's line is outside sphere, orthogonal to center line
        assertNull(SPHERE_P100.findIntersections(new Ray(new Point(1, -1, 1), Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);

        // BV42: Ray starts inside, orthogonal to center line
        assertNull(SPHERE_P100.findIntersections(new Ray(new Point(1, 0.5, 1), Vector.AXIS_Z)),
                ERROR_FIND_INTERSECTIONS);
    }
}