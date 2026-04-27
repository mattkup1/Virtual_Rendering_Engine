package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

/**
 * A basic implementation of a ray tracer that performs simple ray-geometry
 * intersection tests and returns basic color results.
 * <p>
 * In this initial version, the tracer identifies the closest intersection
 * point and colors it using the scene's ambient light intensity, without
 * accounting for direct light sources, shadows, or reflections.
 * </p>
 * * @author mattkuperwasser
 *
 * @author moshehanau
 */
class SimpleRayTracer extends RayTracerBase {

    /**
     * Constructs a SimpleRayTracer with a given scene.
     *
     * @param scene the scene to be rendered
     */
    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    /**
     * Traces a ray into the scene and calculates the color of the closest
     * intersection point found.
     *
     * @param ray the ray to trace
     * @return the color at the closest intersection point, or the scene's
     * background color if no intersections occur.
     */
    @Override
    public Color traceRay(Ray ray) {
        var intersections = this._scene.geometries.findIntersections(ray);
        if (intersections == null)
            return this._scene.background;

        return calcColor(ray.findClosestPoint(intersections));
    }

    /**
     * Calculates the color at a specific intersection point.
     *
     * @param intersection the point on a geometry surface
     * @return the calculated color intensity
     */
    private Color calcColor(Point intersection) {
        return this._scene.ambientLight.getIntensity();
    }
}