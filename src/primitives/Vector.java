package primitives;

/**
 * Immutable representation of a 3D mathematical vector.
 * <p>
 * A vector consists of a magnitude and direction
 * which are essentially represented by a single point
 * <p>
 * the distance between the point and the origin represents the vector's magnitude
 * the direction of the point relative to the origin represents the vector's direction
 * A vector cannot be the zero vector (0,0,0).
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Vector extends Point {
    /**
     * Constant static unit vector in X direction
     */
    public static final Vector AXIS_X = new Vector(1.0, 0.0, 0.0);
    /**
     * Constant static unit vector in Y direction
     */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
    /**
     * Constant static unit vector in Z direction
     */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructs a vector from x, y, z coordinates.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @throws IllegalArgumentException if the vector is the zero vector
     */
    public Vector(double x, double y, double z) {
        super(x, y, z);

        validate();
    }

    /**
     * Constructs a vector from a {@link Double3} object.
     *
     * @param xyz the coordinate container
     * @throws IllegalArgumentException if the vector is the zero vector
     */
    public Vector(Double3 xyz) {
        super(xyz);

        validate();
    }

    /**
     * Validates that the vector is not the zero vector
     *
     * @throws IllegalArgumentException if the vector is the zero vector
     */
    private void validate() {
        if (_xyz.equals(Double3.ZERO))
            throw new IllegalArgumentException("Zero vector is not allowed");
    }

    /**
     * Adds another vector to the current vector.
     *
     * @param other the vector to add
     * @return a new {@link Vector} representing the sum of the two vectors
     * @throws IllegalArgumentException if the result is the zero vector
     */
    public Vector add(Vector other) {
        return new Vector(this._xyz.add(other._xyz));
    }

    /**
     * Scales the vector by a number
     *
     * @param scalar the scalar
     * @return a new {@link Vector} representing this vector scaled by the given number
     * @throws IllegalArgumentException if scalar is zero (result is zero vector)
     */
    public Vector scale(double scalar) {
        return new Vector(this._xyz.scale(scalar));
    }

    /**
     * Computes the dot product between this Vector and another vector
     *
     * @param other the other vector
     * @return the dot product of this vector and other vector
     */
    public double dotProduct(Vector other) {
        Double3 product = this._xyz.product(other._xyz);
        return product._d1() + product._d2() + product._d3();
    }

    /**
     * Computes the cross product between this Vector and another vector
     *
     * @param other the other vector
     * @return a new {@link Vector} representing the cross product
     * @throws IllegalArgumentException if the 2 vectors are parallel (result is zero vector)
     */
    public Vector crossProduct(Vector other) {
        // Store the coordinates of each vector for cleaner computation
        double aX = this._xyz._d1();
        double aY = this._xyz._d2();
        double aZ = this._xyz._d3();
        double bX = other._xyz._d1();
        double bY = other._xyz._d2();
        double bZ = other._xyz._d3();
        // Compute and return the cross product using the algebric formula
        return new Vector(
                (aY * bZ) - (aZ * bY),
                (aZ * bX) - (aX * bZ),
                (aX * bY) - (aY * bX)
        );
    }

    /**
     * Computes the squared length of the vector
     *
     * @return the length of the vector
     */
    public double lengthSquared() {
        return super.distanceSquared(new Point(0, 0, 0));
    }

    /**
     * Compute the length of the vector
     *
     * @return the vector length
     */
    public double length() {
        return super.distance(new Point(0, 0, 0));
    }

    /**
     * Normalizes the vector.
     * <p>
     * Returns a new vector with the same direction but with a length of 1.
     * </p>
     *
     * @return a new {@link Vector} representing the unit vector
     * @throws ArithmeticException if the vector's length is zero (though validate() prevents this)
     */
    public Vector normalize() {
        return new Vector(_xyz.divide(length())); // Assuming 'reduce' or 'divide' scales each component by 1/len
    }

    @Override
    public String toString() {
        return "->" + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return _xyz.equals(((Point) obj)._xyz);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}