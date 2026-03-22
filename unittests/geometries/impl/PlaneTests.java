package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    /**
     * Point on the test plane that is not one of the reference points
     */
    private static final Point OTHER_POINT_ON_PLANE = new Point(0.5, 0.5, 0);
    /**
     * Plane defined by a point and a normal vector
     */
    private static final Plane PLANE_BY_VECTOR = new Plane(POINT, VECTOR);
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
     * Test method for {@link Plane} constructor
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT_X, POINT_Y, POINT_Z), FAILED_CONSTRUCTOR_ERROR);

        // EP02: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT, VECTOR), FAILED_CONSTRUCTOR_ERROR);

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
        assertEquals(NORMAL_VECTOR, PLANE_BY_COORDINATES.getNormal(OTHER_POINT_ON_PLANE), UNMATCH_NORMAL_VECTOR_ERROR);

        // EP02: getNormal returns a unit vector
        assertEquals(1, PLANE_BY_COORDINATES.getNormal(POINT_X).length(), DELTA, UNNORMALIZE_VECTOR_ERROR);

        // EP03: getNormal returns a unit vector
        assertEquals(1, PLANE_BY_VECTOR.getNormal(POINT).length(), DELTA, UNNORMALIZE_VECTOR_ERROR);
    }
}
