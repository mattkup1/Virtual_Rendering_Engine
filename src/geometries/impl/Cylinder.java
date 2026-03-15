package geometries.impl;

import java.util.Objects;
import primitives.Ray;
import primitives.Util;

/**
 * Represents a cylinder in a 3D Cartesian coordinate system.
 * <p>
 * A cylinder is a finite tube with a specific height and two flat bases.
 * It is defined by a central axis (a ray), a radius, and a height.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Cylinder extends Tube {
    /**
     * The height of the cylinder
     */
    private double _height;

    /**
     * Constructs a cylinder with a given radius, axis ray, and height.
     *
     * @param radius the radius of the cylinder
     * @param axis   the central axis ray
     * @param height the height of the cylinder
     * @throws IllegalArgumentException if height is less than or equal to zero
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        if (height <= 0)
            throw new IllegalArgumentException("Cylinder height must be positive");
        _height = height;
    }

    @Override
    public String toString() {
        return "Cylinder: Radius: " + _radius + ", Axis: " + _axis + ", Height: " + _height;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj) && Util.isZero(((Cylinder) obj)._height - _height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _axis, _height);
    }
}