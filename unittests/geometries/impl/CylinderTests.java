package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link Cylinder}.
 * The tests verify:
 * <ul>
 * <li>{@link Cylinder} constructor validity</li>
 * <li>{@link Cylinder#getNormal(Point)}</li>
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
}
