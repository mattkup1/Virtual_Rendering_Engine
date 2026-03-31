package geometries.impl;

import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Represents a sphere in a 3D Cartesian coordinate system.
 * <p>
 * A sphere is defined by a center point and a radius. It consists of all
 * points in 3D space that are at a distance equal to the radius from the center.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Sphere extends RadialGeometry {
    /**
     * The center point of the sphere
     */
    private final Point _center;

    /**
     * Constructs a sphere with a given center point and radius.
     *
     * @param center the center point of the sphere
     * @param radius the radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this._center = center;
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        Point p0 = ray.getOrigin();
        Vector v = ray.getDirection();

        // Vector from ray origin to sphere center
        if (_center.equals(p0)) {
            return List.of(ray.getPoint(_radius));
        }

        Vector u = _center.subtract(p0);
        double t_p = v.dotProduct(u);

        // Squared distance from center to the projection point on the ray
        // Using Pythagoras: d^2 = |u|^2 - tp^2
        double dSquared = u.lengthSquared() - t_p * t_p;

        // Check if the perpendicular distance is greater than or equal to radius
        if (dSquared > _radiusSquared || isZero(dSquared - _radiusSquared))
            return null;

        // Distance from the perpendicular point to the intersection points
        double th = Math.sqrt(_radiusSquared - dSquared);

        double t1 = Util.alignZero(t_p - th);
        double t2 = Util.alignZero(t_p + th);

        // Only return points where t > 0 (in front of the ray)
        if (t1 <= 0 && t2 <= 0) return null;

        if (t1 > 0 && t2 > 0) {
            return List.of(ray.getPoint(t1), ray.getPoint(t2));
        }

        // Case only one intersection
        return t1 > 0 ? List.of(ray.getPoint(t1)) : List.of(ray.getPoint(t2));
    }

    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center);
    }

    @Override
    public String toString() {
        return "Sphere: Center: " + _center + ", Radius: " + _radius;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sphere s = (Sphere) obj;
        return _center.equals(s._center) && isZero(_radius - s._radius);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _center);
    }
}