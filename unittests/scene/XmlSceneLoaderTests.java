package scene;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import renderer.Camera;
import renderer.RayTracerType;

import static java.awt.Color.YELLOW;

/**
 * End-to-end rendering tests driven by XML scene source files.
 * <p>
 * Each test loads a scene from an {@code .xml} file under
 * {@link #XML_FILE_PATH}, renders it with a shared base camera,
 * and writes the result to disk. The actual parsing logic lives in
 * {@link XmlSceneLoader} and is intentionally not duplicated here.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@SuppressWarnings("java:S109")
class XmlSceneLoaderTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    XmlSceneLoaderTests() { /* to satisfy Javadoc generator */ }

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
     * Directory containing the XML scene source files
     */
    private static final String XML_FILE_PATH = "sceneSourceFiles/xml/";

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
     * Loads the given XML scene file, attaches a simple ray tracer and renders
     * the image.
     *
     * @param sceneFileName name of the XML scene file inside {@link #XML_FILE_PATH}
     * @return the camera after rendering
     */
    private static Camera renderScene(String sceneFileName) {
        Scene scene = new XmlSceneLoader("Loaded scene", XML_FILE_PATH + sceneFileName).loadScene();
        return cameraBuilder
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build()
                .renderImage();
    }

    /**
     * Convenience helper for grid-less renders: loads the XML file inside
     * {@link #XML_FILE_PATH} and writes the rendered image to disk under
     * the given image name.
     *
     * @param sceneFileName name of the XML scene file inside {@link #XML_FILE_PATH}
     * @param imageName     name to use for the produced image (without extension)
     */
    private static void createImage(String sceneFileName, String imageName) {
        renderScene(sceneFileName)
                .writeToImage(imageName);
    }

    /**
     * Renders the basic two-colors scene loaded from XML.
     */
    @Test
    void testBasicRenderXML() {
        renderScene("basicRenderTestTwoColors.xml")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("XML basic render test");
    }

    /**
     * Renders the basic scene with a custom ambient (kA) factor loaded from XML.
     */
    @Test
    void testKaXML() {
        renderScene("kA_basicRenderTest.xml")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("XML kA test");
    }

    /**
     * Renders the basic scene with per-geometry emission loaded from XML.
     */
    @Test
    void testEmissionXML() {
        renderScene("emission_basicRenderTest.xml")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("XML emission test");
    }

    /**
     * Renders the multi-light XML scene featuring point and spot lights.
     */
    @Test
    void testLightsXML() {
        createImage("lightsTest.xml", "XML lights test");
    }
}
