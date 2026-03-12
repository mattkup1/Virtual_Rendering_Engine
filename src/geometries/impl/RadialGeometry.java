package geometries.impl;

/**
 * Abstract class representing radial geometric shapes and their unique operations
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
abstract public class RadialGeometry {
    protected final double radius;
    protected final double radiusSquared;

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
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }
}
