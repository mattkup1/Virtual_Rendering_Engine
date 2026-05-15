package scene;

import geometries.impl.Geometries;
import java.util.ArrayList;
import java.util.List;
import lighting.AmbientLight;
import lighting.LightSource;
import primitives.Color;

/**
 * Represents a complete 3D scene containing geometries, lighting, and background properties.
 * <p>
 * The Scene class uses a "Fluent Interface" (setter-chaining) to allow easy
 * configuration of the environment before rendering.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Scene {
    /**
     * The name of the scene.
     */
    public final String name;
    /**
     * The background color of the scene, defaults to Black.
     */
    public Color background = Color.BLACK;
    /**
     * The ambient lighting of the scene, defaults to None.
     */
    public AmbientLight ambientLight = AmbientLight.NONE;
    /**
     * The collection of geometric shapes in the scene.
     */
    public Geometries geometries = new Geometries();

    public List<LightSource> lights = new ArrayList<>();

    /**
     * Constructs a scene with a given name and an empty collection of geometries.
     *
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Sets the background color of the scene.
     *
     * @param background the background color
     * @return the scene object itself for builder-like chaining
     */
    public Scene setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the ambient light of the scene.
     *
     * @param ambientLight the ambient light source
     * @return the scene object itself for builder-like chaining
     */
    public Scene setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * Sets the collection of geometries in the scene.
     *
     * @param geometries the geometries collection
     * @return the scene object itself for builder-like chaining
     */
    public Scene setGeometries(Geometries geometries) {
        this.geometries = geometries;
        return this;
    }
}