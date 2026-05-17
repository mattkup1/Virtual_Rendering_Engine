package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a spotlight: a point light constrained to a cone of illumination.
 * <p>
 * Intensity is the product of {@link PointLight} distance attenuation and a beam
 * factor based on the angle between the spotlight direction and the direction
 * toward the illuminated point. A {@code narrowBeam} exponent controls how focused
 * the cone is.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class SpotLight extends PointLight {

    /**
     * The normalized direction in which the spotlight is aimed.
     */
    private final Vector _direction;

    /**
     * Beam concentration exponent.
     * A value of {@code 1} yields a standard spotlight; larger values produce a narrower beam.
     */
    private int _narrowBeam = 1;

    /**
     * Constructs a spotlight with the given intensity, position, attenuation coefficients,
     * and beam direction.
     *
     * @param intensity the color/intensity of the light
     * @param position  the position of the light source
     * @param kC        the constant attenuation coefficient
     * @param kL        the linear attenuation coefficient
     * @param kQ        the quadratic attenuation coefficient
     * @param direction the direction in which the spotlight is aimed (normalized on construction)
     */
    public SpotLight(Color intensity, Point position, double kC, double kL, double kQ, Vector direction) {
        super(intensity, position, kC, kL, kQ);
        _direction = direction.normalize();
    }

    /**
     * Constructs a spotlight with the given intensity, position, and beam direction.
     * <p>
     * Default attenuation coefficients are {@code kC = 1}, {@code kL = 0}, {@code kQ = 0}.
     * </p>
     *
     * @param intensity the color/intensity of the light
     * @param position  the position of the light source
     * @param direction the direction in which the spotlight is aimed (normalized on construction)
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        _direction = direction.normalize();
    }

    /**
     * Returns the normalized direction vector from the light source toward a given point.
     *
     * @param p the point in space
     * @return the direction vector from the light source to the point
     */
    public Vector getL(Point p) {
        return super.getL(p);
    }

    /**
     * Returns the intensity of the spotlight at a given point.
     * <p>
     * The result is the {@link PointLight} attenuated intensity scaled by
     * {@code max(0, direction · getL(p))^narrowBeam}. If the point coincides with the
     * light position, the base intensity is returned unchanged.
     * </p>
     *
     * @param p the point in space
     * @return the attenuated spotlight intensity at the point
     */
    public Color getIntensity(Point p) {
        if (p.equals(_position)) return _intensity;
        final Color point_intensity = super.getIntensity(p);
        double projection = Math.max(0d, _direction.dotProduct(getL(p)));

        return point_intensity.scale(Math.pow(projection, _narrowBeam));
    }

    /**
     * Sets the beam concentration exponent.
     * <p>
     * Larger values produce a narrower, more focused beam.
     * </p>
     *
     * @param narrowBeam the narrow beam exponent
     * @return this spotlight for builder-style chaining
     */
    public SpotLight setNarrowBeam(int narrowBeam) {
        _narrowBeam = narrowBeam;
        return this;
    }

    /**
     * Sets the constant attenuation coefficient.
     *
     * @param kC the constant attenuation coefficient
     * @return this spotlight for builder-style chaining
     */
    @Override
    public SpotLight setKc(double kC) {
        return (SpotLight) super.setKc(kC);
    }

    /**
     * Sets the linear attenuation coefficient.
     *
     * @param kL the linear attenuation coefficient
     * @return this spotlight for builder-style chaining
     */
    @Override
    public SpotLight setKl(double kL) {
        return (SpotLight) super.setKl(kL);
    }

    /**
     * Sets the quadratic attenuation coefficient.
     *
     * @param kQ the quadratic attenuation coefficient
     * @return this spotlight for builder-style chaining
     */
    @Override
    public SpotLight setKq(double kQ) {
        return (SpotLight) super.setKq(kQ);
    }
}
