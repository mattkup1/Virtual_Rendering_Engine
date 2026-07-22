package geometries.impl;

import java.util.List;
import java.util.Objects;
import primitives.BoundingBox;
import primitives.Point;
import primitives.Ray;
import primitives.UV;
import primitives.Vector;

import static primitives.Util.alignZero;
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
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Point p0 = ray.getOrigin();
        Vector v = ray.getDirection();

        // Vector from ray origin to sphere center
        if (this._center.equals(p0)) {
            return List.of(new Intersection(this, ray.getPoint(this._radius)));
        }

        Vector u = this._center.subtract(p0);
        double t_p = v.dotProduct(u);

        // Squared distance from center to the projection point on the ray
        // Using Pythagoras: d^2 = |u|^2 - tp^2
        double dSquared = u.lengthSquared() - t_p * t_p;

        // Check if the perpendicular distance is greater than or equal to radius
        if (dSquared > this._radiusSquared || isZero(dSquared - this._radiusSquared))
            return null;

        // Distance from the perpendicular point to the intersection points
        double th = Math.sqrt(this._radiusSquared - dSquared);

        double t1 = alignZero(t_p - th);
        double t2 = alignZero(t_p + th);

        // Only return points where t > 0 (in front of the ray)
        if (t1 <= 0 && t2 <= 0) return null;

        final Point potential1 = ray.getPoint(t1);
        final Point potential2 = ray.getPoint(t2);

        final boolean validP1Dist = alignZero(potential1.distance(p0) - maxDistance) <= 0;
        final boolean validP2Dist = alignZero(potential2.distance(p0) - maxDistance) <= 0;

        if (!validP1Dist && !validP2Dist) return null;

        if (t1 > 0 && t2 > 0) {
            if (validP1Dist && validP2Dist) {
                return List.of(
                        new Intersection(this, potential1),
                        new Intersection(this, potential2)
                );
            }

            return validP1Dist ?
                    List.of(new Intersection(this, potential1)) :
                    List.of(new Intersection(this, potential2));
        }

        // Case only one intersection
        if (t1 > 0 && validP1Dist) {
            return List.of(new Intersection(this, potential1));
        }
        if (t2 > 0 && validP2Dist) {
            return List.of(new Intersection(this, potential2));
        }

        return null;
    }

    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center);
    }

    /**
     * Returns normalized equirectangular texture coordinates for the given surface point:
     * {@code u} wraps once around the sphere's equator (longitude), {@code v} runs from
     * the north pole ({@code 0}) to the south pole ({@code 1}) along the Y axis (latitude).
     *
     * @param point a point on the sphere's surface
     * @return the texture coordinates, each in {@code [0,1)}/{@code [0,1]}
     */
    @Override
    public UV getUV(Point point) {
        Vector d = point.equals(_center) ? Vector.AXIS_Y : point.subtract(_center).normalize();
        double u = 0.5 + Math.atan2(d.getZ(), d.getX()) / (2 * Math.PI);
        double clampedY = Math.max(-1, Math.min(1, d.getY()));
        double v = 0.5 - Math.asin(clampedY) / Math.PI;
        return new UV(u, v);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(
                _center.getX() - _radius, _center.getY() - _radius, _center.getZ() - _radius,
                _center.getX() + _radius, _center.getY() + _radius, _center.getZ() + _radius);
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