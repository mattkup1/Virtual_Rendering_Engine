package geometries.impl;

import primitives.Ray;
import primitives.Vector;
import primitives.Point;

public class Tube extends RadialGeometry{
    private final Ray _axis;

    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }

    @Override
    public Vector getNormal(Point point) {
        return null;
    }
}


