package renderer;

import geometries.api.Geometry;
import geometries.impl.CSG;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Renders a showcase of all three {@link CSG} boolean operations, built directly in Java
 * since CSG has no {@code SceneLoader}/JSON wiring - a CSG
 * operand is itself a full sub-geometry tree, which the loader's flat
 * {@code Map<String, String>} attribute model has no way to nest.
 * <p>
 * All three shapes are built from the same two 45/30-radius spheres, offset differently,
 * to make the three operations directly comparable: left is a {@link CSG.Operation#DIFFERENCE}
 * ("Death Star" crater), center is a {@link CSG.Operation#UNION} (a smooth blob/snowman,
 * with no visible seam where the two spheres met), and right is a
 * {@link CSG.Operation#INTERSECTION} (the lens/vesica shape common to both).
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@Tag("render")
class CSGRenderTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    CSGRenderTests() { /* to satisfy Javadoc generator */ }

    /**
     * Builds and renders the CSG showcase scene.
     */
    @Test
    void testCSGShowcase() {
        Scene scene = new Scene("CSG showcase");
        scene.background = new Color(18, 20, 26);
        scene.ambientLight = new AmbientLight(new Color(22, 24, 30));
        scene.lights.add(new DirectionalLight(new Color(60, 60, 70), new Vector(-0.3, -0.7, -0.5)));
        scene.lights.add(new PointLight(new Color(650, 600, 550), new Point(20, 180, -100))
                .setKl(0.0005).setKq(0.000002));

        Material floorMaterial = new Material().setKD(0.4).setKS(0.15).setShininess(50);
        scene.geometries.add(new Plane(new Point(0, -60, 0), Vector.AXIS_Y)
                .setEmission(new Color(10, 11, 14)).setMaterial(floorMaterial));

        Material shapeMaterial = new Material().setKD(0.35).setKS(0.4).setShininess(150);

        // DIFFERENCE: a big sphere with a smaller sphere subtracted out of its
        // upper-right, showing the subtracted sphere's own (differently-colored)
        // material on the inside of the resulting crater.
        Geometry differenceBig = new Sphere(new Point(-140, -15, -300), 45)
                .setEmission(new Color(210, 60, 60)).setMaterial(shapeMaterial);
        Geometry differenceCut = new Sphere(new Point(-115, 5, -280), 28)
                .setEmission(new Color(60, 120, 210)).setMaterial(shapeMaterial);
        scene.geometries.add(new CSG(differenceBig, CSG.Operation.DIFFERENCE, differenceCut));

        // UNION: two overlapping spheres fused into one smooth blob, resting on the floor.
        Geometry unionA = new Sphere(new Point(0, -20, -300), 40)
                .setEmission(new Color(60, 180, 90)).setMaterial(shapeMaterial);
        Geometry unionB = new Sphere(new Point(38, -30, -300), 30)
                .setEmission(new Color(60, 180, 90)).setMaterial(shapeMaterial);
        scene.geometries.add(new CSG(unionA, CSG.Operation.UNION, unionB));

        // INTERSECTION: two equal, significantly overlapping spheres, leaving only their
        // shared lens/vesica-shaped core.
        Geometry lensA = new Sphere(new Point(120, -10, -300), 50)
                .setEmission(new Color(230, 180, 40)).setMaterial(shapeMaterial);
        Geometry lensB = new Sphere(new Point(165, -10, -300), 50)
                .setEmission(new Color(230, 180, 40)).setMaterial(shapeMaterial);
        scene.geometries.add(new CSG(lensA, CSG.Operation.INTERSECTION, lensB));

        Camera.getBuilder()
                .setLocation(new Point(30, 90, 60))
                .setDirection(new Point(20, -20, -300), Vector.AXIS_Y)
                .setVpDistance(420)
                .setVpSize(480, 480)
                .setResolution(1000, 1000)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setAntiAliasing(3)
                .setMultithreading(-1)
                .build()
                .renderImage()
                .writeToImage("CSG showcase");
    }
}
