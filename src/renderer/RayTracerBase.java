package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Abstract base class for all ray tracing engines.
 * <p>
 * This class provides the foundational structure for traversing a scene
 * and determining the color of pixels by tracing rays from the camera
 * into the 3D environment.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
abstract class RayTracerBase {

    /**
     * The scene to be rendered, containing geometries and lighting.
     */
    protected final Scene _scene;

    /**
     * Traces a specific ray into the scene to determine the color at
     * its first intersection point.
     *
     * @param ray the ray to trace through the scene
     * @return the color calculated at the intersection point,
     * or the background color if no intersection is found.
     */
    abstract Color traceRay(Ray ray);

    /**
     * Constructs a RayTracerBase with a reference to the scene it will render.
     *
     * @param scene the scene to be associated with this tracer
     */
    RayTracerBase(Scene scene) {
        _scene = scene;
    }
}