package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a directional light source with parallel rays, similar to distant sunlight.
 * <p>
 * The light has constant intensity at every point in the scene and a fixed direction.
 * Distance attenuation does not apply.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class DirectionalLight extends Light implements LightSource {

    /**
     * The normalized direction of the light rays.
     */
    private final Vector _direction;

    /**
     * Constructs a directional light with the given intensity and direction.
     *
     * @param intensity the color/intensity of the light
     * @param direction the direction of the light rays (normalized on construction)
     */
    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        _direction = direction.normalize();
    }

    /**
     * Returns the direction vector from the light source toward a given point.
     * <p>
     * For a directional light this direction is constant for all points.
     * </p>
     *
     * @param P the point in space
     * @return the normalized direction vector of the light
     */
    public Vector getL(Point P) {
        return _direction;
    }

    /**
     * Returns the intensity of the light at a given point.
     * <p>
     * For a directional light the intensity is constant and does not depend on distance.
     * </p>
     *
     * @param p the point in space
     * @return the light intensity at the point
     */
    public Color getIntensity(Point p) {
        return _intensity;
    }
    
    public double getDistance(Point p) {
        return Double.POSITIVE_INFINITY;
    }
}
