package geometries.impl;

import primitives.Point;
import primitives.Vector;

public class Plane {

    private final Point _point;
    private Vector _normal;

    Plane(Point p1 , Point p2 , Point p3) {
        this._point = p1;
        /*
            Vector vector1 = p1.subtract(p2);
            Vector vector2 = p2.subtract(p3);
            _normal= vector1.crossProduct(vector2);
            _point = p1;
         */
    }

    Plane(Vector normal , Point point) {
        _normal = normal.normalize();
        _point = point;
    }

    public Vector getNormal() {
        return _normal;
    }
}
