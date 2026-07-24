package renderer;

import geometries.api.Intersectable.Intersection;
import java.util.List;
import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import primitives.UV;
import primitives.Vector;
import scene.Scene;
import java.util.HashMap;
import java.util.Map;

import static primitives.Util.alignZero;

/**
 * Recursive Phong ray tracer with transparency, reflection and glossy / blurry beams.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class SimpleRayTracer extends RayTracerBase {

    /**
     * Maximum recursion depth for reflection and transparency rays.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;

    /**
     * Coefficient threshold below which a secondary ray's contribution is discarded.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;

    /**
     * Cumulative attenuation coefficient seeded into the recursive color calculation.
     */
    private static final Double3 INITIAL_K = Double3.ONE;

    /** Enables adaptive sampling for glossy and blurry beams. */
    private static final boolean adaptiveSuperSampling = true;

    /** Number of rays sampled for a top-level glossy reflection or blurry transparency beam. */
    private static final int BLUR_SAMPLES = 65;

    /** Maximum subdivision depth for adaptive beam sampling. */
    private static final int BEAM_ADAPTIVE_LEVEL = 4;

    /** Color threshold below which a beam segment is treated as uniform. */
    private static final double BEAM_COLOR_DELTA = 5;

    /** Number of shadow rays sampled across a soft-shadow light's radius. */
    private static final int SHADOW_SAMPLES = 25;

    /**
     * Constructs a ray tracer for the given scene.
     *
     * @param scene the scene to be rendered
     */
    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    // ===================== Top-level ray tracing =====================

    @Override
    public Color traceRay(Ray ray) {
        var intersection = findClosestIntersection(ray);
        return intersection == null
                ? backgroundColor(ray)
                : calcColor(intersection, ray.getDirection());
    }

    /**
     * Returns the color seen along a ray that hit nothing: the scene's environment map
     * (Skybox), sampled by the ray's own direction, if one is set; otherwise the flat
     * background color.
     *
     * @param ray the ray that missed all scene geometry
     * @return the background color for this ray
     */
    private Color backgroundColor(Ray ray) {
        return _scene.environmentMap != null
                ? _scene.environmentMap.sample(UV.fromDirection(ray.getDirection()))
                : _scene.background;
    }

    /**
     * Calculates the color for a primary-ray hit, including ambient light.
     *
     * @param intersection the closest intersection found by the primary ray
     * @param v            the primary ray direction
     * @return the resolved color at the hit point, or black for invalid orientation
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return preprocessIntersection(intersection, v)
                ? _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K))
                : Color.BLACK;
    }

    /**
     * Combines local lighting with recursive reflection and transparency.
     *
     * @param intersection the current surface intersection
     * @param level        remaining recursion depth
     * @param k            cumulative attenuation along the current ray path
     * @return the color contributed by this intersection
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = calcLocalEffects(intersection, k);
        return level == 1 ? color : color.add(calcGlobalEffects(intersection, level, k));
    }

    // ===================== Local effects =====================

    /**
     * Calculates emission (or, if the material has a texture, the sampled texture color
     * in its place) plus diffuse/specular lighting from all visible lights.
     *
     * @param intersection the shaded intersection
     * @param k            cumulative attenuation used for pruning weak light paths
     * @return the local color contribution
     */
    private Color calcLocalEffects(Intersection intersection, Double3 k) {
        Color color = intersection.material.texture != null
                ? intersection.material.texture.sample(intersection.geometry.getUV(intersection.point))
                : intersection.geometry.getEmission();
        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {
                Double3 ktr = transparency(intersection);
                if (ktr.product(k).isGreaterThan(MIN_CALC_COLOR_K)) {
                    color = color.add(
                            lightSource.getIntensity(intersection.point)
                                    .scale(ktr)
                                    .scale(calcDiffuse(intersection)
                                            .add(calcSpecular(intersection))));
                }
            }
        }
        return color;
    }

    /**
     * Calculates the diffuse component via Lambert's cosine law.
     *
     * @param intersection the intersection with prepared light-normal data
     * @return the diffuse attenuation coefficient
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.lNormal));
    }

    /**
     * Calculates the specular component using Phong's reflection vector.
     *
     * @param intersection the intersection with prepared view/light-normal data
     * @return the specular attenuation coefficient
     */
    private Double3 calcSpecular(Intersection intersection) {
        final Vector r =
                intersection.l.subtract(intersection.normal.scale(2 * intersection.lNormal)).normalize();
        double minusVR = alignZero(-intersection.v.dotProduct(r));
        return minusVR <= 0
                ? Double3.ZERO
                : intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }

    /**
     * Calculates transparency between the intersection point and its active light.
     * <p>
     * For a hard-shadow light ({@link LightSource#getRadius()} {@code == 0}, the default),
     * this is a single shadow ray toward the light. For a light with a non-zero radius,
     * {@link #SHADOW_SAMPLES} shadow rays are jittered across a disk of that radius at the
     * light's actual distance (via {@link BeamSampler}) and their transparency averaged,
     * producing a soft penumbra instead of a hard shadow edge.
     * </p>
     *
     * @param intersection the intersection whose active light was prepared earlier
     * @return accumulated (and, for soft-shadow lights, averaged) transparency through all
     *         shadow-ray blockers
     */
    private Double3 transparency(Intersection intersection) {
        Vector toLight = intersection.l.scale(-1);
        double lightRadius = intersection.light.getRadius();

        if (lightRadius == 0) {
            return transparencyAlongRay(new Ray(intersection.point, toLight, intersection.normal), intersection);
        }

        double lightDistance = intersection.light.getDistance(intersection.point);
        List<Ray> shadowRays = BeamSampler.sampleBeam(
                intersection.point, toLight, intersection.normal, lightRadius, SHADOW_SAMPLES, lightDistance);

        Double3 sum = Double3.ZERO;
        for (Ray shadowRay : shadowRays) {
            sum = sum.add(transparencyAlongRay(shadowRay, intersection));
        }
        return sum.divide(shadowRays.size());
    }

    /**
     * Calculates transparency along a single shadow ray toward a light, up to the light's
     * distance.
     *
     * @param shadowRay    the shadow ray to trace
     * @param intersection the intersection whose active light was prepared earlier
     * @return accumulated transparency through all blockers along this ray
     */
    private Double3 transparencyAlongRay(Ray shadowRay, Intersection intersection) {
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

    // ===================== Global effects =====================

    /**
     * Adds recursive reflection and transparency. At the top recursion level,
     * blurred reflection/transparency materials are expanded into sampled beams.
     *
     * @param intersection the current surface intersection
     * @param level        remaining recursion depth
     * @param k            cumulative attenuation along the current ray path
     * @return the combined global color contribution
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        Ray idealTransparency = constructTransparencyRay(intersection);
        Ray idealReflection = constructReflectionRay(intersection);

        boolean useBeam = level == MAX_CALC_COLOR_LEVEL;

        Color transparencyColor = useBeam
                ? calcBeam(
                BeamSampler.sampleBeam(
                        intersection.point,
                        idealTransparency.getDirection(),
                        intersection.normal,
                        intersection.material.blurT,
                        BLUR_SAMPLES),
                level, k, intersection.material.kT)
                : calcGlobalEffect(idealTransparency, level, k, intersection.material.kT);

        Color reflectionColor = useBeam
                ? calcBeam(
                BeamSampler.sampleBeam(
                        intersection.point,
                        idealReflection.getDirection(),
                        intersection.normal,
                        intersection.material.blurR,
                        BLUR_SAMPLES),
                level, k, intersection.material.kR)
                : calcGlobalEffect(idealReflection, level, k, intersection.material.kR);

        return transparencyColor.add(reflectionColor);
    }

    /**
     * Traces one reflected or transparent ray and applies its attenuation.
     *
     * @param ray   the secondary ray to trace
     * @param level remaining recursion depth before this bounce
     * @param k     cumulative attenuation before this bounce
     * @param kx    attenuation coefficient for this bounce
     * @return the attenuated color contribution of the secondary ray
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        if (kkx.isLowerThan(MIN_CALC_COLOR_K))
            return Color.BLACK;

        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return backgroundColor(ray).scale(kx);
        return preprocessIntersection(intersection, ray.getDirection())
                ? calcColor(intersection, level - 1, kkx).scale(kx)
                : Color.BLACK;
    }

    /**
     * Builds the ideal transparency ray before optional beam scattering.
     *
     * @param intersection the surface intersection
     * @return the offset transparency ray
     */
    private Ray constructTransparencyRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }

    /**
     * Builds the ideal reflection ray before optional beam scattering.
     *
     * @param intersection the surface intersection
     * @return the offset reflection ray
     */
    private Ray constructReflectionRay(Intersection intersection) {
        return new Ray(
                intersection.point,
                intersection.v.subtract(intersection.normal.scale(2 * intersection.vNormal)),
                intersection.normal);
    }

    // ===================== Helpers =====================

    /**
     * Finds the closest scene intersection of the given ray.
     *
     * @param ray the ray to test against the scene
     * @return the closest intersection, or {@code null} if there is no hit
     */
    private Intersection findClosestIntersection(Ray ray) {
        return ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
    }

    /**
     * Averages a glossy/refraction beam, optionally using adaptive sampling to avoid
     * tracing every ray in visually uniform beam segments.
     *
     * @param rays  the sampled beam rays
     * @param level remaining global recursion depth
     * @param k     cumulative attenuation before this bounce
     * @param kx    attenuation coefficient for this bounce
     * @return the average beam color contribution
     */
    private Color calcBeam(
            List<Ray> rays,
            int level,
            Double3 k,
            Double3 kx) {
        if (adaptiveSuperSampling) {
            return adaptiveBeam(
                    rays,
                    level,
                    k,
                    kx,
                    BEAM_ADAPTIVE_LEVEL,
                    new HashMap<>());
        } else {
            Color color = Color.BLACK;
            for  (var ray : rays) {
                color = color.add(calcGlobalEffect(ray, level, k, kx));
            }
            return color.reduce(rays.size());
        }
    }

    /**
     * Recursively samples first/middle/last beam rays and subdivides only when
     * their colors differ enough to need more detail.
     *
     * @param rays          the beam segment to evaluate
     * @param level         remaining global recursion depth
     * @param k             cumulative attenuation before this bounce
     * @param kx            attenuation coefficient for this bounce
     * @param adaptiveLevel remaining adaptive subdivision depth
     * @param cache         traced ray colors reused across overlapping subsegments
     * @return the approximated average color of the beam segment
     */
    private Color adaptiveBeam(
            List<Ray> rays,
            int level,
            Double3 k,
            Double3 kx,
            int adaptiveLevel,
            Map<Ray, Color> cache) {

        int size = rays.size();

        if (size == 0)
            return Color.BLACK;

        if (size == 1)
            return calcGlobalEffect(
                    rays.getFirst(),
                    level,
                    k,
                    kx);

        int middleIndex = size / 2;

        Color firstColor = calcGlobalEffectCached(
                rays.getFirst(),
                level,
                k,
                kx,
                cache);

        Color middleColor = calcGlobalEffectCached(
                rays.get(middleIndex),
                level,
                k,
                kx,
                cache);

        Color lastColor = calcGlobalEffectCached(
                rays.get(size - 1),
                level,
                k,
                kx,
                cache);

        if (adaptiveLevel == 0 ||
                firstColor.equalColors(
                        BEAM_COLOR_DELTA,
                        middleColor,
                        lastColor)) {

            return firstColor
                    .add(middleColor, lastColor)
                    .reduce(3);
        }

        Color leftColor = adaptiveBeam(
                rays.subList(0, middleIndex),
                level,
                k,
                kx,
                adaptiveLevel - 1,
                cache);

        Color rightColor = adaptiveBeam(
                rays.subList(middleIndex, size),
                level,
                k,
                kx,
                adaptiveLevel - 1,
                cache);

        // Each side's color is already the average over its own sub-segment, so combining
        // them back into the average over the full segment requires weighting by segment
        // size (not a plain midpoint) whenever middleIndex doesn't split size evenly - e.g.
        // BLUR_SAMPLES=65 makes every split in this recursion uneven.
        int rightSize = size - middleIndex;
        return leftColor.scale((double) middleIndex / size).add(rightColor.scale((double) rightSize / size));
    }

    /**
     * Returns a cached global-effect color for adaptive beam sampling.
     *
     * @param ray   the ray whose contribution is requested
     * @param level remaining global recursion depth
     * @param k     cumulative attenuation before this bounce
     * @param kx    attenuation coefficient for this bounce
     * @param cache traced ray color cache
     * @return the cached or newly calculated ray contribution
     */
    private Color calcGlobalEffectCached(
            Ray ray,
            int level,
            Double3 k,
            Double3 kx,
            Map<Ray, Color> cache) {

        Color color = cache.get(ray);

        if (color == null) {
            color = calcGlobalEffect(ray, level, k, kx);
            cache.put(ray, color);
        }
        return color;
    }
}
