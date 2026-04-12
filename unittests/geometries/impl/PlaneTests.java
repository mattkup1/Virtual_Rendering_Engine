package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Plane}.
 * The tests verify:
 * <ul>
 * <li>Plane constructor validity</li>
 * <li>{@link Plane#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
public class PlaneTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    PlaneTests() {/* to satisfy Javadoc generator */ }

    // ================== CONSTANTS ==================
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
     * Point used in some tests
     */
    private static final Point P101 = new Point(1, 0, 1);
    /**
     * Normal vector used in plane tests
     */
    private static final Vector V111 = new Vector(1, 1, 1);
    /**
     * Vector used in some tests
     */
    private static final Vector V00n1 = new Vector(0, 0, -1);
    /**
     * Plane used in getNormal tests
     */
    private static final Plane PLANE1 = new Plane(POINT_X, POINT_Y, POINT_Z);
    /**
     * XY plane used in getIntersections tests
     */
    private static final Plane PLANE_XY = new Plane(POINT_X, POINT_Y, Point.ZERO);
    /**
     * Normalized normal vector used in plane tests
     */
    private static final Vector NORMAL_VECTOR = new Vector(1 / Math.sqrt(3), 1 / Math.sqrt(3), 1 / Math.sqrt(3));
    /**
     * Point on the test plane that is not one of the reference points
     */
    private static final Point OTHER_POINT_ON_PLANE = new Point(0.5, 0.5, 0);
    /**
     * Plane defined by a point and a normal vector
     */
    private static final Plane PLANE_BY_VECTOR = new Plane(POINT, V111);
    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;
    /**
     * Error message for failed plane construction
     */
    private static final String FAILED_CONSTRUCTOR_ERROR = "Failed to construct a plane";
    /**
     * Error message for constructing a plane with two identical points
     */
    private static final String TOW_IDENTICAL_POINT_ERROR = "ERROR: constructed a plane with two identical points";
    /**
     * Error message for constructing a plane with three identical points
     */
    private static final String THREE_IDENTICAL_POINT_ERROR = "ERROR: constructed a plane with three identical points";
    /**
     * Error message for constructing a plane from three collinear points
     */
    private static final String THREE_SAME_LINE_ERROR = "ERROR: the three points lie on the same line and cannot define a plane";
    /**
     * Error message for an unnormalized normal vector
     */
    private static final String UNNORMALIZE_VECTOR_ERROR = "Plane normal should be normalized";
    /**
     * Error message for a mismatched normal vector
     */
    private static final String UNMATCH_NORMAL_VECTOR_ERROR = "getNormal should return the same normal for every point on the plane";
    /**
     * Error message for expected intersection
     */
    private static final String ERROR_INTERSECTION_EXPECTED = "ERROR: intersection expected";
    /**
     * Error message for when no intersections were expected
     */
    private static final String ERROR_EXPECTED_NULL = "ERROR: Expected null result";

    /**
     * Test method for {@link Plane} constructor
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT_X, POINT_Y, POINT_Z), FAILED_CONSTRUCTOR_ERROR);

        // EP02: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT, V111), FAILED_CONSTRUCTOR_ERROR);

        // =============== Boundary Values Tests ==================

        // BV01: Three collinear points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_COLLINEAR1, POINT_COLLINEAR2, POINT_COLLINEAR3), THREE_SAME_LINE_ERROR);

        // BV02: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_Z), TOW_IDENTICAL_POINT_ERROR);

        // BV03: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_Y, POINT_X), TOW_IDENTICAL_POINT_ERROR);

        // BV04: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_Y, POINT_Y), TOW_IDENTICAL_POINT_ERROR);

        // BV05: Three identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_X), THREE_IDENTICAL_POINT_ERROR);

    }

    /**
     * Test method for {@link Plane#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: getNormal returns the except normal vector of the plane
        assertEquals(NORMAL_VECTOR, PLANE1.getNormal(OTHER_POINT_ON_PLANE), UNMATCH_NORMAL_VECTOR_ERROR);

        // EP02: getNormal returns a unit vector
        assertEquals(1, PLANE1.getNormal(POINT_X).length(), DELTA, UNNORMALIZE_VECTOR_ERROR);

        // EP03: getNormal returns a unit vector
        assertEquals(1, PLANE_BY_VECTOR.getNormal(POINT).length(), DELTA, UNNORMALIZE_VECTOR_ERROR);
    }

    /**
     * Test method for {@link Plane#findIntersections(Ray)}
     */
    @Test
    void testGetIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // EP01 Ray intersects plane at a single point
        Ray ray1 = new Ray(P101, new Vector(-1, 1, -1));
        assertEquals(1, PLANE_XY.findIntersections(ray1).size(), ERROR_INTERSECTION_EXPECTED);

        // EP02 Ray does not intersect plane
        Ray ray2 = new Ray(new Point(1, 0, 1), new Vector(1, -1, 1));
        assertNull(PLANE_XY.findIntersections(ray2), ERROR_EXPECTED_NULL);

        // =============== Boundary Values Tests ==================

        // Ray is parallel to the plane
        // BV11 Ray is included in the plane (no intersection)
        Ray ray3 = new Ray(POINT_X, Vector.AXIS_X);
        assertNull(PLANE_XY.findIntersections(ray3), ERROR_EXPECTED_NULL);
        // BV12 Ray is not included in the plane
        Ray ray4 = new Ray(P101, Vector.AXIS_Y);
        assertNull(PLANE_XY.findIntersections(ray4), ERROR_EXPECTED_NULL);

        // Ray is orthogonal to the plane
        // BV13 Ray origin before the plane (1 intersection)
        Ray ray5 = new Ray(P101, V00n1);
        assertEquals(1, PLANE_XY.findIntersections(ray5).size(), ERROR_INTERSECTION_EXPECTED);
        // BV14 Ray origin after the plane (no intersection)
        Ray ray6 = new Ray(new Point(1, 0, -1), V00n1);
        assertNull(PLANE_XY.findIntersections(ray6), ERROR_EXPECTED_NULL);
        // BV15 Ray origin on the plane (no intersection)
        Ray ray7 = new Ray(POINT_X, Vector.AXIS_Z);
        assertNull(PLANE_XY.findIntersections(ray7), ERROR_EXPECTED_NULL);

        // Ray is neither orthogonal nor parallel to the plane
        // BV16 Ray origin on the plane (no intersection)
        Ray ray8 = new Ray(POINT_X, V111);
        assertNull(PLANE_XY.findIntersections(ray8), ERROR_EXPECTED_NULL);
        // BV17 Ray origin is the plane reference point (no intersection)
        Ray ray9 = new Ray(POINT_X, V111);
        assertNull(PLANE_XY.findIntersections(ray9), ERROR_EXPECTED_NULL);
    }
}
