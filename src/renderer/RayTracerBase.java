package renderer;

import geometries.api.Intersectable;
import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;

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

    /**
     * Prepares intersection data by calculating geometric properties needed for shading.
     * <p>
     * This method initializes the ray direction vector, the surface normal at the point,
     * and the dot product between them.
     * </p>
     *
     * @param intersection the intersection point data to populate
     * @param v            the direction vector of the ray
     * @return {@code true} if the ray is not orthogonal to the surface normal, {@code false} otherwise
     */
    protected boolean preprocessIntersection(Intersectable.Intersection intersection, Vector v) {
        intersection.v = v;
        intersection.normal = intersection.geometry.getNormal(intersection.point).normalize();
        intersection.vNormal = alignZero(intersection.v.dotProduct(intersection.normal));
        return intersection.vNormal != 0;
    }

    /**
     * Prepares lighting data for a specific light source at an intersection point.
     * <p>
     * This method calculates the light direction vector and the dot product between the
     * light direction and the surface normal. It ensures the light and camera are on
     * the same side of the surface.
     * </p>
     *
     * @param intersection the intersection point data to populate
     * @param light        the light source to process
     * @return {@code true} if the light source contributes to the shading at this point,
     * {@code false} if the light is on the opposite side of the surface
     */
    protected boolean preprocessLightSource(Intersectable.Intersection intersection, LightSource light) {
        intersection.light = light;
        intersection.l = light.getL(intersection.point);
        intersection.lNormal = alignZero(intersection.normal.dotProduct(intersection.l));
        return intersection.lNormal * intersection.vNormal > 0;
    }
}