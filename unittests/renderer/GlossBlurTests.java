package renderer;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Side-by-side tests for the new glossy-reflection ({@code blurR}) and
 * diffuse-transparency ({@code blurT}) material properties.
 * <p>
 * Every test renders the exact same scene &mdash; a chrome ball on the left
 * and a glass ball on the right, both standing in front of a "rainbow wall"
 * of five emissive backdrop orbs &mdash; and varies only the chrome ball's
 * {@code blurR} and the glass ball's {@code blurT}. Comparing the four
 * resulting images shows in isolation how each feature affects the picture:
 * </p>
 * <ul>
 *   <li>{@code blurR > 0} smears the chrome ball's reflection of the
 *       rainbow into a brushed-metal look.</li>
 *   <li>{@code blurT > 0} frosts the rainbow as seen through the glass
 *       ball.</li>
 * </ul>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@SuppressWarnings("java:S109")
class GlossBlurTests {

    /**
     * Blur radius applied to the chrome ball's reflection in the "gloss on"
     * variants. Tuned to be obvious without being so large that the
     * {@link SimpleRayTracer#BLUR_SAMPLES low default sample count} makes
     * the result noisy.
     */
    private static final double BLUR_R = 7;

    /**
     * Blur radius applied to the glass ball's transparency in the "blur on"
     * variants. Slightly larger than {@link #BLUR_R} because diffuse-glass
     * blur is read through a longer light path (front of glass &rarr; back
     * of glass &rarr; backdrop) and therefore needs more spread to be
     * comparably visible.
     */
    private static final double BLUR_T = 9;

    /**
     * Default constructor to satisfy Javadoc generator.
     */
    GlossBlurTests() { /* to satisfy Javadoc generator */ }

    /**
     * Builds the shared scene and stamps the given blur radii onto its
     * chrome and glass balls. Every other geometry, light, and material
     * stays bit-identical across calls.
     *
     * @param name  the scene name (purely diagnostic)
     * @param blurR blur radius applied to the chrome ball's reflection
     * @param blurT blur radius applied to the glass ball's transparency
     * @return the populated scene, ready to render
     */
    private static Scene buildScene(String name, double blurR, double blurT) {
        Scene scene = new Scene(name)
                .setBackground(new Color(8, 12, 22))
                .setAmbientLight(new AmbientLight(new Color(20, 22, 30)));

        // Slightly reflective floor, well below the foreground balls
        scene.geometries.add(
                new Plane(new Point(0, -75, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(8, 10, 16))
                        .setMaterial(new Material()
                                .setKD(0.25).setKS(0.20).setShininess(80)
                                .setKR(0.18)));

        // Rainbow wall: five emissive orbs at z = -500. Their alternating
        // vertical positions make the blur effect immediately readable
        // (a sharp reflection preserves the zig-zag; a blurry one smears it).
        addBackdropOrb(scene, -240, 15, new Color(210, 60, 60));
        addBackdropOrb(scene, -120, -20, new Color(210, 130, 50));
        addBackdropOrb(scene, 0, 15, new Color(220, 200, 60));
        addBackdropOrb(scene, 120, 0, new Color(60, 200, 110));
        addBackdropOrb(scene, 240, 15, new Color(70, 100, 220));

        // Chrome ball on the left — drives blurR
        scene.geometries.add(
                new Sphere(new Point(-55, 0, -200), 38D)
                        .setEmission(new Color(5, 5, 6))
                        .setMaterial(new Material()
                                .setKD(0.05).setKS(0.50).setShininess(250)
                                .setKR(new Double3(0.85, 0.85, 0.88))
                                .setBlurR(blurR)));

        // Glass ball on the right — drives blurT
        scene.geometries.add(
                new Sphere(new Point(55, 0, -200), 38D)
                        .setEmission(new Color(6, 6, 8))
                        .setMaterial(new Material()
                                .setKD(0.05).setKS(0.30).setShininess(200)
                                .setKT(0.85)
                                .setBlurT(blurT)));

        // Cool directional fill so the surfaces are not pitch black where
        // the spot light does not reach.
        scene.lights.add(
                new DirectionalLight(new Color(95, 105, 120),
                        new Vector(-0.3, -0.6, -1)));

        // Warm key spot from above-front, giving each ball a crisp highlight.
        scene.lights.add(
                new SpotLight(new Color(700, 620, 450),
                        new Point(0, 220, -40),
                        new Vector(0, -1, -1.2))
                        .setKl(0.0004).setKq(0.0000015)
                        .setNarrowBeam(2));

        return scene;
    }

    /**
     * Helper that drops one purely-emissive backdrop orb into the scene.
     *
     * @param scene the scene to append to
     * @param x     horizontal position
     * @param y     vertical position
     * @param color the orb's emission color
     */
    private static void addBackdropOrb(Scene scene, double x, double y, Color color) {
        scene.geometries.add(
                new Sphere(new Point(x, y, -500), 70D)
                        .setEmission(color)
                        .setMaterial(new Material()
                                .setKD(0.05).setKS(0).setShininess(1)));
    }

    /**
     * Renders the given scene with the shared GlossBlur camera and writes
     * the result to {@code imageName}.png. Pulled out into a helper so the
     * four tests differ only in their blur configuration.
     *
     * @param scene     the scene to render
     * @param imageName name of the output image (no extension)
     */
    private static void render(Scene scene, String imageName) {
        Camera.getBuilder()
                .setLocation(Point.ZERO)
                .setDirection(new Point(0, 0, -1))
                .setVpDistance(200)
                .setVpSize(200, 200)
                .setResolution(600, 600)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build()
                .renderImage()
                .writeToImage(imageName);
    }

    /**
     * Baseline: pure mirror chrome ({@code blurR = 0}) and clear glass
     * ({@code blurT = 0}). The reflected and refracted rainbows should both
     * appear crisp.
     */
    @Test
    void testNoGlossNoBlur() {
        render(buildScene("GlossBlur — none", 0, 0),
                "GlossBlur_00_none");
    }

    /**
     * Glossy chrome only: chrome ball's reflection of the rainbow is
     * smeared, but the glass ball is still perfectly clear.
     */
    @Test
    void testGlossOnly() {
        render(buildScene("GlossBlur — gloss only", BLUR_R, 0),
                "GlossBlur_01_glossOnly");
    }

    /**
     * Diffuse glass only: glass ball frosts the rainbow seen through it,
     * but the chrome ball still reflects a perfectly sharp image.
     */
    @Test
    void testBlurOnly() {
        render(buildScene("GlossBlur — blur only", 0, BLUR_T),
                "GlossBlur_02_blurOnly");
    }

    /**
     * Both features on: brushed chrome on the left, frosted glass on the
     * right.
     */
    @Test
    void testGlossAndBlur() {
        render(buildScene("GlossBlur — gloss and blur", BLUR_R, BLUR_T),
                "GlossBlur_03_both");
    }
}
