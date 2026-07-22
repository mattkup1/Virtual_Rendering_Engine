package geometries.impl;

import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import primitives.BoundingBox;
import primitives.Point;
import primitives.Ray;

import static primitives.Util.alignZero;

/**
 * Constructive Solid Geometry: combines two operands via a boolean set operation
 * (union, intersection, or difference) into a single new solid.
 * <p>
 * <b>Operands must be closed, solid shapes</b> - {@link Sphere}, {@link Box},
 * {@link Cylinder}, {@link Cone}, {@link Torus}, {@link Ellipsoid}, or another
 * {@code CSG} - whose intersections with any ray alternate entering and exiting the
 * solid's interior in ascending order. Open/unbounded shapes ({@link Plane},
 * {@link Triangle}, {@link Polygon}, {@link Ellipse}, an infinite {@link Tube}) have no
 * well-defined "interior" in this sense and are not supported.
 * </p>
 * <p>
 * <b>Rays must originate outside both operands.</b> A ray that starts inside an operand
 * only reports that operand's exit crossing (entries behind the ray origin are never
 * returned by any shape's intersection math), which this class's entry/exit pairing
 * would misread as an entry. Primary camera rays satisfy this by construction; shadow
 * and reflection/refraction rays do too as long as they don't originate from a surface
 * that is itself inside one of the two operands.
 * </p>
 * <p>
 * A resulting surface point keeps the {@link geometries.api.Geometry} (and therefore the
 * material) of whichever operand's surface actually produced it - e.g. subtracting a blue
 * sphere out of a red one shows blue on the inside wall of the resulting cavity - rather
 * than every point being forced to share one uniform "CSG material". This happens for
 * free: {@code calcIntersectionsHelper} only ever selects among the {@link Intersection}
 * objects the operands themselves already produced, never constructing new ones.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class CSG extends Intersectable {

    /**
     * The boolean set operation a {@link CSG} combines its two operands with.
     */
    public enum Operation {
        /** Points inside either operand. */
        UNION,
        /** Points inside both operands. */
        INTERSECTION,
        /** Points inside the left operand but not the right. */
        DIFFERENCE
    }

    /**
     * A boundary crossing (ray-parameter-ordered) contributed by one of the two operands.
     *
     * @param intersection the operand's own intersection at this crossing
     * @param distance     distance from the ray origin, used for merge-ordering the two
     *                     operands' boundaries together
     * @param fromLeft     {@code true} if this boundary came from the left operand,
     *                     {@code false} if from the right
     * @param isEntry      {@code true} if the ray is entering the operand's solid at this
     *                     boundary, {@code false} if exiting
     */
    private record Boundary(Intersection intersection, double distance, boolean fromLeft, boolean isEntry) {
    }

    /**
     * The left operand
     */
    private final Intersectable _left;
    /**
     * The right operand
     */
    private final Intersectable _right;
    /**
     * The boolean operation combining the two operands
     */
    private final Operation _operation;

    /**
     * Constructs a CSG solid combining two operands with a boolean set operation.
     *
     * @param left      the left operand
     * @param operation the boolean operation
     * @param right     the right operand
     */
    public CSG(Intersectable left, Operation operation, Intersectable right) {
        _left = left;
        _operation = operation;
        _right = right;
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Boundary> boundaries = new ArrayList<>();
        addBoundaries(_left, ray, true, boundaries);
        addBoundaries(_right, ray, false, boundaries);
        if (boundaries.isEmpty()) return null;

        boundaries.sort(Comparator.comparingDouble(Boundary::distance));

        boolean insideLeft = false, insideRight = false;
        List<Intersection> result = null;

        for (Boundary boundary : boundaries) {
            boolean wasInsideResult = combine(insideLeft, insideRight);
            if (boundary.fromLeft()) insideLeft = boundary.isEntry();
            else insideRight = boundary.isEntry();
            boolean isInsideResult = combine(insideLeft, insideRight);

            // The result's own surface is exactly where its "inside the combined solid"
            // state flips - regardless of which operand's crossing triggered the flip.
            if (wasInsideResult != isInsideResult && alignZero(boundary.distance() - maxDistance) <= 0) {
                if (result == null) result = new ArrayList<>();
                result.add(boundary.intersection());
            }
        }

        return result;
    }

    /**
     * Evaluates this CSG's boolean operation given whether the ray is currently inside
     * each operand.
     *
     * @param insideLeft  whether the ray is currently inside the left operand
     * @param insideRight whether the ray is currently inside the right operand
     * @return whether the ray is currently inside the combined solid
     */
    private boolean combine(boolean insideLeft, boolean insideRight) {
        return switch (_operation) {
            case UNION -> insideLeft || insideRight;
            case INTERSECTION -> insideLeft && insideRight;
            case DIFFERENCE -> insideLeft && !insideRight;
        };
    }

    /**
     * Computes an operand's boundary crossings along a ray and appends them, tagged with
     * their side and entry/exit role, to {@code out}.
     * <p>
     * Since every shape's own {@code calcIntersectionsHelper} already excludes crossings
     * behind the ray origin, an operand's intersections - sorted by distance - simply
     * alternate entry, exit, entry, exit, ... from index 0 (see the class-level ray-origin
     * caveat for when this assumption doesn't hold).
     * </p>
     *
     * @param operand the operand to compute boundaries for
     * @param ray     the ray
     * @param fromLeft {@code true} if {@code operand} is the left operand
     * @param out     the list to append this operand's boundaries to
     */
    private static void addBoundaries(Intersectable operand, Ray ray, boolean fromLeft, List<Boundary> out) {
        List<Intersection> hits = operand.calcIntersections(ray);
        if (hits == null) return;

        Point origin = ray.getOrigin();
        List<Intersection> sorted = new ArrayList<>(hits);
        sorted.sort(Comparator.comparingDouble(i -> i.point.distanceSquared(origin)));

        for (int i = 0; i < sorted.size(); i++) {
            Intersection intersection = sorted.get(i);
            out.add(new Boundary(intersection, origin.distance(intersection.point), fromLeft, i % 2 == 0));
        }
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox leftBox = _left.getBoundingBox();
        BoundingBox rightBox = _right.getBoundingBox();

        return switch (_operation) {
            // Result may span either operand.
            case UNION -> (leftBox == null || rightBox == null) ? null : leftBox.union(rightBox);
            // Result is a subset of both operands - either box is a safe (if not maximally
            // tight) conservative bound.
            case INTERSECTION -> leftBox != null ? leftBox : rightBox;
            // Result is always a subset of the left operand alone.
            case DIFFERENCE -> leftBox;
        };
    }
}
