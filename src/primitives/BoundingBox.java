package primitives;

import static primitives.Util.isZero;

/**
 * Immutable axis-aligned bounding box (AABB).
 * <p>
 * Used as a cheap conservative pre-check before exact ray/geometry intersection
 * math: if a ray misses a geometry's bounding box, it necessarily misses the
 * geometry itself, so the expensive exact test can be skipped. A bounding box
 * may be a loose over-approximation of the geometry it bounds (e.g. for a
 * tilted cylinder), but must never be tighter than the geometry's true extent.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class BoundingBox {
    /** Minimum x/y/z coordinates of the box. */
    private final double _minX, _minY, _minZ;
    /** Maximum x/y/z coordinates of the box. */
    private final double _maxX, _maxY, _maxZ;

    /**
     * Constructs a bounding box from explicit min/max coordinates.
     *
     * @param minX minimum x coordinate
     * @param minY minimum y coordinate
     * @param minZ minimum z coordinate
     * @param maxX maximum x coordinate
     * @param maxY maximum y coordinate
     * @param maxZ maximum z coordinate
     */
    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        _minX = minX;
        _minY = minY;
        _minZ = minZ;
        _maxX = maxX;
        _maxY = maxY;
        _maxZ = maxZ;
    }

    /**
     * Builds the smallest axis-aligned bounding box containing all the given points.
     *
     * @param points the points to bound; must contain at least one point
     * @return the bounding box of the given points
     */
    public static BoundingBox of(Point... points) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (Point p : points) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }

        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Combines this bounding box with another into the smallest box containing both.
     *
     * @param other the other bounding box
     * @return the union bounding box
     */
    public BoundingBox union(BoundingBox other) {
        return new BoundingBox(
                Math.min(_minX, other._minX), Math.min(_minY, other._minY), Math.min(_minZ, other._minZ),
                Math.max(_maxX, other._maxX), Math.max(_maxY, other._maxY), Math.max(_maxZ, other._maxZ));
    }

    /**
     * Tests whether a ray intersects this bounding box within a given distance, using the
     * slab method.
     *
     * @param ray         the ray to test
     * @param maxDistance the maximum distance from the ray origin to consider
     * @return {@code true} if the ray may intersect the box (a conservative test - false
     *         positives are possible near the box's boundary, but false negatives are not)
     */
    public boolean intersectsRay(Ray ray, double maxDistance) {
        Point origin = ray.getOrigin();
        Vector dir = ray.getDirection();

        double tMin = 0.0;
        double tMax = maxDistance;

        // X slab
        if (isZero(dir.getX())) {
            if (origin.getX() < _minX || origin.getX() > _maxX) return false;
        } else {
            double t0 = (_minX - origin.getX()) / dir.getX();
            double t1 = (_maxX - origin.getX()) / dir.getX();
            if (t0 > t1) { double tmp = t0; t0 = t1; t1 = tmp; }
            tMin = Math.max(tMin, t0);
            tMax = Math.min(tMax, t1);
            if (tMax < tMin) return false;
        }

        // Y slab
        if (isZero(dir.getY())) {
            if (origin.getY() < _minY || origin.getY() > _maxY) return false;
        } else {
            double t0 = (_minY - origin.getY()) / dir.getY();
            double t1 = (_maxY - origin.getY()) / dir.getY();
            if (t0 > t1) { double tmp = t0; t0 = t1; t1 = tmp; }
            tMin = Math.max(tMin, t0);
            tMax = Math.min(tMax, t1);
            if (tMax < tMin) return false;
        }

        // Z slab
        if (isZero(dir.getZ())) {
            if (origin.getZ() < _minZ || origin.getZ() > _maxZ) return false;
        } else {
            double t0 = (_minZ - origin.getZ()) / dir.getZ();
            double t1 = (_maxZ - origin.getZ()) / dir.getZ();
            if (t0 > t1) { double tmp = t0; t0 = t1; t1 = tmp; }
            tMin = Math.max(tMin, t0);
            tMax = Math.min(tMax, t1);
            if (tMax < tMin) return false;
        }

        return true;
    }
}
