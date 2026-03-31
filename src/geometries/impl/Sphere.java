package geometries.impl;

import java.util.ArrayList;
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
        final Point ray_origin = ray.getOrigin();

        if (_center.equals(ray_origin))
            return List.of(ray.getPoint(_radius));

        // Vector from ray's origin to sphere's center
        final Vector u = _center.subtract(ray_origin);
        final double t_p = u.dotProduct(ray.getDirection());
        final Point p = ray.getPoint(t_p);

        // Squared distance from p to sphere's center point
        final double dist_sq_p_center = _center.distanceSquared(p);

        if (Util.isZero(dist_sq_p_center - _radiusSquared) || dist_sq_p_center > _radiusSquared)
            return null;

        // Distance from p to the intersection points
        final double dist_p_inter = Math.sqrt(_radiusSquared - dist_sq_p_center);

        // Return the list of intersection points
        // Order the points in the list by ascending distance from ray origin
        List<Point> result = new ArrayList<>();
        if ((t_p - dist_p_inter) > 0) result.add(ray.getPoint(t_p - dist_p_inter));
        if ((t_p + dist_p_inter) > 0) result.add(ray.getPoint(t_p + dist_p_inter));
        if (!result.isEmpty()) return result;
        return null;
        //return List.of(ray.getPoint(t_p - dist_p_inter), ray.getPoint(t_p + dist_p_inter));
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