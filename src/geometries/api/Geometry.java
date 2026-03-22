package geometries.api;

import primitives.Point;
import primitives.Vector;

/**
 * Abstract class representing 2D Geometric shapes and operations
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
abstract public class Geometry {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    Geometry() { /* To satisfy JavaDoc generator */ }

    /**
     * Abstract method to get the geometry's Normal vector
     * Every geometry must implement this method
     *
     * @param point point representing the direction relative to the origin
     * @return the geometry's normal vector
     */
    abstract public Vector getNormal(Point point);
}