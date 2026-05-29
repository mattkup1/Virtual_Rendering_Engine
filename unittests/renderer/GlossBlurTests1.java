package renderer;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Minimal-geometry test of glossy reflection ({@code blurR}) and diffuse
 * transparency ({@code blurT}) acting on a <em>single</em> sphere that is
 * both partially reflective and partially transparent.
 * <p>
 * The scene contains exactly four geometries:
 * </p>
 * <ul>
 *   <li>A floor plane &mdash; visual context, lightly reflective.</li>
 *   <li>The <b>demo sphere</b> &mdash; the only geometry whose material
 *       changes between tests. Has both {@code kR} and {@code kT} set so
 *       its surface simultaneously reflects and transmits.</li>
 *   <li>A small bright <b>yellow</b> sphere planted directly behind the
 *       demo sphere and offset to the upper-right. The camera sees it
 *       <em>only</em> through the demo sphere's body, so {@code blurT}
 *       acts on it.</li>
 *   <li>A small bright <b>magenta</b> sphere planted <em>behind the
 *       camera</em>. Forward-going camera rays never touch it; it shows
 *       up only in the demo sphere's reflection of "what is behind the
 *       viewer," so {@code blurR} acts on it.</li>
 * </ul>
 * <p>
 * Because the two targets are different colors and appear in different
 * regions of the demo sphere image (yellow toward the upper-right,
 * magenta at the center), the visual effect of {@code blurR} and
 * {@code blurT} can be read off independently: a soft magenta halo at
 * center is the {@code blurR} signature, a soft yellow halo at upper-right
 * is the {@code blurT} signature.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@SuppressWarnings("java:S109")
class GlossBlurTests1 {

    /**
     * Blur radius applied to the demo sphere's reflection in the "gloss on"
     * variants. Smears the reflected magenta target at the center of the
     * demo sphere image into a wider magenta halo.
     */
    private static final double BLUR_R = 10;

    /**
     * Blur radius applied to the demo sphere's transparency in the "blur on"
     * variants. Smears the transmitted yellow target seen through the demo
     * sphere into a wider yellow halo.
     */
    private static final double BLUR_T = 13;

    /**
     * Default constructor to satisfy Javadoc generator.
     */
    GlossBlurTests1() { /* to satisfy Javadoc generator */ }

    /**
     * Builds the four-geometry test scene and stamps the given blur radii
     * onto the demo sphere. Every other geometry, light, and material
     * stays bit-identical across calls.
     *
     * @param name  the scene name (purely diagnostic)
     * @param blurR blur radius applied to the demo sphere's reflection
     * @param blurT blur radius applied to the demo sphere's transparency
     * @return the populated scene, ready to render
     */
    private static Scene buildScene(String name, double blurR, double blurT) {
        Scene scene = new Scene(name)
                .setBackground(new Color(8, 12, 22))
                .setAmbientLight(new AmbientLight(new Color(20, 22, 30)));

        // 1) Floor — gives the image a horizon and a subtle secondary
        //    reflection underneath the demo sphere.
        scene.geometries.add(
                new Plane(new Point(0, -110, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(8, 10, 16))
                        .setMaterial(new Material()
                                .setKD(0.22).setKS(0.18).setShininess(80)
                                .setKR(0.15)));

        // 2) Demo sphere — the ONLY geometry whose material changes per
        //    test. Both kR and kT non-zero so a single surface produces
        //    both a reflected and a transmitted contribution per pixel.
        scene.geometries.add(
                new Sphere(new Point(0, 0, -350), 80D)
                        .setEmission(new Color(5, 5, 8))
                        .setMaterial(new Material()
                                .setKD(0.05).setKS(0.20).setShininess(200)
                                .setKR(0.45)
                                .setKT(0.55)
                                .setBlurR(blurR)
                                .setBlurT(blurT)));

        // 3) Yellow transmission target — small sharp sphere planted
        //    behind the demo sphere, offset above-right of the optical
        //    axis so its image lands in the upper-right of the demo
        //    sphere body, well clear of the central magenta reflection.
        scene.geometries.add(
                new Sphere(new Point(90, 60, -550), 20D)
                        .setEmission(new Color(255, 220, 80))
                        .setMaterial(new Material()
                                .setKD(0.05).setKS(0).setShininess(1)));

        // 4) Magenta reflection target — small sharp sphere planted
        //    BEHIND the camera, on the optical axis. Camera rays never
        //    hit it because they only go forward, but the demo sphere's
        //    center reflects straight back along +z and lands inside
        //    this lamp, so it shows up ONLY as a reflection in the demo
        //    sphere's center.
        scene.geometries.add(
                new Sphere(new Point(0, 0, 180), 40D)
                        .setEmission(new Color(220, 100, 240))
                        .setMaterial(new Material()
                                .setKD(0.05).setKS(0).setShininess(1)));

        // Lights — same warm key + cool fill recipe as the other test
        //   so the demo sphere has a crisp Phong highlight to anchor the eye.
        scene.lights.add(
                new DirectionalLight(new Color(90, 100, 115),
                        new Vector(-0.3, -0.6, -1)));
        scene.lights.add(
                new SpotLight(new Color(650, 580, 420),
                        new Point(0, 200, -50),
                        new Vector(0, -1, -1.2))
                        .setKl(0.0004).setKq(0.0000015)
                        .setNarrowBeam(2));

        return scene;
    }

    /**
     * Renders the given scene and writes the result to {@code imageName}.png.
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
     * Baseline: perfect mirror plus clear glass on the demo sphere.
     * The yellow transmission target is a sharp dot in the upper-right
     * of the sphere body; the magenta reflection target is a sharp dot
     * at the sphere center.
     */
    @Test
    void testNoGlossNoBlur() {
        render(buildScene("Single-sphere — none", 0, 0),
                "GlossBlur1_00_none");
    }

    /**
     * Gloss only: the magenta reflection at the demo sphere's center
     * spreads into a soft magenta halo. The yellow transmission dot in
     * the upper-right stays sharp.
     */
    @Test
    void testGlossOnly() {
        render(buildScene("Single-sphere — gloss only", BLUR_R, 0),
                "GlossBlur1_01_glossOnly");
    }

    /**
     * Blur only: the yellow transmission dot in the upper-right spreads
     * into a soft yellow halo. The magenta reflection at the sphere
     * center stays sharp.
     */
    @Test
    void testBlurOnly() {
        render(buildScene("Single-sphere — blur only", 0, BLUR_T),
                "GlossBlur1_02_blurOnly");
    }

    /**
     * Both effects active: soft magenta halo at center plus soft yellow
     * halo in the upper-right, simultaneously.
     */
    @Test
    void testGlossAndBlur() {
        render(buildScene("Single-sphere — gloss and blur", BLUR_R, BLUR_T),
                "GlossBlur1_03_both");
    }
}
