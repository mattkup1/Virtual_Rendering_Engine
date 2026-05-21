package geometries.api;

import primitives.Color;
import primitives.Material;
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
     * Default constructor to satisfy Javadoc generator and other problems in children classes
     */
    public Geometry() { /* To satisfy Javadoc generator */ }

    /**
     * The geometry's emission color
     */
    private Color _emission = Color.BLACK;

    /**
     * The geometry's material
     */
    private Material _material = new Material();


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
     * Material getter
     *
     * @return the geometry's material
     */
    public Material getMaterial() {
        return _material;
    }

    /**
     * Material setter
     * this method returns the geometry to allow chained method calls
     *
     * @param material the material
     * @return the geometry
     */
    public Geometry setMaterial(Material material) {
        _material = material;
        return this;
    }

    /**
     * Returns the geometry's unit normal vector at the given surface point.
     * <p>
     * Implementations are expected to return a normalized vector.
     * </p>
     *
     * @param point a point on the geometry's surface
     * @return the unit normal vector at the point
     */
    public abstract Vector getNormal(Point point);
}