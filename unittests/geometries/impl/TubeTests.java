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
 * Unit tests for class {@link Tube}.
 * The tests verify:
 * <ul>
 * <li>Tube constructor validity</li>
 * <li>{@link Tube#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
public class TubeTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    TubeTests() { /* to satisfy Javadoc generator */ }

    // ================== CONSTANTS ==================

    // Points
    /**
     * {@link Point} (1,0,0) used in some tests
     */
    private static final Point P100 = new Point(1, 0, 0);
    /**
     * {@link Point} (0,0,1) used in some tests
     */
    private static final Point P001 = new Point(0, 0, 1);
    /**
     * {@link Point} (0,1,1) used in some tests
     */
    private static final Point P011 = new Point(0, 1, 1);
    /**
     * {@link Point} (0,1,1) used in some tests
     */
    private static final Point P111 = new Point(1, 1, 1);
    /**
     * {@link Point} (0,-1,0) used in some tests
     */
    private static final Point P0N10 = new Point(-1, -1, 0);
    /**
     * {@link Point} (-1,-1,0) used in some tests
     */
    private static final Point PN1N10 = new Point(-1, -1, 0);
    /**
     * {@link Point} (2,0,-1) used in some tests
     */
    private static final Point P20N1 = new Point(2, 0, -1);
    /**
     * {@link Point} (2,1,0) on the tube opposite the axis ray
     */
    private static final Point P210 = new Point(2, 1, 0);
    /**
     * {@link Point} (0,1,0) on the tube opposite behind the axis ray
     */
    private static final Point P010 = new Point(0, 1, 0);
    /**
     * {@link Point} (1,1,0) on the tube opposite the axis head
     */
    private static final Point P110 = new Point(1, 1, 0);
    /**
     * {@link Point} (-1,0,0) used in some tests
     */
    private static final Point PN100 = new Point(-1, 0, 0);
    /**
     * {@link Point} (0,0,-1) used in some tests
     */
    private static final Point P00N1 = new Point(0, 0, -1);

    // Vectors
    /**
     * {@link Vector} -> (0,1,1) used in some tests
     */
    private static final Vector V110 = new Vector(1, 1, 0);
    /**
     * {@link Vector} -> (0,1,1) used in some tests
     */
    private static final Vector V011 = new Vector(0, 1, 1);
    /**
     * {@link Vector} -> (1,1,1) used in some tests
     */
    private static final Vector V111 = new Vector(1, 1, 1);
    /**
     * {@link Vector} -> (1,0,-1) used in some tests
     */
    private static final Vector V10N1 = new Vector(1, 0, -1);

    // Test tube and its components
    /**
     * Axis ray of the test tube
     */
    private static final Ray AXIS_RAY = new Ray(P100, Vector.AXIS_X);
    /**
     * Radius of the test tube
     */
    private static final double RADIUS = 1.0;
    /**
     * Tube defined by the test axis ray and radius used in most tests
     */
    private static final Tube TUBE = new Tube(RADIUS, AXIS_RAY);

    // Expected values

    /**
     * Expected {@link List} of ray-tube intersections used in some tests
     */
    private static final List<Point> EXPECTEDL001 = List.of(P001);
    /**
     * Expected {@link List} of ray-tube intersections used in some tests
     */
    private static final List<Point> EXPECTEDL20N1 = List.of(P20N1);
    /**
     * Expected {@link List} of ray-tube intersections used in some tests
     */
    private static final List<Point> EXPECTEDL201 = List.of(new Point(2, 0, 1));

    // Error messages
    /**
     * Error message for failed plane construction
     */
    private static final String ERROR_FAILED_CONSTRUCTOR = "Failed to construct a tube";
    /**
     * Error message for an unexpected normal vector
     */
    private static final String INCORRECT_VECTOR_NORMAL = "getNormal should return the right normal";
    /**
     * Error message for incorrect intersections
     */
    private static final String ERR_INCORRECT_INTERSECTIONS = "ERROR: Incorrect intersections";

    /**
     * Test method for {@link Tube#getNormal(Point)}
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        assertDoesNotThrow(() -> new Tube(RADIUS, AXIS_RAY), ERROR_FAILED_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Tube#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: getNormal returns the expected normal vector at a point opposite the axis ray
        assertEquals(Vector.AXIS_Y, TUBE.getNormal(P210), INCORRECT_VECTOR_NORMAL);

        // EP02: getNormal returns the expected normal vector at a point opposite behind the axis ray
        assertEquals(Vector.AXIS_Y, TUBE.getNormal(P010), INCORRECT_VECTOR_NORMAL);

        // =============== Boundary Values Tests ==================

        // BV01: getNormal returns the expected normal vector at a point opposite the axis head
        assertEquals(Vector.AXIS_Y, TUBE.getNormal(P110), INCORRECT_VECTOR_NORMAL);
    }

    /**
     * Test method for {@link Tube#findIntersections(Ray)}
     */
    @Test
    void testGetIntersections() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray starts before the tube and intersects the tube tweice (2 points)
        final Ray ray1 = new Ray(new Point(-2, -2, -1), V111);
        final List<Point> EXPECTED1 = List.of(PN1N10, P001);
        assertEquals(EXPECTED1, TUBE.findIntersections(ray1), ERR_INCORRECT_INTERSECTIONS);

        // EP02: Ray starts inside the tube and intersects the tube once (1 point)
        final Ray ray2 = new Ray(new Point(-0.5, -0.5, 0.5), V111);
        assertEquals(EXPECTEDL001, TUBE.findIntersections(ray2), ERR_INCORRECT_INTERSECTIONS);

        // EP03: Ray starts outside the tube and does not intersect the tube in either direction (null)
        final Ray ray3 = new Ray(new Point(1, 1, 5), V111);
        assertNull(TUBE.findIntersections(ray3), ERR_INCORRECT_INTERSECTIONS);

        // EP04: Ray starts after the tube and does not intersect the tube in the ray's direction (null)
        final Ray ray4 = new Ray(P111, V111);
        assertNull(TUBE.findIntersections(ray4), ERR_INCORRECT_INTERSECTIONS);

        // =============== Boundary Values Tests ==================

        // Group 1: Ray is tangent to the tube (all null)
        // BV11: Ray starts before the tangent point
        final Ray ray5 = new Ray(new Point(-5, -5, 1), V110);
        assertNull(TUBE.findIntersections(ray5), ERR_INCORRECT_INTERSECTIONS);

        // BV12: Ray starts at the tangent point
        final Ray ray6 = new Ray(P001, V110);
        assertNull(TUBE.findIntersections(ray6), ERR_INCORRECT_INTERSECTIONS);

        // BV13: Ray starts after the tangent point
        final Ray ray7 = new Ray(P111, V110);
        assertNull(TUBE.findIntersections(ray7), ERR_INCORRECT_INTERSECTIONS);

        // Group 2: Ray starts on the tube
        // BV14: Ray continues into the tube (1 point)
        final Ray ray8 = new Ray(new Point(-1, -1, 1), V111);
        assertEquals(EXPECTEDL001, TUBE.findIntersections(ray8), ERR_INCORRECT_INTERSECTIONS);

        // BV15: Ray continues outside the tube (null)
        final Ray ray9 = new Ray(P001, V111);
        assertNull(TUBE.findIntersections(ray9), ERR_INCORRECT_INTERSECTIONS);

        // Group 3: Ray direction vector orthogonal to the tube axis - regular cases
        // BV16: Ray starts before the tube and intersects the tube twice (2 points)
        final Ray ray10 = new Ray(new Point(0, -2, -1), V011);
        final List<Point> EXPECTED10 = List.of(P0N10, P001);
        assertEquals(EXPECTED10, TUBE.findIntersections(ray10), ERR_INCORRECT_INTERSECTIONS);

        // BV17: Ray starts inside the tube and intersects the tube once (1 point)
        final Ray ray11 = new Ray(new Point(0, -0.5, 0.5), V011);
        assertEquals(EXPECTEDL001, TUBE.findIntersections(ray11), ERR_INCORRECT_INTERSECTIONS);

        // BV18: Ray starts outside the tube and does not intersect the tube in either direction (null)
        final Ray ray12 = new Ray(new Point(0, 0, 5), V011);
        assertNull(TUBE.findIntersections(ray12), ERR_INCORRECT_INTERSECTIONS);

        // BV19: Ray starts after the tube and does not intersect the tube in the ray direction (null)
        final Ray ray13 = new Ray(P011, V011);
        assertNull(TUBE.findIntersections(ray13), ERR_INCORRECT_INTERSECTIONS);

        // Group 4: Ray direction is orthogonal to the tube axis and ray is tangent to the tube
        // BV20: Ray starts before the tangent point
        final Ray ray14 = new Ray(new Point(0, -5, 1), Vector.AXIS_Y);
        assertNull(TUBE.findIntersections(ray14), ERR_INCORRECT_INTERSECTIONS);

        // BV21: Ray starts at the tangent point
        final Ray ray15 = new Ray(P001, Vector.AXIS_Y);
        assertNull(TUBE.findIntersections(ray15), ERR_INCORRECT_INTERSECTIONS);

        // BV22: Ray starts after the tangent point
        final Ray ray16 = new Ray(P011, Vector.AXIS_Y);
        assertNull(TUBE.findIntersections(ray16), ERR_INCORRECT_INTERSECTIONS);

        // Group 5: Ray direction is orthogonal to the tube axis and Ray starts on the tube
        // BV23: Ray direction inside the tube (1 point)
        final Ray ray17 = new Ray(P0N10, V011);
        assertEquals(EXPECTEDL001, TUBE.findIntersections(ray17), ERR_INCORRECT_INTERSECTIONS);

        // BV24: Ray direction outside the tube (null)
        final Ray ray18 = new Ray(P001, V011);
        assertNull(TUBE.findIntersections(ray18), ERR_INCORRECT_INTERSECTIONS);

        // Group 6: Ray intersects the tube axis
        // BV25: Ray starts before the tube (2 points)
        final Ray ray19 = new Ray(new Point(-1, 0, 2), V10N1);
        final List<Point> EXPECTED19 = List.of(P001, P20N1);
        assertEquals(EXPECTED19, TUBE.findIntersections(ray19), ERR_INCORRECT_INTERSECTIONS);

        // BV26: Ray starts on the tube (1 point)
        final Ray ray20 = new Ray(P001, V10N1);
        assertEquals(EXPECTEDL20N1, TUBE.findIntersections(ray20), ERR_INCORRECT_INTERSECTIONS);

        // BV27: Ray starts inside the tube before the tube axis (1 point)
        final Ray ray21 = new Ray(new Point(0.5, 0, 0.5), V10N1);
        assertEquals(EXPECTEDL20N1, TUBE.findIntersections(ray21), ERR_INCORRECT_INTERSECTIONS);

        // BV28: Ray starts on the tube axis and continues outwards (1 point)
        final Ray ray22 = new Ray(new Point(2, 0, 0), Vector.AXIS_Z);
        assertEquals(EXPECTEDL20N1, TUBE.findIntersections(ray22), ERR_INCORRECT_INTERSECTIONS);

        // Group 7: Ray parallel to the tube asix (null)
        // BV29: Ray outside tube
        final Ray ray23 = new Ray(new Point(0, 0, 2), Vector.AXIS_X);
        assertNull(TUBE.findIntersections(ray23), ERR_INCORRECT_INTERSECTIONS);

        // BV30: Ray inside tube but not on axis
        final Ray ray24 = new Ray(new Point(0, 0, 0.5), Vector.AXIS_X);
        assertNull(TUBE.findIntersections(ray24), ERR_INCORRECT_INTERSECTIONS);

        // BV31: Ray on tube
        final Ray ray25 = new Ray(P001, Vector.AXIS_X);
        assertNull(TUBE.findIntersections(ray25), ERR_INCORRECT_INTERSECTIONS);

        // BV32: Ray on tube axis
        final Ray ray26 = new Ray(P100, Vector.AXIS_X);
        assertNull(TUBE.findIntersections(ray26), ERR_INCORRECT_INTERSECTIONS);

        // Group 8: Ray direction is orthogonal to the tube axis and ray intersects the tube axis
        // BV33: Ray starts before the tube (2 points)
        final Ray ray27 = new Ray(new Point(-2, 0, 0), Vector.AXIS_Z);
        final List<Point> EXPECTED27 = List.of(P00N1, P001);
        assertEquals(EXPECTED27, TUBE.findIntersections(ray27), ERR_INCORRECT_INTERSECTIONS);

        // BV34: Ray starts after the tube (null)
        final Ray ray28 = new Ray(new Point(0, 0, 2), Vector.AXIS_Z);
        assertNull(TUBE.findIntersections(ray28), ERR_INCORRECT_INTERSECTIONS);

        // BV35: Ray starts on the tube and goes inwards (1 point)
        final Ray ray29 = new Ray(PN100, Vector.AXIS_Z);
        assertEquals(EXPECTEDL001, TUBE.findIntersections(ray29), ERR_INCORRECT_INTERSECTIONS);

        // BV36: Ray starts on the tube and goes outwards (null)
        final Ray ray30 = new Ray(P001, Vector.AXIS_Z);
        assertNull(TUBE.findIntersections(ray30), ERR_INCORRECT_INTERSECTIONS);

        // BV37: Ray starts inside the tube but not on the tube axis
        final Ray ray31 = new Ray(new Point(0, 0, -0.5), Vector.AXIS_Z);
        assertEquals(EXPECTEDL001, TUBE.findIntersections(ray31), ERR_INCORRECT_INTERSECTIONS);

        // BV38: Ray starts on the tube axis
        final Ray ray32 = new Ray(P100, Vector.AXIS_Z);
        final List<Point> EXPECTED32 = List.of(new Point(1, 0, 1));
        assertEquals(EXPECTED32, TUBE.findIntersections(ray32), ERR_INCORRECT_INTERSECTIONS);

        // Group 9: Ray intersects the tube origin point and ray is orthogonal to the tube axis
        // BV39: Ray starts before the tube (2 points)
        final Ray ray33 = new Ray(new Point(2, 0, -2), Vector.AXIS_Z);
        final List<Point> EXPECTED33 = List.of(new Point(2, 0, -1), new Point(2, 0, 1));
        assertEquals(EXPECTED33, TUBE.findIntersections(ray33), ERR_INCORRECT_INTERSECTIONS);

        // BV40: Ray starts inside the tube (1 point)
        final Ray ray34 = new Ray(new Point(2, 0, -0.5), Vector.AXIS_Z);
        assertEquals(EXPECTEDL201, TUBE.findIntersections(ray34), ERR_INCORRECT_INTERSECTIONS);

        // Bv41: Ray starts after the tube (null)
        final Ray ray35 = new Ray(new Point(2, 0, 2), Vector.AXIS_Z);
        assertNull(TUBE.findIntersections(ray35), ERR_INCORRECT_INTERSECTIONS);

        // BV42: Ray starts on the tube origin point and continues outwards (1 point)
        final Ray ray36 = new Ray(P100, V10N1);
        assertEquals(EXPECTEDL201, TUBE.findIntersections(ray36), ERR_INCORRECT_INTERSECTIONS);
    }
}
