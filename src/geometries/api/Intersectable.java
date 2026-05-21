package geometries.api;

import java.util.List;
import lighting.LightSource;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Abstract class representing intersectable bodies in 3D space
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public abstract class Intersectable {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    public Intersectable() { /* To satisfy Javadoc generator */ }

    /**
     * Get all intersection points between a ray and the geometric shape
     *
     * @param ray the intersecting ray
     * @return the list of intersection points between the ray and the geometric shape
     */
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null ? null
                : intersections.stream()
                .map(intersection -> intersection.point)
                .toList();
    }

    /**
     * Computes all intersections between a ray and this intersectable.
     * <p>
     * Equivalent to {@link #calcIntersections(Ray, double)} with
     * {@link Double#POSITIVE_INFINITY} as the maximum distance.
     * </p>
     *
     * @param ray the ray
     * @return the list of intersections, or {@code null} if no intersections exist
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersections(ray, Double.POSITIVE_INFINITY);
    }

    /**
     * Computes all intersections between a ray and this intersectable that lie within
     * the given maximum distance from the ray origin.
     *
     * @param ray         the ray
     * @param maxDistance the maximum distance from the ray origin to consider
     * @return the list of intersections within the distance bound,
     *         or {@code null} if no intersections exist
     */
    public final List<Intersection> calcIntersections(Ray ray, double maxDistance) {
        return calcIntersectionsHelper(ray, maxDistance);
    }

    /**
     * Subclass-specific intersection calculation.
     * <p>
     * Implementations must return only intersection points that lie at distance
     * less than or equal to {@code maxDistance} from the ray origin.
     * </p>
     *
     * @param ray         the ray
     * @param maxDistance the maximum distance from the ray origin
     * @return the list of intersections within the distance bound,
     *         or {@code null} if no intersections exist
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance);

    /**
     * Passive Data Structure (PDS) representing an intersection point between a ray and a geometry
     * An intersection consists of the point in space as well as the physical geometry
     */
    public static final class Intersection {

        /**
         * The geometry
         */
        public final Geometry geometry;

        /**
         * The geometry's material
         */
        public final Material material;

        /**
         * The intersection point
         */
        public final Point point;

        /**
         * The normal vector to the geometry at the intersection point
         */
        public Vector normal;

        /**
         * The direction vector of the ray that caused the intersection
         */
        public Vector v;

        /**
         * The dot product of the ray's direction vector (v) and the normal vector
         */
        public double vNormal;

        /**
         * The light source affecting this specific intersection point
         */
        public LightSource light;

        /**
         * The direction vector from the light source to the intersection point
         */
        public Vector l;

        /**
         * The dot product of the light direction vector (l) and the normal vector
         */
        public double lNormal;

        /**
         * Constructs an Intersection object
         *
         * @param geometry the geometry
         * @param point    the intersection point
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
            if (geometry != null) {
                this.material = geometry.getMaterial();
            } else {
                this.material = new Material();
            }
        }

        @Override
        public String toString() {
            return "Intersection: " + geometry + " " + point;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            Intersection other = (Intersection) o;
            return this.geometry.equals(other.geometry) && this.point.equals(other.point);
        }
    }
}
