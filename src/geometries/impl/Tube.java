package geometries.impl;

import java.util.ArrayList;
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

//    @Override
//    public List<Point> findIntersections(Ray ray) {
//
//        final Point rayOrigin = ray.getOrigin(), tubeOrigin = this._axis.getOrigin();
//        final Vector tubeDirection = this._axis.getDirection(), rayDirection = ray.getDirection();
//
//        // If the ray and tube directions are equal - no intersection points
//        if (tubeDirection.equals(rayDirection)) return null;
//
//        final Vector deltaP = rayOrigin.subtract(tubeOrigin);
//
//        final double rayTubeDotProduct = alignZero(rayDirection.dotProduct(tubeDirection));
//
//        // Case ray direction and tube direction are orthogonal
//        if (isZero(rayTubeDotProduct)) {
//            if (rayOrigin.equals(tubeOrigin)) {
//                return List.of(rayOrigin.add(rayDirection.scale(this._radius)));
//            }
//            // Case ray origin point orthogonal to tube origin point
//            if (isZero(deltaP.dotProduct(tubeDirection))) {
//                // Case ray origin on tube
//                if (isZero(deltaP.length() - this._radius)) {
//                    final Point p = rayOrigin.add(rayDirection.scale(2 * this._radius));
//                    // Case ray direction inwards
//                    return isZero(p.distance(tubeOrigin) - _radius) ? List.of(p) : null;
//                }
//            } else { // Case ray origin not orthogonal to tube origin
//                // Get the projection of deltaP on the tube axis
//                final Point closestPointOnAxis = tubeOrigin.add(deltaP.project(tubeDirection));
//                // Get the vector from the closest point on the axis to the ray origin
//                final Vector rayOriginToClosest = closestPointOnAxis.subtract(rayOrigin);
//                final boolean rayOriginOnTube = isZero(rayOriginToClosest.lengthSquared() - this._radiusSquared);
//
//                // Case ray origin on the tube and not orthogonal to tube origin
//                if (rayOriginOnTube) {
//                    // Get the projection of the rayOriginToClosest vector on the ray
//                    final double scalar = rayOriginToClosest.projectionScalar(rayDirection);
//                    // Case ray tangent to tube
//                    if (isZero(scalar)) return null;
//                    // Get the possible intersection point
//                    final Point p = rayOrigin.add(rayDirection.scale(2 * scalar));
//                    // If the point is on the tube (ray goes inwards) - return the point, Else return null
//                    return isZero(p.distance(closestPointOnAxis) - this._radius) ? List.of(p) : null;
//                } else { // Case ray origin not on tube
//                    final double projectionScalar = rayOriginToClosest.projectionScalar(rayDirection);
//                    // Case ray origin is orthogonal to the closest point on the axis
//                    if (isZero(projectionScalar)) {
//                        // Case ray starts inside the tube
//                        if (rayOrigin.distanceSquared(closestPointOnAxis) < this._radiusSquared) {
//                            final double edgeLength = Math.sqrt(this._radiusSquared - rayOriginToClosest.lengthSquared());
//                            return List.of(rayOrigin.add(rayDirection.scale(edgeLength)));
//                        } else return null; // Case ray starts outside the tube
//                    }
//                }
//            }
//        }
//
//        // Get the vector from the tube origin  to the ray origin
//        if (rayOrigin.equals(tubeOrigin)) {
//
//            // Get the vector from the tube axis to the intersection point that is orthogonal to the tube axis
//            final Vector orthogonal = rayDirection.subtract(tubeDirection.scale(rayTubeDotProduct));
//
//            // Get the scalar by which the ray direction vector needs to be scaled to get to the intersection point
//            final double scalar = rayTubeDotProduct > 0 ? (_radius / orthogonal.length()) : -_radius / orthogonal.length();
//
//            return List.of(rayOrigin.add(rayDirection.scale(scalar)));
//        }
//
//        final Vector vectorA = deltaP.subtract(tubeDirection.scale(deltaP.dotProduct(tubeDirection)));
//        final Vector vectorB = rayDirection.subtract(tubeDirection.scale(rayDirection.dotProduct(tubeDirection)));
//
//        final double a = alignZero(vectorB.lengthSquared());
//        final double b = alignZero(2 * vectorA.dotProduct(vectorB));
//        final double c = alignZero(vectorA.lengthSquared() - this._radiusSquared);
//
//        // Get the discriminant
//        final double discriminant = alignZero(b * b - 4 * a * c);
//
//        // Discriminant < 0: No intersection points
//        if (discriminant < 0) return null;
//
//        // Discriminant = 0: single intersection point
//        if (isZero(discriminant)) {
//            // TODO: assert that the ray origin is inside the tube before returning results,
//            //  otherwise, the ray is tangent to the tube
//            return List.of(rayDirection.scale(-(b / (2 * a))));
//        }
//
//        // Discriminant > 0: 2 intersection points
//        final double t1 = (-b - Math.sqrt(discriminant)) / (2 * a);
//        final double t2 = (-b + Math.sqrt(discriminant)) / (2 * a);
//
//        // if t1 and t2 are less than 0 than the points is on the ray tail
//        // if t1 or t2 are 0 than - the ray origin is on the tube
//        // if one is less than 0 and the other is greater than 0 - 1 intersection point
//
//        // Case no intersection points
//        if ((t1 < 0 || isZero(t1)) && (t2 < 0 || isZero(t2))) return null;
//        // Case 2 intersection points
//        if (t1 > 0 && t2 > 0) {
//            return List.of(ray.getPoint(t1), ray.getPoint(t2));
//        }
//        // Case one intersection
//        return t1 > 0 ? List.of(ray.getPoint(t1)) : List.of(ray.getPoint(t2));
//    }


    @Override
    public List<Point> findIntersections(Ray ray) {
        final Point p0 = ray.getOrigin();
        final Vector d = ray.getDirection();
        final Point c = this._axis.getOrigin();
        final Vector v = this._axis.getDirection();

        // Calculate base scalar data to prevent zero vector creation
        final double dDotV = alignZero(d.dotProduct(v));

        // Check if the ray is parallel to the tube axis (A = 0)
        // If the dot product is 1 or -1, the ray is parallel and will never intersect the tube
        final double a = alignZero(1 - dDotV * dDotV);
        if (isZero(a)) {
            return null;
        }

        // Handle deltaP (the distance vector between the ray origin and the axis origin)
        // If the points are identical, the scalar products of the difference vector are simply 0
        double dDotDeltaP = 0;
        double deltaPDotV = 0;
        double deltaPDotDeltaP = 0;

        if (!p0.equals(c)) {
            Vector deltaP = p0.subtract(c); // Safe from zero vector exception due to the if statement
            dDotDeltaP = alignZero(d.dotProduct(deltaP));
            deltaPDotV = alignZero(deltaP.dotProduct(v));
            deltaPDotDeltaP = alignZero(deltaP.dotProduct(deltaP));
        }

        // Calculate the quadratic equation coefficients: At^2 + Bt + C = 0
        // Using math identities to avoid physically building projection vectors
        final double b = alignZero(2 * (dDotDeltaP - dDotV * deltaPDotV));
        final double cCoeff = alignZero(deltaPDotDeltaP - deltaPDotV * deltaPDotV - _radius * _radius);

        // Solve the quadratic equation
        final double discriminant = alignZero(b * b - 4 * a * cCoeff);

        // Case of no intersection or tangency (tangency is not considered an intersection)
        if (discriminant <= 0) {
            return null;
        }

        final double sqrtDelta = Math.sqrt(discriminant);
        final double t1 = alignZero((-b - sqrtDelta) / (2 * a));
        final double t2 = alignZero((-b + sqrtDelta) / (2 * a));

        // Filter results according to ray requirements (t > 0)
        // Avoid creating empty lists - return List.of only when valid intersections exist
        if (t1 > 0) {
            return List.of(ray.getPoint(t1), ray.getPoint(t2));
        }

        if (t2 > 0) {
            return List.of(ray.getPoint(t2));
        }
        return null;
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