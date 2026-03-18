package geometries;

import geometries.impl.Plane;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

public class PlaneTests {

    /**
     * Default constructor to satisfy JavaDoc generator
     */
    PlaneTests() {/* to satisfy JavaDoc generator */ }

    /**
     * Vertex (1,2,3) used in plane tests
     */
    private static final Point POINT = new Point(1,2,3);
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
    private static final Vector NORMAL_VECTOR = new Vector(1,1,1);

    /**
     * Plane used in getNormal tests
     */
    private static final Plane PLANE = new Plane(POINT_X, POINT_Y, POINT_Z);
    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    @Test
    void testConstructor () {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct plane defined by three distinct non-collinear points
        assertDoesNotThrow(()-> new Plane(POINT_X,POINT_Y,POINT_Z),
                "Failed constructing a correct plane");

        // TC02: Correct plane defined by a point and a valid normal vector
        assertDoesNotThrow(()-> new Plane(POINT,NORMAL_VECTOR),
                "Failed constructing a correct plane");

        // =============== Boundary Values Tests ==================

        // TC11: Three collinear points
        assertThrows(IllegalArgumentException.class ,() -> new Plane(POINT_COLLINEAR1 , POINT_COLLINEAR2 , POINT_COLLINEAR3),
                "ERROR: the three points lie on the same line and cannot define a plane");

        // TC12: Two identical points
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT_X,POINT_Y,POINT_Y),
                "ERROR: constructed a plane with two identical points");

        // TC13: Zero vector as normal
        assertThrows(IllegalArgumentException.class,()-> new Plane(POINT, new Vector(0,0,0)),
                "ERROR: cannot define a plane with the zero vector as a normal");

        // TC14: Null vector as normal
        assertThrows(NullPointerException.class, ()-> new Plane(POINT,null),
                "ERROR: cannot define a plane with a null vector as a normal");

    }

    @Test
    void testGetNormal() {

        // TC07: getNormal returns the same normal vector for different points on the plane
        assertEquals(PLANE.getNormal(POINT_X), PLANE.getNormal(POINT_Y),
                "getNormal should return the same normal for every point on the plane");

        // TC08: getNormal returns a unit vector
        assertEquals(1, PLANE.getNormal(POINT_X).length(), DELTA,
                "getNormal should return a normalized vector");

        // TC09: normal is orthogonal to vector POINT_X -> POINT_Z
        assertEquals(0, PLANE.getNormal(POINT_X).dotProduct(POINT_Z.subtract(POINT_X)), DELTA,
                "Normal is not orthogonal to vector POINT_X -> POINT_Z");

    }
}
