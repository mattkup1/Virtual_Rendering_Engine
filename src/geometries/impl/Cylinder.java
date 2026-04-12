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
        List<Point> intersections = new LinkedList<>();

        final Ray axisRay = this._axis;
        final Point p0 = axisRay.getOrigin();
        final Vector v = axisRay.getDirection();

        // grab intersections from the infinite tube first
        List<Point> tubeIntersections = super.findIntersections(ray);

        if (tubeIntersections != null) {
            for (Point p : tubeIntersections) {
                // skip if the point is exactly at the axis origin to avoid zero vector
                if (p.equals(p0)) {
                    continue;
                }

                // project the point onto the axis to see where it lands height-wise
                double tProj = alignZero(p.subtract(p0).dotProduct(v));

                // keep it only if it falls strictly inside the cylinder bounds
                if (tProj > 0 && tProj < _height) {
                    intersections.add(p);
                }
            }
        }

        // check the bottom cap (at p0)
        addCapIntersection(ray, p0, v, _radius, intersections);

        // check the top cap (at p0 + v * height)
        Point topCenter = p0.add(v.scale(_height));
        addCapIntersection(ray, topCenter, v, _radius, intersections);

        return intersections.isEmpty() ? null : intersections;
    }

    private void addCapIntersection(Ray ray, Point capCenter, Vector capNormal, double radius, List<Point> intersections) {
        final Vector dir = ray.getDirection();

        double nv = alignZero(capNormal.dotProduct(dir));
        // ray is parallel to the cap
        if (nv == 0) {
            return;
        }

        // avoid zero vector if ray starts exactly at the cap center
        if (ray.getOrigin().equals(capCenter)) {
            return;
        }

        // calculate t for the plane intersection
        double t = alignZero(capNormal.dotProduct(capCenter.subtract(ray.getOrigin())) / nv);

        // ignore points behind the ray
        if (t <= 0) {
            return;
        }

        Point p = ray.getPoint(t);

        // verify the point is actually within the cap's radius
        double dSquared = alignZero(p.distanceSquared(capCenter));
        if (dSquared <= radius * radius) {
            intersections.add(p);
        }
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