package scene;

import geometries.api.Geometry;
import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.LightSource;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
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

        // Process Light Sources
        for (var lightData : getLights()) {
            scene.lights.add(buildLight(lightData));
        }

        // Process Geometries
        // Subclasses provide raw string data in maps, this class builds the objects.
        List<Map<String, String>> geometryData = getGeometries();
        for (var data : geometryData) {
            scene.geometries.add(buildGeometries(data));
        }

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
     * {@code tube}, {@code cylinder}, and {@code polygon}.
     * </p>
     *
     * @param data a map containing the attributes for the geometry
     * @return the constructed {@link Geometry} object
     * @throws IllegalArgumentException if the geometry type is unsupported
     */
    private Geometry buildGeometries(Map<String, String> data) {
        String type = data.get("type");
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
     * Builds a {@link Material} from namespaced material attributes in the geometry map.
     * <p>
     * Recognized keys: {@code material.kA}, {@code material.kD}, {@code material.kS},
     * {@code material.kT}, {@code material.kR}, and {@code material.shininess}.
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

        return hasMaterial ? material : null;
    }

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
     * Applies optional distance-attenuation coefficients to a point-based light source.
     * <p>
     * Recognized keys: {@code kC}, {@code kL}, and {@code kQ}. Attributes that are absent
     * are left unchanged.
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