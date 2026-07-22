package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a point light source that emits light uniformly in all directions from a single point.
 * <p>
 * Light intensity decreases with distance according to constant, linear, and quadratic
 * attenuation coefficients ({@code kC}, {@code kL}, {@code kQ}).
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class PointLight extends Light implements LightSource {

    /**
     * The position of the light source in 3D space.
     */
    protected final Point _position;

    /**
     * Constant attenuation coefficient.
     */
    private double _kC = 1;

    /**
     * Linear attenuation coefficient.
     */
    private double _kL = 0;

    /**
     * Quadratic attenuation coefficient.
     */
    private double _kQ = 0;

    /**
     * The light's physical radius, used for soft (penumbra) shadows; {@code 0} (the
     * default) is a hard-shadow point light.
     */
    private double _radius = 0;

    /**
     * Constructs a point light with the given intensity, position, and attenuation coefficients.
     *
     * @param intensity the color/intensity of the light
     * @param position  the position of the light source
     * @param kC        the constant attenuation coefficient
     * @param kL        the linear attenuation coefficient
     * @param kQ        the quadratic attenuation coefficient
     */
    public PointLight(Color intensity, Point position, double kC, double kL, double kQ) {
        super(intensity);
        _position = position;
        _kC = kC;
        _kL = kL;
        _kQ = kQ;
    }

    /**
     * Constructs a point light with the given intensity and position.
     * <p>
     * Default attenuation coefficients are {@code kC = 1}, {@code kL = 0}, {@code kQ = 0}.
     * </p>
     *
     * @param intensity the color/intensity of the light
     * @param position  the position of the light source
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        _position = position;
    }

    /**
     * Returns the normalized direction vector from the light source toward a given point.
     *
     * @param p the point in space
     * @return the direction vector from the light source to the point
     */
    public Vector getL(Point p) {
        return p.subtract(_position).normalize();
    }

    /**
     * Returns the intensity of the light at a given point.
     * <p>
     * Intensity is scaled by distance attenuation:
     * {@code 1 / (kC + kL * d + kQ * d²)}, where {@code d} is the distance from the
     * light source to the point. If the point coincides with the light position,
     * the base intensity is returned unchanged.
     * </p>
     *
     * @param p the point in space
     * @return the attenuated light intensity at the point
     */
    public Color getIntensity(Point p) {
        if (_position.equals(p)) {
            return _intensity;
        }
        final double d = _position.distance(p);
        return _intensity.scale(1d / (_kC + (_kL * d) + (_kQ * d * d)));
    }

    /**
     * Returns the Euclidean distance from the light source to a given point.
     *
     * @param p the target point in space
     * @return the distance from the light's position to {@code p}
     */
    public double getDistance(Point p) {
        return _position.distance(p);
    }

    @Override
    public double getRadius() {
        return _radius;
    }

    /**
     * Sets the light's physical radius, enabling soft (penumbra) shadows: shadow rays
     * are sampled across a disk of this radius (at the light's actual distance) instead
     * of a single ray toward its exact position. {@code 0} (the default) is a hard-shadow
     * point light.
     *
     * @param radius the light's radius
     * @return this point light for builder-style chaining
     * @throws IllegalArgumentException if {@code radius} is negative
     */
    public PointLight setRadius(double radius) {
        if (radius < 0) throw new IllegalArgumentException("Light radius must not be negative");
        _radius = radius;
        return this;
    }

    /**
     * Sets the constant attenuation coefficient.
     *
     * @param kC the constant attenuation coefficient
     * @return this point light for builder-style chaining
     */
    public PointLight setKc(double kC) {
        _kC = kC;
        return this;
    }

    /**
     * Sets the linear attenuation coefficient.
     *
     * @param kL the linear attenuation coefficient
     * @return this point light for builder-style chaining
     */
    public PointLight setKl(double kL) {
        _kL = kL;
        return this;
    }

    /**
     * Sets the quadratic attenuation coefficient.
     *
     * @param kQ the quadratic attenuation coefficient
     * @return this point light for builder-style chaining
     */
    public PointLight setKq(double kQ) {
        _kQ = kQ;
        return this;
    }
}
