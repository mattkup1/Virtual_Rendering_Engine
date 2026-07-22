package scene;

import org.junit.jupiter.api.Test;
import primitives.Color;
import renderer.Camera;
import renderer.RayTracerType;

import static java.awt.Color.YELLOW;

/**
 * End-to-end rendering tests driven by JSON scene source files.
 * <p>
 * Each test loads a scene from a {@code .json} file under
 * {@link #JSON_FILE_PATH}, renders it with the camera settings loaded from that same
 * file, and writes the result to disk. The actual parsing logic lives in
 * {@link JsonSceneLoader} and is intentionally not duplicated here.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@SuppressWarnings("java:S109")
class JsonSceneLoaderTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    JsonSceneLoaderTests() { /* to satisfy Javadoc generator */ }

    /**
     * Directory containing the JSON scene source files
     */
    private static final String JSON_FILE_PATH = "sceneSourceFiles/json/";

    /**
     * Loads the given JSON scene file, builds a camera from its {@link CameraSettings},
     * attaches a simple ray tracer, and renders the image.
     *
     * @param sceneFileName name of the JSON scene file inside {@link #JSON_FILE_PATH}
     * @return the camera after rendering
     */
    private static Camera renderScene(String sceneFileName) {
        Scene scene = new JsonSceneLoader("Loaded scene", JSON_FILE_PATH + sceneFileName).loadScene();
        return Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build()
                .renderImage();
    }

    /**
     * Convenience helper for grid-less renders: loads the JSON file inside
     * {@link #JSON_FILE_PATH} and writes the rendered image to disk under
     * the given image name.
     *
     * @param sceneFileName name of the JSON scene file inside {@link #JSON_FILE_PATH}
     * @param imageName     name to use for the produced image (without extension)
     */
    private static void createImage(String sceneFileName, String imageName) {
        renderScene(sceneFileName)
                .writeToImage(imageName);
    }

    /**
     * Renders the basic two-colors scene loaded from JSON.
     */
    @Test
    void testBasicRenderJSON() {
        renderScene("basicRenderTestTwoColors.json")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("JSON basic render test");
    }

    /**
     * Renders the basic scene with a custom ambient (kA) factor loaded from JSON.
     */
    @Test
    void testKaJSON() {
        renderScene("kA_basicRenderTest.json")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("JSON kA test");
    }

    /**
     * Renders the basic scene with per-geometry emission loaded from JSON.
     */
    @Test
    void testBasicRenderEmissionJSON() {
        renderScene("emission_basicRenderTest.json")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("JSON emission test");
    }

    /**
     * Renders the multi-light JSON scene featuring point and spotlights.
     */
    @Test
    void testLightsJSON() {
        createImage("lightsTest.json", "JSON lights test");
    }

    /**
     * Renders the multi-light JSON scene featuring transparent and reflective geometries
     */
    @Test
    void testTransparencyReflectionJSON() {
        createImage("transparencyReflection.json", "JSON trans refl test");
    }

    /**
     * Renders given JSON scenes
     */
    @Test
    void miscTestsJSON() {
        createImage("coolScene.json", "JSON cool scene");
        createImage("courseHome.json", "JSON course home pic");
        createImage("sunnyHouse.json", "JSON house pic");
    }

    /**
     * Renders given json scene
     */
    @Test
    void houseJSON() {
        createImage("sunnyHouse.json", "JSON house test");
    }

    /**
     * Renders the LEGO street scene: two houses, a car, minifigures,
     * a lamppost, trees, and a sunny sky with clouds.
     * <p>
     * Uses the elevated, tilted camera angle and narrow field of view defined in
     * {@code legoScene.json} for a classic LEGO diorama perspective.
     * </p>
     */
    @Test
    void testLegoScene() {
        Scene scene = new JsonSceneLoader("LEGO Scene", JSON_FILE_PATH + "legoScene.json").loadScene();
        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(-1)
                .setDebugPrint(1.0)
                .build()
                .renderImage()
                .writeToImage("LEGO Scene");
    }

    /**
     * Renders the JSON "glossy &amp; blurry" showroom that exercises the new
     * {@code blurR} and {@code blurT} material properties.
     * <p>
     * Scene layout:
     * </p>
     * <ul>
     *   <li><b>Top row of glass spheres</b> &mdash; identical glass spheres
     *       with {@code blurT = 0 / 3 / 9} (clear &rarr; mildly diffuse
     *       &rarr; heavily frosted). They sit in front of a "rainbow wall"
     *       of five emissive backdrop orbs, so the transparency blur is
     *       directly visible as a soft-to-fuzzy distortion of those orbs.</li>
     *   <li><b>Bottom row of chrome spheres</b> &mdash; identical mirror
     *       spheres with {@code blurR = 0 / 2.5 / 8} (perfect mirror
     *       &rarr; glossy &rarr; brushed). They reflect the same rainbow
     *       wall (and the floor), so the reflection blur is visible as
     *       a sharp-to-smeared reflection.</li>
     * </ul>
     * <p>
     * Uses the tighter field of view defined in {@code glossyAndBlurryScene.json}
     * so the off-axis spheres are not fisheye-distorted.
     * </p>
     */
    @Test
    void testGlossyAndBlurryJSON() {
        Scene scene = new JsonSceneLoader(
                "Glossy & Blurry showroom",
                JSON_FILE_PATH + "glossyAndBlurryScene.json")
                .loadScene();

        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(1)
                .setDebugPrint(1.0)
                .build()
                .renderImage()
                .writeToImage("11-adss");
    }

    /**
     * Renders a showcase scene featuring the four new geometries added in
     * {@code geometries.impl} - {@link geometries.impl.Box}, {@link geometries.impl.Cone},
     * {@link geometries.impl.Torus}, and {@link geometries.impl.Ellipse} (as a disk) -
     * lined up on a reflective floor plane under ambient, directional, and point lighting.
     */
    @Test
    void testNewShapesShowcase() {
        createImage("newShapesShowcase.json", "New shapes showcase");
    }
}
