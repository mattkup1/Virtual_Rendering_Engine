package geometries.impl;

import geometries.api.Geometry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import primitives.BoundingBox;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a finite right circular cone in a 3D Cartesian coordinate system.
 * <p>
 * The cone is defined by its apex and axis (as a {@link Ray}, mirroring
 * {@link Cylinder}'s constructor), a base radius, and a height measured from the
 * apex along the axis to the flat circular base cap.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Cone extends Geometry {
    /**
     * The apex point and opening direction of the cone, as a ray
     */
    private final Ray _axis;
    /**
     * The radius of the cone's base cap
     */
    private final double _radius;
    /**
     * The height of the cone, measured from the apex to the base cap along the axis
     */
    private final double _height;
    /**
     * The squared cosine of the cone's half-angle, precomputed for the lateral
     * surface's quadratic intersection equation
     */
    private final double _cosHalfAngleSquared;

    /**
     * Constructs a cone with a given base radius, axis ray (apex and opening direction), and height.
     *
     * @param radius the radius of the cone's base cap
     * @param axis   the apex point and opening direction, as a ray
     * @param height the height of the cone, from the apex to the base cap
     * @throws IllegalArgumentException if radius or height is not positive
     */
    public Cone(double radius, Ray axis, double height) {
        if (radius <= 0) throw new IllegalArgumentException("Cone base radius must be positive");
        if (height <= 0) throw new IllegalArgumentException("Cone height must be positive");
        _radius = radius;
        _axis = axis;
        _height = height;
        _cosHalfAngleSquared = (height * height) / (height * height + radius * radius);
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        final Point apex = _axis.getOrigin();
        final Vector v = _axis.getDirection();
        final Point rayOrigin = ray.getOrigin();
        final Vector d = ray.getDirection();

        List<Intersection> intersections = null;

        // Lateral surface: solve the quadratic (D.V)^2 - cos^2(theta) = 0 form
        if (!rayOrigin.equals(apex)) {
            final Vector co = rayOrigin.subtract(apex);
            final double dV = d.dotProduct(v);
            final double coV = co.dotProduct(v);
            final double dCo = d.dotProduct(co);
            final double coCo = co.dotProduct(co);

            final double a = alignZero(dV * dV - _cosHalfAngleSquared);
            final double b = alignZero(2 * (dV * coV - dCo * _cosHalfAngleSquared));
            final double c = alignZero(coV * coV - coCo * _cosHalfAngleSquared);

            if (!isZero(a)) {
                final double discriminant = alignZero(b * b - 4 * a * c);
                if (discriminant >= 0) {
                    final double sqrtDiscriminant = Math.sqrt(discriminant);
                    final double t1 = alignZero((-b - sqrtDiscriminant) / (2 * a));
                    final double t2 = alignZero((-b + sqrtDiscriminant) / (2 * a));
                    intersections = addLateralIfValid(intersections, ray, apex, v, t1, maxDistance);
                    intersections = addLateralIfValid(intersections, ray, apex, v, t2, maxDistance);
                }
            } else if (!isZero(b)) {
                final double t = alignZero(-c / b);
                intersections = addLateralIfValid(intersections, ray, apex, v, t, maxDistance);
            }
        }

        // Base cap
        final Point capCenter = _axis.getPoint(_height);
        final Plane capPlane = new Plane(capCenter, v);
        final var capPlaneIntersections = capPlane.findIntersections(ray);
        if (capPlaneIntersections != null) {
            final Point capPoint = capPlaneIntersections.getFirst();
            if (alignZero(capPoint.distanceSquared(capCenter) - _radius * _radius) <= 0
                    && alignZero(capPoint.distance(rayOrigin) - maxDistance) <= 0) {
                if (intersections == null) intersections = new ArrayList<>();
                intersections.add(new Intersection(this, capPoint));
            }
        }

        if (intersections == null) return null;
        if (intersections.size() > 1) {
            intersections.sort((i1, i2) ->
                    Double.compare(i1.point.distanceSquared(rayOrigin), i2.point.distanceSquared(rayOrigin)));
        }
        return intersections;
    }

    /**
     * Validates a lateral-surface quadratic root and, if it lies in front of the ray and
     * within the cone's finite height, appends the corresponding intersection.
     *
     * @param intersections the intersections collected so far, or {@code null}
     * @param ray           the ray being tested
     * @param apex          the cone's apex point
     * @param v             the cone's axis direction
     * @param t             the candidate ray parameter
     * @param maxDistance   the maximum distance from the ray origin to consider
     * @return the (possibly newly-allocated) intersections list
     */
    private List<Intersection> addLateralIfValid(
            List<Intersection> intersections, Ray ray, Point apex, Vector v, double t, double maxDistance) {
        if (t <= 0) return intersections;

        final Point p = ray.getPoint(t);
        final double m = p.equals(apex) ? 0 : alignZero(p.subtract(apex).dotProduct(v));
        if (m <= 0 || m >= _height) return intersections; // apex, mirror nappe, or beyond the base cap

        if (alignZero(p.distance(ray.getOrigin()) - maxDistance) > 0) return intersections;

        if (intersections == null) intersections = new ArrayList<>();
        intersections.add(new Intersection(this, p));
        return intersections;
    }

    @Override
    public Vector getNormal(Point point) {
        final Point apex = _axis.getOrigin();
        final Vector v = _axis.getDirection();

        if (point.equals(apex)) return v.scale(-1);

        final Vector co = point.subtract(apex);
        final double m = co.dotProduct(v);
        if (isZero(m - _height)) return v; // on the base cap

        return co.subtract(v.scale(m / _cosHalfAngleSquared)).normalize();
    }

    @Override
    public BoundingBox getBoundingBox() {
        final Point apex = _axis.getOrigin();
        final Point baseCenter = _axis.getPoint(_height);
        return new BoundingBox(
                Math.min(apex.getX(), baseCenter.getX()) - _radius,
                Math.min(apex.getY(), baseCenter.getY()) - _radius,
                Math.min(apex.getZ(), baseCenter.getZ()) - _radius,
                Math.max(apex.getX(), baseCenter.getX()) + _radius,
                Math.max(apex.getY(), baseCenter.getY()) + _radius,
                Math.max(apex.getZ(), baseCenter.getZ()) + _radius);
    }

    @Override
    public String toString() {
        return "Cone: Apex: " + _axis.getOrigin() + ", Axis: " + _axis.getDirection()
                + ", Radius: " + _radius + ", Height: " + _height;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cone other = (Cone) obj;
        return _axis.equals(other._axis) && Util.isZero(_radius - other._radius)
                && Util.isZero(_height - other._height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_axis, _radius, _height);
    }
}
