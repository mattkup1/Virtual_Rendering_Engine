package scene;

import org.junit.jupiter.api.Tag;
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
@Tag("render")
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
     * Renders the Courtyard Spire diorama: an original tapered, ringed tower with a
     * base colonnade and spire, standing over a mirror-flat reflecting pool at dusk,
     * flanked by pine trees, lantern posts, flags, and a foreground cluster of mirror
     * spheres under a two-tone painted sky.
     */
    @Test
    void testCourtyardSpireScene() {
        Scene scene = new JsonSceneLoader("Courtyard Spire", JSON_FILE_PATH + "courtyardSpire.json").loadScene();
        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(-1)
                .setDebugPrint(1.0)
                .build()
                .renderImage()
                .writeToImage("Courtyard Spire");
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
     * Renders {@code glossyAndBlurryScene.json} again, this time with depth of field
     * enabled via {@link Camera.Builder#setDepthOfField(double, double)}, focused on the
     * bottom-row chrome spheres (centered around {@code z=-180}, ~214 units from the
     * camera at the origin). The rainbow backdrop spheres sit much farther away
     * ({@code z=-500}, ~539 units out), so they should render visibly blurred while the
     * chrome spheres stay in sharp focus - demonstrating the thin-lens depth-of-field
     * effect against a scene with a clear near/far depth split.
     */
    @Test
    void testDepthOfFieldShowcase() {
        Scene scene = new JsonSceneLoader(
                "Depth of field showroom",
                JSON_FILE_PATH + "glossyAndBlurryScene.json")
                .loadScene();

        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setResolution(400, 400)
                .setDepthOfField(4, 214)
                .setMultithreading(-1)
                .setDebugPrint(1.0)
                .build()
                .renderImage()
                .writeToImage("Depth of field showcase");
    }

    /**
     * Renders a showcase scene featuring the new geometries added in {@code geometries.impl}
     * - {@link geometries.impl.Box}, {@link geometries.impl.Cone}, {@link geometries.impl.Torus},
     * {@link geometries.impl.Ellipse} (as a disk), and an imported {@code .obj} triangle mesh
     * (loaded and scaled/translated via {@link ObjMeshLoader}) - lined up on a reflective floor
     * plane under ambient, directional, and point lighting.
     */
    @Test
    void testNewShapesShowcase() {
        createImage("newShapesShowcase.json", "New shapes showcase");
    }

    /**
     * Renders a crystal cluster: six instances of the same hand-authored gem {@code .obj}
     * mesh ({@link ObjMeshLoader}), scaled/translated/colored differently per instance,
     * jutting out of a dark reflective floor. Glassy (high {@code kT}) jewel-tone materials
     * under a violet key light and a cyan spotlight, to show off the mesh system with
     * multiple transformed instances of one imported shape rather than one mesh in
     * isolation.
     */
    @Test
    void testCrystalCluster() {
        createImage("crystalCluster.json", "Crystal cluster");
    }

    /**
     * Renders a texture showcase: an infinite checkerboard floor plane, and three
     * spheres each demonstrating a different {@link primitives.Texture} - procedural
     * stripes, an imported image ({@code sunburst.png}), and procedural rings - to show
     * off both procedural and image-based texture mapping on the two UV-mapped shapes
     * ({@link geometries.impl.Plane}, {@link geometries.impl.Sphere}).
     * <p>
     * Also demonstrates the {@code environment-map} (skybox) replacing the flat background
     * in the dead-sky area, and renders with 4x4 anti-aliasing enabled. This visibly
     * smooths sphere silhouettes and the near-camera checker cells, but - honestly - the
     * far-horizon checker cells (shrunk to sub-pixel size by perspective) still alias:
     * simple supersampling only resolves aliasing down to roughly 1/n a pixel; fixing
     * cells smaller than that needs prefiltering/mipmapping, a different technique this
     * doesn't implement.
     * </p>
     */
    @Test
    void testTextureShowcase() {
        Scene scene = new JsonSceneLoader("Texture showcase", JSON_FILE_PATH + "textureShowcase.json").loadScene();
        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setAntiAliasing(4)
                .setMultithreading(-1)
                .build()
                .renderImage()
                .writeToImage("Texture showcase");
    }

    /**
     * Renders a showcase combining three more additions: a {@link geometries.impl.Ellipsoid}
     * (a stretched "rugby ball" sphere), a {@code checker}-based {@code normalTexture} bump
     * map on the orange sphere (visible as an embossed grid pattern in its specular
     * highlight, not its flat color), and a soft-shadow {@link lighting.PointLight} (via
     * {@code radius}) - its penumbra should read as a visibly graduated, not hard-edged,
     * shadow on the floor.
     */
    @Test
    void testSoftShadowBumpShowcase() {
        Scene scene = new JsonSceneLoader(
                "Soft shadow & bump showcase", JSON_FILE_PATH + "softShadowBumpShowcase.json").loadScene();
        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setAntiAliasing(3)
                .setMultithreading(-1)
                .build()
                .renderImage()
                .writeToImage("Soft shadow and bump showcase");
    }

    /**
     * Renders a "moonlit garden patio" showcase: a stone table on four legs, standing on
     * a bump-mapped tile floor, under a night-sky {@code environment-map}. Three imported
     * {@code .obj} meshes ({@link ObjMeshLoader}) - a cow, the Stanford bunny, and Blender's
     * Suzanne - sit on the table as statues, alongside a fruit bowl ({@link geometries.impl.Torus}
     * plus three spheres), a candle ({@link geometries.impl.Cylinder} plus a flame sphere and
     * a wide-beam {@code spot-light} glow), a flower vase (cylinder body, three angled stem
     * cylinders, three flower-head spheres), and a polished {@link geometries.impl.Ellipsoid}
     * ornament. Lit by a cool ambient/directional "moonlight" pair plus a warm overhead
     * {@code point-light} with a soft-shadow {@code radius}, to combine most of this project's
     * rendering features in one "real life" still-life scene.
     */
    @Test
    void testGardenPatioShowcase() {
        Scene scene = new JsonSceneLoader(
                "Garden patio showcase", JSON_FILE_PATH + "gardenPatioShowcase.json").loadScene();
        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setAntiAliasing(3)
                .setMultithreading(-1)
                .build()
                .renderImage()
                .writeToImage("Garden patio showcase");
    }

    /**
     * Renders the classic Cornell Box, imported directly from the Williams College/Cornell
     * {@code CornellBox-Original.obj} package (public domain) rather than hand-built from
     * primitives. Exercises {@link ObjMeshLoader#loadTrianglesWithMaterials} - the {@code .obj}
     * file's own {@code mtllib}/{@code usemtl} face groups (red/green side walls, white
     * walls/boxes, an emissive ceiling panel) are resolved into per-triangle materials
     * directly, via {@code "materialsFromObj": true"}, instead of one material applied to the
     * whole mesh. Since this is a Whitted-style ray tracer with no radiosity/global
     * illumination, the emissive ceiling panel only makes itself visibly glow - it does not
     * actually illuminate the room - so an explicit {@code point-light} positioned at the
     * panel (with a soft-shadow {@code radius}) provides the actual lighting.
     */
    @Test
    void testCornellBox() {
        Scene scene = new JsonSceneLoader("Cornell Box", JSON_FILE_PATH + "cornellBox.json").loadScene();
        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setAntiAliasing(3)
                .setMultithreading(-1)
                .build()
                .renderImage()
                .writeToImage("Cornell Box");
    }
}
