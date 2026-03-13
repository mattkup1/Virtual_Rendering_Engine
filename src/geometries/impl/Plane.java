package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a plane in 3D Cartesian coordinate system.
 * A plane is defined by a point on the plane and a normal vector perpendicular to it.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Plane extends Geometry {

    /** A point on the plane */
    private final Point _point;

    /** The normal vector to the plane */
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
        this._point = p1;

        /*
        // TODO: check that all 3 points are not on the same straight
        Vector v1 = p2.subtract(p1);
        Vector v2 = p3.subtract(p1);

        // The normal is the cross product of two vectors on the plane
        this._normal = v1.crossProduct(v2).normalize();
         */
    }

    /**
     * Constructs a plane from a normal vector and a point on the plane.
     *
     * @param normal the normal vector to the plane
     * @param point  a point on the plane
     */
    public Plane(Vector normal, Point point) {
        this._normal = normal.normalize();
        this._point = point;
    }

    /**
     * Returns the normal vector of the plane.
     *
     * @return the normal vector
     */
    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }

    /**
     * Returns the point defining the plane.
     *
     * @return the point
     */
    public Point getPoint() {
        return _point;
    }
}