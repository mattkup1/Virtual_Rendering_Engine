package geometries.api;

import primitives.Color;
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
    public Geometry() { /* To satisfy Javadoc generator */ }

    /**
     * The geometry's emission color
     */
    private Color _emission = Color.BLACK;

    /**
     * Gets the geometry's emission color
     *
     * @return the geometry's emission color
     */
    public Color getEmission() {
        return _emission;
    }

    /**
     * Sets the geometry's emission color and returns the geometry to allow chained method calls
     *
     * @param emission the emission color
     * @return the geometry
     */
    public Geometry setEmission(Color emission) {
        _emission = emission;
        return this;
    }

    /**
     * Abstract method to get the geometry's Normal vector
     * Every geometry must implement this method
     *
     * @param point point representing the direction relative to the origin
     * @return the geometry's normal vector
     */
    public abstract Vector getNormal(Point point);
}