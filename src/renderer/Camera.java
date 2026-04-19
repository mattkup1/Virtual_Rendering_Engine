package renderer;

import java.util.MissingResourceException;
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

    /**
     * Construct a {@link Ray} from the camera to a given pixel in the view plane
     *
     * @param xIndex the pixel column number (zero indexed)
     * @param yIndex the pixel row number (zero indexed)
     * @return the ray
     */
    public Ray constructRay(int xIndex, int yIndex) {
        final double xJ = (xIndex - (_nX - 1) / 2.0) * _pixelWidth;
        final double yI = -(yIndex - (_nY - 1) / 2.0) * _pixelHeight;

        if (isZero(xJ) && isZero(yI))
            return new Ray(this._p0, this._vTo);

        Point intersectionPoint;

        if (isZero(xJ))
            intersectionPoint = this._vpCenter.add(this._vUp.scale(yI));
        else if (isZero(yI))
            intersectionPoint = this._vpCenter.add(this._vRight.scale(xJ));
        else intersectionPoint = this._vpCenter.add(this._vRight.scale(xJ)).add(this._vUp.scale(yI));

        return new Ray(this._p0, intersectionPoint.subtract(this._p0).normalize());
    }

    /**
     * {@link Camera} builder class
     */
    public static class Builder {

        /**
         * Default constructor to satisfy Javadoc generator
         */
        public Builder() { /* To satisfy Javadoc generator */ }

        /**
         * Initialize {@link Camera} object
         */

        private final Camera _camera = new Camera();
        /**
         * A {@link Point} which the camera is facing
         */
        private Point _pTarget;

        /**
         * Set the {@link Camera} location
         *
         * @param p0 the camera location
         * @return the builder object
         */
        public Builder setLocation(Point p0) {
            _camera._p0 = p0;
            return this;
        }

        /**
         * Set the {@link Camera} direction
         *
         * @param vTo the direction in which the camera is pointing
         * @param vUp the upward direction relative to the camera
         * @return the builder object
         * @throws IllegalArgumentException if the vectors are not orthogonal
         */
        public Builder setDirection(Vector vTo, Vector vUp) {
            _camera._vTo = vTo.normalize();
            _camera._vUp = vUp.normalize();
            this._pTarget = null;

            return this;
        }

        /**
         * Initializes the {@link Camera} direction vectors based on a given target point and up vector
         *
         * @param pTarget the target point at which the camera is pointing
         * @param vUp     the upward direction vector relative to the camera
         * @return the builder object
         */
        public Builder setDirection(Point pTarget, Vector vUp) {
            this._pTarget = pTarget;
            _camera._vUp = vUp.normalize();

            return this;
        }

        /**
         * Sets the {@link Camera} direction based on a single point
         * The upward direction is assumed to be the y-axis
         *
         * @param p the point at which the camera is pointing
         * @return the builder object
         * @throws IllegalArgumentException if the camera direction and the y-axis are not orthogonal
         */
        public Builder setDirection(Point p) {
            this._pTarget = p;
            _camera._vUp = Vector.AXIS_Y;

            return this;
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
         * Rotates the camera around its viewing direction by the given angle in degrees.
         * The rotation updates the up and right vectors while keeping the viewing direction unchanged.
         * Positive angles rotate clockwise around the viewing direction.
         *
         * @param angle the rotation angle in degrees
         * @return the builder object
         * @throws MissingResourceException if the camera direction vectors have not been initialized yet
         */
        public Builder rotate(double angle) {

            // in case the angle is zero then there is no rotate
            if (isZero(angle)) return this;

            if (_camera._vUp == null)
                throw new MissingResourceException("Camera vUp vector must be initialized before rotation",
                        "Camera.Builder", "vUp");

            if (_camera._vTo == null && this._pTarget == null)
                throw new MissingResourceException(
                        "Camera vTo vector or pTarget point must be initialized before rotation",
                        "Camera.Builder", "vTo or pTarget");

            if (_camera._p0 == null)
                throw new MissingResourceException("Camera location must be initialized before rotation",
                        "Camera.Builder", "p0");

            if (_camera._vTo == null)
                _camera._vTo = this._pTarget.subtract(_camera._p0).normalize();

            if (_camera._vRight == null)
                _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();

            final double radians = Math.toRadians(angle);
            final double cos = Math.cos(radians);
            final double sin = Math.sin(radians);

            Vector oldVUp = _camera._vUp;
            Vector oldVRight = _camera._vRight;
            Vector newVUp;

            if (isZero(cos)) {
                newVUp = oldVRight.scale(-sin);
            } else if (isZero(sin)) {
                newVUp = oldVUp.scale(cos);
            } else {
                newVUp = oldVUp.scale(cos).add(oldVRight.scale(-sin));
            }

            _camera._vUp = newVUp.normalize();
            _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();

            return this;
        }

        /**
         * Helper function to calculate the camera direction vectors
         */
        private void calcVectors() {
            if (_camera._vTo == null)
                _camera._vTo = this._pTarget.subtract(_camera._p0).normalize();

            _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();
        }

        /**
         * Helper function to calculate the view plane center point
         */
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
         * Helper function to validate the width and height of the view plane and compute the pixel measurements
         *
         * @throws IllegalArgumentException if the view plane width or height are non-positive or if the distance
         *                                  between the camera and the view plane is non-positive
         */
        private void checkViewPlane() {
            if (_camera._width <= 0)
                throw new IllegalArgumentException("view plane width must be positive");
            if (_camera._height <= 0)
                throw new IllegalArgumentException("view plane height must be positive");
            if (_camera._distance <= 0)
                throw new IllegalArgumentException("distance must be positive");

            calcVpCenter();
            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;
        }

        /**
         * Final camera build method
         *
         * @return the initialized camera object
         * @throws IllegalArgumentException if illegal camera parameters are given
         * @throws MissingResourceException if any crucial camera parameters are missing
         */
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