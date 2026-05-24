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
     * Constant displacement factor used to offset shadow/secondary ray origins.
     * <p>
     * Due to floating-point precision limitations, a secondary ray originating exactly
     * at a surface intersection point may erroneously intersect the same geometry surface
     * again. This causes visual artifacts known as "shadow acne." Moving the ray's
     * starting point slightly along the surface normal (or away from it) by this small
     * factor prevents self-shading.
     * </p>
     */
    private static final double DELTA = 1e-3;

    public Ray(Point origin, Vector direction, Vector normal) {
        Vector delta = normal.scale(direction.dotProduct(normal) > 0 ? DELTA : -DELTA);
        this(origin.add(delta), direction);
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


    /**
     * Finds the closest intersection point to the ray's origin from a list of intersections.
     * <p>
     * This method iterates through the provided list and identifies the intersection
     * with the minimum squared distance to the ray's origin
     * Using {@link Point#distanceSquared(Point)} is more efficient for comparisons
     * as it avoids the overhead of square root operations.
     * </p>
     *
     * @param intersections a list of potential intersections; may be {@code null}
     * @return the {@link Intersection} object closest to the ray's origin,
     * or {@code null} if the input list is null or empty
     */
    public Intersection findClosestIntersection(List<Intersection> intersections) {
        if (intersections == null) return null;

        double minDistSq = Double.POSITIVE_INFINITY;
        Intersection closestIntersection = null;

        for (Intersection intersection : intersections) {
            // Calculate squared distance to origin to optimize performance
            double distSq = intersection.point.distanceSquared(this._origin);
            if (distSq < minDistSq) {
                closestIntersection = intersection;
                minDistSq = distSq;
            }
        }
        return closestIntersection;
    }

    /**
     * Finds the closest point to the ray's origin from a given list of points.
     * <p>
     * This is a convenience method that wraps points into {@link Intersection}
     * objects to utilize the optimized {@link #findClosestIntersection(List)} logic
     * </p>
     *
     * @param points the list of points to check; may be {@code null}
     * @return the {@link Point} closest to the ray's origin,
     * or {@code null} if the input list is null or empty
     */
    public Point findClosestPoint(List<Point> points) {
        return points == null || points.isEmpty() ? null
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