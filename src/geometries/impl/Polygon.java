package geometries.impl;

import geometries.api.Geometry;
import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a convex polygon in a 3D Cartesian coordinate system.
 * <p>
 * The polygon is defined by an ordered sequence of vertices.
 * All vertices must lie in the same plane and be arranged along the
 * polygon edge path.
 * </p>
 * <p>
 * The polygon must be convex.
 * </p>
 *
 * @author Dan Zilberstein
 */
public class Polygon extends Geometry {
    /**
     * Ordered list of polygon vertices
     */
    protected final List<Point> _vertices;
    /**
     * Plane containing the polygon
     */
    protected final Plane _plane;
    /**
     * Number of vertices
     */
    private final int _size;

    /**
     * Constructs a convex polygon from ordered vertices.
     * <p>
     * The vertices must:
     * </p>
     * <ul>
     * <li>Contain at least three points</li>
     * <li>Be ordered along the polygon edge path</li>
     * <li>Lie in the same plane</li>
     * <li>Form a convex polygon</li>
     * </ul>
     *
     * @param vertices polygon vertices in edge order
     * @throws IllegalArgumentException if the vertices do not form a valid convex
     *                                  polygon
     */
    public Polygon(Point... vertices) {
        if (vertices.length < 3)
            throw new IllegalArgumentException("A polygon can't have less than 3 vertices");
        _vertices = List.of(vertices);
        _size = vertices.length;

        // Create the supporting plane using the first three vertices.
        // The plane stores the constant normal of the polygon.
        _plane = new Plane(vertices[0], vertices[1], vertices[2]);
        if (_size == 3) return; // no need for more tests for a Triangle

        Vector n = _plane.getNormal(vertices[0]);
        // Subtracting identical vertices would create a zero vector (illegal)
        Vector edge1 = vertices[_size - 1].subtract(vertices[_size - 2]);
        Vector edge2 = vertices[0].subtract(vertices[_size - 1]);

        // Cross product of consecutive edges determines orientation.
        // All edge pairs must produce the same sign relative to the normal,
        // otherwise the polygon is concave or vertices are unordered.
        boolean positive = edge1.crossProduct(edge2).dotProduct(n) > 0;
        for (var i = 1; i < _size; ++i) {
            // Test that the point is in the same plane as calculated originally
            if (!isZero(vertices[i].subtract(vertices[0]).dotProduct(n)))
                throw new IllegalArgumentException("All vertices of a polygon must lay in the same plane");
            // Test the consequent edges have
            edge1 = edge2;
            edge2 = vertices[i].subtract(vertices[i - 1]);
            if (positive != (edge1.crossProduct(edge2).dotProduct(n) > 0))
                throw new IllegalArgumentException("All vertices must be ordered and the polygon must be convex");
        }
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        // Get 3 polygon vertices to define the plane containing the polygon
        final Point A = _vertices.get(0),
                B = _vertices.get(1),
                C = _vertices.get(2),
                rayOrigin = ray.getOrigin();

        final int numVertices = this._vertices.size();

        // Get the ray's intersection point with the triangle's plane
        final Plane PolyPlane = new Plane(A, B, C);
        final var planeIntersection = PolyPlane.findIntersections(ray);
        if (planeIntersection == null)
            return null;

        // Get the vectors from the ray origin to the polygon vertices
        Vector[] vList = new Vector[numVertices];

        for (int i = 0; i < numVertices; ++i) {
            vList[i] = this._vertices.get(i).subtract(rayOrigin);
        }


        // Get the normalized normal vectors to the planes represented by each 2 of the above vectors
        // together with the ray origin
        Vector[] nList = new Vector[numVertices];
        for (int i = 0; i < numVertices; ++i) {
            nList[i] = vList[i].crossProduct(vList[(i + 1) % numVertices]);
        }


        // Get the ray direction vector
        final Vector rayDirection = ray.getDirection();

        // Get the dot product of each pair of the above normal vectors
        double[] sList = new double[numVertices];
        for (int i = 0; i < numVertices; ++i) {
            sList[i] = alignZero(rayDirection.dotProduct(nList[i]));
            if (isZero(sList[i])) return null;
        }

        // If all dot products produce the same sign - the intersection point is inside the polygon
        // If one or more of the dot products produce zero, then the ray intersects a polygon edge (or vertex)
        if (sList[0] > 0) {
            for (int i = 1; i < numVertices; ++i) {
                if (sList[i] < 0) return null;
            }
        } else {
            for (int i = 1; i < numVertices; ++i) {
                if (sList[i] > 0) return null;
            }
        }
        return planeIntersection;
    }

    @Override
    public Vector getNormal(Point point) {
        return _plane.getNormal(point);
    }

    @Override
    public String toString() {
        return _vertices.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Polygon other = (Polygon) obj;
        // Use List.equals(), which checks the equality of every Point in order
        return this._vertices.equals(other._vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_vertices, _plane, _size);
    }
}
