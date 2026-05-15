package lighting;

import primitives.Color;

abstract class Light {

    /**
     * The intensity (color) of the ambient light.
     */
    protected final Color _intensity;

    /**
     * Constructs a Light object with the given intensity value
     *
     * @param intensity the light intensity (color)
     */
    protected Light(Color intensity) {
        _intensity = intensity;
    }

    /**
     * Returns the intensity of the ambient light.
     *
     * @return the color intensity
     */
    public Color getIntensity() {
        return this._intensity;
    }
}
