package geometries.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

import static primitives.Util.alignZero;

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
    private final double _height;

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
    public Vector getNormal(Point p) {
        // Case point on cylinder is equal to the cylinder origin - return the normal to bottom base
        if (p.equals(_axis.getOrigin())) return this._axis.getDirection().scale(-1);
        // Dot product between the vector represented by the point and the cylinder axis direction vector
        // This computation essentially returns the projection of the point vector on the axis
        // If the result is 0, then the point is on the bottom base
        // If the result is equal to the height of the cylinder then the point is on the top base
        // If the result is between 0 and the height of the cylinder then the point is on the round surface
        // We assume the point is somewhere on the cylinder
        double t = _axis.getDirection().dotProduct(p.subtract(_axis.getOrigin()));
        if (Util.isZero(t - this._height)) {
            // Case point on top base (t == height)
            return this._axis.getDirection();
        } else if (Util.isZero(t)) {
            // Case point on bottom base (t == 0)
            return this._axis.getDirection().scale(-1);
        } else {
            // Case point on round surface
            // Get the projection point on the axis by adding the projection vector to the cylinder origin point
            Point projectionPoint = _axis.getOrigin().add(_axis.getDirection().scale(t));
            // Return the vector from the projection point to the point on the cylinder
            return p.subtract(projectionPoint).normalize();
        }
    }


    @Override
    public List<Point> findIntersections(Ray ray) {
        List<Point> intersections = null;

        final Point tubeOrig = this._axis.getOrigin();
        final Vector tubeDir = this._axis.getDirection();

        // Check the infinite tube first
        List<Point> tubeIntersections = super.findIntersections(ray);

        if (tubeIntersections != null) {
            for (Point p : tubeIntersections) {
                // Prevent zero-vector exception on subtraction if point is at origin
                double tProj;
                if (p.equals(tubeOrig)) {
                    tProj = 0;
                } else {
                    tProj = alignZero(p.subtract(tubeOrig).dotProduct(tubeDir));
                }

                // Verify the point is within the cylinder's height boundary (0 < t < height)
                if (tProj > 0 && tProj < this._height) {
                    if (intersections == null) intersections = new LinkedList<>();
                    intersections.add(p);
                }
            }
        }

        // Check Bottom Cap (at origin)
        Point bottomIntersection = getPointOnCap(ray, tubeOrig);
        if (bottomIntersection != null) {
            if (intersections == null) intersections = new LinkedList<>();
            intersections.add(bottomIntersection);
        }

        // Check Top Cap (at origin + height)
        Point topIntersection = getPointOnCap(ray, this._axis.getPoint(this._height));
        if (topIntersection != null) {
            if (intersections == null) intersections = new LinkedList<>();
            intersections.add(topIntersection);
        }

        return intersections;
    }

    /**
     * Helper function to get the intersection point between a ray and cylinder disk (cap)
     *
     * @param ray    the ray being tested for intersection
     * @param midCap the center point of the given cap
     * @return the intersection point between the ray and the cap, or null if no valid intersection
     */
    private Point getPointOnCap(Ray ray, Point midCap) {
        // Create the plane representing the base/cap
        final Plane plane = new Plane(midCap, this._axis.getDirection());

        // Get the intersection with the infinite plane
        var capIntersections = plane.findIntersections(ray);

        if (capIntersections != null) {
            final Point p = capIntersections.getFirst();
            // Point must be within the disk boundary (distance <= radius)
            // Using alignZero to handle floating point precision
            if (alignZero(p.distanceSquared(midCap) - this._radiusSquared) <= 0) {
                return p;
            }
        }
        return null;
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