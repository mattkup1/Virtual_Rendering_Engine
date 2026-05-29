package renderer;

import java.util.ArrayList;
import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Builds a beam of rays around an ideal reflection or transparency direction so
 * the ray tracer can produce glossy reflections and diffuse (blurry) transparency.
 * <p>
 * Each ideal direction is treated as the center of a sampling disk located at
 * {@link #TARGET_DISTANCE} units past the surface, perpendicular to the ideal
 * direction. The disk's radius is the material's blur parameter, and the disk
 * is sampled uniformly using Shirley's concentric mapping. A blur radius of
 * zero collapses the beam to a single ideal ray, preserving mirror-sharp
 * behavior on smooth surfaces.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class BeamSampler {

    /**
     * Distance from the surface at which the sampling disk is centered.
     */
    private static final double TARGET_DISTANCE = 100;

    /**
     * Utility class — not instantiable.
     */
    private BeamSampler() { /* not instantiable */ }

    /**
     * Builds a beam of rays around an {@code idealDirection}, sampling a disk
     * of the given {@code blurRadius} at distance {@link #TARGET_DISTANCE} from
     * {@code surfacePoint}.
     * <p>
     * Used to produce <em>glossy surfaces</em> (when called with the reflection
     * direction) and <em>diffuse (blurry) glass</em> (when called with the
     * transparency direction). A {@code blurRadius} of {@code 0} &mdash; or a
     * {@code sampleCount} of {@code 1} &mdash; short-circuits to a single ideal
     * ray, leaving mirror-smooth surfaces and clear glass unchanged.
     * </p>
     * <p>
     * Samples that would cross to the opposite side of {@code surfaceNormal}
     * (relative to {@code idealDirection}) are silently dropped, so the
     * returned list may be shorter than {@code sampleCount}. If every sample
     * is dropped (extreme blur at grazing angles), the single ideal ray is
     * returned as a fallback rather than an empty list. Callers should
     * therefore divide their accumulated color by {@code beam.size()}, not
     * {@code sampleCount}.
     * </p>
     *
     * @param surfacePoint   the un-offset surface intersection point
     * @param idealDirection the ideal reflection or transparency direction;
     *                       does not need to be unit length (it is normalized
     *                       internally)
     * @param surfaceNormal  the surface normal at {@code surfacePoint}
     * @param blurRadius     radius of the sampling disk at
     *                       {@link #TARGET_DISTANCE}; {@code 0} collapses the
     *                       beam to the single ideal ray
     * @param sampleCount    desired number of rays; values {@code <= 1}
     *                       collapse the beam to the single ideal ray
     * @return a non-empty list of rays scattered around the ideal direction
     */
    public static List<Ray> sampleBeam(
            Point surfacePoint,
            Vector idealDirection,
            Vector surfaceNormal,
            double blurRadius,
            int sampleCount
    ) {
        // Normalize once; every step below assumes a unit-length direction
        Vector direction = idealDirection.normalize();

        // Fast path: perfectly smooth surface or single-ray request
        if (blurRadius == 0 || sampleCount <= 1) {
            return List.of(new Ray(surfacePoint, direction, surfaceNormal));
        }

        Point targetCenter = surfacePoint.add(direction.scale(TARGET_DISTANCE));
        Vector[] basis = orthonormalBasis(direction);
        Vector uAxis = basis[0];
        Vector vAxis = basis[1];

        double idealSide = direction.dotProduct(surfaceNormal);
        List<Ray> beam = new ArrayList<>(sampleCount);

        for (int i = 0; i < sampleCount; i++) {
            double[] disk = concentricDiskMap(Math.random(), Math.random());
            double s = disk[0] * blurRadius;
            double t = disk[1] * blurRadius;

            Point samplePoint = targetCenter.add(uAxis.scale(s)).add(vAxis.scale(t));
            Vector sampleDirection = samplePoint.subtract(surfacePoint).normalize();

            // Wrong-side filter: drop samples whose direction crossed to the
            // opposite side of the surface relative to the ideal direction.
            if (idealSide * sampleDirection.dotProduct(surfaceNormal) <= 0) continue;

            beam.add(new Ray(surfacePoint, sampleDirection, surfaceNormal));
        }

        // Fallback: if every sample was filtered out (grazing angle, extreme
        // blur), return the single ideal ray rather than an empty beam.
        if (beam.isEmpty()) {
            beam.add(new Ray(surfacePoint, direction, surfaceNormal));
        }
        return beam;
    }

    /**
     * Builds two orthonormal vectors spanning the plane perpendicular to
     * {@code direction}. Used to lay out the 2-D sampling disk in world space.
     *
     * @param direction a unit-length direction vector
     * @return an array {@code [u, v]} of two unit vectors, each perpendicular
     *         to {@code direction} and to each other
     */
    private static Vector[] orthonormalBasis(Vector direction) {
        // Pick a helper vector that is not (nearly) parallel to direction so the
        // cross product is well-conditioned. If direction is mostly along Y,
        // use the X axis as helper; otherwise use the Y axis.
        Vector helper = Math.abs(direction.dotProduct(new Vector(0, 1, 0))) > 0.9
                ? new Vector(1, 0, 0)
                : new Vector(0, 1, 0);

        Vector u = direction.crossProduct(helper).normalize();
        Vector v = direction.crossProduct(u);   // unit length already (cross of two perpendicular unit vectors)
        return new Vector[]{u, v};
    }

    /**
     * Maps a point from the unit square {@code [0, 1]}<sup>2</sup> to a
     * uniformly distributed point inside the unit disk using Shirley &amp;
     * Chiu's concentric mapping (1997). The output {@code (s, t)} satisfies
     * {@code s}<sup>2</sup>{@code  + t}<sup>2</sup>{@code  <= 1}.
     *
     * @param u the first uniform random value in {@code [0, 1]}
     * @param v the second uniform random value in {@code [0, 1]}
     * @return a two-element array {@code [s, t]} representing the disk-space sample
     */
    private static double[] concentricDiskMap(double u, double v) {
        // Remap [0,1]^2 to [-1,1]^2
        double a = 2 * u - 1;
        double b = 2 * v - 1;

        if (a == 0 && b == 0) return new double[]{0, 0};

        double r, phi;
        if (a * a > b * b) {
            r = a;
            phi = (Math.PI / 4) * (b / a);
        } else {
            r = b;
            phi = (Math.PI / 2) - (Math.PI / 4) * (a / b);
        }
        return new double[]{r * Math.cos(phi), r * Math.sin(phi)};
    }
}
