package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Interface for light sources in the scene.
 * Provides methods to calculate the direction and intensity of light
 * at a specific point in 3D space.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public interface LightSource {

    /**
     * Get the direction vector from the light source to a given point.
     *
     * @param p the point in space
     * @return the direction vector from the light source to the point
     */
    public Vector getL(Point p);

    /**
     * Get the intensity of the light at a given point.
     * This takes into account the light's original intensity and
     * any attenuation factors based on distance.
     *
     * @param p the point in space
     * @return the {@link Color} representing the light intensity at the point
     */
    public Color getIntensity(Point p);
}
