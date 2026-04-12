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
    private final Point p0;
    /**
     * {@link Vector} representing the direction in which the camera is pointing
     */
    private final Vector vTo;
    /**
     * {@link Vector} representing the upward direction relative to the camera
     */
    private final Vector vUp;
    /**
     * {@link Vector} representing the right hand side direction relative to the camera
     */
    private final Vector vRight;

    private Camera() {
    }
}
