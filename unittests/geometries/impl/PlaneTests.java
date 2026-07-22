package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.UV;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Plane}.
 * The tests verify:
 * <ul>
 * <li>{@link Plane} constructor validity</li>
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
    // Points
    /**
     * {@link Point} (1,2,3) used in plane tests
     */
    private static final Point POINT = new Point(1, 2, 3);
    /**
     * {@link Point} (1,0,0) used in plane tests
     */
    private static final Point POINT_X = new Point(1, 0, 0);
    /**
     * {@link Point} (0,1,0) used in plane tests
     */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /**
     * {@link Point} (0,0,1) used in plane tests
     */
    private static final Point POINT_Z = new Point(0, 0, 1);
    /**
     * First collinear {@link Point} (1,1,1) used in plane tests
     */
    private static final Point POINT_COLLINEAR1 = new Point(1, 1, 1);
    /**
     * Second collinear {@link Point} (2,2,2) used in plane tests
     */
    private static final Point POINT_COLLINEAR2 = new Point(2, 2, 2);
    /**
     * Third collinear {@link Point} (3,3,3) used in plane tests
     */
    private static final Point POINT_COLLINEAR3 = new Point(3, 3, 3);
    /**
     * {@link Point} (1,0,1) used in some tests
     */
    private static final Point P101 = new Point(1, 0, 1);

    // Vectors
    /**
     * {@link Vector} (1,1,1) used in plane tests
     */
    private static final Vector V111 = new Vector(1, 1, 1);
    /**
     * {@link Vector} (0,0,-1) used in some tests
     */
    private static final Vector V00N1 = new Vector(0, 0, -1);

    // Planes
    /**
     * {@link Plane} used in getNormal tests
     */
    private static final Plane PLANE1 = new Plane(POINT_X, POINT_Y, POINT_Z);
    /**
     * XY {@link Plane} used in getIntersections tests
     */
    private static final Plane PLANE_XY = new Plane(POINT_X, POINT_Y, Point.ZERO);
    /**
     * {@link Plane} defined by a point and a normal vector
     */
    private static final Plane PLANE_BY_VECTOR = new Plane(POINT, V111);

    // MISC
    /**
     * Normalized normal {@link Vector} used in plane tests
     */
    private static final Vector NORMAL_VECTOR = new Vector(1 / Math.sqrt(3), 1 / Math.sqrt(3), 1 / Math.sqrt(3));
    /**
     * {@link Point} on the test {@link Plane} that is not one of the reference points
     */
    private static final Point OTHER_POINT_ON_PLANE = new Point(0.5, 0.5, 0);
    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;
    /**
     * Error message for failed plane construction
     */
    private static final String ERR_CONSTRUCTOR = "Failed to construct a plane";
    /**
     * Error message for constructing a plane with two identical points
     */
    private static final String ERR_TWO_IDENTICAL_POINTS = "ERROR: constructed a plane with two identical points";
    /**
     * Error message for constructing a plane with three identical points
     */
    private static final String ERR_THREE_IDENTICAL_PTS = "ERROR: constructed a plane with three identical points";
    /**
     * Error message for constructing a plane from three collinear points
     */
    private static final String ERR_THREE_SAME_LINE = "ERROR: the three points lie on the same line and cannot define a plane";
    /**
     * Error message for an unnormalized normal vector
     */
    private static final String ERR_NOT_NORMALIZED_VECTOR = "Plane normal should be normalized";
    /**
     * Error message for a mismatched normal vector
     */
    private static final String ERR_INCORRECT_NORMAL = "ERROR: Incorrect normal vector";
    /**
     * Error message for expected intersection
     */
    private static final String ERR_INTERSECTION_EXPECTED = "ERROR: intersection expected";
    /**
     * Error message for when no intersections were expected
     */
    private static final String ERR_EXPECTED_NULL = "ERROR: Expected null";

    /**
     * Test method for {@link Plane} constructor
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT_X, POINT_Y, POINT_Z), ERR_CONSTRUCTOR);

        // EP02: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT, V111), ERR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // BV01: Three collinear points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_COLLINEAR1, POINT_COLLINEAR2, POINT_COLLINEAR3), ERR_THREE_SAME_LINE);

        // BV02: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_Z), ERR_TWO_IDENTICAL_POINTS);

        // BV03: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_Y, POINT_X), ERR_TWO_IDENTICAL_POINTS);

        // BV04: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_Y, POINT_Y), ERR_TWO_IDENTICAL_POINTS);

        // BV05: Three identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X, POINT_X, POINT_X), ERR_THREE_IDENTICAL_PTS);

    }

    /**
     * Test method for {@link Plane#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: getNormal returns the except normal vector of the plane
        assertEquals(NORMAL_VECTOR, PLANE1.getNormal(OTHER_POINT_ON_PLANE), ERR_INCORRECT_NORMAL);

        // EP02: getNormal returns a unit vector
        assertEquals(1, PLANE1.getNormal(POINT_X).length(), DELTA, ERR_NOT_NORMALIZED_VECTOR);

        // EP03: getNormal returns a unit vector
        assertEquals(1, PLANE_BY_VECTOR.getNormal(POINT).length(), DELTA, ERR_NOT_NORMALIZED_VECTOR);
    }

    /**
     * Test method for {@link Plane#getUV(Point)}.
     * <p>
     * Since the plane's U/V axes are an internal implementation detail, correctness is
     * checked via two implementation-independent properties instead of exact values:
     * the plane's own reference point maps to the UV origin, and UV-space distances
     * between on-plane points match their real (isometric) distances.
     * </p>
     */
    @Test
    void testGetUV() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: The plane's own reference point maps to the UV origin
        assertEquals(new UV(0, 0), PLANE_XY.getUV(POINT_X), "ERROR: reference point should map to UV (0,0)");

        // EP02: UV-space distances between on-plane points match their real distances
        UV uvA = PLANE_XY.getUV(POINT_X);
        UV uvB = PLANE_XY.getUV(POINT_Y);
        UV uvC = PLANE_XY.getUV(Point.ZERO);

        double uvDistAB = Math.hypot(uvA.u() - uvB.u(), uvA.v() - uvB.v());
        double uvDistAC = Math.hypot(uvA.u() - uvC.u(), uvA.v() - uvC.v());

        assertEquals(POINT_X.distance(POINT_Y), uvDistAB, DELTA, "ERROR: UV mapping is not distance-preserving");
        assertEquals(POINT_X.distance(Point.ZERO), uvDistAC, DELTA, "ERROR: UV mapping is not distance-preserving");
    }

    /**
     * Test method for {@link Plane#findIntersections(Ray)}
     */
    @Test
    void testGetIntersections() {
        // ============ Equivalence Partitions Tests ==============

        // EP01 Ray intersects plane at a single point
        Ray ray1 = new Ray(P101, new Vector(-1, 1, -1));
        assertEquals(1, PLANE_XY.findIntersections(ray1).size(), ERR_INTERSECTION_EXPECTED);

        // EP02 Ray does not intersect plane
        Ray ray2 = new Ray(new Point(1, 0, 1), new Vector(1, -1, 1));
        assertNull(PLANE_XY.findIntersections(ray2), ERR_EXPECTED_NULL);

        // =============== Boundary Values Tests ==================

        // Ray is parallel to the plane
        // BV11 Ray is included in the plane (no intersection)
        Ray ray3 = new Ray(POINT_X, Vector.AXIS_X);
        assertNull(PLANE_XY.findIntersections(ray3), ERR_EXPECTED_NULL);
        // BV12 Ray is not included in the plane
        Ray ray4 = new Ray(P101, Vector.AXIS_Y);
        assertNull(PLANE_XY.findIntersections(ray4), ERR_EXPECTED_NULL);

        // Ray is orthogonal to the plane
        // BV13 Ray origin before the plane (1 intersection)
        Ray ray5 = new Ray(P101, V00N1);
        assertEquals(1, PLANE_XY.findIntersections(ray5).size(), ERR_INTERSECTION_EXPECTED);
        // BV14 Ray origin after the plane (no intersection)
        Ray ray6 = new Ray(new Point(1, 0, -1), V00N1);
        assertNull(PLANE_XY.findIntersections(ray6), ERR_EXPECTED_NULL);
        // BV15 Ray origin on the plane (no intersection)
        Ray ray7 = new Ray(POINT_X, Vector.AXIS_Z);
        assertNull(PLANE_XY.findIntersections(ray7), ERR_EXPECTED_NULL);

        // Ray is neither orthogonal nor parallel to the plane
        // BV16 Ray origin on the plane (no intersection)
        Ray ray8 = new Ray(POINT_X, V111);
        assertNull(PLANE_XY.findIntersections(ray8), ERR_EXPECTED_NULL);
        // BV17 Ray origin is the plane reference point (no intersection)
        Ray ray9 = new Ray(POINT_X, V111);
        assertNull(PLANE_XY.findIntersections(ray9), ERR_EXPECTED_NULL);
    }
}
