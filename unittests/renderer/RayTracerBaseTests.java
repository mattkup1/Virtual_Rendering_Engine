package renderer;

import geometries.api.Intersectable.Intersection;
import geometries.impl.Plane;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Unit tests for {@link RayTracerBase#preprocessIntersection}'s bump-mapping step.
 * <p>
 * Uses a custom lambda {@link primitives.Texture} with a color proportional to U (a
 * constant, everywhere-nonzero gradient) rather than one of the hard-edged procedural
 * textures, since those have zero gradient almost everywhere and would make the test
 * depend on hitting a UV point within one finite-difference epsilon of a hard edge -
 * fragile given {@link geometries.impl.Plane}'s in-plane U/V axis choice is an internal
 * implementation detail.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class RayTracerBaseTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    RayTracerBaseTests() { /* to satisfy Javadoc generator */ }

    /**
     * Test method verifying that a material with a {@code normalTexture} perturbs the
     * intersection's normal away from the geometry's raw normal, and that the result
     * stays unit length.
     */
    @Test
    void testPreprocessIntersectionAppliesNormalMap() {
        Plane plane = new Plane(Point.ZERO, Vector.AXIS_Z);
        Material material = new Material()
                .setNormalTexture(uv -> new Color(Math.abs(uv.u()) * 50, 0, 0))
                .setBumpStrength(1);
        plane.setMaterial(material);

        Intersection intersection = new Intersection(plane, new Point(1, 1, 0));
        SimpleRayTracer tracer = new SimpleRayTracer(new Scene("bump test"));
        tracer.preprocessIntersection(intersection, new Vector(0, 0, -1));

        assertNotEquals(plane.getNormal(intersection.point), intersection.normal,
                "ERROR: a normal-mapped material should perturb the normal");
        assertEquals(1, intersection.normal.length(), 1e-6,
                "ERROR: the perturbed normal should stay unit length");
    }

    /**
     * Test method verifying that a material with no {@code normalTexture} leaves the
     * normal exactly as the geometry's own {@code getNormal} computed it.
     */
    @Test
    void testPreprocessIntersectionWithoutNormalMapLeavesNormalUnchanged() {
        Plane plane = new Plane(Point.ZERO, Vector.AXIS_Z);
        Intersection intersection = new Intersection(plane, new Point(1, 1, 0));
        SimpleRayTracer tracer = new SimpleRayTracer(new Scene("no bump test"));
        tracer.preprocessIntersection(intersection, new Vector(0, 0, -1));

        assertEquals(plane.getNormal(intersection.point), intersection.normal,
                "ERROR: normal should be unchanged when no normal texture is set");
    }
}
