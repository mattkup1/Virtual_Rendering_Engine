package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import scene.JsonSceneLoader;
import scene.Scene;

public class cameraRenderIntegration {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    cameraRenderIntegration() { /* To satisfy Javadoc generator */ }

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
    private static Camera.Builder baseCameraBuilder(Point location) {
        return Camera.getBuilder()
                .setLocation(location)
                .setDirection(LOOK_AT)
                .setVpDistance(VP_DISTANCE)
                .setVpSize(VP_SIZE, VP_SIZE)
                .setResolution(RESOLUTION, RESOLUTION);
    }

    /**
     * Loads the given JSON scene file, attaches a simple ray tracer and renders
     * the image.
     *
     * @param sceneFileName name of the JSON scene file inside {@link #JSON_FILE_PATH}
     * @return the camera after rendering
     */
    private static Camera renderScene(String sceneFileName, Point location, double rotation) {
        Scene scene = new JsonSceneLoader("Loaded scene", JSON_FILE_PATH + sceneFileName).loadScene();
        return baseCameraBuilder(location)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .rotate(rotation)
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
    private static void createImage(String sceneFileName, String imageName, Point location, double rotation) {
        Scene scene = new JsonSceneLoader("Loaded scene", JSON_FILE_PATH + sceneFileName).loadScene();
        baseCameraBuilder(location)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .rotate(rotation)
                .build()
                .renderImage()
                .writeToImage(imageName);
    }

    @Test
    void testRotate() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Test various angle rotations
        createImage("coolScene.json", "Rotation 45", LOCATION, 45);
        createImage("coolScene.json", "Rotation 30", LOCATION, 30);
        createImage("coolScene.json", "Rotation 70", LOCATION, 70);
        createImage("coolScene.json", "Rotation 10", LOCATION, 10);
    }

    @Test
    void testLocation() {
        final Point far = new Point(0, 0, 1000);
        final Point above = new Point(0, 1000, 0);
        final Point behind = new Point(-300, 0, -1000);
        final Point behindAbove = new Point(-300, 0, -1000);
        
        // Look at scene from further away
        createImage("coolScene.json", "LocationFar", far, 0);
        // Look at scene from above
        createImage("coolScene.json", "LocationAbove", above, 0);
        // Look at the scene from behind
        createImage("coolScene.json", "LocationBehind", behind, 0);
        // Look at the scene from behind and above
        createImage("coolScene.json", "LocationBehindAbove", behindAbove, 0);
    }
}
