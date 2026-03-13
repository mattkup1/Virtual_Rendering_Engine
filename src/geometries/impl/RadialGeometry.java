package geometries.impl;

import geometries.api.Geometry;

/**
 * Abstract class representing all geometric shapes with a radial property.
 * <p>
 * This class serves as a base for shapes such as spheres, tubes, and cylinders.
 * It provides centralized storage and management of the radius and its square
 * for performance optimization in geometric calculations.
 * </p>
 * @author mattkuperwasser
 * @author moshehanau
 */
abstract public class RadialGeometry extends Geometry {
    /** The radius of the geometric shape */
    protected final double _radius;

    /** The squared radius, pre-calculated to optimize distance and intersection math */
    protected final double _radiusSquared;

    /**
     * Constructs a radial geometry with a given radius.
     *
     * @param radius the radius of the geometry
     * @throws IllegalArgumentException if radius is negative or zero
     */
    public RadialGeometry(double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be greater than zero");
        }
        this._radius = radius;
        this._radiusSquared = radius * radius;
    }

    /**
     * Returns the radius of the geometry.
     * @return the radius
     */
    public double getRadius() {
        return _radius;
    }
}