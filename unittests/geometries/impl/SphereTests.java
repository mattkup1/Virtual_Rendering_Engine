package geometries.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.UV;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link Sphere}.
 * <p>
 * Tests include:
 * - Constructor validation
 * - getNormal()
 * - findIntersections()
 * <p>
 * Based on:
 * Equivalence Partitions (EP) and Boundary Values (BV)
 */
public class SphereTests {

    /**
     * Default constructor for Javadoc
     */
    SphereTests() {
    }

    // ================== CONSTANTS ==================

    /**
     * Radius used for sphere tests
     */
    private static final double RADIUS = 1.0;

    // Points
    /**
     * {@link Point} (1,1,1) used in some tests
     */
    private static final Point P111 = new Point(1, 1, 1);
    /**
     * {@link Point} (2,1,1) used in some tests
     */
    private static final Point P211 = new Point(2, 1, 1);
    /**
     * {@link Point} (1,1,0) used in some tests
     */
    private static final Point P110 = new Point(1, 1, 0);
    /**
     * {@link Point} (1,1,2) used in some tests
     */
    private static final Point P112 = new Point(1, 1, 2);
    /**
     * {@link Point} (1,0,0) used in some tests
     */
    private static final Point P100 = new Point(1, 0, 0);
    /**
     * {@link Point} (-1,0,0) used in some tests
     */
    private static final Point PN100 = new Point(-1, 0, 0);

    // Vectors
    /**
     * {@link Vector} (1,0,0) used in some tests
     */
    private static final Vector V100 = new Vector(1, 0, 0);
    /**
     * {@link Vector} (1,1,0) used in some tests
     */
    private static final Vector V110 = new Vector(1, 1, 0);
    /**
     * {@link Vector} (3,1,0) used in some tests
     */
    private static final Vector V310 = new Vector(3, 1, 0);

    // Spheres
    /**
     * {@link Sphere} with center (1,1,1) and radius 1
     */
    private static final Sphere SPHERE_P111 = new Sphere(P111, RADIUS);
    /**
     * {@link Sphere} with center (1,0,0) and radius 1
     */
    private static final Sphere SPHERE_P100 = new Sphere(P100, RADIUS);

    // Expected results
    /**
     * {@link Point} used as intersection in some tests
     */
    private static final Point INTERSECTION1 =
            new Point(0.0651530771650466, 0.355051025721682, 0);
    /**
     * {@link Point} used as intersection in some tests
     */
    private static final Point INTERSECTION2 =
            new Point(1.53484692283495, 0.844948974278318, 0);
    /**
     * Expected list of intersections in some tests
     */
    private static final List<Point> EXPECTED_TWO_POINTS =
            List.of(INTERSECTION1, INTERSECTION2);
    /**
     * Expected list of intersections in some tests
     */
    private static final List<Point> EXPECTED_P112 =
            List.of(P112);

    // Messages
    /**
     * Error message for failed sphere construction
     */
    private static final String ERR_CONSTRUCTOR = "Failed to construct a sphere";
    /**
     * Error message for incorrect normal result
     */
    private static final String ERR_NORMAL = "getNormal returned wrong result";
    /**
     * Error message for incorrect intersection result
     */
    private static final String ERR_INTERSECTIONS = "Wrong sphere intersection result";
    /**
     * Error message for incorrect UV result
     */
    private static final String ERR_UV = "Wrong sphere UV result";
    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    // ================== TESTS ==================

    /**
     * Test {@link Sphere} constructor
     */
    @Test
    void testConstructor() {
        // EP01: valid sphere
        assertDoesNotThrow(() -> new Sphere(P111, RADIUS), ERR_CONSTRUCTOR);
    }

