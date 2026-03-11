package primitives;

/**
 * Immutable representation of a point in a 3D Cartesian coordinate system.
 * <p>
 * A {@code Point} represents a location in space defined by three coordinates.
 * This class also serves as the base class for {@link Vector}.
 * </p>
 *
 * @author mattkuperwasser
 */
public class Point {

    /**
     * The coordinates of the point.
     */
    protected final Double3 _xyz;

    /**
     * Constant representation of the origin (0,0,0).
     */
    protected static final Double3 ZERO = Double3.ZERO;

    /**
     * Constructs a point with the given x, y and z coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public Point(double x, double y, double z) {
        this._xyz = new Double3(x, y, z);
    }

    /**
     * Constructs a point from a {@link Double3} object representing
     * the three coordinates.
     *
     * @param xyz the coordinate triad representing (x,y,z)
     */
    public Point(Double3 xyz) {
        this._xyz = xyz;
    }

    @Override
    public String toString() { return "" + _xyz; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return _xyz.equals(((Point) obj)._xyz);
    }

    @Override
    public int hashCode() {
        return _xyz.hashCode();
    }

    /**
     * Computes the vector from the given point to this point.
     *
     * @param other the starting point (tail of the resulting vector)
     * @return a {@link Vector} pointing from {@code other} to this point
     */
    public Vector subtract(Point other) {
        return new Vector(_xyz.subtract(other._xyz));
    }

    /**
     * Adds a vector to this point.
     * <p>
     * The result is a new point translated by the vector.
     * </p>
     *
     * @param vector the vector to add
     * @return a new {@code Point} translated by the given vector
     */
    public Point add(Vector vector) {
        return new Point(_xyz.add(vector._xyz));
    }

    /**
     * Computes the squared distance between this point and another point.
     * <p>
     * This method avoids the square root calculation and is therefore
     * more efficient when only relative distances are required.
     * </p>
     *
     * @param other the point to compute the distance to
     * @return the squared distance between the two points
     */
    public double distanceSquared(Point other) {
        // Store the point representing the vector from other to this
        Double3 diff = _xyz.subtract(other._xyz);
        // Compute the squared distance between the 2 points
        return diff._d1() * diff._d1()
                + diff._d2() * diff._d2()
                + diff._d3() * diff._d3();
    }

    /**
     * Computes the Euclidean distance between this point and another point.
     *
     * @param other the point to compute the distance to
     * @return the distance between the two points
     */
    public double distance(Point other) {
        return Math.sqrt(distanceSquared(other));
    }
}