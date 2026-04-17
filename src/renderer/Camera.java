package renderer;

import java.util.MissingResourceException;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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
    private double _pixelWidth;
    /**
     * Individual pixel height
     */
    private double _pixelHeight;

    /**
     * Empty camera constructor
     */
    private Camera() {
    }

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
         * A {@link Point} which the camera is facing
         */
        private Point _pTarget;

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
//            if (!isZero(vTo.dotProduct(vUp)))
//                throw new IllegalArgumentException("vTo and vUp must be orthogonal");

            _camera._vTo = vTo.normalize();
            _camera._vUp = vUp.normalize();
            this._pTarget = null;

            return this;
        }

        /**
         * Initializes the camera direction vectors based on a given target point and up vector
         *
         * @param pTarget the target point at which the camera is pointing
         * @param vUp     the upward direction vector relative to the camera
         * @return the builder object
         */
        public Builder setDirection(Point pTarget, Vector vUp) {
            this._pTarget = pTarget;
//            final Vector vTo = pTarget.subtract(_camera._p0);
//            if (!isZero(vTo.dotProduct(vUp)))
//                throw new IllegalArgumentException("vTo and vUp must be orthogonal");

//            _camera._vTo = vTo.normalize();
            _camera._vUp = vUp.normalize();
            this._pTarget = null;
//            _camera._vRight = _camera._vUp.crossProduct(_camera._vTo).normalize();

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
//            final Vector vTo = p.subtract(_camera._p0);
            this._pTarget = p;
            final Vector vUp = Vector.AXIS_Y;

            return this;
//            if (!isZero(vTo.dotProduct(vUp)))
//                throw new IllegalArgumentException("Camera direction and the y axis must be orthogonal");

//            _camera._vTo = vTo.normalize();
//            _camera._vUp = vUp;
//            _camera._vRight = vTo.crossProduct(vUp).normalize();
        }

        /**
         * Set the distance between the camera and the view plane
         *
         * @param distance the distance
         * @return the builder objects
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Set the measurements of the view plane
         *
         * @param width  the view plane width
         * @param height the view plane height
         * @return the builder object
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;

            return this;
        }

        /**
         * Set the resolution (number of pixels in the view plane)
         *
         * @param nX number of pixel columns
         * @param nY number of pixel rows
         * @return the builder object
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;

            return this;
        }

        /**
         * Helper function to calculate the camera direction vectors
         *
         */
        private void calcVectors() {
            if (_camera._vTo == null)
                _camera._vTo = this._pTarget.subtract(_camera._p0).normalize();

            _camera._vRight = _camera._vUp.crossProduct(_camera._vTo).normalize();
            _camera._vUp = _camera._vTo.crossProduct(_camera._vRight).normalize();
        }

        private void calcVpCenter() {
            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));

        }

        /**
         * Validate number of pixels in the rows and columns of the view plane
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0)
                throw new IllegalArgumentException("Number of pixels must be positive");
        }

        /**
         * Validate the location and direction of the camera
         *
         * @throws MissingResourceException if p0 is missing
         * @throws IllegalArgumentException if vTo is parallel to vUp
         */
        private void checkLocationAndDirection() {
            if (_camera._p0 == null)
                throw new MissingResourceException("Camera location must be initialized", "Camera", "p0");

            if (_camera._vUp == null)
                throw new MissingResourceException("Camera vUp vector must be initialized", "Camera", "vUp");

            if (_camera._vTo == null && this._pTarget == null)
                throw new MissingResourceException(
                        "Camera vTo vector or pTarget point must be initialized", "Camera", "vTo or pTarget");

            calcVectors();
        }

        /**
         *
         *
         */
        private void checkViewPlane() {
            if (_camera._width < 0)
                throw new IllegalArgumentException("view plane must be positive");
            if (_camera._height < 0)
                throw new IllegalArgumentException("view plane height must be positive");
            if (_camera._distance < 0)
                throw new IllegalArgumentException("distance must be positive");

            calcVpCenter();
            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;
        }

        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();
            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException _) {
                return null;
            }
        }
    }
}


//Camera camera = Camera.getBuilder()
//        .setLocation(...)
//        .setDirection(...)
//    .setVpSize(...)
//    .setVpDistance(...)
//    .setResolution(...)
//    .build();