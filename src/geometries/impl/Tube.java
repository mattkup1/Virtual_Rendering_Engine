package geometries.impl;

import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents an infinite tube in a 3D Cartesian coordinate system.
 * <p>
 * A tube is defined by an infinite central axis ray and a radius.
 * It consists of all points at a fixed distance (the radius) from the axis.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Tube extends RadialGeometry {
    /**
     * The central axis ray of the tube
     */
    protected final Ray _axis;

    /**
     * Constructs a tube with a given radius and axis ray.
     *
     * @param radius the radius of the tube
     * @param axis   the central axis ray
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        _axis = axis;
    }

    @Override
    public List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Point p0 = ray.getOrigin();
        Vector vD = ray.getDirection();
        Point pC = this._axis.getOrigin();
        Vector vC = this._axis.getDirection();

        // Compute the dot product between the ray direction vector and the tube direction vector
        // The result will be used to determine crucial information before proceeding with further computations
        // and will help avoid construction of zero vectors
        double vD_dot_vC = alignZero(vD.dotProduct(vC));

        // Check if the ray is parallel to the tube axis
        // This happens if the dot product is 1 or -1
        // in which case, the ray does not intersect the tube at all
        double a = alignZero(1 - vD_dot_vC * vD_dot_vC);
        if (isZero(a)) {
            return null;
        }

        double vD_dot_dP = 0;
        double dP_dot_vC = 0;
        double dP_dot_dP = 0;

        // Case ray origin != tube axis origin
        if (!p0.equals(pC)) {
            // Get the vector from the tube axis origin to the ray origin
            // The if statement above asserts that this vector will not be the zero vector
            Vector deltaP = p0.subtract(pC);
            // Get the dot product between deltaP and the ray direction vector
            vD_dot_dP = alignZero(vD.dotProduct(deltaP));
            // Get the dot product between deltaP and the tube axis direction vector
            dP_dot_vC = alignZero(deltaP.dotProduct(vC));
            // Compute the length of the deltaP vector
            dP_dot_dP = alignZero(deltaP.dotProduct(deltaP));
        }

        // Compute the squared equation coefficients  At^2 + Bt + C = 0 (A already computed above)
        // using mathematical identities helps avoid constructing unnecessary vectors
        // b represents the absolute value of the direction in which the ray moves in relative to the tube axis
        // Or in other words, the distance between a point P on the ray and the tube axis as we move P along the ray
        double b = alignZero(2 * (vD_dot_dP - vD_dot_vC * dP_dot_vC));
        // c represents the squared distance between the ray origin and the closest point on the tube axis
        // after subtracting the tube radius squared
        double c = alignZero(dP_dot_dP - dP_dot_vC * dP_dot_vC - this._radiusSquared);

        // Solve the equation
        double discriminant = alignZero(b * b - 4 * a * c);

        // Case no intersection / single tangent point
        if (discriminant <= 0) {
            return null;
        }

        // Get the equation roots
        double sqrtDelta = Math.sqrt(discriminant);
        double t1 = alignZero((-b - sqrtDelta) / (2 * a));
        double t2 = alignZero((-b + sqrtDelta) / (2 * a));

        // Return all intersection points where t > 0 in order
        if ((t1 <= 0 && t2 <= 0) ||
                (t1 >= maxDistance && t2 >= maxDistance) ||
                (t1 < 0 && t2 > maxDistance)) {
            return null;
        }

        final Point potential1 = ray.getPoint(t1);
        final Point potential2 = ray.getPoint(t2);

        final boolean validP1Dist = alignZero(t1 - maxDistance) <= 0;
        final boolean validP2Dist = alignZero(t2 - maxDistance) <= 0;

        // Since t2 > t1, This test suffices for both
        if (t1 > 0) {
            if (validP1Dist && validP2Dist) {
                return List.of(
                        new Intersection(this, potential1),
                        new Intersection(this, potential2)
                );
            }
            return validP1Dist ?
                    List.of(new Intersection(this, potential1)) :
                    List.of(new Intersection(this, potential2));
        }

        return t2 > 0 ? List.of(new Intersection(this, potential2)) : null;
    }

    @Override
    public Vector getNormal(Point point) {
        double t = _axis.getDirection().dotProduct(point.subtract(_axis.getOrigin()));
        Point projectionPoint = isZero(t) ? _axis.getOrigin() : _axis.getOrigin().add(_axis.getDirection().scale(t));
        Vector normal = point.subtract(projectionPoint);
        return normal.normalize();
    }

    @Override
    public String toString() {
        return "Tube: Radius: " + _radius + " Axis: " + _axis;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tube t = (Tube) obj;
        return isZero(_radius - t._radius) && _axis.equals(t._axis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _axis);
    }
}