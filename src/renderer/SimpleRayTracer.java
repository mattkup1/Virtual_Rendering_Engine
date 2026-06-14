package renderer;

import geometries.api.Intersectable.Intersection;
import java.util.List;
import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;

/**
 * Recursive Phong ray tracer.
 * <p>
 * Combines emission, ambient, diffuse and specular shading with transparency-aware
 * shadows and recursive reflection / transparency rays. Recursion is bounded by
 * {@link #MAX_CALC_COLOR_LEVEL} and short-circuited once the cumulative attenuation
 * coefficient drops below {@link #MIN_CALC_COLOR_K}.
 * </p>
 * <p>
 * At the top of the recursion stack, glossy reflections and diffuse (blurry)
 * transparency are simulated by replacing each ideal secondary ray with a
 * {@link #BLUR_SAMPLES}-ray beam scattered around it (see {@link BeamSampler}).
 * Deeper recursion levels always trace the single ideal ray, keeping total ray
 * count tractable.
 * </p>
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

    /**
     * Add adaptive super sampling in blur and gloss beams or not
     */
    private static final boolean adaptiveSuperSampling = true;

    /**
     * Number of rays in each top-level glossy reflection or diffuse transparency
     * beam. Higher values reduce Monte Carlo noise (speckle / "black dots") at
     * the cost of proportionally more rays per affected pixel. Materials with
     * zero blur ({@code blurR} / {@code blurT}) still trace a single ideal ray
     * regardless of this value, so the cost is paid only on glossy or
     * blurry-transparent surfaces.
     */
    private static final int BLUR_SAMPLES = 65;
    /**
     * Maximum recursion depth used by adaptive beam sampling.
     * <p>
     * Each adaptive step subdivides the beam into smaller groups of rays
     * when sampled colors differ significantly. Higher values increase
     * sampling accuracy at the cost of additional ray tracing work.
     * </p>
     */
    private static final int BEAM_ADAPTIVE_LEVEL = 4;
    /**
     * Color difference threshold used by adaptive beam sampling.
     * <p>
     * If the sampled colors within a beam segment differ by less than this
     * amount, the segment is considered sufficiently uniform and no further
     * subdivision is performed.
     * </p>
     */
    private static final double BEAM_COLOR_DELTA = 5;
    /**
     * Constructs a SimpleRayTracer with a given scene.
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
                ? _scene.background
                : calcColor(intersection, ray.getDirection());
    }

    /**
     * Entry-point color calculation for a primary-ray hit: applies the ambient term
     * and seeds the recursive Phong evaluation.
     *
     * @param intersection the closest intersection of a primary ray
     * @param v            the direction vector of the incoming ray
     * @return the resolved color, or {@link Color#BLACK} if the surface is
     * orthogonal to the ray
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return preprocessIntersection(intersection, v)
                ? _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K))
                : Color.BLACK;
    }

    /**
     * Recursive Phong color calculation.
     * <p>
     * Combines local lighting (emission, diffuse, specular) with reflection and
     * transparency contributions. Recursion stops once {@code level} reaches {@code 1}.
     * </p>
     *
     * @param intersection the intersection point
     * @param level        remaining recursion budget
     * @param k            cumulative attenuation coefficient along this ray path
     * @return the color contributed at this intersection
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = calcLocalEffects(intersection, k);
        return level == 1 ? color : color.add(calcGlobalEffects(intersection, level, k));
    }

    // ===================== Local effects =====================

    /**
     * Sums the local Phong contributions (emission, diffuse, specular) from every
     * contributing light source, each attenuated by transparent occluders between
     * the surface point and the light.
     *
     * @param intersection the intersection point
     * @param k            cumulative attenuation coefficient along this ray path
     * @return the combined local color
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
                                            .add(calcSpecular(intersection))));
                }
            }
        }
        return color;
    }

    /**
     * Diffuse component via Lambert's cosine law: {@code kD * |l &middot; n|}.
     *
     * @param intersection the intersection point (with light direction preset)
     * @return the diffuse attenuation triad
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.lNormal));
    }

    /**
     * Specular component using Phong's reflection vector {@code r = l - 2(l &middot; n)n}:
     * {@code kS * max(0, -v &middot; r)^nShininess}.
     *
     * @param intersection the intersection point (with light direction preset)
     * @return the specular attenuation triad
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
     * Cumulative transparency between the intersection point and its active light source.
     * <p>
     * Traces a shadow ray from the point toward the light, multiplying the running
     * coefficient by every transparent occluder's {@code kT}. Returns {@link Double3#ZERO}
     * as soon as the product falls below {@link #MIN_CALC_COLOR_K}.
     * </p>
     *
     * @param intersection the intersection point (with an active light source preset)
     * @return the transparency attenuation triad along the shadow ray
     */
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

    // ===================== Global effects =====================

    /**
     * Sums the recursive transparency and reflection contributions at an intersection.
     * <p>
     * At the top recursion level ({@code level == }{@link #MAX_CALC_COLOR_LEVEL}),
     * each branch expands its ideal secondary ray into a {@link #BLUR_SAMPLES}-ray
     * beam via {@link BeamSampler}, producing glossy reflections (driven by
     * {@code blurR}) and diffuse transparency (driven by {@code blurT}). At
     * deeper levels each branch traces exactly the single ideal ray, so glossy
     * and diffuse effects do not multiply the ray count exponentially.
     * </p>
     *
     * @param intersection the intersection point
     * @param level        remaining recursion budget
     * @param k            cumulative attenuation coefficient along this ray path
     * @return the combined global color
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
     * Single recursive bounce: traces one secondary ray, multiplies its result by
     * {@code kx} and short-circuits to {@link Color#BLACK} once the cumulative
     * attenuation drops below {@link #MIN_CALC_COLOR_K}.
     *
     * @param ray   the secondary ray (reflection or transparency)
     * @param level remaining recursion budget
     * @param k     cumulative attenuation coefficient prior to this bounce
     * @param kx    per-bounce attenuation triad ({@code kR} or {@code kT})
     * @return the color contributed by the secondary ray
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        if (kkx.isLowerThan(MIN_CALC_COLOR_K))
            return Color.BLACK;

        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return _scene.background.scale(kx);
        return preprocessIntersection(intersection, ray.getDirection())
                ? calcColor(intersection, level - 1, kkx).scale(kx)
                : Color.BLACK;
    }

    /**
     * Builds the ideal transparency ray that continues through the surface in
     * the same direction as the incoming ray, offset slightly past the surface
     * to avoid self-intersection. This is the un-blurred direction that
     * {@link BeamSampler} scatters around when {@code blurT > 0}.
     *
     * @param intersection the intersection point
     * @return the ideal transparency ray
     */
    private Ray constructTransparencyRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }

    /**
     * Builds the ideal reflection ray {@code r = v - 2(v &middot; n)n}, offset
     * slightly along the surface normal to avoid self-intersection. This is the
     * un-blurred direction that {@link BeamSampler} scatters around when
     * {@code blurR > 0}.
     *
     * @param intersection the intersection point
     * @return the ideal reflection ray
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
     * @param ray the ray to test
     * @return the closest intersection, or {@code null} if the ray hits nothing
     */
    private Intersection findClosestIntersection(Ray ray) {
        return ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
    }

    /**
     * Calculates the color contribution of a glossy-reflection or diffuse-transparency
     * beam using adaptive beam sampling.
     * <p>
     * Instead of tracing every ray in the beam uniformly, this method delegates
     * to {@link #adaptiveBeam(List, int, Double3, Double3, int)}, which recursively
     * evaluates only as much of the beam as needed based on color variation.
     * This reduces the number of traced rays in regions where the beam produces
     * nearly uniform colors while preserving detail in high-frequency regions.
     * </p>
     *
     * @param rays  the beam of secondary rays
     * @param level remaining recursion budget
     * @param k     cumulative attenuation coefficient prior to this bounce
     * @param kx    per-bounce attenuation triad ({@code kR} or {@code kT})
     * @return the beam's average color contribution
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
                    BEAM_ADAPTIVE_LEVEL);
        } else {
            Color color = Color.BLACK;
            for  (var ray : rays) {
                color.add(calcGlobalEffect(ray, level, k, kx));
            }
            return color.reduce(rays.size());
        }
    }
    /**
     * Recursively evaluates a beam of rays using adaptive sampling.
     * <p>
     * The method traces representative rays from the beginning, middle, and end
     * of the beam segment. If their resulting colors are sufficiently similar
     * (according to {@link #BEAM_COLOR_DELTA}) or the adaptive recursion limit
     * has been reached, their average is used as an approximation of the entire
     * segment.
     * </p>
     * <p>
     * Otherwise, the beam is split into two sub-beams which are evaluated
     * recursively. The final color is the average of the two sub-beam colors.
     * This approach concentrates ray tracing effort only in parts of the beam
     * where color varies significantly, reducing rendering time while maintaining
     * visual quality.
     * </p>
     *
     * @param rays           the beam segment being evaluated
     * @param level          remaining global recursion budget
     * @param k              cumulative attenuation coefficient prior to this bounce
     * @param kx             per-bounce attenuation triad ({@code kR} or {@code kT})
     * @param adaptiveLevel  remaining adaptive subdivision depth
     * @return the approximated average color of the beam segment
     */
    private Color adaptiveBeam(
            List<Ray> rays,
            int level,
            Double3 k,
            Double3 kx,
            int adaptiveLevel) {

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

        Color firstColor = calcGlobalEffect(
                rays.getFirst(),
                level,
                k,
                kx);

        Color middleColor = calcGlobalEffect(
                rays.get(middleIndex),
                level,
                k,
                kx);

        Color lastColor = calcGlobalEffect(
                rays.get(size - 1),
                level,
                k,
                kx);

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
                adaptiveLevel - 1);

        Color rightColor = adaptiveBeam(
                rays.subList(middleIndex, size),
                level,
                k,
                kx,
                adaptiveLevel - 1);

        return leftColor.add(rightColor).reduce(2);
    }
}
