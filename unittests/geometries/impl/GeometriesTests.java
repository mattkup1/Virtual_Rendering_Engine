package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for class {@link Geometries}.
 * The tests verify:
 * <ul>
 * <li>Geometries constructor validity</li>
 * <li>{@link Geometries#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
public class GeometriesTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    GeometriesTests() {/* to satisfy Javadoc generator */ }

    private static final String ERR_INCORRECT_INTERSECTIONS = "ERROR: Number of intersections is incorrect";

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}
     */
    @Test
    public void testFindIntersections() {

        final Ray ray1 = new Ray(new Point(-2, -2, -5), Vector.AXIS_Z); // Some shapes (3 points)
        final Ray ray2 = new Ray(new Point(5, 5, 1), Vector.AXIS_Z); // No shapes
        final Ray ray3 = new Ray(new Point(5, 5, -5), Vector.AXIS_Z); // 1 Shape (1 Point)
        final Ray ray4 = new Ray(new Point(-1, -1, -5), Vector.AXIS_Z); // All shapes
        final Plane plane0 = new Plane(Point.ZERO, Vector.AXIS_Z);
        final Sphere sphere1 = new Sphere(new Point(1, 1, 8), 3.0);
        final Sphere sphere2 = new Sphere(new Point(-1, -1, 3), 2.0);
        final Geometries geometries = new Geometries(plane0, sphere1, sphere2);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Some (but not all) shapes are intersected
        assertEquals(3, geometries.findIntersections(ray1).size(), ERR_INCORRECT_INTERSECTIONS);

        // =============== Boundary Values Tests ==================
        // BV11: No shapes are intersected -> null
        assertNull(geometries.findIntersections(ray2), ERR_INCORRECT_INTERSECTIONS);
        // BV12: Only 1 shape is intersected -> 1 Point
        assertEquals(1, geometries.findIntersections(ray3).size(), ERR_INCORRECT_INTERSECTIONS);
        // BV13: All shapes are intersected -> A few points
        assertEquals(4, geometries.findIntersections(ray4).size(), ERR_INCORRECT_INTERSECTIONS);
    }

}
