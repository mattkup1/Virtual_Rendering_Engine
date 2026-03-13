package primitives;

import java.util.Objects;

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
    public final Vector _direction;

    public Ray(Point origin, Vector direction) {
        this._origin = origin;
        // Direction vector must be normalized
        this._direction = direction.normalize();
    }

    /**
     * Returns the direction vector of the object.
     *
     * @return direction vector
     */
    public Vector direction() {
        return  _direction;
    }

    @Override
    public String toString() {
        return "the ray pass in point" + _origin + "with direction " + _direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return this._origin.equals(((Ray)obj)._origin) && this._direction.equals(((Ray)obj)._direction);
    }

    @Override
    public int hashCode() { return Objects.hash(_origin,_direction);}
}
