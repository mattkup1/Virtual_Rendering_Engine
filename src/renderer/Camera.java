package renderer;

import primitives.Point;
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
    private final Point _p0;
    /**
     * {@link Vector} representing the direction in which the camera is pointing
     */
    private final Vector _vTo;
    /**
     * {@link Vector} representing the upward direction relative to the camera
     */
    private final Vector _vUp;
    /**
     * {@link Vector} representing the right hand side direction relative to the camera
     */
    private final Vector _vRight;
    /**
     * View plane width
     */
    private final double _width;
    /**
     * View plane height
     */
    private final double _height;
    /**
     * Distance from the camera to the view plane
     */
    private final double _distance;
    /**
     * Number of pixel columns in the view plane
     */
    private final int _nX;
    /**
     * Number of pixel rows in the view plane
     */
    private final int _nY;
    /**
     * {@link Point} representing the center of the view plane
     */
    private final Point _vpCenter;
    /**
     * Individual pixel width
     */
    private final double pixelWidth;
    /**
     * Individual pixel height
     */
    private final double pixelHeight;

    /**
     * Empty camera constructor
     */
    private Camera() {
//        _p0 = Point.ZERO;
//        _vTo = Vector.AXIS_Z;
//        _vUp = Vector.AXIS_Y;
//        _vRight = Vector.AXIS_X;
//        _width = 0;
//        _height = 0;
//        _distance = 0;
//        _nX = 0;
//        _nY = 0;
//        _vpCenter = Point.ZERO;
//        pixelWidth = 0;
//        pixelHeight = 0;
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
     * Camera builder class
     */
    public static class Builder {
        /**
         * Initialize camera object
         */
        private final Camera _camera = new Camera();

        public Builder setLocation(Point p0) {
            return null;
        }

        public Builder setDirection(Vector vTo, Vector vUp) {
            return null;
        }

        public Builder setDirection(Point p, Vector v) {
            return null;
        }

        public Builder setDirection(Point p) {
            return null;
        }

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
