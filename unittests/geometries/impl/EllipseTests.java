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
 * Unit tests for class {@link Ellipse}.
 * The tests verify:
 * <ul>
 * <li>{@link Ellipse} constructor validity</li>
 * <li>{@link Ellipse#getNormal(Point)}</li>
 * <li>{@link Ellipse#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
@Tag("unit")
public class EllipseTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    EllipseTests() { /* to satisfy Javadoc generator */ }

    /**
     * Circular disk of radius 2 centered at the origin, lying in the XY plane
     */
    private static final Ellipse DISK = new Ellipse(Point.ZERO, Vector.AXIS_Z, 2);

    /**
     * Elliptical patch centered at the origin, lying in the XY plane, radius 3 along X and 1 along Y
     */
    private static final Ellipse ELLIPSE = new Ellipse(Point.ZERO, Vector.AXIS_Z, Vector.AXIS_X, 3, 1);

    /**
     * Error message for failed Ellipse construction
     */
    private static final String ERR_CONSTRUCTOR = "Failed to construct an ellipse";
    /**
     * Error message for incorrect normal
     */
    private static final String ERROR_INCORRECT_NORMAL = "ERROR: Incorrect normal";
    /**
     * Error message for incorrect result
     */
    private static final String ERR_INCORRECT_INTERSECTION = "ERROR: Incorrect intersections";

    /**
     * Test method for {@link Ellipse} constructor.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct circular disk
        assertDoesNotThrow(() -> new Ellipse(Point.ZERO, Vector.AXIS_Z, 2), ERR_CONSTRUCTOR);
        // EP02: Correct ellipse with distinct radii
        assertDoesNotThrow(() -> new Ellipse(Point.ZERO, Vector.AXIS_Z, Vector.AXIS_X, 3, 1), ERR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Non-positive radius is invalid
        assertThrows(IllegalArgumentException.class, () -> new Ellipse(Point.ZERO, Vector.AXIS_Z, 0),
                ERR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Ellipse#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normal is the plane's normal regardless of which point on the disk is queried
        assertEquals(Vector.AXIS_Z, DISK.getNormal(new Point(1, 0, 0)), ERROR_INCORRECT_NORMAL);
        assertEquals(Vector.AXIS_Z, DISK.getNormal(Point.ZERO), ERROR_INCORRECT_NORMAL);
    }

    /**
     * Test method for {@link Ellipse#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray hits the disk within its radius (1 point)
        Ray ray1 = new Ray(new Point(1, 0, -1), Vector.AXIS_Z);
        assertEquals(List.of(new Point(1, 0, 0)), DISK.findIntersections(ray1), ERR_INCORRECT_INTERSECTION);

        // EP02: Ray hits the disk's supporting plane, but outside the radius (0 points)
        Ray ray2 = new Ray(new Point(3, 0, -1), Vector.AXIS_Z);
        assertNull(DISK.findIntersections(ray2), ERR_INCORRECT_INTERSECTION);

        // EP03: Ray is parallel to the disk's plane (0 points)
        Ray ray3 = new Ray(new Point(0, 0, 1), Vector.AXIS_X);
        assertNull(DISK.findIntersections(ray3), ERR_INCORRECT_INTERSECTION);

        // =============== Boundary Values Tests ==================
        // BV01: Ray hits exactly on the disk's boundary circle (1 point)
        Ray ray4 = new Ray(new Point(2, 0, -1), Vector.AXIS_Z);
        assertEquals(List.of(new Point(2, 0, 0)), DISK.findIntersections(ray4), ERR_INCORRECT_INTERSECTION);

        // BV02: Ray hits exactly the center point (1 point)
        Ray ray5 = new Ray(new Point(0, 0, -1), Vector.AXIS_Z);
        assertEquals(List.of(Point.ZERO), DISK.findIntersections(ray5), ERR_INCORRECT_INTERSECTION);

        // BV03: Elliptical (non-circular) patch - point within the long radius but outside the short one (0 points)
        Ray ray6 = new Ray(new Point(2.5, 0, -1), Vector.AXIS_Z);
        assertEquals(List.of(new Point(2.5, 0, 0)), ELLIPSE.findIntersections(ray6), ERR_INCORRECT_INTERSECTION);
        Ray ray7 = new Ray(new Point(0, 1.5, -1), Vector.AXIS_Z);
        assertNull(ELLIPSE.findIntersections(ray7), ERR_INCORRECT_INTERSECTION);
    }
}
