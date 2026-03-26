package geometries.impl;

import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

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
        // TODO: implement
        return List.of();
    }

    @Override
    public Vector getNormal(Point point) {
        double t = _axis.getDirection().dotProduct(point.subtract(_axis.getOrigin()));
        Point projectionPoint = Util.isZero(t) ? _axis.getOrigin() : _axis.getOrigin().add(_axis.getDirection().scale(t));
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
        return Util.isZero(_radius - t._radius) && _axis.equals(t._axis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _axis);
    }
}