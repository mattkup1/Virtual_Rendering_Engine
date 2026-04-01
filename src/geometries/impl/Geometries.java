package geometries.impl;

import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import primitives.Point;
import primitives.Ray;

/**
 * Representation of a collection of geometric shapes
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Geometries extends Intersectable {
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
     *
     * @param ray the intersecting ray
     * @return the list of intersection points with the ray
     */
    public List<Point> findIntersections(Ray ray) {
        List<Point> intersections = new ArrayList<>();
        for (Intersectable geometry : geometries) {

        }
    }
}
