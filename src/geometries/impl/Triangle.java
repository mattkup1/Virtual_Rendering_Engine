package geometries.impl;

import primitives.Point;

/**
 * Represents a triangle in a 3D Cartesian coordinate system.
 * <p>
 * A triangle is a flat, three-sided polygon defined by three vertices.
 * It is a fundamental primitive in 3D graphics and ray tracing.
 * </p>
 * * @author mattkuperwasser
 * @author moshehanau
 */
public final class Triangle extends Polygon {

    /**
     * Constructs a triangle from three vertices.
     * <p>
     * The points must be unique and not collinear to form a valid triangle.
     * </p>
     *
     * @param p1 the first vertex
     * @param p2 the second vertex
     * @param p3 the third vertex
     * @throws IllegalArgumentException if vertices are collinear or duplicate
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }

    /**
     * Returns a string representation of the triangle.
     *
     * @return a string describing the triangle vertices
     */
    @Override
    public String toString() {
        return "Triangle: " + super.toString();
    }
}