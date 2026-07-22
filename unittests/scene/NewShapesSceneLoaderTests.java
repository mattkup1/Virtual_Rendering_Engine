package scene;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that {@link SceneLoader} correctly parses the {@code box}, {@code cone},
 * {@code torus}, {@code disk}/{@code ellipse}, and {@code mesh} geometry types from both
 * the JSON and XML {@code newShapesTest} source files, by firing a targeted ray at each
 * shape and checking the resulting intersection point - rather than re-rendering a full
 * image like the other {@link JsonSceneLoader}/{@link XmlSceneLoader} tests, since this
 * only needs to confirm each shape was constructed with the right parameters.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class NewShapesSceneLoaderTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    NewShapesSceneLoaderTests() { /* to satisfy Javadoc generator */ }

    /**
     * The JSON-loaded scene, shared across the assertions in {@link #testNewShapesLoadCorrectly()}
     */
    private static final Scene JSON_SCENE =
            new JsonSceneLoader("New shapes (JSON)", "sceneSourceFiles/json/newShapesTest.json").loadScene();

    /**
     * The XML-loaded scene, shared across the assertions in {@link #testNewShapesLoadCorrectlyXml()}
     */
    private static final Scene XML_SCENE =
            new XmlSceneLoader("New shapes (XML)", "sceneSourceFiles/xml/newShapesTest.xml").loadScene();

    /**
     * Error message for incorrect intersections
     */
    private static final String ERR_INCORRECT_INTERSECTION = "ERROR: Incorrect intersections";

    /**
     * Fires a ray at each of the four new shapes in the given scene and verifies the
     * intersection point, confirming {@link SceneLoader} parsed each one with the correct
     * parameters.
     *
     * @param scene the scene loaded from either the JSON or XML source file
     */
    private static void assertNewShapesLoadedCorrectly(Scene scene) {

        // Box: min (10,-1,-1) to max (12,1,1) - ray along Z hits the near (-Z) face
        Ray boxRay = new Ray(new Point(11, 0, -5), Vector.AXIS_Z);
        assertEquals(new Point(11, 0, -1), scene.geometries.findIntersections(boxRay).getFirst(),
                ERR_INCORRECT_INTERSECTION);

        // Cone: apex (20,0,0), axis +Z, radius 1, height 2 - ray along -Z hits the base cap center
        Ray coneRay = new Ray(new Point(20, 0, 5), new Vector(0, 0, -1));
        assertEquals(new Point(20, 0, 2), scene.geometries.findIntersections(coneRay).getFirst(),
                ERR_INCORRECT_INTERSECTION);

        // Torus: center (40,0,0), axis Z, major radius 3, minor radius 1 - ray along X hits the
        // outer tube wall at x = 40 - (3+1) = 36
        Ray torusRay = new Ray(new Point(30, 0, 0), Vector.AXIS_X);
        assertEquals(new Point(36, 0, 0), scene.geometries.findIntersections(torusRay).getFirst(),
                ERR_INCORRECT_INTERSECTION);

        // Disk: center (50,0,0), normal Z, radius 2 - ray along Z hits the center
        Ray diskRay = new Ray(new Point(50, 0, -5), Vector.AXIS_Z);
        assertEquals(new Point(50, 0, 0), scene.geometries.findIntersections(diskRay).getFirst(),
                ERR_INCORRECT_INTERSECTION);

        // Mesh: testPyramid.obj scaled by 10 and translated by (70,0,0) - the pyramid's apex
        // moves from (0,1,0) to (70,10,0). A ray aimed at the centroid of the first side face
        // (apex, v2, v3) - itself scaled/translated from (-1,0,-1) and (1,0,-1) - confirms the
        // mesh's vertices were both parsed and transformed correctly.
        Point apex = new Point(70, 10, 0);
        Point v2 = new Point(60, 0, -10);
        Point v3 = new Point(80, 0, -10);
        Point centroid = new Point(
                (apex.getX() + v2.getX() + v3.getX()) / 3,
                (apex.getY() + v2.getY() + v3.getY()) / 3,
                (apex.getZ() + v2.getZ() + v3.getZ()) / 3);
        Ray meshRay = new Ray(new Point(centroid.getX(), centroid.getY(), -50), Vector.AXIS_Z);
        assertEquals(centroid, scene.geometries.findIntersections(meshRay).getFirst(),
                ERR_INCORRECT_INTERSECTION);
    }

    /**
     * Test method for the new shapes as parsed by {@link JsonSceneLoader}.
     */
    @Test
    void testNewShapesLoadCorrectlyJson() {
        assertNewShapesLoadedCorrectly(JSON_SCENE);
    }

    /**
     * Test method for the new shapes as parsed by {@link XmlSceneLoader}.
     */
    @Test
    void testNewShapesLoadCorrectlyXml() {
        assertNewShapesLoadedCorrectly(XML_SCENE);
    }
}
