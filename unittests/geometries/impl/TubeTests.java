package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
     * Default constructor to satisfy JavaDoc generator
     */
    TubeTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Head point of the tube axis ray
     */
    private static final Point AXIS_HEAD = new Point(1, 0, 0);
    /**
     * Direction vector of the tube axis ray
     */
    private static final Vector AXIS_DIRECTION = new Vector(1, 0, 0);
    /**
     * Axis ray of the test tube
     */
    private static final Ray AXIS_RAY = new Ray(AXIS_HEAD, AXIS_DIRECTION);
    /**
     * Radius of the test tube
     */
    private static final double RADIUS = 1.0;
    /**
     * Tube defined by the test axis ray and radius
     */
    private static final Tube TUBE = new Tube(RADIUS, AXIS_RAY);
    /**
     * Point on the tube opposite the axis ray
     */
    private static final Point POINT_OPPOSITE_AXIS_RAY = new Point(2, 1, 0);
    /**
     * Point on the tube opposite behind the axis ray
     */
    private static final Point POINT_OPPOSITE_BEHIND_AXIS_RAY = new Point(0, 1, 0);
    /**
     * Point on the tube opposite the axis head
     */
    private static final Point POINT_OPPOSITE_AXIS_HEAD = new Point(1, 1, 0);
    /**
     * Normal vector at the test points on the tube
     */
    private static final Vector NORMAL_VECTOR = new Vector(0, 1, 0);
    /**
     * Test method for {@link Tube#getNormal(Point)}
     */


    @Test
    void tesrConstructor() {
        // ============ Equivalence Partitions Tests ==============
        assertDoesNotThrow(() -> new Tube(RADIUS, AXIS_RAY),
                "Failed to construct a plane");
    }

    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: getNormal returns the expected normal vector at a point opposite the axis ray
        assertEquals(NORMAL_VECTOR, TUBE.getNormal(POINT_OPPOSITE_AXIS_RAY),
                "getNormal should return the expected normal vector at a point opposite the axis ray");

        // EP02: getNormal returns the expected normal vector at a point opposite behind the axis ray
        assertEquals(NORMAL_VECTOR, TUBE.getNormal(POINT_OPPOSITE_BEHIND_AXIS_RAY),
                "getNormal should return the expected normal vector at a point opposite behind the axis ray");

        // =============== Boundary Values Tests ==================

        // BV01: getNormal returns the expected normal vector at a point opposite the axis head
        assertEquals(NORMAL_VECTOR, TUBE.getNormal(POINT_OPPOSITE_AXIS_HEAD),
                "getNormal should return the expected normal vector at a point opposite the axis head");
    }
}