    /**
     * Test {@link Sphere#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        // EP01: valid normal
        assertEquals(V100, SPHERE_P111.getNormal(P211), ERR_NORMAL);
    }

    /**
     * Test {@link Sphere#findIntersections(Ray)}
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions ============

        // EP01: Ray outside sphere → 0 points
        assertNull(
                SPHERE_P111.findIntersections(new Ray(PN100, V110)),
                ERR_INTERSECTIONS
        );

        // EP02: Ray crosses sphere → 2 points
        var result1 = SPHERE_P100.findIntersections(new Ray(PN100, V310));
        assertEquals(EXPECTED_TWO_POINTS, result1, ERR_INTERSECTIONS);

        // EP03: Ray starts inside → 1 point
        var ray1 = new Ray(new Point(1, 1, 0.5), Vector.AXIS_Z);
        var result2 = SPHERE_P111.findIntersections(ray1);
        assertEquals(List.of(P112), result2, ERR_INTERSECTIONS);

        // EP04: Ray after sphere → 0 points
        assertNull(SPHERE_P111.findIntersections(new Ray(PN100, V310.scale(-1))),
                ERR_INTERSECTIONS);

        // ============ Boundary Values ============

        // ---- Group 1: Ray crosses sphere (not through center)

        // BV11: starts on sphere, goes inside → 1 point
        var ray2 = new Ray(P110, new Vector(-1, 0, 1));
        assertEquals(List.of(new Point(0, 1, 1)),
                SPHERE_P111.findIntersections(ray2),
                ERR_INTERSECTIONS);

        // BV12: starts on sphere, goes outside → 0 points
        var ray3 = new Ray(P110, new Vector(-1, 0, 1).scale(-1));
        assertNull(SPHERE_P100.findIntersections(ray3), ERR_INTERSECTIONS);

        // ---- Group 2: Ray through center

        // BV21: before sphere → 2 points
        var ray4 = new Ray(new Point(1, 1, -1), Vector.AXIS_Z);
        assertEquals(List.of(P110, P112),
                SPHERE_P111.findIntersections(ray4),
                ERR_INTERSECTIONS);

        // BV22: Ray starts on sphere and goes inside → 1 point
        assertEquals(EXPECTED_P112, SPHERE_P111.findIntersections(new Ray(P110, Vector.AXIS_Z)),
                ERR_INTERSECTIONS);
        // BV23: Ray starts inside the sphere → 1 point
        assertEquals(EXPECTED_P112,
                SPHERE_P111.findIntersections(new Ray(new Point(1, 1, 1.5), Vector.AXIS_Z)),
                ERR_INTERSECTIONS);
        // BV24: Ray starts at the center → 1 point
        assertEquals(EXPECTED_P112,
                SPHERE_P111.findIntersections(new Ray(P111, Vector.AXIS_Z)),
                ERR_INTERSECTIONS);
        // BV25: Ray starts at the sphere and goes outside → 0 points
        assertNull(SPHERE_P111.findIntersections(new Ray(P112, Vector.AXIS_Z)),
                ERR_INTERSECTIONS);
        // BV26: Ray starts beyond the sphere → 0 points
        assertNull(SPHERE_P111.findIntersections(new Ray(new Point(1, 1, 3), Vector.AXIS_Z)),
                ERR_INTERSECTIONS);

        // ---- Group 3: Tangent → always 0 points

        // BV31: Ray starts before the tangent point
        assertNull(SPHERE_P111.findIntersections(new Ray(P100, Vector.AXIS_Z)), ERR_INTERSECTIONS);
        // BV32: Ray starts at the tangent point
        assertNull(SPHERE_P111.findIntersections(new Ray(new Point(1, 0, 1), Vector.AXIS_Z)),
                ERR_INTERSECTIONS);
        // BV33: Ray starts beyond the tangent point
        assertNull(SPHERE_P111.findIntersections(new Ray(new Point(1, 0, 2), Vector.AXIS_Z)),
                ERR_INTERSECTIONS);

        // ---- Group 4: Special cases
        // BV41: Ray's line is outside sphere, ray is orthogonal to ray start to sphere's center line
        assertNull(SPHERE_P111.findIntersections(new Ray(new Point(1, -1, 1), Vector.AXIS_Z)),
                ERR_INTERSECTIONS);
        // BV42: Ray starts inside and is orthogonal to sphere's center line
        assertNull(SPHERE_P100.findIntersections(new Ray(new Point(1, 0.5, 1), Vector.AXIS_Z)),
                ERR_INTERSECTIONS);
    }

    /**
     * Test {@link Sphere#getUV(Point)}
     */
    @Test
    void testGetUV() {
        // ============ Equivalence Partitions Tests ==============

        // EP01: Point on the equator along +Z relative to the center
        UV uv1 = SPHERE_P111.getUV(P112);
        assertEquals(0.75, uv1.u(), DELTA, ERR_UV);
        assertEquals(0.5, uv1.v(), DELTA, ERR_UV);

        // EP02: Point on the equator along +X relative to the center
        UV uv2 = SPHERE_P111.getUV(P211);
        assertEquals(0.5, uv2.u(), DELTA, ERR_UV);
        assertEquals(0.5, uv2.v(), DELTA, ERR_UV);

        // =============== Boundary Values Tests ==================

        // BV01: North pole (+Y from center) -> v = 0
        assertEquals(0, SPHERE_P111.getUV(new Point(1, 2, 1)).v(), DELTA, ERR_UV);
        // BV02: South pole (-Y from center) -> v = 1
        assertEquals(1, SPHERE_P111.getUV(new Point(1, 0, 1)).v(), DELTA, ERR_UV);
    }

