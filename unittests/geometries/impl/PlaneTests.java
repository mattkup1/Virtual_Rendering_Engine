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
    private static final Vector VECTOR = new Vector(1, 1, 1);

    /**
     * Plane used in getNormal tests
     */
    private static final Plane PLANE_BY_COORDINATES = new Plane(POINT_X, POINT_Y, POINT_Z);
    /**
     * Normalized normal vector used in plane tests
     */
    private static final Vector NORMAL_VECTOR = new Vector(1 / Math.sqrt(3), 1 / Math.sqrt(3), 1 / Math.sqrt(3));

    private static final Point OTHER_POINT_ON_PLANE = new Point(0.5,0.5,0);

    private static final Plane PLANE_BY_VECTOR = new Plane(POINT, VECTOR);
    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;


    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT_X, POINT_Y, POINT_Z),
                "Failed to construct a plane");

        // EP02: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT, VECTOR),
                "Failed to construct a plane");

        // =============== Boundary Values Tests ==================

        // BV01: Three collinear points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_COLLINEAR1, POINT_COLLINEAR2, POINT_COLLINEAR3),
                "ERROR: the three points lie on the same line and cannot define a plane");

        // BV02: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_Z),
                "ERROR: constructed a plane with two identical points");

        // BV03: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_Y, POINT_X),
                "ERROR: constructed a plane with two identical points");

        // BV04: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_Y, POINT_Y),
                "ERROR: constructed a plane with two identical points");

        // BV05: Three identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_X),
                "ERROR: constructed a plane with three identical points");

    }

    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: getNormal returns the except normal vector of the plane
        assertEquals(NORMAL_VECTOR, PLANE_BY_COORDINATES.getNormal(OTHER_POINT_ON_PLANE),
                "getNormal should return the same normal for every point on the plane");

        // EP02: getNormal returns a unit vector
        assertEquals(1, PLANE_BY_COORDINATES.getNormal(POINT_X).length(), DELTA,
                "Plane normal should be normalized");

        // EP03: getNormal returns a unit vector
        assertEquals(1, PLANE_BY_VECTOR.getNormal(POINT).length(), DELTA,
                "Plane normal should be normalized");

        // =============== Boundary Values Tests ==================

        // BV01: getNormal returns the except normal vector of the plane
        assertEquals(NORMAL_VECTOR, PLANE_BY_COORDINATES.getNormal(POINT_Y),
                "getNormal should return the same normal for every point on the plane");

    }
}
