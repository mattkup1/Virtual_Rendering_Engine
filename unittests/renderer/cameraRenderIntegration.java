package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import scene.JsonSceneLoader;
import scene.Scene;

/**
 * Integration tests covering how camera location and rotation affect the
 * rendered output of a fixed scene.
 * <p>
 * Each test loads {@code coolScene.json} from {@link #JSON_FILE_PATH} and
 * renders it through a freshly built camera whose location and roll angle
 * vary per test case. The shared view-plane geometry ({@link #VP_SIZE},
 * {@link #VP_DISTANCE}, {@link #RESOLUTION}) and look-at target
 * ({@link #LOOK_AT}) are held constant so that any visual differences
 * across rendered images can be attributed to the location/rotation
 * parameters under test.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
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
     * Default camera location point. Used by tests that only vary rotation,
     * and overridden by per-test locations in {@link #testLocation()} and
     * {@link #testLocationRotation()}.
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
     * Builds a fresh, pre-configured camera builder positioned at the given location.
     * <p>
     * The returned builder is seeded with the shared test settings — view-plane
     * distance ({@link #VP_DISTANCE}), view-plane size ({@link #VP_SIZE}),
     * look-at target ({@link #LOOK_AT}) and image resolution
     * ({@link #RESOLUTION}). Callers still need to attach a ray tracer (and
     * optionally apply rotation) before invoking {@link Camera.Builder#build()}.
     * A new builder instance is returned on every call, so tests do not share
     * builder state.
     * </p>
     *
     * @param location world-space point at which to place the camera
     * @return a pre-configured {@link Camera.Builder} ready for further customization
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
     * Loads the named JSON scene, renders it through a camera positioned and
     * rotated as specified, and writes the resulting image to disk.
     *
     * @param sceneFileName name of the JSON scene file inside {@link #JSON_FILE_PATH}
     * @param imageName     name to use for the produced image (without extension)
     * @param location      world-space point at which to place the camera
     * @param rotation      camera roll angle in degrees, applied around the
     *                      view direction via {@link Camera.Builder#rotate(double)}
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

    /**
     * Renders {@code coolScene.json} from the default {@link #LOCATION} at
     * four distinct roll angles (10°, 30°, 45°, 70°), exercising the
     * camera's rotation parameter while every other camera setting is held
     * constant.
     */
    @Test
    void testRotate() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Test various angle rotations
        createImage("coolScene.json", "Rotation 45", LOCATION, 45);
        createImage("coolScene.json", "Rotation 30", LOCATION, 30);
        createImage("coolScene.json", "Rotation 70", LOCATION, 70);
        createImage("coolScene.json", "Rotation 10", LOCATION, 10);
    }

    /**
     * Renders {@code coolScene.json} from four distinct camera positions
     * (far in front of the scene, directly above it, behind-and-to-the-left,
     * and behind-and-above) with no rotation applied, exercising the camera's
     * translation parameter while every other setting is held constant.
     */
    @Test
    void testLocation() {

        final Point far = new Point(0, 0, 1000);
        final Point above = new Point(0, 1000, 0);
        final Point behind = new Point(-300, 0, -1000);
        final Point behindAbove = new Point(10, 200, -1000);

        // ============ Equivalence Partitions Tests ==============

        // EP01: Look at scene from further away
        createImage("coolScene.json", "LocationFar", far, 0);
        // EP02: Look at scene from above
        createImage("coolScene.json", "LocationAbove", above, 0);
        // EP03: Look at the scene from behind
        createImage("coolScene.json", "LocationBehind", behind, 0);
        // EP04: Look at the scene from behind and above
        createImage("coolScene.json", "LocationBehindAbove", behindAbove, 0);
    }

    /**
     * Renders {@code coolScene.json} with the camera both translated to a
     * behind-and-offset viewpoint and rotated by 45°, verifying that the
     * translation and rotation parameters compose correctly when applied
     * together rather than in isolation.
     */
    @Test
    void testLocationRotation() {
        final Point behind = new Point(-300, 0, -1000);
        createImage("coolScene.json", "Location and Rotation", behind, 45);
    }
}
