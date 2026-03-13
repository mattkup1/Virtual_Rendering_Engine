package geometries.impl;

import java.util.Objects;

import primitives.Point;
import primitives.Util;
import primitives.Vector;

/**
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Sphere extends RadialGeometry {
    private final Point _center;

    public Sphere(Point center, double radius) {
        super(radius);
        this._center = center;
    }

    @Override
    public Vector getNormal(Point point) {
            return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sphere s = (Sphere) obj;
        return _center.equals(s._center) && Util.isZero(_radius - s._radius);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_center, _radius);
    }
}
