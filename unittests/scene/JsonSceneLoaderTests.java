package scene;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import renderer.Camera;
import renderer.RayTracerType;

import static java.awt.Color.YELLOW;

/**
 * End-to-end rendering tests driven by JSON scene source files.
 * <p>
 * Each test loads a scene from a {@code .json} file under
 * {@link #JSON_FILE_PATH}, renders it with a shared base camera,
 * and writes the result to disk. The actual parsing logic lives in
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
     * Physical size of the View Plane (it is a square: SIZE x SIZE)
     */
    private static final double VP_SIZE = 500;
    /**
     * Distance from the Camera to the View Plane
     */
    private static final double VP_DISTANCE = 100;
    /**
     * Camera location point
     */
    private static final Point LOCATION = Point.ZERO;
    /**
     * Camera direction target point
     */
    private static final Point LOOK_AT = new Point(0, 0, -1);
    /**
     * Image resolution (it is a square: N x N)
     */
    private static final int RESOLUTION = 1000;
    /**
     * Directory containing the JSON scene source files
     */
    private static final String JSON_FILE_PATH = "sceneSourceFiles/json/";

    /**
     * Shared camera builder pre-configured with the common test settings.
     * <p>
     * Each test calls {@link Camera.Builder#setRayTracer(Scene, RayTracerType)} on this
     * builder before {@link Camera.Builder#build()} produces a fresh {@link Camera}
     * clone. This relies on sequential test execution; enabling JUnit parallel
     * execution would race on the builder's internal state.
     * </p>
     */
    private static final Camera.Builder cameraBuilder = Camera.getBuilder()
            .setLocation(LOCATION)
            .setDirection(LOOK_AT)
            .setVpDistance(VP_DISTANCE)
            .setVpSize(VP_SIZE, VP_SIZE)
            .setResolution(RESOLUTION, RESOLUTION);

    /**
     * Loads the given JSON scene file, attaches a simple ray tracer and renders
     * the image.
     *
     * @param sceneFileName name of the JSON scene file inside {@link #JSON_FILE_PATH}
     * @return the camera after rendering
     */
    private static Camera renderScene(String sceneFileName) {
        Scene scene = new JsonSceneLoader("Loaded scene", JSON_FILE_PATH + sceneFileName).loadScene();
        return cameraBuilder
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
     * Renders the JSON scene that resembles the course home-page picture.
     */
    @Test
    void testCourseHomeJSON() {
        createImage("courseHome.json", "JSON course home pic");
    }
}
