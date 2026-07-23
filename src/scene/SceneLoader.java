package scene;

import geometries.api.Geometry;
import geometries.api.Intersectable;
import geometries.impl.Box;
import geometries.impl.Cone;
import geometries.impl.Cylinder;
import geometries.impl.Ellipse;
import geometries.impl.Ellipsoid;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Torus;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.LightSource;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.CheckerTexture;
import primitives.Color;
import primitives.Double3;
import primitives.ImageTexture;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.RingTexture;
import primitives.StripeTexture;
import primitives.Texture;
import primitives.Vector;

/**
 * Abstract base class for scene loading operations.
 * <p>
 * This class implements the Template Method Pattern. It defines the high-level
 * algorithm for populating a {@link Scene} (background, lights, and geometries) from a file,
 * while delegating format-specific data extraction (XML or JSON) to subclasses.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public abstract class SceneLoader {

    /**
     * The scene object being populated by this loader
     */
    private final Scene scene;

    /**
     * Initializes a new loader and creates a new Scene with the given name.
     *
     * @param sceneName the identifier for the scene
     */
    public SceneLoader(String sceneName) {
        this.scene = new Scene(sceneName);
    }

    /**
     * The Template Method. Orchestrates the loading process.
     * <p>
     * It executes the algorithm steps in order:
     * 1. Setting the background color.
     * 2. Initializing ambient lighting.
     * 3. Constructing all light sources.
     * 4. Constructing all geometric shapes.
     * 5. Constructing the camera settings, if the source file defines any.
     * </p>
     *
     * @return the fully populated {@link Scene}
     */
    public Scene loadScene() {
        // Process Background Color
        String backgroundColor = getBackgroundColor();
        if (backgroundColor != null) {
            scene.background = parseColor(backgroundColor);
        }

        // Process Ambient Light
        String ambientColor = getAmbientLight();
        if (ambientColor != null) {
            scene.ambientLight = new AmbientLight(parseColor(ambientColor));
        }

        // Process Environment Map
        Map<String, String> environmentMapData = getEnvironmentMap();
        if (environmentMapData != null) {
            scene.environmentMap = buildTexture(environmentMapData, "");
        }

        // Process Light Sources
        for (var lightData : getLights()) {
            scene.lights.add(buildLight(lightData));
        }

        // Process Camera
        Map<String, String> cameraData = getCamera();
        if (cameraData != null) {
            scene.cameraSettings = buildCameraSettings(cameraData);
        }

        // Process Geometries
        // Subclasses provide raw string data in maps, this class builds the objects.
        List<Map<String, String>> geometryData = getGeometries();
        List<Intersectable> topLevelGeometries = new ArrayList<>(geometryData.size());
        for (var data : geometryData) {
            topLevelGeometries.add(buildGeometries(data));
        }
        scene.geometries = Geometries.buildBVH(topLevelGeometries);

        return scene;
    }

    /**
     * Centralized Factory Method for creating geometric objects.
     * <p>
     * This method handles the logic of converting raw string attributes into
     * concrete geometry implementations, then optionally attaches emission and material data.
     * </p>
     * <p>
     * Supported geometry types: {@code sphere}, {@code triangle}, {@code plane},
     * {@code tube}, {@code cylinder}, {@code polygon}, {@code box}, {@code cone},
     * {@code torus}, {@code ellipse} (alias {@code disk}), {@code ellipsoid}, and
     * {@code mesh} (an imported {@code .obj} triangle mesh).
     * </p>
     *
     * @param data a map containing the attributes for the geometry
     * @return the constructed {@link Geometry}, or a {@link Geometries} composite for
     *         {@code mesh} (both are {@link Intersectable})
     * @throws IllegalArgumentException if the geometry type is unsupported
     */
    private Intersectable buildGeometries(Map<String, String> data) {
        String type = data.get("type");
        if ("mesh".equals(type)) {
            return buildMesh(data);
        }

        Geometry geometry;
        switch (type) {
            case "sphere" -> {
                Point center = parsePoint(data.get("center"));
                double radius = Double.parseDouble(data.get("radius"));
                geometry = new Sphere(center, radius);
            }
            case "triangle" -> {
                Point p0 = parsePoint(data.get("p0"));
                Point p1 = parsePoint(data.get("p1"));
                Point p2 = parsePoint(data.get("p2"));
                geometry = new Triangle(p0, p1, p2);
            }
            case "plane" -> {
                Point p0 = parsePoint(data.get("p0"));
                // Supports Plane(point, normal) or Plane(p0, p1, p2)
                if (data.containsKey("normal")) {
                    geometry = new Plane(p0, parseVector(data.get("normal")));
                } else {
                    Point p1 = parsePoint(data.get("p1"));
                    Point p2 = parsePoint(data.get("p2"));
                    geometry = new Plane(p0, p1, p2);
                }
            }
            case "tube" -> {
                double radius = Double.parseDouble(data.get("radius"));
                Ray axis = new Ray(parsePoint(data.get("origin")), parseVector(data.get("AxisDirection")));
                geometry = new Tube(radius, axis);
            }
            case "cylinder" -> {
                double radius = Double.parseDouble(data.get("radius"));
                Ray axis = new Ray(parsePoint(data.get("origin")), parseVector(data.get("AxisDirection")));
                double height = Double.parseDouble(data.get("height"));
                geometry = new Cylinder(radius, axis, height);
            }
            case "polygon" -> {
                // Dynamically parses vertices labeled p0, p1, p2... based on the count
                int numVertices = Integer.parseInt(data.get("number of vertices"));
                Point[] vertices = new Point[numVertices];
                for (int k = 0; k < numVertices; ++k)
                    vertices[k] = parsePoint(data.get("p" + k));
                geometry = new Polygon(vertices);
            }
            case "box" -> {
                Point min = parsePoint(data.get("min"));
                Point max = parsePoint(data.get("max"));
                geometry = new Box(min, max);
            }
            case "cone" -> {
                double radius = Double.parseDouble(data.get("radius"));
                Ray axis = new Ray(parsePoint(data.get("origin")), parseVector(data.get("AxisDirection")));
                double height = Double.parseDouble(data.get("height"));
                geometry = new Cone(radius, axis, height);
            }
            case "torus" -> {
                Point center = parsePoint(data.get("center"));
                Vector axis = parseVector(data.get("axis"));
                double majorRadius = Double.parseDouble(data.get("majorRadius"));
                double minorRadius = Double.parseDouble(data.get("minorRadius"));
                geometry = new Torus(center, axis, majorRadius, minorRadius);
            }
            case "ellipse", "disk" -> {
                Point center = parsePoint(data.get("center"));
                Vector normal = parseVector(data.get("normal"));
                // Supports a circular disk ("radius") or an elliptical patch
                // ("axisDirection", "radiusX", "radiusY")
                if (data.containsKey("radius")) {
                    geometry = new Ellipse(center, normal, Double.parseDouble(data.get("radius")));
                } else {
                    Vector axisDirection = parseVector(data.get("axisDirection"));
                    double radiusX = Double.parseDouble(data.get("radiusX"));
                    double radiusY = Double.parseDouble(data.get("radiusY"));
                    geometry = new Ellipse(center, normal, axisDirection, radiusX, radiusY);
                }
            }
            case "ellipsoid" -> {
                Point center = parsePoint(data.get("center"));
                Vector radii = parseVector(data.get("radii"));
                geometry = new Ellipsoid(center, radii.getX(), radii.getY(), radii.getZ());
            }
            default -> throw new IllegalArgumentException("Unknown geometry type: " + type);
        }
        // Add emission light
        String emission = data.get("emission");
        if (emission != null)
            geometry.setEmission(parseColor(emission));

        Material material = buildMaterial(data);
        if (material != null)
            geometry.setMaterial(material);

        return geometry;
    }

    /**
     * Builds a {@link Geometries} composite from an imported {@code .obj} triangle mesh.
     * <p>
     * Recognized keys: {@code file} (required, path to the {@code .obj} source), optional
     * {@code scale} (uniform factor, relative to the origin) and {@code translate}
     * (applied after scaling), plus the same {@code emission}/{@code material.*} keys as
     * other geometry types - applied uniformly to every triangle in the mesh.
     * </p>
     *
     * @param data the mesh attribute map
     * @return a {@link Geometries} composite containing the mesh's triangles
     */
    private Geometries buildMesh(Map<String, String> data) {
        double scale = data.containsKey("scale") ? Double.parseDouble(data.get("scale")) : 1;
        Vector translate = data.containsKey("translate") ? parseVector(data.get("translate")) : null;

        List<Triangle> triangles = ObjMeshLoader.loadTriangles(data.get("file"), scale, translate);

        Color emission = data.containsKey("emission") ? parseColor(data.get("emission")) : null;
        Material material = buildMaterial(data);

        List<Intersectable> texturedTriangles = new ArrayList<>(triangles.size());
        for (Triangle triangle : triangles) {
            if (emission != null) triangle.setEmission(emission);
            if (material != null) triangle.setMaterial(material);
            texturedTriangles.add(triangle);
        }
        // BVH-organized: meshes commonly carry hundreds of triangles, where a flat scan
        // (even with a per-triangle bounding-box check) is far more ray-intersection work
        // than a spatial tree.
        return Geometries.buildBVH(texturedTriangles);
    }

    /**
     * Builds a {@link Material} from namespaced material attributes in the geometry map.
     * <p>
     * Recognized keys: {@code material.kA}, {@code material.kD}, {@code material.kS},
     * {@code material.kT}, {@code material.kR}, {@code material.shininess},
     * {@code material.texture.*} and {@code material.normalTexture.*} (see
     * {@link #buildTexture}), and {@code material.bumpStrength}.
     * </p>
     *
     * @param data geometry attribute map
     * @return a material if at least one material property is defined, otherwise {@code null}
     */
    private Material buildMaterial(Map<String, String> data) {
        Material material = new Material();
        boolean hasMaterial = false;

        hasMaterial |= apply(data, "material.kA", v -> material.setKA(parseMaterialCoefficient(v)));
        hasMaterial |= apply(data, "material.kD", v -> material.setKD(parseMaterialCoefficient(v)));
        hasMaterial |= apply(data, "material.kS", v -> material.setKS(parseMaterialCoefficient(v)));
        hasMaterial |= apply(data, "material.kT", v -> material.setKT(parseMaterialCoefficient(v)));
        hasMaterial |= apply(data, "material.kR", v -> material.setKR(parseMaterialCoefficient(v)));
        hasMaterial |= apply(data, "material.shininess", v -> material.setShininess(Integer.parseInt(v)));
        hasMaterial |= apply(data, "material.blurR", v -> material.setBlurR(Double.parseDouble(v)));
        hasMaterial |= apply(data, "material.blurT", v -> material.setBlurT(Double.parseDouble(v)));
        hasMaterial |= apply(data, "material.bumpStrength", v -> material.setBumpStrength(Double.parseDouble(v)));

        Texture texture = buildTexture(data, "material.texture.");
        if (texture != null) {
            material.setTexture(texture);
            hasMaterial = true;
        }

        Texture normalTexture = buildTexture(data, "material.normalTexture.");
        if (normalTexture != null) {
            material.setNormalTexture(normalTexture);
            hasMaterial = true;
        }

        return hasMaterial ? material : null;
    }

    /**
     * Builds a {@link Texture} from attributes namespaced under the given prefix, if present.
     * Used both for a geometry's {@code material.texture.*} and for the scene-level
     * {@code environment-map} (with an empty prefix, since its attributes aren't nested
     * under anything else).
     * <p>
     * Recognized {@code <prefix>type} values, with their own {@code <prefix>*} attributes:
     * </p>
     * <ul>
     * <li>{@code checker} - {@code colorA}, {@code colorB}, {@code cellSize}</li>
     * <li>{@code stripe} - {@code colorA}, {@code colorB}, {@code stripeWidth}</li>
     * <li>{@code ring} - {@code colorA}, {@code colorB}, {@code ringWidth}</li>
     * <li>{@code image} - {@code file}, {@code repeatU}, {@code repeatV}</li>
     * </ul>
     *
     * @param data   the attribute map to read from
     * @param prefix the key prefix (e.g. {@code "material.texture."}, or {@code ""})
     * @return the constructed texture, or {@code null} if no {@code <prefix>type} is present
     * @throws IllegalArgumentException if the texture type is unsupported
     */
    private Texture buildTexture(Map<String, String> data, String prefix) {
        String type = data.get(prefix + "type");
        if (type == null) return null;

        return switch (type) {
            case "checker" -> new CheckerTexture(
                    parseColor(data.get(prefix + "colorA")),
                    parseColor(data.get(prefix + "colorB")),
                    Double.parseDouble(data.get(prefix + "cellSize")));
            case "stripe" -> new StripeTexture(
                    parseColor(data.get(prefix + "colorA")),
                    parseColor(data.get(prefix + "colorB")),
                    Double.parseDouble(data.get(prefix + "stripeWidth")));
            case "ring" -> new RingTexture(
                    parseColor(data.get(prefix + "colorA")),
                    parseColor(data.get(prefix + "colorB")),
                    Double.parseDouble(data.get(prefix + "ringWidth")));
            case "image" -> new ImageTexture(
                    data.get(prefix + "file"),
                    Double.parseDouble(data.get(prefix + "repeatU")),
                    Double.parseDouble(data.get(prefix + "repeatV")));
            default -> throw new IllegalArgumentException("Unknown texture type: " + type);
        };
    }

    /**
     * Applies a setter to a map value when the requested key is present.
     *
     * @param data   the attribute map to read from
     * @param key    the key whose value should be applied
     * @param setter the action that consumes the value
     * @return {@code true} if the key was present and the setter was invoked,
     *         otherwise {@code false}
     */
    private static boolean apply(Map<String, String> data, String key, Consumer<String> setter) {
        String value = data.get(key);
        if (value == null) return false;
        setter.accept(value);
        return true;
    }

    /**
     * Parses a material coefficient that may be either a scalar or an "x y z" triad.
     *
     * @param value the coefficient string
     * @return the parsed {@link Double3}
     */
    private Double3 parseMaterialCoefficient(String value) {
        String trimmed = value.trim();
        return trimmed.contains(" ") ? parseDouble3(trimmed) : new Double3(Double.parseDouble(trimmed));
    }

    /**
     * Builds {@link CameraSettings} from a raw camera attribute map.
     * <p>
     * Recognized keys: {@code location}, {@code direction} (the point the camera is
     * aimed at), optional {@code up} (defaults to the Y axis), {@code vpDistance},
     * {@code vpWidth}, {@code vpHeight}, {@code resolutionX}, and {@code resolutionY}.
     * </p>
     *
     * @param data the camera attribute map
     * @return the constructed {@link CameraSettings}
     */
    private CameraSettings buildCameraSettings(Map<String, String> data) {
        CameraSettings settings = new CameraSettings();
        settings.location = parsePoint(data.get("location"));
        settings.direction = parsePoint(data.get("direction"));
        if (data.containsKey("up"))
            settings.up = parseVector(data.get("up"));
        settings.vpDistance = Double.parseDouble(data.get("vpDistance"));
        settings.vpWidth = Double.parseDouble(data.get("vpWidth"));
        settings.vpHeight = Double.parseDouble(data.get("vpHeight"));
        settings.resolutionX = Integer.parseInt(data.get("resolutionX"));
        settings.resolutionY = Integer.parseInt(data.get("resolutionY"));
        return settings;
    }

    /**
     * Factory method for creating light sources from raw attribute maps.
     * <p>
     * Supported light types: {@code directional-light}, {@code point-light}, and {@code spot-light}.
     * </p>
     *
     * @param data a map containing the attributes for the light
     * @return the constructed {@link LightSource}
     * @throws IllegalArgumentException if the light type is unsupported
     */
    private LightSource buildLight(Map<String, String> data) {
        String type = data.get("type");
        Color color = parseColor(data.get("color"));

        return switch (type) {
            case "directional-light" -> new DirectionalLight(color, parseVector(data.get("direction")));
            case "point-light" -> applyAttenuation(new PointLight(color, parsePoint(data.get("position"))), data);
            case "spot-light" -> buildSpotLight(color, data);
            default -> throw new IllegalArgumentException("Unknown light type: " + type);
        };
    }

    /**
     * Builds a {@link SpotLight} and applies optional attenuation and beam parameters.
     *
     * @param color the light intensity (color)
     * @param data  attribute map containing position, direction, {@code kC}, {@code kL},
     *              {@code kQ}, and optional {@code narrowBeam}
     * @return the configured spotlight
     */
    private SpotLight buildSpotLight(Color color, Map<String, String> data) {
        SpotLight spotLight = applyAttenuation(
                new SpotLight(color, parsePoint(data.get("position")), parseVector(data.get("direction"))),
                data);

        String narrowBeam = data.get("narrowBeam");
        if (narrowBeam != null)
            spotLight.setNarrowBeam(Integer.parseInt(narrowBeam));

        return spotLight;
    }

    /**
     * Applies optional distance-attenuation coefficients and soft-shadow radius to a
     * point-based light source.
     * <p>
     * Recognized keys: {@code kC}, {@code kL}, {@code kQ}, and {@code radius} (the
     * area-light radius used for soft shadows - see {@link PointLight#setRadius}).
     * Attributes that are absent are left unchanged.
     * </p>
     *
     * @param <T>   a {@link PointLight} or subclass such as {@link SpotLight}
     * @param light the light to configure
     * @param data  attribute map containing attenuation coefficients
     * @return the same light instance for chaining
     */
    private <T extends PointLight> T applyAttenuation(T light, Map<String, String> data) {
        String kC = data.get("kC");
        if (kC != null)
            light.setKc(Double.parseDouble(kC));

        String kL = data.get("kL");
        if (kL != null)
            light.setKl(Double.parseDouble(kL));

        String kQ = data.get("kQ");
        if (kQ != null)
            light.setKq(Double.parseDouble(kQ));

        String radius = data.get("radius");
        if (radius != null)
            light.setRadius(Double.parseDouble(radius));

        return light;
    }

    // --- Abstract hooks: implemented by format-specific subclasses ---

    /**
     * Returns the scene background color from the source file as a string.
     *
     * @return the background color in {@code "r g b"} format, or {@code null} if undefined
     */
    protected abstract String getBackgroundColor();

    /**
     * Returns the ambient light color from the source file as a string.
     *
     * @return the ambient color in {@code "r g b"} format, or {@code null} if undefined
     */
    protected abstract String getAmbientLight();

    /**
     * Returns a list of maps representing the string-based attributes for each light source.
     * <p>
     * Each map must include a {@code type} key (for example, {@code point-light}).
     * Ambient light is excluded and handled by {@link #getAmbientLight()}.
     * </p>
     *
     * @return light attribute maps; empty if the scene defines no external lights
     */
    protected abstract List<Map<String, String>> getLights();

    /**
     * Returns a list of maps representing the string-based attributes for each geometry.
     * <p>
     * Each map must include a {@code type} key (for example, {@code sphere}).
     * Material properties use the {@code material.*} namespace.
     * </p>
     *
     * @return geometry attribute maps; empty if the scene defines no geometries
     */
    protected abstract List<Map<String, String>> getGeometries();

    /**
     * Returns a map representing the string-based attributes of the scene's camera.
     *
     * @return the camera attribute map, or {@code null} if the source file defines no camera
     */
    protected abstract Map<String, String> getCamera();

    /**
     * Returns a map representing the string-based attributes of the scene's environment
     * map (skybox), in the same unprefixed form {@link #buildTexture} expects (e.g.
     * {@code type}, {@code file}, {@code repeatU}, {@code repeatV} for an image texture).
     *
     * @return the environment-map attribute map, or {@code null} if the source file defines none
     */
    protected abstract Map<String, String> getEnvironmentMap();

    // --- Shared internal helpers ---

    /**
     * Splits a string by whitespace and converts it to a {@link Double3}.
     *
     * @param str the components in {@code "x y z"} string format
     * @return the constructed {@link Double3}
     */
    private Double3 parseDouble3(String str) {
        String[] p = str.trim().split("\\s+");
        return new Double3(Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2]));
    }

    /**
     * Converts a coordinate string {@code "x y z"} into a {@link Point}.
     *
     * @param str the point coordinates in string format
     * @return the constructed {@link Point}
     */
    protected Point parsePoint(String str) {
        return new Point(parseDouble3(str));
    }

    /**
     * Converts a direction string {@code "x y z"} into a {@link Vector}.
     *
     * @param str the vector components in string format
     * @return the constructed {@link Vector}
     */
    protected Vector parseVector(String str) {
        return new Vector(parseDouble3(str));
    }

    /**
     * Converts a color string {@code "r g b"} into a {@link Color}.
     *
     * @param str the color components in string format
     * @return the constructed {@link Color}
     */
    protected Color parseColor(String str) {
        Double3 d = parseDouble3(str);
        return new Color(d._d1(), d._d2(), d._d3());
    }
}
