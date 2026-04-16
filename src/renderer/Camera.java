package renderer;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Camera class representing a camera in a 3D space
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Camera implements Cloneable {
    /**
     * {@link Point} representing the camera location
     */
    private Point _p0;
    /**
     * {@link Vector} representing the direction in which the camera is pointing
     */
    private Vector _vTo;
    /**
     * {@link Vector} representing the upward direction relative to the camera
     */
    private Vector _vUp;
    /**
     * {@link Vector} representing the right hand side direction relative to the camera
     */
    private Vector _vRight;
    /**
     * View plane width
     */
    private double _width;
    /**
     * View plane height
     */
    private double _height;
    /**
     * Distance from the camera to the view plane
     */
    private double _distance;
    /**
     * Number of pixel columns in the view plane
     */
    private int _nX = 1;
    /**
     * Number of pixel rows in the view plane
     */
    private int _nY = 1;
    /**
     * {@link Point} representing the center of the view plane
     */
    private Point _vpCenter;
    /**
     * Individual pixel width
     */
    private double pixelWidth;
    /**
     * Individual pixel height
     */
    private double pixelHeight;

    /**
     * Empty camera constructor
     */
    private Camera() {}

    /**
     * Camera builder getter
     *
     * @return the camera builder object
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    public Ray constructRay(int xIndex, int yIndex) {
        return null;
    }

    /**
     * Camera builder class
     */
    public static class Builder {
        /**
         * Initialize camera object
         */
        private final Camera _camera = new Camera();

        /**
         * Set the camera location
         *
         * @param p0 the camera location
         * @return the builder object
         */
        public Builder setLocation(Point p0) {
            _camera._p0 = p0;
            return this;
        }

        /**
         * Set the camera direction
         *
         * @param vTo the direction in which the camera is pointing
         * @param vUp the upward direction relative to the camera
         * @return the builder object
         * @throws IllegalArgumentException if the vectors are not orthogonal
         */
        public Builder setDirection(Vector vTo, Vector vUp) {
            if (!isZero(vTo.dotProduct(vUp)))
                throw new IllegalArgumentException("vTo and vUp must be orthogonal");

            _camera._vTo = vTo;
            _camera._vUp = vUp;

            return this;
        }

        /**
         * Initializes the camera direction vectors based on a a given target point and up vector
         *
         * @param pTarget the target point at which the camera is pointing
         * @param vUp     the upward direction vector relative to the camera
         * @return the builder object
         */
        public Builder setDirection(Point pTarget, Vector vUp) {
            final Vector vTo = pTarget.subtract(_camera._p0);
            if (!isZero(vTo.dotProduct(vUp)))
                throw new IllegalArgumentException("vTo and vUp must be orthogonal");

            _camera._vTo = vTo.normalize();
            _camera._vUp = vUp.normalize();
            _camera._vRight = _camera._vUp.crossProduct(_camera._vTo).normalize();

            return this;
        }

        /**
         * Sets the camera direction based on a single point
         * The upward direction is assumed to be the y axis
         *
         * @param p the point at which the camera is pointing
         * @return the builder object
         * @throws IllegalArgumentException if the camera direction and the y axis are not orthogonal
         */
        public Builder setDirection(Point p) {
            final Vector vTo = p.subtract(_camera._p0);
            final Vector vUp = Vector.AXIS_Y
            if (!isZero(vTo.dotProduct(vUp)))
                throw new IllegalArgumentException("Camera direction and the y axis must be orthogonal");

            _camera._vTo = vTo.normalize();
            _camera._vUp = vUp;
            _camera._vRight = vTo.crossProduct(vUp).normalize();
        }

        /**
         * Set the distance between the camera and the view plane
         *
         * @param distance the distance
         * @return the builder objects
         */
        public Builder setVpDistance(double distance) {
            return null;
        }

        public Builder setVpSize(double d1, double d2) {
            return null;
        }

        public Builder setResolution(int i1, int i2) {
            return null;
        }

        private Builder calcVectors() {
            return null;
        }

        private Builder calcVpCenter() {
            return null;
        }

        private Builder checkAndSetResolution() {
            return null;
        }

        private Builder checkAndSetOrientation() {
            return null;
        }

        private Builder checkAndSetViewPlane() {
            return null;
        }

        public Camera build() {
            return _camera;
        }
    }
}
