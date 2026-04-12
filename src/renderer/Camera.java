package renderer;

import primitives.Point;
import primitives.Vector;

/**
 * Camera class representing a camera in a 3D space
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Camera {
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

    private Camera() {
    }
}
