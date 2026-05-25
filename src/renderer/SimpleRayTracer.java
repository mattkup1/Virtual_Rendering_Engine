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

    private static final int MAX_CALC_COLOR_LEVEL = 10;
    private static final double MIN_CALC_COLOR_K = 0.001;
    private static final Double3 INITIAL_K = Double3.ONE;

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
        var intersection = findClosestIntersection(ray);
        return intersection == null
                ? _scene.background
                : calcColor(intersection, ray.getDirection());
    }

    private Double3 transparency(Intersection intersection) {
        var shadowRay = new Ray(intersection.point, intersection.l.scale(-1), intersection.normal);
        double lightDistance = intersection.light.getDistance(intersection.point);
        var shadowIntersections = _scene.geometries.calcIntersections(shadowRay, lightDistance);
        Double3 ktr = Double3.ONE;

        if (shadowIntersections == null)
            return ktr;

        for (var shadowIntersection : shadowIntersections) {
            ktr = ktr.product(shadowIntersection.material.kT);

            if (ktr.isLowerThan(MIN_CALC_COLOR_K)) {
                return Double3.ZERO;
            }
        }
        return ktr;
    }


    private Color calcColor(Intersection intersection, Vector v) {
        return preprocessIntersection(intersection, v) ?
                _scene.ambientLight.getIntensity().scale(intersection.geometry.getMaterial().kA)
                        .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K))
                : Color.BLACK;
    }

    /**
     * Recursive {@code calcColor}
     *
     * @param intersection
     * @param level
     * @param k
     * @return
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = calcLocalEffects(intersection, k);
        return 1 == level ? color : color.add(calcGlobalEffects(intersection, level, k));
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
        // Reflection vector formula: r = l - 2 * (l * n) * n - normalized
        final Vector r =
                intersection.l.subtract(intersection.normal.scale(2 * intersection.lNormal)).normalize();

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
    private Color calcLocalEffects(Intersection intersection, Double3 k) {

        Color color = intersection.geometry.getEmission();

        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {
                Double3 ktr = transparency(intersection);
                if (ktr.product(k).isGreaterThan(MIN_CALC_COLOR_K)) {
                    color = color.add(
                            lightSource.getIntensity(intersection.point)
                                    .scale(ktr)
                                    .scale(calcDiffuse(intersection)
                                            .add(calcSpecular(intersection))
                                    ));
                }
            }
        }
        return color;
    }

    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {

        Double3 kkx = k.product(kx);
        if (kkx.isLowerThan((MIN_CALC_COLOR_K)))
            return Color.BLACK;

        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return _scene.background.scale(kx);
        return preprocessIntersection(intersection, ray.getDirection())
                ? calcColor(intersection, level - 1, kkx).scale(kx)
                : Color.BLACK;
    }

    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        return calcGlobalEffect(
                constructTransparencyRay(intersection),
                level,
                k,
                intersection.material.kT
        ).add(
                calcGlobalEffect(
                        constructReflectionRay(intersection),
                        level,
                        k,
                        intersection.material.kR
                )
        );
    }

    private Ray constructTransparencyRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }

    private Ray constructReflectionRay(Intersection intersection) {
        return new Ray(
                intersection.point,
                intersection.v.subtract(intersection.normal.scale(2 * intersection.vNormal)),
                intersection.normal
        );
    }

    private Intersection findClosestIntersection(Ray ray) {
        return ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
    }
}