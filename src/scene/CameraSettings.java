package scene;

import primitives.Point;
import primitives.Vector;

/**
 * Passive Data Structure (PDS) holding the intrinsic camera parameters that can be
 * loaded from a scene source file: where it sits, where it looks, its view-plane
 * geometry, and the output resolution.
 * <p>
 * Execution/session settings such as multithreading or debug-print interval are
 * deliberately not included here - they configure how a render is carried out, not
 * what the scene looks like, so they stay in code at the call site.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class CameraSettings {
    /**
     * The camera's location
     */
    public Point location;
    /**
     * The point the camera is aimed at
     */
    public Point direction;
    /**
     * The camera's upward direction; defaults to the Y axis
     */
    public Vector up = Vector.AXIS_Y;
    /**
     * Distance from the camera to the view plane
     */
    public double vpDistance;
    /**
     * View plane width
     */
    public double vpWidth;
    /**
     * View plane height
     */
    public double vpHeight;
    /**
     * Number of pixel columns in the view plane
     */
    public int resolutionX;
    /**
     * Number of pixel rows in the view plane
     */
    public int resolutionY;

    /**
     * Default constructor to satisfy Javadoc generator
     */
    public CameraSettings() { /* To satisfy Javadoc generator */ }
}
