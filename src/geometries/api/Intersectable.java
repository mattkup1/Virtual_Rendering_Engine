package geometries.api;

import java.util.List;
import primitives.Material;
import primitives.Point;
import primitives.Ray;

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
     * Helper function to calculate the intersections between a ray and an intersectable
     *
     * @param ray the ray
     * @return the list of intersections or null if no intersections
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray);

    /**
     * Computes the intersections between a ray and an intersectable
     *
     * @param ray the ray
     * @return the list of intersections or null if no intersections
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersectionsHelper(ray);
    }

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
