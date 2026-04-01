package geometries.impl;

import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;

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
    public List<Point> findIntersections(Ray ray) {
        // Get triangle vertices and ray origin
        final Point A = _vertices.get(0),
                B = _vertices.get(1),
                C = _vertices.get(2),
                RayOrigin = ray.getOrigin();

        // Get the ray's intersection point with the triangle's plane
        final Plane triPlane = new Plane(A, B, C);
        final var planeIntersection = triPlane.findIntersections(ray);
        if (planeIntersection == null)
            return null;

        // Get the vectors from the ray origin to the triangle vertices
        final Vector v1 = A.subtract(RayOrigin);
        final Vector v2 = B.subtract(RayOrigin);
        final Vector v3 = C.subtract(RayOrigin);

        // Get the normalized normal vectors to the planes represented by each 2 of the above vectors
        // together with the ray origin
        final Vector n1 = v1.crossProduct(v2).normalize();
        final Vector n2 = v2.crossProduct(v3).normalize();
        final Vector n3 = v3.crossProduct(v1).normalize();

        // Get the ray direction vector
        final Vector rayDirection = ray.getDirection();

        // Get the dot product of each pair of the above normal vectors
        final double s1 = alignZero(rayDirection.dotProduct(n1)),
                s2 = alignZero(rayDirection.dotProduct(n2)),
                s3 = alignZero(rayDirection.dotProduct(n3));

        // If all dot products produce the same sign - the intersection point is inside the triangle
        // If one or more of the dot products produce zero, then the ray intersects the triangle edge (or vertex)
        if ((s1 > 0 && s2 > 0 && s3 > 0) || (s1 < 0 && s2 < 0 && s3 < 0)) {
            return planeIntersection;
        }

        return null;
    }

    @Override
    public String toString() {
        return "Triangle: " + super.toString();
    }
}