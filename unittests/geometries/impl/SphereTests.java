package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link Sphere}.
 * The tests verify:
 * <ul>
 * <li>Sphere constructor validity</li>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
public class SphereTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    SphereTests() {/* to satisfy JavaDoc generator */ }

    /**
     * Center point of the test sphere
     */
    private static final Point CENTER = new Point(1, 1, 1);
    /**
     * Radius of the test sphere
     */
    private static final double RADIUS = 1.0;
    /**
     * Sphere defined by the test center and radius
     */
    private static final Sphere SPHERE = new Sphere(CENTER, RADIUS);
    /**
     * Point on the surface of the test sphere
     */
    private static final Point POINT_ON_SPHERE = new Point(2, 1, 1);
    /**
     * Normal vector at the test point on the sphere
     */
    private static final Vector NORMAL_VECTOR = new Vector(1, 0, 0);
    /**
     * Error message for failed sphere construction
     */
    private static final String FAILED_CONSTRUCTOR_ERROR = "Failed to construct a sphere";
    /**
     * Error message for an unexpected normal vector
     */
    private static final String UNMATCH_VECTOR_NORMAL = "getNormal should return the right normal";

    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Correct plane defined by center and radius
        assertDoesNotThrow(() -> new Sphere(CENTER, RADIUS), FAILED_CONSTRUCTOR_ERROR);
    }

    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        assertEquals(NORMAL_VECTOR, SPHERE.getNormal(POINT_ON_SPHERE), UNMATCH_VECTOR_NORMAL);
    }

}
