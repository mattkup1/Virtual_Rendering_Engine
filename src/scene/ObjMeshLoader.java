package scene;

import geometries.impl.Triangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import primitives.Color;
import primitives.Material;
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
                if (hasTag(trimmed, "v")) {
                    vertices.add(transform(parseVertex(trimmed), scale, translate));
                } else if (hasTag(trimmed, "f")) {
                    triangulateFace(trimmed, vertices, triangles, null);
                }
                // Everything else (vt, vn, o, g, s, comments, blank lines) is ignored.
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load OBJ mesh from: " + path, e);
        }

        return triangles;
    }

    /**
     * Checks whether a trimmed line begins with the given tag followed by whitespace (or is
     * exactly the tag, for zero-argument lines) - unlike a plain {@code startsWith(tag + " ")},
     * this tolerates any whitespace character as the separator, not just a literal space.
     * <p>
     * Some public-domain {@code .obj} packages (e.g. the Cornell Box) are inconsistent about
     * this - some lines use a tab after {@code v} where most use a space - and a plain
     * space-only check would silently skip those lines, corrupting every subsequent face's
     * vertex indices rather than failing loudly.
     * </p>
     *
     * @param line the trimmed line
     * @param tag  the tag to check for (e.g. {@code "v"}, {@code "f"}, {@code "mtllib"})
     * @return whether the line starts with {@code tag} followed by whitespace (or end of line)
     */
    private static boolean hasTag(String line, String tag) {
        return line.startsWith(tag)
                && (line.length() == tag.length() || Character.isWhitespace(line.charAt(tag.length())));
    }

    /**
     * Loads a {@code .obj} file exactly as {@link #loadTriangles(String, double, Vector)}
     * does, but also resolves the file's own {@code mtllib}/{@code usemtl} face-group
     * materials (Wavefront {@code .mtl} format) and applies each face's material directly to
     * that face's triangles, instead of leaving every triangle with the default black/
     * matte material for the caller to override uniformly.
     * <p>
     * Intended for multi-material scene packages (e.g. the Cornell Box) where the source
     * file's own per-face-group colors should be used. A face group's material maps its
     * {@code Kd} (diffuse) - or {@code Ke} (emissive), when present, since {@code Kd} alone
     * would leave genuinely light-emitting faces looking like flat matte paint - onto this
     * renderer's convention of carrying a surface's visible base color in {@code emission}
     * (see {@link Material}), with a modest fixed diffuse/specular response layered on top.
     * A face whose {@code usemtl} name has no matching {@code newmtl} block (or a file with
     * no {@code mtllib} at all) falls back to the default, uncolored material.
     * </p>
     *
     * @param path      the path to the {@code .obj} file
     * @param scale     factor to scale every vertex by, relative to the origin
     * @param translate offset to add to every (already-scaled) vertex, or {@code null} for
     *                  no translation
     * @return the mesh's triangles, each already carrying its face group's material/emission
     * @throws RuntimeException if the {@code .obj} or a referenced {@code .mtl} file cannot
     *                          be read, or a face references a vertex index that doesn't exist
     */
    public static List<Triangle> loadTrianglesWithMaterials(String path, double scale, Vector translate) {
        List<Point> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();
        Map<String, MtlMaterial> materials = new HashMap<>();
        MtlMaterial currentMaterial = MtlMaterial.DEFAULT;

        try {
            Path objPath = Paths.get(path);
            for (String line : Files.readAllLines(objPath)) {
                String trimmed = line.trim();
                if (hasTag(trimmed, "v")) {
                    vertices.add(transform(parseVertex(trimmed), scale, translate));
                } else if (hasTag(trimmed, "f")) {
                    triangulateFace(trimmed, vertices, triangles, currentMaterial);
                } else if (hasTag(trimmed, "mtllib")) {
                    Path mtlPath = objPath.resolveSibling(trimmed.substring("mtllib".length()).trim());
                    materials.putAll(loadMaterials(mtlPath));
                } else if (hasTag(trimmed, "usemtl")) {
                    String name = trimmed.substring("usemtl".length()).trim();
                    currentMaterial = materials.getOrDefault(name, MtlMaterial.DEFAULT);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load OBJ mesh (with materials) from: " + path, e);
        }

        return triangles;
    }

    /**
     * A single named material parsed from a Wavefront {@code .mtl} file, holding just the
     * fields {@link #loadTrianglesWithMaterials} maps into this renderer's convention -
     * {@code Kd} (diffuse), {@code Ks} (specular), and {@code Ke} (emissive), each a raw
     * {@code [0,1]}-range RGB triple as the {@code .mtl} format stores them.
     *
     * @param kd diffuse color
     * @param ks specular color
     * @param ke emissive color
     */
    private record MtlMaterial(double[] kd, double[] ks, double[] ke) {
        /** Plain white diffuse, no specular or emission - used when a face's material is unresolvable. */
        private static final MtlMaterial DEFAULT =
                new MtlMaterial(new double[]{1, 1, 1}, new double[]{0, 0, 0}, new double[]{0, 0, 0});
    }

    /**
     * Parses a Wavefront {@code .mtl} material library file into its named materials.
     *
     * @param mtlPath the path to the {@code .mtl} file
     * @return the file's materials, keyed by their {@code newmtl} name
     * @throws IOException if the file cannot be read
     */
    private static Map<String, MtlMaterial> loadMaterials(Path mtlPath) throws IOException {
        Map<String, MtlMaterial> materials = new HashMap<>();
        String name = null;
        double[] kd = {1, 1, 1};
        double[] ks = {0, 0, 0};
        double[] ke = {0, 0, 0};

        for (String rawLine : Files.readAllLines(mtlPath)) {
            String line = rawLine.trim();
            if (hasTag(line, "newmtl")) {
                if (name != null) {
                    materials.put(name, new MtlMaterial(kd, ks, ke));
                }
                name = line.substring("newmtl".length()).trim();
                kd = new double[]{1, 1, 1};
                ks = new double[]{0, 0, 0};
                ke = new double[]{0, 0, 0};
            } else if (hasTag(line, "Kd")) {
                kd = parseRgbTriple(line);
            } else if (hasTag(line, "Ks")) {
                ks = parseRgbTriple(line);
            } else if (hasTag(line, "Ke")) {
                ke = parseRgbTriple(line);
            }
            // Everything else (Ka, Ns, Ni, illum, map_*, comments) is ignored.
        }
        if (name != null) {
            materials.put(name, new MtlMaterial(kd, ks, ke));
        }
        return materials;
    }

    /**
     * Parses a {@code <tag> r g b} line's three leading numeric tokens, ignoring any trailing
     * inline comment (e.g. {@code "Kd 0.63 0.065 0.05 # Red"}).
     *
     * @param line the trimmed line, starting with a two-character tag and a space
     * @return the three parsed components, in order
     */
    private static double[] parseRgbTriple(String line) {
        String[] parts = line.substring(2).trim().split("\\s+");
        return new double[]{
                Double.parseDouble(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2])};
    }

    /**
     * Multiplies a material's raw {@code Ke} into this renderer's {@code [0,255]} emission
     * convention. {@code .mtl} emissive values are typically small path-tracer radiance
     * multipliers (e.g. ~10-20 for a bright area light), not already-scaled display values.
     */
    private static final double EMISSIVE_SCALE = 15;

    /**
     * Below this per-component threshold, a material's {@code Ke} is treated as "not
     * actually emissive" (many non-emissive materials still declare {@code Ke 0 0 0}
     * exactly, but this tolerates near-zero noise too).
     */
    private static final double EMISSIVE_THRESHOLD = 0.01;

    /**
     * Derives this renderer's emission color for a parsed material: its (scaled) {@code Ke}
     * if it is meaningfully emissive, otherwise its {@code Kd} scaled to {@code [0,255]}.
     *
     * @param material the parsed material
     * @return the derived emission color
     */
    private static Color deriveEmission(MtlMaterial material) {
        double[] ke = material.ke();
        boolean isEmissive = ke[0] > EMISSIVE_THRESHOLD || ke[1] > EMISSIVE_THRESHOLD || ke[2] > EMISSIVE_THRESHOLD;
        double[] rgb = isEmissive ? ke : material.kd();
        double scale = isEmissive ? EMISSIVE_SCALE : 255;
        return new Color(rgb[0] * scale, rgb[1] * scale, rgb[2] * scale);
    }

    /**
     * Derives a modest {@link Material} for a parsed material: a fixed diffuse response (the
     * face's actual color already lives in {@link #deriveEmission}), plus a specular
     * highlight only when the source material declared a non-zero {@code Ks}.
     *
     * @param material the parsed material
     * @return the derived material
     */
    private static Material deriveMaterial(MtlMaterial material) {
        double[] ks = material.ks();
        boolean hasSpecular = ks[0] > 0 || ks[1] > 0 || ks[2] > 0;
        return new Material()
                .setKD(0.3)
                .setKS(hasSpecular ? 0.2 : 0.02)
                .setShininess(hasSpecular ? 60 : 20);
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
     * @param material  the face's current material (from the most recent {@code usemtl}), or
     *                  {@code null} to leave each triangle's material/emission untouched
     */
    private static void triangulateFace(String line, List<Point> vertices, List<Triangle> triangles,
                                         MtlMaterial material) {
        String[] tokens = line.substring(2).trim().split("\\s+");
        Point[] faceVertices = new Point[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            faceVertices[i] = vertices.get(resolveIndex(tokens[i], vertices.size()));
        }

        // Fan triangulation around the first vertex.
        for (int i = 1; i < faceVertices.length - 1; i++) {
            Triangle triangle = new Triangle(faceVertices[0], faceVertices[i], faceVertices[i + 1]);
            if (material != null) {
                triangle.setEmission(deriveEmission(material)).setMaterial(deriveMaterial(material));
            }
            triangles.add(triangle);
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
