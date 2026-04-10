package geometries.impl;

import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents an infinite tube in a 3D Cartesian coordinate system.
 * <p>
 * A tube is defined by an infinite central axis ray and a radius.
 * It consists of all points at a fixed distance (the radius) from the axis.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Tube extends RadialGeometry {
    /**
     * The central axis ray of the tube
     */
    protected final Ray _axis;

    /**
     * Constructs a tube with a given radius and axis ray.
     *
     * @param radius the radius of the tube
     * @param axis   the central axis ray
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }

    @Override
    public List<Point> findIntersections(Ray ray) {

        final Point rayOrigin = ray.getOrigin(), tubeOrigin = this._axis.getOrigin();
        final Vector tubeDirection = this._axis.getDirection(), rayDirection = ray.getDirection();

        // If the ray and tube directions are equal - no intersection points
        if (tubeDirection.equals(rayDirection)) return null;

        // Get the vector from the tube origin point to the ray origin point
        final Vector deltaP = rayOrigin.subtract(tubeOrigin);

        final Vector vectorA = deltaP.subtract(tubeDirection.scale(deltaP.dotProduct(tubeDirection)));
        final Vector vectorB = rayDirection.subtract(tubeDirection.scale(rayDirection.dotProduct(tubeDirection)));

        final double a = alignZero(vectorB.lengthSquared());
        final double b = alignZero(2 * vectorA.dotProduct(vectorB));
        final double c = alignZero(vectorA.lengthSquared() - this._radiusSquared);

        // Get the discriminant
        final double discriminant = alignZero(b * b - 4 * a * c);

        // Discriminant < 0: No intersection points
        if (discriminant < 0) return null;

        // Discriminant = 0: single intersection point
        if (isZero(discriminant)) {
            return List.of(rayDirection.scale(-(b / (2 * a))));
        }

        // Discriminant > 0: 2 intersection points
        final double t1 = (-b - discriminant) / (2 * a);
        final double t2 = (-b + discriminant) / (2 * a);

        // if t1 and t2 are less than 0 than the points is on the ray tail
        // if t1 or t2 are 0 than - the ray origin is on the tube
        // if one is less than 0 and the other is greater than 0 - 1 intersection point

        // Case no intersection points
        if ((t1 < 0 || isZero(t1)) && (t2 < 0 || isZero(t2))) return null;
        // Case 2 intersection points
        if (t1 > 0 && t2 > 0) {
            return List.of(ray.getPoint(t1), ray.getPoint(t2));
        }
        // Case one intersection
        return t1 > 0 ? List.of(ray.getPoint(t1)) : List.of(ray.getPoint(t2));
    }

    @Override
    public Vector getNormal(Point point) {
        double t = _axis.getDirection().dotProduct(point.subtract(_axis.getOrigin()));
        Point projectionPoint = isZero(t) ? _axis.getOrigin() : _axis.getOrigin().add(_axis.getDirection().scale(t));
        Vector normal = point.subtract(projectionPoint);
        return normal.normalize();
    }

    @Override
    public String toString() {
        return "Tube: Radius: " + _radius + " Axis: " + _axis;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tube t = (Tube) obj;
        return isZero(_radius - t._radius) && _axis.equals(t._axis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _axis);
    }
}