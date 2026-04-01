package geometries.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for class {@link Triangle}.
 * The tests verify:
 * <ul>
 * <li>Triangle constructor validity</li>
 * <li>{@link Triangle#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
public class TriangleTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    TriangleTests() {/* to satisfy JavaDoc generator */ }

    /**
     * Vertex (1,0,0) used in Triangle tests
     */
    private static final Point POINT_X = new Point(1, 0, 0);
    /**
     * Vertex (0,1,0) used in Triangle tests
     */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /**
     * Vertex (0,0,1) used in Triangle tests
     */
    private static final Point POINT_Z = new Point(0, 0, 1);
    /**
     * Triangle defined by the three test vertices
     */
    private static final Triangle TRIANGLE = new Triangle(POINT_X, POINT_Y, POINT_Z);
    /**
     * Normalized normal vector of the test triangle
     */
    private static final Vector NORMAL_VECTOR = new Vector(1 / Math.sqrt(3), 1 / Math.sqrt(3), 1 / Math.sqrt(3));
    /**
     * Error message for failed triangle construction
     */
    private static final String FAILED_CONSTRUCTOR_ERROR = "Failed to construct a triangle";
    /**
     * Error message for an unexpected normal vector
     */
    private static final String UNMATCH_VECTOR_NORMAL = "getNormal should return the right normal";
    /**
     * Error message for incorrect intersection
     */
    private static final String ERR_INCORRECT_INTERSECTION = "ERROR: Incorrect intersection";

    /**
     * Test method for {@link Triangle} constructor
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct Triangle defined by three points
        assertDoesNotThrow(() -> new Triangle(POINT_X, POINT_Y, POINT_Z), FAILED_CONSTRUCTOR_ERROR);
    }

    /**
     * Test method for {@link Triangle#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        assertEquals(NORMAL_VECTOR, TRIANGLE.getNormal(POINT_X), UNMATCH_VECTOR_NORMAL);
    }

    /**
     * Test method for {@link Triangle#findIntersections(Ray)}
     */
    @Test
    void testFindIntersections() {
        // Points
        final Point point1 = new Point(5, 0, 5);
        final Point point2 = new Point(0, 5, 5);
        final Point point3 = new Point(5, 5, 5);
        // Triangle
        final Triangle triangle = new Triangle(point1, point2, point3);
        // Rays
        final Ray rayIn = new Ray(new Point(4, 4, 0), Vector.AXIS_Z);
        final Ray rayAgainstEdge = new Ray(new Point(6, 3, 0), Vector.AXIS_Z);
        final Ray rayAgainstVertex = new Ray(new Point(6, 6, 0), Vector.AXIS_Z);
        final Ray onEdge = new Ray(new Point(5, 4, 0), Vector.AXIS_Z);
        final Ray onVertex = new Ray(new Point(5, 5, 0), Vector.AXIS_Z);
        final Ray onEdgeContinuation = new Ray(new Point(6, 5, 0), Vector.AXIS_Z);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Inside triangle
        assertEquals(List.of(new Point(4, 4, 5)), triangle.findIntersections(rayIn),
                ERR_INCORRECT_INTERSECTION);
        // EP02: against triangle edge
        assertNull(triangle.findIntersections(rayAgainstEdge), ERR_INCORRECT_INTERSECTION);
        // EP03: against triangle vertex
        assertNull(triangle.findIntersections(rayAgainstVertex), ERR_INCORRECT_INTERSECTION);

        // =============== Boundary Values Tests ==================
        // BV11: On triangle edge
        assertNull(triangle.findIntersections(onEdge), ERR_INCORRECT_INTERSECTION);
        // BV12: On triangle vertex
        assertNull(triangle.findIntersections(onVertex), ERR_INCORRECT_INTERSECTION);
        // BV13: On triangle edge continuation
        assertNull(triangle.findIntersections(onEdgeContinuation), ERR_INCORRECT_INTERSECTION);
    }
}
