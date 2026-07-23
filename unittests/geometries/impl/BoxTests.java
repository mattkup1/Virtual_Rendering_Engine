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
 * Unit tests for class {@link Box}.
 * The tests verify:
 * <ul>
 * <li>{@link Box} constructor validity</li>
 * <li>{@link Box#getNormal(Point)}</li>
 * <li>{@link Box#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
@Tag("unit")
public class BoxTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    BoxTests() { /* to satisfy Javadoc generator */ }

    /**
     * Box spanning (0,0,0) to (2,2,2), used across the tests
     */
    private static final Box BOX = new Box(Point.ZERO, new Point(2, 2, 2));

    /**
     * Error message for failed Box construction
     */
    private static final String ERR_CONSTRUCTOR = "Failed to construct a box";
    /**
     * Error message for incorrect normal
     */
    private static final String ERROR_INCORRECT_NORMAL = "ERROR: Incorrect normal";
    /**
     * Error message for incorrect result
     */
    private static final String ERR_INCORRECT_INTERSECTION = "ERROR: Incorrect intersections";

    /**
     * Test method for {@link Box} constructor.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct box defined by two opposite corners, in either order
        assertDoesNotThrow(() -> new Box(Point.ZERO, new Point(1, 1, 1)), ERR_CONSTRUCTOR);
        assertDoesNotThrow(() -> new Box(new Point(1, 1, 1), Point.ZERO), ERR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================
        // BV01: Zero extent on one axis is not a valid box
        assertThrows(IllegalArgumentException.class, () -> new Box(Point.ZERO, new Point(1, 1, 0)),
                ERR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Box#getNormal(Point)}.
     */
    @Test
    void testGetNormal() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point on the -X face
        assertEquals(new Vector(-1, 0, 0), BOX.getNormal(new Point(0, 1, 1)), ERROR_INCORRECT_NORMAL);
        // EP02: Point on the +X face
        assertEquals(new Vector(1, 0, 0), BOX.getNormal(new Point(2, 1, 1)), ERROR_INCORRECT_NORMAL);
        // EP03: Point on the -Y face
        assertEquals(new Vector(0, -1, 0), BOX.getNormal(new Point(1, 0, 1)), ERROR_INCORRECT_NORMAL);
        // EP04: Point on the +Y face
        assertEquals(new Vector(0, 1, 0), BOX.getNormal(new Point(1, 2, 1)), ERROR_INCORRECT_NORMAL);
        // EP05: Point on the -Z face
        assertEquals(new Vector(0, 0, -1), BOX.getNormal(new Point(1, 1, 0)), ERROR_INCORRECT_NORMAL);
        // EP06: Point on the +Z face
        assertEquals(new Vector(0, 0, 1), BOX.getNormal(new Point(1, 1, 2)), ERROR_INCORRECT_NORMAL);
    }

    /**
     * Test method for {@link Box#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ray starts outside and crosses the box through two faces (2 points)
        Ray ray1 = new Ray(new Point(-1, 1, 1), Vector.AXIS_X);
        assertEquals(List.of(new Point(0, 1, 1), new Point(2, 1, 1)), BOX.findIntersections(ray1),
                ERR_INCORRECT_INTERSECTION);

        // EP02: Ray completely misses the box (0 points)
        Ray ray2 = new Ray(new Point(-1, 5, 1), Vector.AXIS_X);
        assertNull(BOX.findIntersections(ray2), ERR_INCORRECT_INTERSECTION);

        // =============== Boundary Values Tests ==================
        // BV01: Ray starts inside the box and exits through one face (1 point)
        Ray ray3 = new Ray(new Point(1, 1, 1), Vector.AXIS_X);
        assertEquals(List.of(new Point(2, 1, 1)), BOX.findIntersections(ray3), ERR_INCORRECT_INTERSECTION);

        // BV02: Ray starts on a face and points outward (0 points)
        Ray ray4 = new Ray(new Point(2, 1, 1), Vector.AXIS_X);
        assertNull(BOX.findIntersections(ray4), ERR_INCORRECT_INTERSECTION);

        // BV03: Ray starts on a face and points inward (1 point, the opposite face)
        Ray ray5 = new Ray(new Point(0, 1, 1), Vector.AXIS_X);
        assertEquals(List.of(new Point(2, 1, 1)), BOX.findIntersections(ray5), ERR_INCORRECT_INTERSECTION);

        // BV04: Ray grazes an edge of the box (2 points, coincident on the edge line)
        Ray ray6 = new Ray(new Point(-1, 0, 0), Vector.AXIS_X);
        assertEquals(List.of(Point.ZERO, new Point(2, 0, 0)), BOX.findIntersections(ray6),
                ERR_INCORRECT_INTERSECTION);

        // BV05: Ray is parallel to a face and outside the box's slab on that axis (0 points)
        Ray ray7 = new Ray(new Point(-1, 5, 1), Vector.AXIS_Z);
        assertNull(BOX.findIntersections(ray7), ERR_INCORRECT_INTERSECTION);
    }
}
