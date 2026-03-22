package geometries.impl;

import java.util.Objects;
import primitives.Point;
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