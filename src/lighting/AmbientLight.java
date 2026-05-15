package lighting;

import primitives.Color;

/**
 * Ambient Light represents a fixed-intensity, fixed-color light source
 * that affects all objects in the scene equally.
 * <p>
 * In a ray tracer, ambient light is used to simulate the indirect reflection
 * of light from all surfaces in the environment, ensuring that even parts
 * of objects not directly hit by a light source are slightly visible.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class AmbientLight extends Light {

    /**
     * Static constant representing no ambient light (Black).
     */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

    /**
     * Constructs an AmbientLight instance with a specific intensity.
     *
     * @param intensity the color/intensity of the ambient light
     */
    public AmbientLight(Color intensity) {
        super(intensity);
    }
}