    /**
     * Test {@link Sphere#calcIntersections(Ray, double)} with a bounded {@code maxDistance}.
     * <p>
     * Uses {@link #SPHERE_P111} (center (1,1,1), radius 1). For a ray along the X-axis
     * starting at (-1, 1, 1) the sphere is entered at distance 1 and exited at distance 3.
     * For a ray starting at (0.5, 1, 1) (inside the sphere) the exit is at distance 1.5.
     * The six cases below correspond to the textbook diagram.
     * </p>
     */
    @Test
    void testCalcIntersectionsWithMaxDistance() {
        // Rays along the X-axis used in the cases below
        final Ray rayInto   = new Ray(new Point(-1, 1, 1), V100);  // starts outside, points into sphere
        final Ray rayInside = new Ray(new Point(0.5, 1, 1), V100); // starts inside the sphere
        final Ray rayPast   = new Ray(new Point(3, 1, 1), V100);   // starts past sphere, points away

        // ============ Equivalence Partitions Tests (maxDistance) ============

        // EP01 (ray1): max distance ends before sphere → 0 intersections
        assertNull(SPHERE_P111.calcIntersections(rayInto, 0.5),
                ERR_INTERSECTIONS);

        // EP02 (ray2): max distance ends inside sphere (after entry, before exit) → 1 intersection
        final var oneEntry = SPHERE_P111.calcIntersections(rayInto, 2);
        assertNotNull(oneEntry, ERR_INTERSECTIONS);
        assertEquals(1, oneEntry.size(), ERR_INTERSECTIONS);

        // EP03 (ray3): max distance past the sphere → 2 intersections
        final var twoPoints = SPHERE_P111.calcIntersections(rayInto, 5);
        assertNotNull(twoPoints, ERR_INTERSECTIONS);
        assertEquals(2, twoPoints.size(), ERR_INTERSECTIONS);

        // EP04 (ray4): starts inside, max distance past exit → 1 intersection (exit)
        final var oneExit = SPHERE_P111.calcIntersections(rayInside, 5);
        assertNotNull(oneExit, ERR_INTERSECTIONS);
        assertEquals(1, oneExit.size(), ERR_INTERSECTIONS);

        // EP05 (ray5): starts inside, max distance ends before exit → 0 intersections
        assertNull(SPHERE_P111.calcIntersections(rayInside, 0.5),
                ERR_INTERSECTIONS);

        // EP06 (ray6): ray entirely past the sphere → 0 intersections
        assertNull(SPHERE_P111.calcIntersections(rayPast, 5),
                ERR_INTERSECTIONS);
    }
}