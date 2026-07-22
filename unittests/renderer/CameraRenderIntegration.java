package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import scene.JsonSceneLoader;
import scene.Scene;

/**
 * Integration tests covering how camera location and rotation affect the
 * rendered output of a fixed scene.
 * <p>
 * Each test loads {@code coolScene.json} from {@link #JSON_FILE_PATH} - including its
 * {@link scene.CameraSettings camera settings} (view-plane geometry, resolution, and
 * default look-at target) - and renders it through a camera whose location and roll
 * angle are then overridden per test case. Since location and rotation are exactly
 * what these tests vary, they stay as explicit per-test-case parameters in code rather
 * than moving into the scene file; every other camera setting comes from
 * {@code coolScene.json} so visual differences across rendered images can be
 * attributed solely to the location/rotation parameters under test.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class CameraRenderIntegration {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    CameraRenderIntegration() { /* To satisfy Javadoc generator */ }

    /**
     * Default camera location point. Used by tests that only vary rotation,
     * and overridden by per-test locations in {@link #testLocation()} and
     * {@link #testLocationRotation()}.
     */
    private static final Point LOCATION = Point.ZERO;
    /**
     * Directory containing the JSON scene source files
     */
    private static final String JSON_FILE_PATH = "sceneSourceFiles/json/";

    /**
     * Loads the named JSON scene, renders it through a camera built from that scene's
     * {@link scene.CameraSettings}, positioned and rotated as specified, and writes the
     * resulting image to disk.
     *
     * @param sceneFileName name of the JSON scene file inside {@link #JSON_FILE_PATH}
     * @param imageName     name to use for the produced image (without extension)
     * @param location      world-space point at which to place the camera, overriding
     *                      the location loaded from the scene file
     * @param rotation      camera roll angle in degrees, applied around the
     *                      view direction via {@link Camera.Builder#rotate(double)}
     */
    private static void createImage(String sceneFileName, String imageName, Point location, double rotation) {
        Scene scene = new JsonSceneLoader("Loaded scene", JSON_FILE_PATH + sceneFileName).loadScene();
        Camera.getBuilder()
                .loadFrom(scene.cameraSettings)
                .setLocation(location)
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
