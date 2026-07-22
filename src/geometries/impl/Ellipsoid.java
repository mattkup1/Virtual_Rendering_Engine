package geometries.impl;

import geometries.api.Geometry;
import java.util.List;
import java.util.Objects;
import primitives.BoundingBox;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents an axis-aligned ellipsoid (a non-uniformly stretched sphere) in a 3D
 * Cartesian coordinate system.
 * <p>
 * Defined by a center point and three per-axis radii. Intersection and normal math
 * work by transforming into "unit-sphere space" (dividing coordinates by the
 * respective radius) and solving the standard sphere quadratic there.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Ellipsoid extends Geometry {
    /**
     * The center point of the ellipsoid
     */
    private final Point _center;
    /**
     * The radius along the X axis
     */
    private final double _rx;
    /**
     * The radius along the Y axis
     */
    private final double _ry;
    /**
     * The radius along the Z axis
     */
    private final double _rz;

    /**
     * Constructs an ellipsoid with a given center point and per-axis radii.
     *
     * @param center the center point of the ellipsoid
     * @param rx     the radius along the X axis
     * @param ry     the radius along the Y axis
     * @param rz     the radius along the Z axis
     * @throws IllegalArgumentException if any radius is not positive
     */
    public Ellipsoid(Point center, double rx, double ry, double rz) {
        if (rx <= 0 || ry <= 0 || rz <= 0)
            throw new IllegalArgumentException("Ellipsoid radii must be positive");
        _center = center;
        _rx = rx;
        _ry = ry;
        _rz = rz;
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        final Point origin = ray.getOrigin();
        final Vector dir = ray.getDirection();

        // Transform into unit-sphere space: divide relative coordinates by each axis' radius.
        final double ox = (origin.getX() - _center.getX()) / _rx;
        final double oy = (origin.getY() - _center.getY()) / _ry;
        final double oz = (origin.getZ() - _center.getZ()) / _rz;
        final double dx = dir.getX() / _rx;
        final double dy = dir.getY() / _ry;
        final double dz = dir.getZ() / _rz;

        final double a = dx * dx + dy * dy + dz * dz;
        final double b = 2 * (ox * dx + oy * dy + oz * dz);
        final double c = ox * ox + oy * oy + oz * oz - 1;

        final double discriminant = alignZero(b * b - 4 * a * c);
        // A non-positive discriminant means either a miss or an exact tangent graze; both
        // are treated as "no intersection", matching Sphere's convention for a tangent ray.
        if (discriminant <= 0) return null;

        final double sqrtDiscriminant = Math.sqrt(discriminant);
        final double t1 = alignZero((-b - sqrtDiscriminant) / (2 * a));
        final double t2 = alignZero((-b + sqrtDiscriminant) / (2 * a));

        if (t1 <= 0 && t2 <= 0) return null;

        final Point potential1 = ray.getPoint(t1);
        final Point potential2 = ray.getPoint(t2);

        final boolean validP1Dist = alignZero(potential1.distance(origin) - maxDistance) <= 0;
        final boolean validP2Dist = alignZero(potential2.distance(origin) - maxDistance) <= 0;

        if (!validP1Dist && !validP2Dist) return null;

        if (t1 > 0 && t2 > 0) {
            if (validP1Dist && validP2Dist) {
                return List.of(new Intersection(this, potential1), new Intersection(this, potential2));
            }
            return validP1Dist ? List.of(new Intersection(this, potential1)) : List.of(new Intersection(this, potential2));
        }

        if (t1 > 0 && validP1Dist) return List.of(new Intersection(this, potential1));
        if (t2 > 0 && validP2Dist) return List.of(new Intersection(this, potential2));

        return null;
    }

    @Override
    public Vector getNormal(Point point) {
        final Vector relative = point.subtract(_center);
        return new Vector(
                relative.getX() / (_rx * _rx),
                relative.getY() / (_ry * _ry),
                relative.getZ() / (_rz * _rz)).normalize();
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(
                _center.getX() - _rx, _center.getY() - _ry, _center.getZ() - _rz,
                _center.getX() + _rx, _center.getY() + _ry, _center.getZ() + _rz);
    }

    @Override
    public String toString() {
        return "Ellipsoid: Center: " + _center + ", Radii: (" + _rx + ", " + _ry + ", " + _rz + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ellipsoid other = (Ellipsoid) obj;
        return _center.equals(other._center) && isZero(_rx - other._rx)
                && isZero(_ry - other._ry) && isZero(_rz - other._rz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_center, _rx, _ry, _rz);
    }
}
