package geometries.impl;

import geometries.api.Geometry;

/**
 * Abstract class representing radial geometric shapes and their unique operations
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
abstract public class RadialGeometry extends Geometry {
    protected final double _radius;
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

    @Override
    public String toString() {
        return "Radius: " + _radius;
    }
}
