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
     *
     * @param radius the shape's radius
     */
    public RadialGeometry(double radius) {
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }
}
