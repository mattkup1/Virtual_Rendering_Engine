package geometries.api;

import primitives.Point;
import primitives.Vector;

/**
 * Abstract class representing Geometric shapes and operations
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public abstract class Geometry extends Intersectable {
    /**
     * Default constructor to satisfy JavaDoc generator and other problems in children classes
     */
    public Geometry() { /* To satisfy JavaDoc generator */ }

    /**
     * Abstract method to get the geometry's Normal vector
     * Every geometry must implement this method
     *
     * @param point point representing the direction relative to the origin
     * @return the geometry's normal vector
     */
    public abstract Vector getNormal(Point point);
}