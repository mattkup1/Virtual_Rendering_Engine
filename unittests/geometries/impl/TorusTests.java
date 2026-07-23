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
 * Unit tests for class {@link Torus}.
 * The tests verify:
 * <ul>
 * <li>{@link Torus} constructor validity</li>
 * <li>{@link Torus#getNormal(Point)}</li>
 * <li>{@link Torus#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * <p>
 * The test torus is centered at the origin with its axis along Z, a major radius
 * of 3 and a minor radius of 1 - so its tube spans x in [2,4] along the X axis at
 * z=0 (and symmetrically at x in [-4,-2]).
 * </p>
 */
@Tag("unit")
public class TorusTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    TorusTests() { /* to satisfy Javadoc generator */ }

    /**
     * Major radius of the test torus
     */
    private static final double MAJOR_RADIUS = 3.0;
    /**
     * Minor radius of the test torus
     */
    private static final double MINOR_RADIUS = 1.0;
    /**
     * Torus used in Torus tests
     */
    private static final Torus TORUS = new Torus(Point.ZERO, Vector.AXIS_Z, MAJOR_RADIUS, MINOR_RADIUS);

    /**
     * Error message for failed Torus construction
     */
    private static final String ERR_CONSTRUCTOR = "Failed to construct a torus";
    /**
     * Error message for incorrect normal
     */
    private static final String ERROR_INCORRECT_NORMAL = "ERROR: Incorrect normal";
    /**
     * Error message for incorrect result
     */
    private static final String ERR_INCORRECT_INTERSECTION = "ERROR: Incorrect intersections";

    /**
     * Test method for {@link Torus} constructor.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct torus with major radius greater than minor radius
        assertDoesNotThrow(() -> new Torus(Point.ZERO, Vector.AXIS_Z, MAJOR_RADIUS, MINOR_RADIUS),
                ERR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Non-positive major radius is invalid
        assertThrows(IllegalArgumentException.class,
                () -> new Torus(Point.ZERO, Vector.AXIS_Z, 0, MINOR_RADIUS), ERR_CONSTRUCTOR);
        // BV02: Non-positive minor radius is invalid
        assertThrows(IllegalArgumentException.class,
                () -> new Torus(Point.ZERO, Vector.AXIS_Z, MAJOR_RADIUS, 0), ERR_CONSTRUCTOR);
        // BV03: Minor radius greater than or equal to major radius is invalid (self-intersecting)
        assertThrows(IllegalArgumentException.class,
                () -> new Torus(Point.ZERO, Vector.AXIS_Z, 1, 1), ERR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Torus#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on the top of the tube (offset from the center circle along the axis)
        assertEquals(Vector.AXIS_Z, TORUS.getNormal(new Point(3, 0, 1)), ERROR_INCORRECT_NORMAL);

        // =============== Boundary Values Tests ==================
        // BV01: Point on the outer equator (farthest point from the axis)
        assertEquals(new Vector(1, 0, 0), TORUS.getNormal(new Point(4, 0, 0)), ERROR_INCORRECT_NORMAL);
        // BV02: Point on the inner equator (closest point to the axis - normal points toward the hole)
        assertEquals(new Vector(-1, 0, 0), TORUS.getNormal(new Point(2, 0, 0)), ERROR_INCORRECT_NORMAL);
    }

    /**
     * Test method for {@link Torus#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray crosses the torus's equatorial plane through all four tube walls (4 points)
        Ray ray1 = new Ray(new Point(-10, 0, 0), Vector.AXIS_X);
        assertEquals(
                List.of(new Point(-4, 0, 0), new Point(-2, 0, 0), new Point(2, 0, 0), new Point(4, 0, 0)),
                TORUS.findIntersections(ray1), ERR_INCORRECT_INTERSECTION);

        // EP02: Ray misses the torus entirely (0 points)
        Ray ray2 = new Ray(new Point(-10, 0, 10), Vector.AXIS_X);
        assertNull(TORUS.findIntersections(ray2), ERR_INCORRECT_INTERSECTION);

        // =============== Boundary Values Tests ==================
        // BV01: Ray along the axis of symmetry, through the donut hole (0 points)
        Ray ray3 = new Ray(new Point(0, 0, -5), Vector.AXIS_Z);
        assertNull(TORUS.findIntersections(ray3), ERR_INCORRECT_INTERSECTION);

        // BV02: Ray starts inside the tube and exits through one wall (1 point)
        Ray ray4 = new Ray(new Point(3, 0, 0), Vector.AXIS_X);
        assertEquals(List.of(new Point(4, 0, 0)), TORUS.findIntersections(ray4), ERR_INCORRECT_INTERSECTION);
    }

    /**
     * Test method for {@link Torus#findIntersections(Ray)} on a torus whose center is not
     * at the origin, to exercise the general (non-zero delta) case of the quartic derivation.
     */
    @Test
    void testFindIntersectionsOffCenter() {

        // Same relative geometry as the 4-point case in testFindIntersections, translated by
        // (5,5,5): center (5,5,5) instead of the origin, ray origin (-5,5,5) instead of (-10,0,0).
        Torus offCenterTorus = new Torus(new Point(5, 5, 5), Vector.AXIS_Z, MAJOR_RADIUS, MINOR_RADIUS);
        Ray ray = new Ray(new Point(-5, 5, 5), Vector.AXIS_X);

        assertEquals(
                List.of(new Point(1, 5, 5), new Point(3, 5, 5), new Point(7, 5, 5), new Point(9, 5, 5)),
                offCenterTorus.findIntersections(ray), ERR_INCORRECT_INTERSECTION);
    }
}
