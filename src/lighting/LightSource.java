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

    /**
     * Calculates the distance from the light source to a given point in 3D space.
     * <p>
     * This distance is typically used during shadow mapping computations to determine
     * if an intersecting geometry sits between the light source and the point being shaded.
     * </p>
     *
     * @param p the target point in space
     * @return the Euclidean distance from the light source to the point,
     * or {@code Double.POSITIVE_INFINITY} for directional light sources (like the sun)
     */
    public double getDistance(Point p);
}
