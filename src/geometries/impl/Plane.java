package geometries.impl;

import geometries.api.Geometry;
import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents a plane in 3D Cartesian coordinate system.
 * A plane is defined by a point on the plane and a normal vector perpendicular to it.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Plane extends Geometry {

    /**
     * A point on the plane
     */
    private final Point _point;

    /**
     * The normal vector to the plane
     */
    private Vector _normal;

    /**
     * Constructs a plane from three points on the plane.
     * The points must be unique and not collinear.
     *
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @throws IllegalArgumentException if points are collinear or identical
     */
    public Plane(Point p1, Point p2, Point p3) {
        // Check whether any two of the given points are identical
        if (p1.equals(p2) || p1.equals(p3) || p2.equals(p3)) {
            throw new IllegalArgumentException("Plane cannot be defined by identical points");
        }
        // in case that the three point are collinear then the cross product throw exception
        Vector vec = p1.subtract(p2).crossProduct(p2.subtract(p3));
        this._point = p1;
        // create 2 vectors by using subtract method then we compute the normal to these vectors and finally normalizing them.
        this._normal = vec.normalize();

    }

    /**
     * Constructs a plane from a normal vector and a point on the plane.
     *
     * @param normal the normal vector to the plane
     * @param point  a point on the plane
     */
    public Plane(Point point, Vector normal) {
        if (normal == null) throw new NullPointerException("the normal of the Plane cannot be null");
        this._normal = normal.normalize();
        this._point = point;
    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        // TODO: implement
        return List.of();
    }

    /**
     * Returns the normal vector of the plane.
     *
     * @return the normal vector
     */
    @Override
    public Vector getNormal(Point point) {
        return _normal.normalize();
    }

    /**
     * Returns the point defining the plane.
     *
     * @return the point
     */
    public Point getPoint() {
        return _point;
    }

    @Override
    public String toString() {
        return "Plane: Point: " + _point + ", Normal: " + _normal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_point, _normal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return this._point.equals(((Plane) obj)._point) && this._normal.equals(((Plane) obj)._normal);
    }
}
