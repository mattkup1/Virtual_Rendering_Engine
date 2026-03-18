package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlaneTests {

    /**
     * Default constructor to satisfy JavaDoc generator
     */
    PlaneTests() {/* to satisfy JavaDoc generator */ }

    /**
     * Vertex (1,2,3) used in plane tests
     */
    private static final Point POINT = new Point(1, 2, 3);
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
     * First collinear point used in plane tests
     */
    private static final Point POINT_COLLINEAR1 = new Point(1, 1, 1);
    /**
     * Second collinear point used in plane tests
     */
    private static final Point POINT_COLLINEAR2 = new Point(2, 2, 2);
    /**
     * Third collinear point used in plane tests
     */
    private static final Point POINT_COLLINEAR3 = new Point(3, 3, 3);

    /**
     * Normal vector used in plane tests
     */
    private static final Vector NORMAL_VECTOR = new Vector(1, 1, 1);

    /**
     * Plane used in getNormal tests
     */
    private static final Plane PLANE = new Plane(POINT_X, POINT_Y, POINT_Z);
    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for {@link Plane} construction failure
     */
    private static final String CONSTRUCTION_ERROR = "Failed to construct a plane";
    /**
     * Error message for {@link Plane} construction with the zero vector as the normal vector
     */
    private static final String ZERO_NORMAL = "ERROR: Cannot define plane with zero vector as the Normal.";

    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT_X, POINT_Y, POINT_Z),
                CONSTRUCTION_ERROR);

        // EP02: Correct plane defined by a point and a valid normal vector
        assertDoesNotThrow(() -> new Plane(POINT, NORMAL_VECTOR),
                CONSTRUCTION_ERROR);

        // =============== Boundary Values Tests ==================

        // BV01: Three collinear points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_COLLINEAR1, POINT_COLLINEAR2, POINT_COLLINEAR3),
                "ERROR: the three points lie on the same line and cannot define a plane");

        // BV02: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_Y, POINT_Y),
                "ERROR: constructed a plane with two identical points");

        // BV03: Zero vector as normal
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT, new Vector(0, 0, 0)),
                ZERO_NORMAL);

        // BV04: Null vector as normal
        assertThrows(NullPointerException.class, () -> new Plane(POINT, null),
                ZERO_NORMAL);

    }

    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: getNormal returns the same normal vector for different points on the plane
        assertEquals(PLANE.getNormal(POINT_X), PLANE.getNormal(POINT_Y),
                "getNormal should return the same normal for every point on the plane");

        // EP02: getNormal returns a unit vector
        assertEquals(1, PLANE.getNormal(POINT_X).length(), DELTA,
                "getNormal should return a normalized vector");

        // EP03: normal is orthogonal to vector POINT_X -> POINT_Z
        assertEquals(0, PLANE.getNormal(POINT_X).dotProduct(POINT_Z.subtract(POINT_X)), DELTA,
                "Normal is not orthogonal to vector POINT_X -> POINT_Z");

    }
}
