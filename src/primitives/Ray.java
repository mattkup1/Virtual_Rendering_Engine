package primitives;

import java.util.Objects;

/**
 * Representation of a ray in 3D space.
 * <p>
 * A ray is a semi-infinite line starting at an origin point and extending
 * infinitely in a specific direction. The direction vector is always normalized.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Ray {
    /** The starting point of the ray */
    private final Point _origin;
    /** The normalized direction vector of the ray */
    public final Vector _direction;

    /**
     * Constructs a ray with a given origin point and direction vector.
     * <p>
     * The direction vector is automatically normalized during construction.
     * </p>
     * * @param origin    the starting point of the ray
     * @param direction the direction vector (will be normalized)
     */
    public Ray(Point origin, Vector direction) {
        this._origin = origin;
        this._direction = direction.normalize();
    }

    /**
     * Returns the origin point of the ray.
     *
     * @return the origin point
     */
    public Point point() {
        return _origin;
    }

    /**
     * Returns the direction vector of the ray.
     *
     * @return the normalized direction vector
     */
    public Vector direction() {
        return _direction;
    }

    @Override
    public String toString() {
        return "Ray: Origin: " + _origin + ", Direction: " + _direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return _origin.equals(other._origin) && _direction.equals(other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }
}