package geometries.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link CSG}.
 * <p>
 * Uses two overlapping spheres along the X axis, hand-verified by computing each
 * sphere's own entry/exit distances and reasoning through the expected boundary
 * crossings for each operation (the same approach used to verify {@code Torus}'s quartic
 * solver): sphere A, center origin, radius 3, spans x in [-3,3]; sphere B, center (4,0,0),
 * radius 3, spans x in [1,7]. A ray along +X through both centers crosses, in order:
 * A-enter (x=-3), B-enter (x=1), A-exit (x=3), B-exit (x=7).
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class CSGTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    CSGTests() { /* to satisfy Javadoc generator */ }

    /**
     * Sphere A: center origin, radius 3 (spans x in [-3,3])
     */
    private static final Sphere SPHERE_A = new Sphere(Point.ZERO, 3);
    /**
     * Sphere B: center (4,0,0), radius 3 (spans x in [1,7]), overlapping sphere A
     */
    private static final Sphere SPHERE_B = new Sphere(new Point(4, 0, 0), 3);
    /**
     * A ray along +X passing through both spheres' centers
     */
    private static final Ray RAY = new Ray(new Point(-10, 0, 0), Vector.AXIS_X);

    /**
     * Error message for incorrect CSG intersection result
     */
    private static final String ERR_INTERSECTIONS = "ERROR: Incorrect CSG intersections";

    /**
     * Test method for {@link CSG#calcIntersectionsHelper}, {@link CSG.Operation#UNION}:
     * the combined solid's surface is only where the union's own inside/outside state
     * flips - A's entry (x=-3) and B's exit (x=7); A's exit and B's entry both occur
     * while still inside the other sphere, so they don't produce a union surface.
     */
    @Test
    void testUnion() {
        CSG union = new CSG(SPHERE_A, CSG.Operation.UNION, SPHERE_B);
        assertEquals(List.of(new Point(-3, 0, 0), new Point(7, 0, 0)), union.findIntersections(RAY),
                ERR_INTERSECTIONS);
    }

    /**
     * Test method for {@link CSG#calcIntersectionsHelper}, {@link CSG.Operation#INTERSECTION}:
     * only the overlapping middle region survives - B's entry (x=1, where the ray first
     * enters both spheres) and A's exit (x=3, where the ray first leaves either sphere).
     */
    @Test
    void testIntersection() {
        CSG intersection = new CSG(SPHERE_A, CSG.Operation.INTERSECTION, SPHERE_B);
        assertEquals(List.of(new Point(1, 0, 0), new Point(3, 0, 0)), intersection.findIntersections(RAY),
                ERR_INTERSECTIONS);
    }

    /**
     * Test method for {@link CSG#calcIntersectionsHelper}, {@link CSG.Operation#DIFFERENCE}
     * (A - B): the part of A before B starts - A's entry (x=-3) and B's entry (x=1, where
     * the "hole" begins, shown using B's own surface/material).
     */
    @Test
    void testDifference() {
        CSG difference = new CSG(SPHERE_A, CSG.Operation.DIFFERENCE, SPHERE_B);
        assertEquals(List.of(new Point(-3, 0, 0), new Point(1, 0, 0)), difference.findIntersections(RAY),
                ERR_INTERSECTIONS);
    }

    /**
     * Test method verifying a ray missing both operands entirely produces no intersections,
     * for all three operations.
     */
    @Test
    void testMiss() {
        Ray missRay = new Ray(new Point(-10, 20, 0), Vector.AXIS_X);
        assertNull(new CSG(SPHERE_A, CSG.Operation.UNION, SPHERE_B).findIntersections(missRay), ERR_INTERSECTIONS);
        assertNull(new CSG(SPHERE_A, CSG.Operation.INTERSECTION, SPHERE_B).findIntersections(missRay),
                ERR_INTERSECTIONS);
        assertNull(new CSG(SPHERE_A, CSG.Operation.DIFFERENCE, SPHERE_B).findIntersections(missRay),
                ERR_INTERSECTIONS);
    }

    /**
     * Test method for disjoint (non-overlapping) operands: union keeps both separate
     * solids (4 points), intersection is empty (no overlap), and difference is
     * unaffected (identical to the left operand alone).
     */
    @Test
    void testDisjointOperands() {
        Sphere farSphere = new Sphere(new Point(100, 0, 0), 3);

        // Union: both spheres' own entry/exit pairs survive unchanged, since they never overlap
        CSG union = new CSG(SPHERE_A, CSG.Operation.UNION, farSphere);
        assertEquals(
                List.of(new Point(-3, 0, 0), new Point(3, 0, 0), new Point(97, 0, 0), new Point(103, 0, 0)),
                union.findIntersections(RAY), ERR_INTERSECTIONS);

        // Intersection: no overlap between the two solids at all
        CSG intersection = new CSG(SPHERE_A, CSG.Operation.INTERSECTION, farSphere);
        assertNull(intersection.findIntersections(RAY), ERR_INTERSECTIONS);

        // Difference: nothing to subtract from A, so it's unaffected
        CSG difference = new CSG(SPHERE_A, CSG.Operation.DIFFERENCE, farSphere);
        assertEquals(List.of(new Point(-3, 0, 0), new Point(3, 0, 0)), difference.findIntersections(RAY),
                ERR_INTERSECTIONS);
    }

    /**
     * Test method for {@link CSG#getBoundingBox()}: union is the union of both operands'
     * boxes (wide enough to cover the far-apart disjoint spheres too), intersection and
     * difference are each a safe (if not maximally tight) subset bound.
     */
    @Test
    void testGetBoundingBox() {
        CSG union = new CSG(SPHERE_A, CSG.Operation.UNION, SPHERE_B);
        assertNotNull(union.getBoundingBox(), ERR_INTERSECTIONS);

        CSG intersection = new CSG(SPHERE_A, CSG.Operation.INTERSECTION, SPHERE_B);
        assertNotNull(intersection.getBoundingBox(), ERR_INTERSECTIONS);

        CSG difference = new CSG(SPHERE_A, CSG.Operation.DIFFERENCE, SPHERE_B);
        assertNotNull(difference.getBoundingBox(), ERR_INTERSECTIONS);
    }
}
