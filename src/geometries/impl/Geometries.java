package geometries.impl;

import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import primitives.BoundingBox;
import primitives.Ray;

import static primitives.Util.alignZero;

/**
 * Representation of a collection of geometric shapes
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Geometries extends Intersectable {

    /**
     * Collection of geometric shapes
     */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Parameterized constructor
     *
     * @param geometries the geometric shapes in the collection
     */
    public Geometries(Intersectable... geometries) {
        this.add(geometries);
    }

    /**
     * Add geometric shapes to the collection
     *
     * @param geometries the geometric shapes to add to the collection
     */
    public void add(Intersectable... geometries) {
        this.geometries.addAll(Arrays.asList(geometries));
    }

    /**
     * Find and return the intersection points with a given ray in a list
     * return only points that are within a given distance from the ray's origin
     *
     * @param ray the intersecting ray
     * @param maxDistance the maximum distance from the ray origin
     *                    which the intersection point must be within
     *                    to be added to the intersections list
     * @return the list of intersection points with the ray
     */
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> intersections = null;
        for (Intersectable geometry : geometries) {
            final BoundingBox box = geometry.getBoundingBox();
            if (box != null && !box.intersectsRay(ray, maxDistance)) {
                continue; // Ray misses this geometry's bounding box - skip the exact math
            }

            final var geometry_intersection = geometry.calcIntersections(ray);
            if (geometry_intersection != null) {
                for (var intersection : geometry_intersection) {
                    if (alignZero(intersection.point.distance(ray.getOrigin())) <= maxDistance) {
                        if (intersections == null) {
                            intersections = new ArrayList<>();
                        }
                        intersections.add(intersection);
                    }
                }
            }
        }
        return intersections; // Null if no intersection points
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox union = null;
        for (Intersectable geometry : geometries) {
            final BoundingBox box = geometry.getBoundingBox();
            if (box == null) return null; // An unbounded child makes the whole group unbounded
            union = union == null ? box : union.union(box);
        }
        return union; // Null if this collection is empty
    }
}
