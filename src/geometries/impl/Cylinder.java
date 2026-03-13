package geometries.impl;

import primitives.Ray;

/**
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Cylinder extends Tube {
    private double _height;

    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        if (height <= 0)
            throw new IllegalArgumentException("Cylinder height must be positive");
        _height = height;
    }
}
