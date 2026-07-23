package geometries.impl;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for class {@link Cylinder}.
 * The tests verify:
 * <ul>
 * <li>{@link Cylinder} constructor validity</li>
 * <li>{@link Cylinder#getNormal(Point)}</li>
 * <li>{@link Cylinder#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
@Tag("unit")
public class CylinderTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    CylinderTests() { /* to satisfy Javadoc generator */ }

    // ================== CONSTANTS ==================

    // Cylinder
    /**
     * Radius of the test cylinder
     */
    private static final double RADIUS = 1.0;
    /**
     * Height of the test cylinder
     */
    private static final double HEIGHT = 5.0;
    /**
     * Head point of the test cylinder axis ray
     */
    private static final Point ORIGIN = new Point(1, 0, 0);
    /**
     * Axis ray of the test cylinder
     */
    private static final Ray AXIS = new Ray(ORIGIN, Vector.AXIS_X);
    /**
     * Cylinder used in Cylinder tests
     */
    private static final Cylinder CYLINDER = new Cylinder(RADIUS, AXIS, HEIGHT);

    // Points
    /**
     * {@link Point} (2,1,0) on the round surface of the test cylinder
     */
    private static final Point P210 = new Point(2, 1, 0);
    /**
     * {@link Point} (6,0.5,0.5) on the top base of the test cylinder
     */
    private static final Point P60505 = new Point(6, 0.5, 0.5);
    /**
     * {@link Point} (6,0,0) - center point of the top base of the test cylinder
     */
    private static final Point P600 = new Point(6, 0, 0);
    /**
     * {@link Point} (1,0.5,0.5) on the bottom base of the test cylinder
     */
    private static final Point P10505 = new Point(1, 0.5, 0.5);
    /**
     * {@link Point} (1,0,0) - the center point of the bottom base of the test cylinder
     */
    private static final Point P100 = new Point(1, 0, 0);
    /**
     * {@link Point} (6,1,0) on the edge of the top base of the test cylinder
     */
    private static final Point P610 = new Point(6, 1, 0);
    /**
     * {@link Point} (1,1,0) on the edge of the bottom base of the test cylinder
     */
    private static final Point P110 = new Point(1, 1, 0);
    /**
     * {@link Point} (0,2,0) used in some tests
     */
    private static final Point P020 = new Point(0, 2, 0);
    /**
     * {@link Point} (3,0,0) used in some tests
     */
    private static final Point P300 = new Point(3, 0, 0);
    /**
     * {@link Point} (1,0.5,0) used in some tests
     */
    private static final Point P1050 = new Point(1, 0.5, 0);
    /**
     * {@link Point} (3,1,0) used in some tests
     */
    private static final Point P310 = new Point(3, 1, 0);
    /**
     * {@link Point} (3,-1,0) used in some tests
     */
    private static final Point P3N10 = new Point(3, -1, 0);

    // Vectors
    /**
     * Normal {@link Vector} to the bottom base of the test cylinder
     */
    private static final Vector VN100 = new Vector(-1, 0, 0);

    // Error messages
    /**
     * Error message for failed Cylinder construction
     */
    private static final String ERR_CONSTRUCTOR = "Failed to construct a cylinder";
    /**
     * Error message for incorrect normal
     */
    private static final String ERROR_INCORRECT_NORMAL = "ERROR: Incorrect normal";
    /**
     * Error message for incorrect result
     */
    private static final String ERR_INCORRECT_INTERSECTION = "ERROR: Incorrect intersections";

    /**
     * Test method for {@link Cylinder} constructor.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct plane defined by radius ray and height
        assertDoesNotThrow(() -> new Cylinder(RADIUS, AXIS, HEIGHT), ERR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Cylinder#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on top base
        assertEquals(Vector.AXIS_X, CYLINDER.getNormal(P60505), ERROR_INCORRECT_NORMAL);
        // EP02: Point on bottom base
        assertEquals(VN100, CYLINDER.getNormal(P10505), ERROR_INCORRECT_NORMAL);
        // EP03: Point on round surface
        assertEquals(Vector.AXIS_Y, CYLINDER.getNormal(P210), ERROR_INCORRECT_NORMAL);

        // =============== Boundary Values Tests ==================
        // BV01: Point on edge of top base
        assertEquals(Vector.AXIS_X, CYLINDER.getNormal(P610), ERROR_INCORRECT_NORMAL);
        // BV02: Point on edge of bottom base
        assertEquals(VN100, CYLINDER.getNormal(P110), ERROR_INCORRECT_NORMAL);
        // BV03: Point on center of top base
        assertEquals(Vector.AXIS_X, CYLINDER.getNormal(P600), ERROR_INCORRECT_NORMAL);
        // BV04: Point on center of bottom base
        assertEquals(VN100, CYLINDER.getNormal(P100), ERROR_INCORRECT_NORMAL);
    }

    /**
     * Test method for {@link Cylinder#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray intersects the round surface twice within the height (2 points)
        Ray ray1 = new Ray(new Point(3, -2, 0), Vector.AXIS_Y);
        var result1 = CYLINDER.findIntersections(ray1);
        assertEquals(List.of(P3N10, P310), result1,
                ERR_INCORRECT_INTERSECTION);

        // EP02: Ray intersects both the bottom and top bases (2 points)
        Ray ray2 = new Ray(new Point(0, 0.5, 0), Vector.AXIS_X);
        var result2 = CYLINDER.findIntersections(ray2);
        assertEquals(List.of(new Point(1, 0.5, 0), new Point(6, 0.5, 0)), result2,
                ERR_INCORRECT_INTERSECTION);

        // EP03: Ray intersects the round surface and one of the bases (2 points)
        Ray ray3 = new Ray(P020, new Vector(3, -1, 0));
        var result3 = CYLINDER.findIntersections(ray3);
        assertEquals(List.of(P310, P600), result3, ERR_INCORRECT_INTERSECTION);

        // EP04: Ray misses the cylinder completely (0 points)
        Ray ray4 = new Ray(P020, Vector.AXIS_Y);
        assertNull(CYLINDER.findIntersections(ray4), ERR_INCORRECT_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BV01: Ray intersects the infinite tube, but outside the cylinder's height (0 points)
        Ray ray5 = new Ray(new Point(8, -2, 0), Vector.AXIS_Y);
        assertNull(CYLINDER.findIntersections(ray5), ERR_INCORRECT_INTERSECTION);

        // BV02: Ray intersects the base plane, but outside the base radius (0 points)
        Ray ray6 = new Ray(P020, Vector.AXIS_X);
        assertNull(CYLINDER.findIntersections(ray6), ERR_INCORRECT_INTERSECTION);

        // BV03: Ray starts inside the cylinder and goes out through the round surface (1 point)
        Ray ray7 = new Ray(P300, Vector.AXIS_Y);
        assertEquals(List.of(P310), CYLINDER.findIntersections(ray7),
                ERR_INCORRECT_INTERSECTION);

        // BV04: Ray starts inside the cylinder and goes out through a base (1 point)
        Ray ray8 = new Ray(P300, Vector.AXIS_X);
        assertEquals(List.of(P600), CYLINDER.findIntersections(ray8),
                ERR_INCORRECT_INTERSECTION);

        // BV05: Ray starts exactly on the top base and goes inwards (1 point)
        Ray ray9 = new Ray(new Point(6, 0.5, 0), VN100);
        assertEquals(List.of(P1050), CYLINDER.findIntersections(ray9),
                ERR_INCORRECT_INTERSECTION);

        // BV06: Ray starts exactly on the bottom base and goes outwards (0 points)
        Ray ray10 = new Ray(P1050, VN100);
        assertNull(CYLINDER.findIntersections(ray10), ERR_INCORRECT_INTERSECTION);

        // BV07: Ray parallel to the cylinder axis, passing exactly through the bases centers (2 points)
        Ray ray11 = new Ray(Point.ZERO, Vector.AXIS_X);
        assertEquals(List.of(P100, P600), CYLINDER.findIntersections(ray11),
                ERR_INCORRECT_INTERSECTION);

        // BV08: Ray starts exactly on the round surface and goes inwards (1 point)
        Ray ray12 = new Ray(P310, new Vector(0, -1, 0));
        assertEquals(List.of(P3N10), CYLINDER.findIntersections(ray12), ERR_INCORRECT_INTERSECTION);
    }
}