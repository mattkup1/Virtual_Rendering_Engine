package lighting;

import primitives.Color;

/**
 * Abstract base class for all light sources in the scene.
 * <p>
 * Stores the base color/intensity shared by every concrete light implementation.
 * Subclasses may apply distance attenuation, direction, or other lighting models.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
abstract class Light {

    /**
     * The base intensity (color) of the light source.
     */
    protected final Color _intensity;

    /**
     * Constructs a light with the given intensity.
     *
     * @param intensity the light intensity (color)
     */
    protected Light(Color intensity) {
        _intensity = intensity;
    }

    /**
     * Returns the base intensity of the light source.
     *
     * @return the light intensity (color)
     */
    public Color getIntensity() {
        return this._intensity;
    }
}
