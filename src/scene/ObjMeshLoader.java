package scene;

import geometries.impl.Triangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import primitives.Point;
import primitives.Vector;

/**
 * Loader for Wavefront {@code .obj} triangle meshes.
 * <p>
 * Only vertex ({@code v}) and face ({@code f}) lines are read; texture
 * coordinates ({@code vt}), normals ({@code vn}), object/group names
 * ({@code o}/{@code g}), smoothing groups ({@code s}), and comments are
 * ignored, since this renderer computes its own face/vertex normals and has
 * no texture mapping. Faces with more than three vertices are fan-triangulated
 * around their first vertex.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class ObjMeshLoader {

    /**
     * Private constructor - this is a static utility class, not meant to be instantiated.
     */
    private ObjMeshLoader() { /* Utility class, no instances */ }

    /**
     * Loads a {@code .obj} file into a list of triangles.
     *
     * @param path the path to the {@code .obj} file
     * @return the mesh's triangles, in face order (n-gon faces expand to multiple triangles)
     * @throws RuntimeException if the file cannot be read, or a face references a vertex
     *                          index that doesn't exist
     */
    public static List<Triangle> loadTriangles(String path) {
        return loadTriangles(path, 1, null);
    }

    /**
     * Loads a {@code .obj} file into a list of triangles, uniformly scaling and then
     * translating every vertex.
     * <p>
     * The transform is applied per-vertex while parsing, since {@link Triangle}
     * exposes no way to read back or re-derive its vertices after construction.
     * </p>
     *
     * @param path      the path to the {@code .obj} file
     * @param scale     factor to scale every vertex by, relative to the origin
     * @param translate offset to add to every (already-scaled) vertex, or {@code null}
     *                  for no translation
     * @return the mesh's triangles, in face order (n-gon faces expand to multiple triangles)
     * @throws RuntimeException if the file cannot be read, or a face references a vertex
     *                          index that doesn't exist
     */
    public static List<Triangle> loadTriangles(String path, double scale, Vector translate) {
        List<Point> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();

        try {
            for (String line : Files.readAllLines(Paths.get(path))) {
                String trimmed = line.trim();
                if (trimmed.startsWith("v ")) {
                    vertices.add(transform(parseVertex(trimmed), scale, translate));
                } else if (trimmed.startsWith("f ")) {
                    triangulateFace(trimmed, vertices, triangles);
                }
                // Everything else (vt, vn, o, g, s, comments, blank lines) is ignored.
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load OBJ mesh from: " + path, e);
        }

        return triangles;
    }

    /**
     * Parses a {@code v x y z} vertex line into a {@link Point}.
     *
     * @param line the trimmed vertex line, starting with {@code "v "}
     * @return the parsed vertex point
     */
    private static Point parseVertex(String line) {
        String[] parts = line.substring(2).trim().split("\\s+");
        return new Point(
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]));
    }

    /**
     * Scales a point relative to the origin, then translates it.
     *
     * @param point     the point to transform
     * @param scale     the scale factor
     * @param translate the translation offset, or {@code null} for none
     * @return the transformed point
     */
    private static Point transform(Point point, double scale, Vector translate) {
        Point scaled = new Point(point.getX() * scale, point.getY() * scale, point.getZ() * scale);
        return translate == null ? scaled : scaled.add(translate);
    }

    /**
     * Parses an {@code f ...} face line and appends its fan-triangulated triangles.
     * <p>
     * Each face token may be a bare vertex index ({@code 5}) or a slash-separated
     * group with texture/normal indices ({@code 5/2/1}, {@code 5//1}) - only the
     * vertex index (the part before the first slash) is used.
     * </p>
     *
     * @param line      the trimmed face line, starting with {@code "f "}
     * @param vertices  the vertices parsed so far
     * @param triangles the list to append the face's triangles to
     */
    private static void triangulateFace(String line, List<Point> vertices, List<Triangle> triangles) {
        String[] tokens = line.substring(2).trim().split("\\s+");
        Point[] faceVertices = new Point[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            faceVertices[i] = vertices.get(resolveIndex(tokens[i], vertices.size()));
        }

        // Fan triangulation around the first vertex.
        for (int i = 1; i < faceVertices.length - 1; i++) {
            triangles.add(new Triangle(faceVertices[0], faceVertices[i], faceVertices[i + 1]));
        }
    }

    /**
     * Resolves a face-token's vertex index to a zero-based index into {@code vertices}.
     * <p>
     * OBJ vertex indices are 1-based; a negative index is relative to the current end
     * of the vertex list (e.g. {@code -1} is the most recently defined vertex).
     * </p>
     *
     * @param token       the face token (e.g. {@code "5"}, {@code "5/2/1"})
     * @param vertexCount the number of vertices parsed so far
     * @return the zero-based index into the vertex list
     */
    private static int resolveIndex(String token, int vertexCount) {
        int index = Integer.parseInt(token.split("/")[0]);
        return index > 0 ? index - 1 : vertexCount + index;
    }
}
