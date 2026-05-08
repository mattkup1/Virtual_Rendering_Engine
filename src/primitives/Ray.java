package primitives;

import geometries.api.Intersectable.Intersection;
import java.util.List;
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
    /**
     * The starting point of the ray
     */
    private final Point _origin;
    /**
     * The normalized direction vector of the ray
     */
    private final Vector _direction;

    /**
     * Constructs a ray with a given origin point and direction vector.
     * <p>
     * The direction vector is automatically normalized during construction.
     * </p>
     *
     * @param origin    the starting point of the ray
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
     * Getter method for the {@link Ray} origin
     *
     * @return the point on the ray (ray origin)
     */
    public Point getOrigin() {
        return _origin;
    }

    /**
     * Returns the direction vector of the ray.
     *
     * @return the normalized direction vector
     */
    public Vector getDirection() {
        return _direction.normalize();
    }

    /**
     * Gets a point on the ray
     *
     * @param t the distance of the point from the ray's origin
     * @return the point at distance t from the ray's origin
     */
    public Point getPoint(double t) {
        try {
            return _origin.add(_direction.scale(t));
        } catch (IllegalArgumentException e) { // Zero vector produced in try
            return _origin;
        }
    }

    public Intersection findClosestIntersection(List<Intersection> intersections) {
        if (intersections == null) return null;

        double minDistSq = Double.POSITIVE_INFINITY;
        Intersection closestIntersection = null;

        for (Intersection intersection : intersections) {
            double distSq = intersection.point.distanceSquared(this._origin);
            if (distSq < minDistSq) {
                closestIntersection = intersection;
                minDistSq = distSq;
            }
        }
        return closestIntersection;
    }

    /**
     * Returns the closest point to the ray origin from the given list of points
     *
     * @param points the list of points
     * @return the closest point to the ray origin
     */
    public Point findClosestPoint(List<Point> points) {
        return points == null ? null
                : findClosestIntersection(
                points.stream()
                .map(point -> new Intersection(null, point))
                .toList()
        ).point;
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