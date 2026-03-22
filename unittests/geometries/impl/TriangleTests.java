package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TriangleTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    TriangleTests() {/* to satisfy JavaDoc generator */ }

    /**
     * Vertex (1,0,0) used in plane tests
     */
    private static final Point POINT_X = new Point(1, 0, 0);
    /**
     * Vertex (0,1,0) used in plane tests
     */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /**
     * Vertex (0,0,1) used in plane tests
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


    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct Triangle defined by three points
        assertDoesNotThrow(() -> new Triangle(POINT_X, POINT_Y, POINT_Z),
                "Failed to construct a plane");
    }

    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        assertEquals(NORMAL_VECTOR, TRIANGLE.getNormal(POINT_X),
                "ERROR: the vectors must be equals");
    }
}
