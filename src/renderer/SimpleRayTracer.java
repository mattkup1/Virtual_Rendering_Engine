package renderer;

import geometries.api.Intersectable.Intersection;
import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;

/**
 * A basic implementation of a ray tracer that performs simple ray-geometry
 * intersection tests and returns basic color results.
 * <p>
 * In this initial version, the tracer identifies the closest intersection
 * point and colors it using the scene's ambient light intensity, without
 * accounting for direct light sources, shadows, or reflections.
 * </p>
 *
 * @author mattkuperwasser
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
        var intersections = _scene.geometries.calcIntersections(ray);
        return intersections == null
                ? _scene.background
                : calcColor(ray.findClosestIntersection(intersections), ray.getDirection());
    }

    /**
     * Calculates the color at a specific intersection point.
     *
     * @param intersection the point on a geometry surface
     * @return the calculated color intensity
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return !preprocessIntersection(intersection, v) ? Color.BLACK :
                _scene.ambientLight
                .getIntensity()
                .scale(intersection.material.kA)
                .add(calcLocalEffects(intersection));
    }

    /**
     * Calculates the diffuse component of the reflection at an intersection point.
     * <p>
     * Diffuse reflection is modeled using Lambert's cosine law, where intensity
     * is proportional to the cosine of the angle between the light direction
     * and the surface normal.
     * </p>
     *
     * @param intersection the intersection data including light direction and normal
     * @return the scaling factor triad for the diffuse light component
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.lNormal));
    }

    /**
     * Calculates the specular component of the reflection at an intersection point.
     * <p>
     * Specular reflection represents the "shiny" highlights on a surface. It is
     * calculated based on the reflection vector (r) of the light and the
     * direction of the viewer (v).
     * </p>
     *
     * @param intersection the intersection data including material properties and vectors
     * @return the scaling factor triad for the specular light component
     */
    private Double3 calcSpecular(Intersection intersection) {
        // Reflection vector formula: r = l - 2 * (l . n) * n
        final Vector r =
                intersection.l.subtract(intersection.normal.scale(2 * intersection.lNormal));

        double minusVR = alignZero(-intersection.v.dotProduct(r));

        return minusVR <= 0 ?
                Double3.ZERO : intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }

    /**
     * Calculates the combined local lighting effects (diffuse and specular)
     * from all light sources affecting the intersection point.
     *
     * @param intersection the intersection point on the geometry surface
     * @return the total color from local lighting and emission
     */
    private Color calcLocalEffects(Intersection intersection) {
        Color color = intersection.geometry.getEmission();
        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {
                color = color.add(
                        lightSource.getIntensity(intersection.point)
                                .scale(calcDiffuse(intersection)
                                        .add(calcSpecular(intersection))
                                ));
            }
        }
        return color;
    }
}