package geometries.impl;

import geometries.api.Geometry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import primitives.BoundingBox;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a torus (donut shape) in a 3D Cartesian coordinate system.
 * <p>
 * The torus is defined by a center point, a unit axis of rotational symmetry, a
 * major radius (distance from the center to the middle of the tube) and a minor
 * radius (the tube's own radius). Ray intersection reduces to a quartic equation
 * in the ray parameter, solved via Ferrari's method (with a Cardano-solved
 * resolvent cubic).
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Torus extends Geometry {
    /**
     * The center point of the torus
     */
    private final Point _center;
    /**
     * The unit axis of rotational symmetry of the torus
     */
    private final Vector _axis;
    /**
     * The major radius: distance from the center to the middle of the tube
     */
    private final double _majorRadius;
    /**
     * The minor radius: the radius of the tube itself
     */
    private final double _minorRadius;

    /**
     * Constructs a torus from a center point, an axis of rotational symmetry, a major
     * radius, and a minor radius.
     *
     * @param center      the center point of the torus
     * @param axis        the axis of rotational symmetry (need not be pre-normalized)
     * @param majorRadius the distance from the center to the middle of the tube
     * @param minorRadius the radius of the tube
     * @throws IllegalArgumentException if either radius is not positive, or if the
     *                                  minor radius is not smaller than the major radius
     *                                  (which would make the torus self-intersecting)
     */
    public Torus(Point center, Vector axis, double majorRadius, double minorRadius) {
        if (majorRadius <= 0 || minorRadius <= 0)
            throw new IllegalArgumentException("Torus radii must be positive");
        if (minorRadius >= majorRadius)
            throw new IllegalArgumentException("Torus minor radius must be smaller than the major radius");

        _center = center;
        _axis = axis.normalize();
        _majorRadius = majorRadius;
        _minorRadius = minorRadius;
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        final Point origin = ray.getOrigin();
        final Vector direction = ray.getDirection();

        // Δ = ray origin relative to the torus center (kept as raw components since it may
        // legitimately be the zero vector, which primitives.Vector does not allow)
        final double deltaX = origin.getX() - _center.getX();
        final double deltaY = origin.getY() - _center.getY();
        final double deltaZ = origin.getZ() - _center.getZ();

        final double deltaDotAxis = deltaX * _axis.getX() + deltaY * _axis.getY() + deltaZ * _axis.getZ();
        final double dirDotAxis = direction.dotProduct(_axis);
        final double deltaDotDir = deltaX * direction.getX() + deltaY * direction.getY() + deltaZ * direction.getZ();
        final double deltaSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

        final double bigR2 = _majorRadius * _majorRadius;
        final double smallR2 = _minorRadius * _minorRadius;
        final double k = bigR2 - smallR2;

        // f(t) = t^2 + b1*t + b0  (b1,b0 as in the class-level derivation)
        final double b1 = 2 * deltaDotDir;
        final double b0 = deltaSquared + k;

        // ρ²(t) = p2*t^2 + p1*t + p0
        final double p2 = 1 - dirDotAxis * dirDotAxis;
        final double p1 = 2 * (deltaDotDir - deltaDotAxis * dirDotAxis);
        final double p0 = deltaSquared - deltaDotAxis * deltaDotAxis;

        final double fourBigR2 = 4 * bigR2;
        final double a4 = 1;
        final double a3 = 2 * b1;
        final double a2 = b1 * b1 + 2 * b0 - fourBigR2 * p2;
        final double a1 = 2 * b1 * b0 - fourBigR2 * p1;
        final double a0 = b0 * b0 - fourBigR2 * p0;

        final double[] roots = solveQuartic(a4, a3, a2, a1, a0);

        List<Intersection> intersections = null;
        for (double t : roots) {
            if (t <= 0) continue;
            if (alignZero(t - maxDistance) > 0) continue;
            if (intersections == null) intersections = new ArrayList<>();
            intersections.add(new Intersection(this, ray.getPoint(t)));
        }

        if (intersections == null) return null;
        if (intersections.size() > 1) {
            intersections.sort((i1, i2) ->
                    Double.compare(i1.point.distanceSquared(origin), i2.point.distanceSquared(origin)));
        }
        return intersections;
    }

    @Override
    public Vector getNormal(Point point) {
        // Gradient of the implicit torus function F(P) = (|CP|^2 + R^2 - r^2)^2 - 4R^2(|CP|^2 - z^2),
        // where z = CP . axis, simplifies to: CP*(|CP|^2 - R^2 - r^2) + 2R^2 * z * axis
        final Vector cp = point.subtract(_center);
        final double s = cp.dotProduct(cp);
        final double z = cp.dotProduct(_axis);
        final double bigR2 = _majorRadius * _majorRadius;

        final double cpFactor = s - bigR2 - _minorRadius * _minorRadius;
        final double axisFactor = 2 * bigR2 * z;

        Vector normal = isZero(cpFactor) ? null : cp.scale(cpFactor);
        if (!isZero(axisFactor)) {
            final Vector axisTerm = _axis.scale(axisFactor);
            normal = normal == null ? axisTerm : normal.add(axisTerm);
        }
        // Both terms vanishing simultaneously can't happen for a point exactly on a
        // non-degenerate torus's surface, but numerical residue from the quartic solver
        // could nudge a point just barely off it - fall back to a safe non-zero direction.
        return normal == null ? _axis : normal.normalize();
    }

    @Override
    public BoundingBox getBoundingBox() {
        // Every point on the torus is within (majorRadius + minorRadius) of the center,
        // regardless of the torus's orientation - a simple, safe conservative bound.
        final double extent = _majorRadius + _minorRadius;
        return new BoundingBox(
                _center.getX() - extent, _center.getY() - extent, _center.getZ() - extent,
                _center.getX() + extent, _center.getY() + extent, _center.getZ() + extent);
    }

    @Override
    public String toString() {
        return "Torus: Center: " + _center + ", Axis: " + _axis
                + ", MajorRadius: " + _majorRadius + ", MinorRadius: " + _minorRadius;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Torus other = (Torus) obj;
        return _center.equals(other._center) && _axis.equals(other._axis)
                && Util.isZero(_majorRadius - other._majorRadius)
                && Util.isZero(_minorRadius - other._minorRadius);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_center, _axis, _majorRadius, _minorRadius);
    }

    // ===================== Quartic/cubic/quadratic solvers (Ferrari/Cardano) =====================

    /**
     * Solves a quadratic {@code a*x^2 + b*x + c = 0} for its real roots.
     *
     * @param a quadratic coefficient (must be non-zero)
     * @param b linear coefficient
     * @param c constant coefficient
     * @return the real roots (0, 1, or 2 of them)
     */
    private static double[] solveQuadratic(double a, double b, double c) {
        final double p = b / (2 * a);
        final double q = c / a;
        final double d = p * p - q;

        if (isZero(d)) return new double[]{-p};
        if (d < 0) return new double[0];

        final double sqrtD = Math.sqrt(d);
        return new double[]{sqrtD - p, -sqrtD - p};
    }

    /**
     * Solves a cubic {@code a*x^3 + b*x^2 + c*x + d = 0} for its real roots, via Cardano's formula.
     *
     * @param a cubic coefficient (must be non-zero)
     * @param b quadratic coefficient
     * @param c linear coefficient
     * @param d constant coefficient
     * @return the real roots (1, 2, or 3 of them - a cubic always has at least one)
     */
    private static double[] solveCubic(double a, double b, double c, double d) {
        final double A = b / a, B = c / a, C = d / a;
        final double sqA = A * A;
        final double p = (1.0 / 3) * (-(1.0 / 3) * sqA + B);
        final double q = 0.5 * ((2.0 / 27) * A * sqA - (1.0 / 3) * A * B + C);
        final double cubedP = p * p * p;
        final double disc = q * q + cubedP;

        double[] roots;
        if (isZero(disc)) {
            if (isZero(q)) {
                roots = new double[]{0};
            } else {
                final double u = Math.cbrt(-q);
                roots = new double[]{2 * u, -u};
            }
        } else if (disc < 0) {
            final double phi = (1.0 / 3) * Math.acos(-q / Math.sqrt(-cubedP));
            final double t = 2 * Math.sqrt(-p);
            roots = new double[]{
                    t * Math.cos(phi),
                    -t * Math.cos(phi + Math.PI / 3),
                    -t * Math.cos(phi - Math.PI / 3)};
        } else {
            final double sqrtDisc = Math.sqrt(disc);
            final double u = Math.cbrt(sqrtDisc - q);
            final double v = -Math.cbrt(sqrtDisc + q);
            roots = new double[]{u + v};
        }

        final double sub = A / 3.0;
        for (int i = 0; i < roots.length; ++i) roots[i] -= sub;
        return roots;
    }

    /**
     * Solves a quartic {@code a4*x^4 + a3*x^3 + a2*x^2 + a1*x + a0 = 0} for its real roots,
     * via Ferrari's method (reduction to a depressed quartic and a resolvent cubic).
     *
     * @param a4 quartic coefficient (must be non-zero)
     * @param a3 cubic coefficient
     * @param a2 quadratic coefficient
     * @param a1 linear coefficient
     * @param a0 constant coefficient
     * @return the real roots (0 to 4 of them)
     */
    private static double[] solveQuartic(double a4, double a3, double a2, double a1, double a0) {
        final double A = a3 / a4, B = a2 / a4, C = a1 / a4, D = a0 / a4;
        final double sqA = A * A;
        final double p = -3.0 / 8 * sqA + B;
        final double q = 1.0 / 8 * sqA * A - 0.5 * A * B + C;
        final double r = -3.0 / 256 * sqA * sqA + 1.0 / 16 * sqA * B - 0.25 * A * C + D;

        double[] roots;
        if (isZero(r)) {
            // y*(y^3 + p*y + q) = 0
            final double[] cubicRoots = solveCubic(1, 0, p, q);
            roots = new double[cubicRoots.length + 1];
            System.arraycopy(cubicRoots, 0, roots, 0, cubicRoots.length);
            roots[cubicRoots.length] = 0;
        } else {
            // Resolvent cubic: z^3 - (p/2)*z^2 - r*z + (r*p/2 - q^2/8) = 0
            final double[] cubicRoots = solveCubic(1, -0.5 * p, -r, 0.5 * r * p - 0.125 * q * q);
            final double z = cubicRoots[0];

            double u = z * z - r;
            double v = 2 * z - p;
            if (isZero(u)) u = 0;
            else if (u > 0) u = Math.sqrt(u);
            else return new double[0];
            if (isZero(v)) v = 0;
            else if (v > 0) v = Math.sqrt(v);
            else return new double[0];

            final double signedV = q < 0 ? -v : v;
            final double[] roots1 = solveQuadratic(1, signedV, z - u);
            final double[] roots2 = solveQuadratic(1, -signedV, z + u);
            roots = new double[roots1.length + roots2.length];
            System.arraycopy(roots1, 0, roots, 0, roots1.length);
            System.arraycopy(roots2, 0, roots, roots1.length, roots2.length);
        }

        final double sub = A / 4.0;
        for (int i = 0; i < roots.length; ++i) roots[i] -= sub;
        return roots;
    }
}
