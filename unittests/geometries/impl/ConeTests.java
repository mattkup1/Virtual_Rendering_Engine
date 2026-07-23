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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Cone}.
 * The tests verify:
 * <ul>
 * <li>{@link Cone} constructor validity</li>
 * <li>{@link Cone#getNormal(Point)}</li>
 * <li>{@link Cone#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * <p>
 * The test cone has its apex at the origin, opens along the +X axis, a base
 * radius of 1 and a height of 1 - i.e. a 45-degree half-angle, so the base
 * radius at any x in [0,1] equals x.
 * </p>
 */
@Tag("unit")
public class ConeTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    ConeTests() { /* to satisfy Javadoc generator */ }

    /**
     * Base radius of the test cone
     */
    private static final double RADIUS = 1.0;
    /**
     * Height of the test cone
     */
    private static final double HEIGHT = 1.0;
    /**
     * Axis ray of the test cone: apex at the origin, opening along +X
     */
    private static final Ray AXIS = new Ray(Point.ZERO, Vector.AXIS_X);
    /**
     * Cone used in Cone tests
     */
    private static final Cone CONE = new Cone(RADIUS, AXIS, HEIGHT);

    /**
     * Error message for failed Cone construction
     */
    private static final String ERR_CONSTRUCTOR = "Failed to construct a cone";
    /**
     * Error message for incorrect normal
     */
    private static final String ERROR_INCORRECT_NORMAL = "ERROR: Incorrect normal";
    /**
     * Error message for incorrect result
     */
    private static final String ERR_INCORRECT_INTERSECTION = "ERROR: Incorrect intersections";

    /**
     * Test method for {@link Cone} constructor.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct cone defined by a positive radius, axis ray, and height
        assertDoesNotThrow(() -> new Cone(RADIUS, AXIS, HEIGHT), ERR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Non-positive radius is invalid
        assertThrows(IllegalArgumentException.class, () -> new Cone(0, AXIS, HEIGHT), ERR_CONSTRUCTOR);
        // BV02: Non-positive height is invalid
        assertThrows(IllegalArgumentException.class, () -> new Cone(RADIUS, AXIS, 0), ERR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Cone#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on the lateral surface (45-degree half-angle -> normal tilts 45 degrees back)
        assertEquals(new Vector(-1, 1, 0).normalize(), CONE.getNormal(new Point(0.5, 0.5, 0)),
                ERROR_INCORRECT_NORMAL);

        // =============== Boundary Values Tests ==================
        // BV01: Point at the apex
        assertEquals(new Vector(-1, 0, 0), CONE.getNormal(Point.ZERO), ERROR_INCORRECT_NORMAL);
        // BV02: Point at the center of the base cap
        assertEquals(Vector.AXIS_X, CONE.getNormal(new Point(1, 0, 0)), ERROR_INCORRECT_NORMAL);
        // BV03: Point on the edge of the base cap
        assertEquals(Vector.AXIS_X, CONE.getNormal(new Point(1, 1, 0)), ERROR_INCORRECT_NORMAL);
    }

    /**
     * Test method for {@link Cone#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray crosses the lateral surface twice (2 points)
        Ray ray1 = new Ray(new Point(0.5, -2, 0), Vector.AXIS_Y);
        assertEquals(List.of(new Point(0.5, -0.5, 0), new Point(0.5, 0.5, 0)), CONE.findIntersections(ray1),
                ERR_INCORRECT_INTERSECTION);

        // EP02: Ray misses the cone entirely (0 points)
        Ray ray2 = new Ray(new Point(5, 5, 0), Vector.AXIS_Y);
        assertNull(CONE.findIntersections(ray2), ERR_INCORRECT_INTERSECTION);

        // EP03: Ray crosses both the base cap and the lateral surface (2 points)
        Ray ray3 = new Ray(new Point(2, 0.5, 0), new Vector(-1, 0, 0));
        assertEquals(List.of(new Point(1, 0.5, 0), new Point(0.5, 0.5, 0)), CONE.findIntersections(ray3),
                ERR_INCORRECT_INTERSECTION);

        // =============== Boundary Values Tests ==================
        // BV01: Ray parallel to the axis, inside the base radius (enters through the lateral
        // surface, then exits through the base cap - 2 points)
        Ray ray4 = new Ray(new Point(0, 0.5, 0), Vector.AXIS_X);
        assertEquals(List.of(new Point(0.5, 0.5, 0), new Point(1, 0.5, 0)), CONE.findIntersections(ray4),
                ERR_INCORRECT_INTERSECTION);

        // BV02: Ray passes through the apex along the axis (1 point, the base cap center)
        Ray ray5 = new Ray(new Point(-1, 0, 0), Vector.AXIS_X);
        assertEquals(List.of(new Point(1, 0, 0)), CONE.findIntersections(ray5), ERR_INCORRECT_INTERSECTION);
    }
}
