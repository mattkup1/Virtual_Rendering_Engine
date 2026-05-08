package renderer;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import scene.JsonSceneLoader;
import scene.Scene;
import scene.XmlSceneLoader;

import static java.awt.Color.YELLOW;

/**
 * End-to-end rendering tests.
 * <p>
 * These tests demonstrate the full rendering pipeline:
 * scene construction → camera setup → ray tracing → image generation.
 * <p>
 * The first test produces a simple scene intended as a reference image
 * for validating Camera and Renderer implementations.
 */

@SuppressWarnings("java:S109")
class RenderTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    RenderTests() { /* to satisfy Javadoc generator */ }

    /**
     * Physical size of View Plane (it is a square: SIZExSIZE)
     */
    static final double VP_SIZE = 500;
    /**
     * Distance from Camera to View Plane
     */
    static final double VP_DISTANCE = 100;

    /**
     * Camera location point
     */
    static final Point LOCATION = Point.ZERO;
    /**
     * Camera direction target point
     */
    static final Point LOOK_AT = new Point(0, 0, -1);
    /**
     * Image resolution (it is a square: NxN)
     */
    static final int RESOLUTION = 1000;
    /**
     * json scene source file - file path
     */
    static final String jsonFilePath = "sceneSourceFiles/json/";
    /**
     * xml scene source file - file path
     */
    static final String xmlFilePath = "sceneSourceFiles/xml/";

    /**
     * Creates a base camera builder for the tests.
     *
     * @return camera builder configured with the common test settings
     */
    private static Camera.Builder baseCameraBuilder() {
        return Camera.getBuilder() //
                .setLocation(LOCATION).setDirection(LOOK_AT) //
                .setVpDistance(VP_DISTANCE).setVpSize(VP_SIZE, VP_SIZE) //
                .setResolution(RESOLUTION, RESOLUTION);
    }

    /**
     * Produce a scene with basic 3D model and render it into a png image with a
     * grid
     */
    @Test
    void testBasicRenderTwoColors() {
        Scene scene = new Scene("Two colors")                   //
                .setBackground(new Color(75, 127, 90))                       //
                .setAmbientLight(new AmbientLight(new Color(255, 191, 191)));

        final double Z = -100D;
        // Left, Middle, Right X Bottom, Middle, Top
        Point pLM = new Point(-100, 0, Z);
        Point pMT = new Point(0, 100, Z);
        Point pLT = new Point(-100, 100, Z);
        Point pMB = new Point(0, -100, Z);
        Point pLB = new Point(-100, -100, Z);
        Point pRM = new Point(100, 0, Z);
        Point pRB = new Point(100, -100, Z);
        Point o = new Point(0, 0, Z);
        double radius = 50D;

        scene.geometries
                .add(// center
                        new Sphere(o, radius),
                        // up left
                        new Triangle(pLM, pMT, pLT),
                        // down left
                        new Triangle(pLM, pMB, pLB),
                        // down right
                        new Triangle(pRM, pMB, pRB));

        baseCameraBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build()
                .renderImage()
                .printGrid(100, new Color(YELLOW))
                .writeToImage("Two colors render test");
    }

    /**
     * Renders a scene loaded from an XML file.
     * <p>
     * Note: parsing logic should not be implemented inside tests.
     *
     * @param builder the camera builder to use
     * @param xmlName the XML scene file name
     * @return the camera after rendering
     */
    Camera renderSceneXML(Camera.Builder builder, String xmlName) {

        XmlSceneLoader SceneLoader = new XmlSceneLoader("Using XML", xmlName);

        Scene scene = SceneLoader.loadScene();

        return builder
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build()
                .renderImage();
    }

    /**
     * Renders a scene loaded from a JSON file.
     * <p>
     * Note: parsing logic should not be implemented inside tests.
     *
     * @param builder  the camera builder to use
     * @param jsonName the JSON scene file name
     * @return the camera after rendering
     */
    static Camera renderSceneJSON(Camera.Builder builder, String jsonName) {

        JsonSceneLoader SceneLoader = new JsonSceneLoader("Using JSON", jsonName);

        Scene scene = SceneLoader.loadScene();

        return builder
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build()
                .renderImage();
    }

    /**
     * Test for XML base scene - for bonus
     */
    @Test
    void testBasicRenderXml() {
        renderSceneXML(baseCameraBuilder(), xmlFilePath + "basicRenderTestTwoColors.xml")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("render test xml");
    }

    /**
     * Test for XML base scene with kA factor - for bonus
     */
    @Test
    void testBasicRenderXml_kA() {
        renderSceneXML(baseCameraBuilder(), xmlFilePath + "kA_basicRenderTest.xml")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("xml with kA test");
    }

    /**
     * Test for XML base scene with emission light - for bonus
     */
    @Test
    void testBasicRenderXml_emission() {
        renderSceneXML(baseCameraBuilder(), xmlFilePath + "emission_basicRenderTest.xml")
                .printGrid(100, new Color(YELLOW))
                .writeToImage("xml with emission test");
    }

    /**
     * Test for JSON based scene - for bonus
     */
    @Test
    void testBasicRenderJson() {
        renderSceneJSON(baseCameraBuilder(), jsonFilePath + "basicRenderTestTwoColors.json") //
                .printGrid(100, new Color(YELLOW)) //
                .writeToImage("render test json");
    }

    /**
     * Test for JSON base scene with kA - for bonus
     */
    @Test
    void testBasicRenderJson_kA() {
        renderSceneJSON(baseCameraBuilder(), jsonFilePath + "kA_basicRenderTest.json") //
                .printGrid(100, new Color(YELLOW)) //
                .writeToImage("json with kA test");
    }

    /**
     * Test for JSON base scene with emission light - for bonus
     */
    @Test
    void testBasicRenderJson_emission() {
        renderSceneJSON(baseCameraBuilder(), jsonFilePath + "emission_basicRenderTest.json") //
                .printGrid(100, new Color(YELLOW)) //
                .writeToImage("json with emission test");
    }
}
