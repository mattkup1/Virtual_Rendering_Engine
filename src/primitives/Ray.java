package primitives;

/**
 * Representation of a ray in 3D space
 *
 * <p>
 *     A ray is represented by an origin point and a normalized direction vector
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Ray {
    private Point origin;
    private Vector direction;

    public Ray(Point origin, Vector direction) {
        this.origin = origin;
        // Direction vector must be normalized
        this.direction = direction.normalize();
    }
}
