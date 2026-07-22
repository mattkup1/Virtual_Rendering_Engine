package scene;

import geometries.impl.Triangle;
import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ObjMeshLoader}.
 * <p>
 * Tests are driven by {@code sceneSourceFiles/obj/testPyramid.obj} - a 5-vertex,
 * 5-face square pyramid (4 triangular sides, 1 quad base) - and check the resulting
 * triangles directly via {@link Triangle#equals}, since face order and vertex order
 * within each face are entirely deterministic from the file.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class ObjMeshLoaderTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    ObjMeshLoaderTests() { /* to satisfy Javadoc generator */ }

    /**
     * Path to the test pyramid mesh
     */
    private static final String PYRAMID_PATH = "sceneSourceFiles/obj/testPyramid.obj";

    /**
     * The pyramid's apex vertex
     */
    private static final Point APEX = new Point(0, 1, 0);
    /**
     * The pyramid's base vertices, in file order
     */
    private static final Point V2 = new Point(-1, 0, -1);
    private static final Point V3 = new Point(1, 0, -1);
    private static final Point V4 = new Point(1, 0, 1);
    private static final Point V5 = new Point(-1, 0, 1);

    /**
     * Error message for an incorrect triangle count
     */
    private static final String ERR_TRIANGLE_COUNT = "ERROR: Incorrect number of triangles";
    /**
     * Error message for an incorrectly constructed triangle
     */
    private static final String ERR_TRIANGLE = "ERROR: Incorrect triangle";

    /**
     * Test method for {@link ObjMeshLoader#loadTriangles(String)}: verifies that the four
     * triangular side faces pass through unchanged (one triangle each, in file vertex order),
     * that a negative (relative) vertex index resolves correctly, and that the quad base face
     * is fan-triangulated into two triangles.
     */
    @Test
    void testLoadTriangles() {
        List<Triangle> triangles = ObjMeshLoader.loadTriangles(PYRAMID_PATH);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Total triangle count - 4 pass-through triangle faces + 2 from the fan-triangulated quad
        assertEquals(6, triangles.size(), ERR_TRIANGLE_COUNT);

        // EP02: Triangular faces pass through with vertices in file order
        assertEquals(new Triangle(APEX, V2, V3), triangles.get(0), ERR_TRIANGLE);
        assertEquals(new Triangle(APEX, V3, V4), triangles.get(1), ERR_TRIANGLE);
        assertEquals(new Triangle(APEX, V4, V5), triangles.get(2), ERR_TRIANGLE);

        // =============== Boundary Values Tests ==================
        // BV01: Face "f 1 5 -4" resolves the negative index (-4) to vertex 2 (V2)
        assertEquals(new Triangle(APEX, V5, V2), triangles.get(3), ERR_TRIANGLE);

        // BV02: Quad base face "f 2 3 4 5" fan-triangulates into two triangles around V2
        assertEquals(new Triangle(V2, V3, V4), triangles.get(4), ERR_TRIANGLE);
        assertEquals(new Triangle(V2, V4, V5), triangles.get(5), ERR_TRIANGLE);
    }

    /**
     * Test method for {@link ObjMeshLoader#loadTriangles(String, double, Vector)}: verifies
     * that every vertex is scaled relative to the origin and then translated.
     */
    @Test
    void testLoadTrianglesWithTransform() {
        List<Triangle> triangles = ObjMeshLoader.loadTriangles(PYRAMID_PATH, 2, new Vector(10, 0, 0));

        Point expectedApex = new Point(10, 2, 0);
        Point expectedV2 = new Point(8, 0, -2);
        Point expectedV3 = new Point(12, 0, -2);

        assertEquals(new Triangle(expectedApex, expectedV2, expectedV3), triangles.getFirst(), ERR_TRIANGLE);
    }
}
