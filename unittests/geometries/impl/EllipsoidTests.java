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
 * Unit tests for class {@link Ellipsoid}.
 * The tests verify:
 * <ul>
 * <li>{@link Ellipsoid} constructor validity</li>
 * <li>{@link Ellipsoid#getNormal(Point)}</li>
 * <li>{@link Ellipsoid#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * <p>
 * The test ellipsoid is centered at the origin, stretched along X (radius 2) and
 * unit radius along Y/Z (radius 1) - a "rugby ball" lying on its side.
 * </p>
 */
@Tag("unit")
public class EllipsoidTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    EllipsoidTests() { /* to satisfy Javadoc generator */ }

    /**
     * Radius along the X axis of the test ellipsoid
     */
    private static final double RX = 2.0;
    /**
     * Radius along the Y axis of the test ellipsoid
     */
    private static final double RY = 1.0;
    /**
     * Radius along the Z axis of the test ellipsoid
     */
    private static final double RZ = 1.0;
    /**
     * Ellipsoid used across the tests
     */
    private static final Ellipsoid ELLIPSOID = new Ellipsoid(Point.ZERO, RX, RY, RZ);

    /**
     * Error message for failed Ellipsoid construction
     */
    private static final String ERR_CONSTRUCTOR = "Failed to construct an ellipsoid";
    /**
     * Error message for incorrect normal
     */
    private static final String ERR_NORMAL = "ERROR: Incorrect normal";
    /**
     * Error message for incorrect intersection result
     */
    private static final String ERR_INTERSECTIONS = "ERROR: Incorrect intersections";

    /**
     * Test method for {@link Ellipsoid} constructor.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct ellipsoid with positive radii
        assertDoesNotThrow(() -> new Ellipsoid(Point.ZERO, RX, RY, RZ), ERR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Non-positive radius is invalid
        assertThrows(IllegalArgumentException.class, () -> new Ellipsoid(Point.ZERO, 0, RY, RZ), ERR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Ellipsoid#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point at the +X vertex (the stretched axis)
        assertEquals(new Vector(1, 0, 0), ELLIPSOID.getNormal(new Point(2, 0, 0)), ERR_NORMAL);
        // EP02: Point at the +Y vertex
        assertEquals(new Vector(0, 1, 0), ELLIPSOID.getNormal(new Point(0, 1, 0)), ERR_NORMAL);
        // EP03: Point at the +Z vertex
        assertEquals(new Vector(0, 0, 1), ELLIPSOID.getNormal(new Point(0, 0, 1)), ERR_NORMAL);

        // =============== Boundary Values Tests ==================
        // BV01: Normal is a unit vector even off the coordinate axes
        Ellipsoid unitSphere = new Ellipsoid(Point.ZERO, 1, 1, 1);
        assertEquals(1, unitSphere.getNormal(new Point(1, 0, 0)).length(), 1e-9, ERR_NORMAL);
    }

    /**
     * Test method for {@link Ellipsoid#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray along the stretched (X) axis crosses both X vertices
        Ray ray1 = new Ray(new Point(-5, 0, 0), Vector.AXIS_X);
        assertEquals(List.of(new Point(-2, 0, 0), new Point(2, 0, 0)), ELLIPSOID.findIntersections(ray1),
                ERR_INTERSECTIONS);

        // EP02: Ray along Y crosses both Y vertices (radius 1)
        Ray ray2 = new Ray(new Point(0, -5, 0), Vector.AXIS_Y);
        assertEquals(List.of(new Point(0, -1, 0), new Point(0, 1, 0)), ELLIPSOID.findIntersections(ray2),
                ERR_INTERSECTIONS);

        // EP03: Ray misses the ellipsoid entirely
        Ray ray3 = new Ray(new Point(0, 5, 5), Vector.AXIS_X);
        assertNull(ELLIPSOID.findIntersections(ray3), ERR_INTERSECTIONS);

        // =============== Boundary Values Tests ==================
        // BV01: Ray starts at the center (1 point, the exit)
        Ray ray4 = new Ray(Point.ZERO, Vector.AXIS_X);
        assertEquals(List.of(new Point(2, 0, 0)), ELLIPSOID.findIntersections(ray4), ERR_INTERSECTIONS);

        // BV02: Ray starts on the surface and points outward (0 points)
        Ray ray5 = new Ray(new Point(2, 0, 0), Vector.AXIS_X);
        assertNull(ELLIPSOID.findIntersections(ray5), ERR_INTERSECTIONS);

        // BV03: Ray is tangent to the ellipsoid (0 points)
        Ray ray6 = new Ray(new Point(-5, 1, 0), Vector.AXIS_X);
        assertNull(ELLIPSOID.findIntersections(ray6), ERR_INTERSECTIONS);
    }
}
