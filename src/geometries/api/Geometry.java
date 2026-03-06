package geometries.api;

import primitives.*;

/**
 * Abstract class representing 2D Geometric shapes and operations
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
abstract public class Geometry {
    /**
     * Abstract method to get the geometry's Normal vector
     * Every geometry must implement this method
     *
     * @param point point representing the direction relative to the origin
     * @return the geometry's normal vector
     */
    abstract public Vector getNormal(Point point);
}