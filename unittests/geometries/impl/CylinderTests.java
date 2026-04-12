package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

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
public class CylinderTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    CylinderTests() { /* to satisfy Javadoc generator */ }

    /*
        For these tests we will construct a cylinder with the following properties:
        Radius = 1.0
        Origin = (1,0,0)
        Direction = (1,0,0) (positive X axis)
        Height = 5.0
     */
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
     * Direction vector of the test cylinder axis ray
     */
    private static final Vector DIRECTION = new Vector(1, 0, 0);
    /**
     * Axis ray of the test cylinder
     */
    private static final Ray AXIS = new Ray(ORIGIN, DIRECTION);
    /**
     * Cylinder used in Cylinder tests
     */
    private static final Cylinder CYLINDER = new Cylinder(RADIUS, AXIS, HEIGHT);

    /**
     * Point on the round surface of the test cylinder
     */
    private static final Point pointOnRoundSurface = new Point(2, 1, 0);
    /**
     * Point on the top base of the test cylinder
     */
    private static final Point pointOnTopBase = new Point(6, 0.5, 0.5);
    /**
     * Center point of the top base of the test cylinder
     */
    private static final Point pointMidTopBase = new Point(6, 0, 0);
    /**
     * Point on the bottom base of the test cylinder
     */
    private static final Point pointOnBottomBase = new Point(1, 0.5, 0.5);
    /**
     * Center point of the bottom base of the test cylinder
     */
    private static final Point pointMidBottomBase = new Point(1, 0, 0);
    /**
     * Point on the edge of the top base of the test cylinder
     */
    private static final Point pointEdgeTopBase = new Point(6, 1, 0);
    /**
     * Point on the edge of the bottom base of the test cylinder
     */
    private static final Point pointEdgeBottomBase = new Point(1, 1, 0);

    /**
     * Normal vector to the top base of the test cylinder
     */
    private static final Vector normalToTopBase = new Vector(1, 0, 0);
    /**
     * Normal vector to the bottom base of the test cylinder
     */
    private static final Vector normalToBottomBase = new Vector(-1, 0, 0);
    /**
     * Normal vector to the round surface of the test cylinder
     */
    private static final Vector normalToRoundSurface = new Vector(0, 1, 0);

    /**
     * Error message for failed Cylinder construction
     */
    private static final String FAILED_CONSTRUCTOR_ERROR = "Failed to construct a cylinder";

    /**
     * Error message for incorrect result
     */
    private static final String ERROR_INCORRECT_RESULT = "ERROR: Incorrect result";

    /**
     * Test method for {@link Cylinder} constructor.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct plane defined by radius ray and height
        assertDoesNotThrow(() -> new Cylinder(RADIUS, AXIS, HEIGHT), FAILED_CONSTRUCTOR_ERROR);
    }

    /**
     * Test method for {@link Cylinder#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on top base
        assertEquals(normalToTopBase, CYLINDER.getNormal(pointOnTopBase), ERROR_INCORRECT_RESULT);
        // EP02: Point on bottom base
        assertEquals(normalToBottomBase, CYLINDER.getNormal(pointOnBottomBase), ERROR_INCORRECT_RESULT);
        // EP03: Point on round surface
        assertEquals(normalToRoundSurface, CYLINDER.getNormal(pointOnRoundSurface), ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Point on edge of top base
        assertEquals(normalToTopBase, CYLINDER.getNormal(pointEdgeTopBase), ERROR_INCORRECT_RESULT);
        // BV02: Point on edge of bottom base
        assertEquals(normalToBottomBase, CYLINDER.getNormal(pointEdgeBottomBase), ERROR_INCORRECT_RESULT);
        // BV03: Point on center of top base
        assertEquals(normalToTopBase, CYLINDER.getNormal(pointMidTopBase), ERROR_INCORRECT_RESULT);
        // BV04: Point on center of bottom base
        assertEquals(normalToBottomBase, CYLINDER.getNormal(pointMidBottomBase), ERROR_INCORRECT_RESULT);
    }

    /**
     * Test method for {@link Cylinder#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray intersects the round surface twice within the height (2 points)
        Ray ray1 = new Ray(new Point(3, -2, 0), new Vector(0, 1, 0));
        var result1 = CYLINDER.findIntersections(ray1);
        assertEquals(2, result1.size(), "Wrong number of points");
        assertEquals(List.of(new Point(3, -1, 0), new Point(3, 1, 0)), result1, ERROR_INCORRECT_RESULT);

        // EP02: Ray intersects both the bottom and top bases (2 points)
        Ray ray2 = new Ray(new Point(0, 0.5, 0), new Vector(1, 0, 0));
        var result2 = CYLINDER.findIntersections(ray2);
        assertEquals(2, result2.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, 0.5, 0), new Point(6, 0.5, 0)), result2, ERROR_INCORRECT_RESULT);

        // EP03: Ray intersects the round surface and one of the bases (2 points)
        Ray ray3 = new Ray(new Point(0, 2, 0), new Vector(3, -1, 0));
        var result3 = CYLINDER.findIntersections(ray3);
        assertEquals(2, result3.size(), "Wrong number of points");
        assertEquals(List.of(new Point(3, 1, 0), new Point(6, 0, 0)), result3, ERROR_INCORRECT_RESULT);

        // EP04: Ray misses the cylinder completely (0 points)
        Ray ray4 = new Ray(new Point(0, 2, 0), new Vector(0, 1, 0));
        assertNull(CYLINDER.findIntersections(ray4), ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================

        // BV01: Ray intersects the infinite tube, but outside the cylinder's height (0 points)
        Ray ray5 = new Ray(new Point(8, -2, 0), new Vector(0, 1, 0));
        assertNull(CYLINDER.findIntersections(ray5), ERROR_INCORRECT_RESULT);

        // BV02: Ray intersects the base plane, but outside the base radius (0 points)
        Ray ray6 = new Ray(new Point(0, 2, 0), new Vector(1, 0, 0));
        assertNull(CYLINDER.findIntersections(ray6), ERROR_INCORRECT_RESULT);

        // BV03: Ray starts inside the cylinder and goes out through the round surface (1 point)
        Ray ray7 = new Ray(new Point(3, 0, 0), new Vector(0, 1, 0));
        var result7 = CYLINDER.findIntersections(ray7);
        assertEquals(1, result7.size(), "Wrong number of points");
        assertEquals(List.of(new Point(3, 1, 0)), result7, ERROR_INCORRECT_RESULT);

        // BV04: Ray starts inside the cylinder and goes out through a base (1 point)
        Ray ray8 = new Ray(new Point(3, 0, 0), new Vector(1, 0, 0));
        var result8 = CYLINDER.findIntersections(ray8);
        assertEquals(1, result8.size(), "Wrong number of points");
        assertEquals(List.of(new Point(6, 0, 0)), result8, ERROR_INCORRECT_RESULT);

        // BV05: Ray starts exactly on the top base and goes inwards (1 point)
        Ray ray9 = new Ray(new Point(6, 0.5, 0), new Vector(-1, 0, 0));
        var result9 = CYLINDER.findIntersections(ray9);
        assertEquals(1, result9.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, 0.5, 0)), result9, ERROR_INCORRECT_RESULT);

        // BV06: Ray starts exactly on the bottom base and goes outwards (0 points)
        Ray ray10 = new Ray(new Point(1, 0.5, 0), new Vector(-1, 0, 0));
        assertNull(CYLINDER.findIntersections(ray10), ERROR_INCORRECT_RESULT);

        // BV07: Ray parallel to the cylinder axis, passing exactly through the bases centers (2 points)
        Ray ray11 = new Ray(new Point(0, 0, 0), new Vector(1, 0, 0));
        var result11 = CYLINDER.findIntersections(ray11);
        assertEquals(2, result11.size(), "Wrong number of points");
        assertEquals(List.of(new Point(1, 0, 0), new Point(6, 0, 0)), result11, ERROR_INCORRECT_RESULT);

        // BV08: Ray starts exactly on the round surface and goes inwards (1 point)
        Ray ray12 = new Ray(new Point(3, 1, 0), new Vector(0, -1, 0));
        var result12 = CYLINDER.findIntersections(ray12);
        assertEquals(1, result12.size(), "Wrong number of points");
        assertEquals(List.of(new Point(3, -1, 0)), result12, ERROR_INCORRECT_RESULT);
    }
}