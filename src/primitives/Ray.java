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
    private final Point _origin;
    private final Vector _direction;

    public Ray(Point origin, Vector direction) {
        this._origin = origin;
        // Direction vector must be normalized
        this._direction = direction.normalize();
    }
}
