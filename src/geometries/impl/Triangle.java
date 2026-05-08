package geometries.impl;

import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a triangle in a 3D Cartesian coordinate system.
 * <p>
 * A triangle is a flat, three-sided polygon defined by three vertices.
 * It is a fundamental primitive in 3D graphics and ray tracing.
 * </p>
 *
 * @author mattkuperwasser
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

    @Override
    public List<Intersection> calcIntersectionsHelper(Ray ray) {
        // Setup basic components of the ray and triangle
        Point p0 = ray.getOrigin();
        Vector rayDir = ray.getDirection();
        Point v0 = _vertices.get(0);
        Point v1 = _vertices.get(1);
        Point v2 = _vertices.get(2);

        // Find vectors for two edges sharing the first vertex (v0)
        Vector edge1 = v1.subtract(v0);
        Vector edge2 = v2.subtract(v0);

        // Begin calculating the determinant.
        // The algorithm treats the intersection as a system of linear equations.
        // 'pvec' is a helper vector used for the triple product (scalar triple product).
        Vector pvec = rayDir.crossProduct(edge2);
        double det = edge1.dotProduct(pvec);

        // If the determinant is zero, the ray lies in the plane of the triangle
        // or is parallel to it.
        if (isZero(det)) return null;

        // We pre-calculate the inverse determinant to replace divisions with multiplications (faster)
        double invDet = 1.0 / det;

        // Calculate the distance from v0 to the ray origin
        Vector tvec = p0.subtract(v0);

        // Calculate the 'u' barycentric coordinate.
        // This represents the weight of vertex v1.
        double u = tvec.dotProduct(pvec) * invDet;

        // If u < 0 or u > 1, the intersection point is outside the triangle
        if (u < 0 || u > 1 || isZero(u)) return null;

        // Calculate the 'w' barycentric coordinate.
        // 'qvec' is another helper vector for the second part of the system.
        Vector qvec = tvec.crossProduct(edge1);

        // This represents the weight of vertex v2.
        double w = rayDir.dotProduct(qvec) * invDet;

        // If w < 0 or u + w > 1, the point is outside the triangle.
        // (u + v + w must equal 1, and since u+w is the portion of the other
        // two vertices, their sum cannot exceed 1).
        if (w < 0 || isZero(w) || u + w > 1 || isZero(1 - u - w)) return null;

        // Calculate t - the distance from the ray origin to the intersection point.
        double t = edge2.dotProduct(qvec) * invDet;

        // If t is negative or zero, the intersection is behind the ray origin.
        return alignZero(t) <= 0 ? null : List.of(new Intersection(this, ray.getPoint(t)));
    }

    @Override
    public String toString() {
        return "Triangle: " + super.toString();
    }
}