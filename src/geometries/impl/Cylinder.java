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

        final Point TubeOrig = this._axis.getOrigin();
        final Vector TubeDir = this._axis.getDirection();
        final Vector RayDir = ray.getDirection();

        // Check the infinite tube first
        List<Point> tubeIntersections = super.findIntersections(ray);

        if (tubeIntersections != null) {
            for (Point p : tubeIntersections) {
                // Prevent zero-vector exception
                if (p.equals(TubeOrig)) continue;

                // Verify the point is within the cylinder's height
                double tProj = alignZero(p.subtract(TubeOrig).dotProduct(TubeDir));
                if (tProj > 0 && tProj < _height) {
                    // Initialize the intersections result list if needed
                    if (intersections == null) intersections = new LinkedList<>();
                    // Add the intersection point to the list
                    intersections.add(p);
                }
            }
        }

        // Check bottom and top caps
        double nv = alignZero(TubeDir.dotProduct(RayDir));

        // Proceed only if the ray is not parallel to the caps
        if (nv != 0) {
            // Bottom cap
            if (!ray.getOrigin().equals(TubeOrig)) {
                double tBottom = alignZero(TubeDir.dotProduct(TubeOrig.subtract(ray.getOrigin())) / nv);
                intersections = getPointsOnCap(ray, intersections, TubeOrig, tBottom);
            }

            // Top cap
            Point topCenter = TubeOrig.add(TubeDir.scale(this._height));
            if (!ray.getOrigin().equals(topCenter)) {
                double tTop = alignZero(TubeDir.dotProduct(topCenter.subtract(ray.getOrigin())) / nv);
                intersections = getPointsOnCap(ray, intersections, topCenter, tTop);
            }
        }

        return intersections;
    }

    /**
     * Helper function to determine if a ray's intersection with the plane of a cylinder's cap
     * falls within the actual boundaries of the disk (cap).
     *
     * @param ray           the ray being tested for intersection
     * @param intersections the current list of found intersection points (may be null)
     * @param capCenter     the center point of the cap (either bottom origin or top center)
     * @param t             the distance from the ray origin to the intersection with the cap's plane
     * @return an updated list of intersection points including the cap intersection if valid
     */
    private List<Point> getPointsOnCap(Ray ray, List<Point> intersections, Point capCenter, double t) {
        // Intersection must be in the positive direction of the ray
        if (t > 0) {
            Point p = ray.getPoint(t);
            // Verify the point is inside the cap's radius (on the disk)
            // Using distanceSquared is more efficient than distance to avoid a square root
            if (alignZero(p.distanceSquared(capCenter) - _radius * _radius) <= 0) {
                if (intersections == null)
                    intersections = new LinkedList<>();
                intersections.add(p);
            }
        }
        return intersections;
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