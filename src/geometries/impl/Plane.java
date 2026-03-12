package geometries.impl;

import primitives.Point;
import primitives.Vector;

public class Plane {

    // is the field need to be private and final?
    private Point _point;
    private Vector _vector;
    Plane(Point p1 , Point p2 , Point p3) {
            Vector vector1 = p1.subtract(p2);
            Vector vector2 = p2.subtract(p3);
            _vector = vector1.crossProduct(vector2);
            _point = p1;
    }

    Plane(Vector normal , Point point) {
        _vector = normal.normalize();
        _point = point;
    }

    public Vector getNormal() {
        return _vector;
    }
}
