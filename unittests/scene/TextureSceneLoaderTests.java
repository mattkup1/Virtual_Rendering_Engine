package scene;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that {@link SceneLoader} correctly parses {@code material.texture.*}
 * attributes into a working {@link primitives.Texture} on the resulting
 * {@link primitives.Material}, for both a procedural ({@code checker}) and an
 * image-based texture, by inspecting the {@link geometries.api.Intersectable.Intersection}
 * returned for a targeted ray rather than rendering a full image.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class TextureSceneLoaderTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    TextureSceneLoaderTests() { /* to satisfy Javadoc generator */ }

    /**
     * The scene loaded from {@code textureTest.json}, shared across the test methods
     */
    private static final Scene SCENE =
            new JsonSceneLoader("Texture test", "sceneSourceFiles/json/textureTest.json").loadScene();

    /**
     * Error message for a missing intersection
     */
    private static final String ERR_NO_INTERSECTION = "ERROR: Expected an intersection";
    /**
     * Error message for an incorrectly parsed texture
     */
    private static final String ERR_TEXTURE = "ERROR: Incorrectly parsed texture";

    /**
     * Test method verifying the {@code checker} texture on the first sphere (center
     * origin, radius 10): a ray hitting (0,0,10) lands at UV (0.75, 0.5), which the
     * parsed 0.1-unit checker should resolve to {@code colorA} (red).
     */
    @Test
    void testCheckerTextureParsed() {
        Ray ray = new Ray(new Point(0, 0, 20), new Vector(0, 0, -1));
        var intersections = SCENE.geometries.calcIntersections(ray);
        assertNotNull(intersections, ERR_NO_INTERSECTION);

        var intersection = intersections.getFirst();
        var uv = intersection.geometry.getUV(intersection.point);
        assertEquals(new Color(255, 0, 0), intersection.material.texture.sample(uv), ERR_TEXTURE);
    }

    /**
     * Test method verifying the {@code image} texture on the second sphere (center
     * (100,0,0), radius 10): a ray hitting (100,0,10) lands at UV (0.75, 0.5), which the
     * parsed {@code testSwatch.png} (loaded via {@link primitives.ImageTexture}) should
     * resolve to its bottom-right quadrant (white).
     */
    @Test
    void testImageTextureParsed() {
        Ray ray = new Ray(new Point(100, 0, 20), new Vector(0, 0, -1));
        var intersections = SCENE.geometries.calcIntersections(ray);
        assertNotNull(intersections, ERR_NO_INTERSECTION);

        var intersection = intersections.getFirst();
        var uv = intersection.geometry.getUV(intersection.point);
        assertEquals(new Color(255, 255, 255), intersection.material.texture.sample(uv), ERR_TEXTURE);
    }
}
