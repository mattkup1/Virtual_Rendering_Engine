package geometries.impl;

import geometries.impl.RadialGeometry;
import primitives.Point;
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sphere s = (Sphere) obj;
        return _center == s._center && _radius == s._radius;
    }
}
