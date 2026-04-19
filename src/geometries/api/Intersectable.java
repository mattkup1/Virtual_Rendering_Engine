package geometries.api;

import java.util.List;
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
    public abstract List<Point> findIntersections(Ray ray);
}
