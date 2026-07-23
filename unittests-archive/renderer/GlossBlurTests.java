package renderer;

import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Disabled;
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
     * variants. Aggressive enough that the colored band reflected off the
     * chrome's silhouette turns into an unmistakable brushed-metal smear,
     * so the gloss-on / gloss-off comparison is the dominant visual cue.
     */
    private static final double BLUR_R = 15;

    /**
     * Blur radius applied to the glass ball's transparency in the "blur on"
     * variants. Substantially larger than {@link #BLUR_R} because the glass
     * ball acts as a pass-through "window" rather than a curved mirror, so
     * the blur kernel only has visible effect where the backdrop behind the
     * window contains sharp edges (orb/background transitions). A wider
     * kernel is required to span those transitions clearly.
     */
    private static final double BLUR_T = 18;

    /**
     * Toggle the single-sphere demo's glossy reflection sampling.
     */
    private static final boolean SPHERE_GLOSS_ON = false;

    /**
     * Toggle the single-sphere demo's diffuse transparency sampling.
     */
    private static final boolean SPHERE_BLUR_ON = true;

    /**
     * Reflection blur radius used by {@link #testSphereGlossBlur()} when gloss is enabled.
     */
    private static final double SPHERE_BLUR_R = 35;

    /**
     * Transparency blur radius used by {@link #testSphereGlossBlur()} when blur is enabled.
     */
    private static final double SPHERE_BLUR_T = 16;

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

        // Rainbow wall: five large emissive orbs at z = -500. They are
        // intentionally spaced so they just touch / very lightly overlap,
        // producing a near-continuous strip of color across the back of
        // the scene. That continuous strip is what the chrome ball's
        // silhouette reflects, and what gives blurR a thick band of color
        // to smear into a brushed-metal look.
        addBackdropOrb(scene, -270, 20, new Color(210, 60, 60));
        addBackdropOrb(scene, -135, -25, new Color(210, 130, 50));
        addBackdropOrb(scene, 0, 20, new Color(220, 200, 60));
        addBackdropOrb(scene, 135, -25, new Color(60, 200, 110));
        addBackdropOrb(scene, 270, 20, new Color(70, 100, 220));

        // Bright "lamp" planted directly behind the glass ball. The glass
        // ball is a refraction-free "window" onto whatever sits behind it,
        // so blurT has nothing to do unless that window contains a sharp
        // high-contrast feature. This single bright point IS that feature:
        // without blurT it appears as a sharp warm dot through the glass;
        // with blurT it spreads into a soft glowing halo. Sized small so
        // it does not dominate the rest of the composition.
        scene.geometries.add(
                new Sphere(new Point(130, 0, -380), 12D)
                        .setEmission(new Color(255, 230, 130))
                        .setMaterial(new Material()
                                .setKD(0.05).setKS(0).setShininess(1)));

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
     * The fixed radius is large enough that, at the call-site spacing,
     * adjacent orbs just touch, forming the continuous strip of color the
     * chrome ball's edges reflect.
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
    @Disabled
    void testNoGlossNoBlur() {
        render(buildScene("GlossBlur — none", 0, 0),
                "GlossBlur_00_none");
    }

    /**
     * Glossy chrome only: chrome ball's reflection of the rainbow is
     * smeared, but the glass ball is still perfectly clear.
     */
    @Test
    @Disabled
    void testGlossOnly() {
        render(buildScene("GlossBlur — gloss only", BLUR_R, 0),
                "GlossBlur_01_glossOnly");
    }

    /**
     * Diffuse glass only: glass ball frosts the rainbow seen through it,
     * but the chrome ball still reflects a perfectly sharp image.
     */
    @Test
    @Disabled
    void testBlurOnly() {
        render(buildScene("GlossBlur — blur only", 0, BLUR_T),
                "GlossBlur_02_blurOnly");
    }

    /**
     * Both features on: brushed chrome on the left, frosted glass on the
     * right.
     */
    @Test
    @Disabled
    void testGlossAndBlur() {
        render(buildScene("GlossBlur — gloss and blur", BLUR_R, BLUR_T),
                "GlossBlur_03_both");
    }

    /** Test Gloss and Blur on a glass sphere */
    @Test
    @Disabled
    void testSphereGlossBlur() {
        double blurR = SPHERE_GLOSS_ON ? SPHERE_BLUR_R : 0;
        double blurT = SPHERE_BLUR_ON ? SPHERE_BLUR_T : 0;
        double transparency = SPHERE_BLUR_ON ? 0.30 : 0;

        Scene scene = new Scene("Single sphere gloss/blur")
                .setBackground(new Color(10, 12, 24))
                .setAmbientLight(new AmbientLight(new Color(18, 18, 24)));

        // Colored transmission cards behind the sphere. When blurT is on,
        // their sharp borders smear through the transparent sphere.
        scene.geometries.add(
                new Polygon(
                        new Point(-130, -90, -430),
                        new Point(-40, -90, -430),
                        new Point(-40, 90, -430),
                        new Point(-130, 90, -430))
                        .setEmission(new Color(210, 55, 55))
                        .setMaterial(new Material().setKD(0.05).setKS(0)),
                new Polygon(
                        new Point(-40, -90, -430),
                        new Point(40, -90, -430),
                        new Point(40, 90, -430),
                        new Point(-40, 90, -430))
                        .setEmission(new Color(65, 205, 95))
                        .setMaterial(new Material().setKD(0.05).setKS(0)));
//                new Polygon(
//                        new Point(40, -90, -430),
//                        new Point(130, -90, -430),
//                        new Point(130, 90, -430),
//                        new Point(40, 90, -430))
//                        .setEmission(new Color(60, 95, 225))
//                        .setMaterial(new Material().setKD(0.05).setKS(0)));

        // Large high-contrast reflection stripes behind the camera. Primary
        // rays never hit them, but the sphere reflects them. With gloss off,
        // the stripes are crisp; with gloss on, they smear into a soft band.
        scene.geometries.add(
                new Polygon(
                        new Point(-220, -130, 150),
                        new Point(-110, -130, 150),
                        new Point(-110, 130, 150),
                        new Point(-220, 130, 150))
                        .setEmission(new Color(255, 255, 255))
                        .setMaterial(new Material().setKD(0.05).setKS(0)),
                new Polygon(
                        new Point(-110, -130, 150),
                        new Point(0, -130, 150),
                        new Point(0, 130, 150),
                        new Point(-110, 130, 150))
                        .setEmission(new Color(245, 35, 230))
                        .setMaterial(new Material().setKD(0.05).setKS(0)),
                new Polygon(
                        new Point(0, -130, 150),
                        new Point(110, -130, 150),
                        new Point(110, 130, 150),
                        new Point(0, 130, 150))
                        .setEmission(new Color(40, 230, 255))
                        .setMaterial(new Material().setKD(0.05).setKS(0)),
                new Polygon(
                        new Point(110, -130, 150),
                        new Point(220, -130, 150),
                        new Point(220, 130, 150),
                        new Point(110, 130, 150))
                        .setEmission(new Color(255, 220, 40))
                        .setMaterial(new Material().setKD(0.05).setKS(0)));

        // The single foreground sphere. Toggle SPHERE_GLOSS_ON and
        // SPHERE_BLUR_ON above to switch the two sampling effects.
        scene.geometries.add(
                new Sphere(new Point(0, 0, -230), 72D)
                        .setEmission(new Color(4, 4, 7))
                        .setMaterial(new Material()
                                .setKD(0.02).setKS(0.55).setShininess(300)
                                .setKR(new Double3(0.95, 0.95, 1.00))
                                .setKT(transparency)
                                .setBlurR(blurR)
                                .setBlurT(blurT)));

        scene.lights.add(
                new DirectionalLight(new Color(90, 100, 120),
                        new Vector(-0.3, -0.5, -1)));
        scene.lights.add(
                new SpotLight(new Color(650, 580, 420),
                        new Point(0, 170, -40),
                        new Vector(0, -1, -1.4))
                        .setKl(0.0004).setKq(0.0000015)
                        .setNarrowBeam(2));

        render(scene, "SphereGlossBlur_"
                + (SPHERE_GLOSS_ON ? "gloss" : "sharp")
                + "_"
                + (SPHERE_BLUR_ON ? "blur" : "clear"));
    }
}
