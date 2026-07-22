package renderer;

import geometries.api.Intersectable;
import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Texture;
import primitives.UV;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

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
        applyNormalMap(intersection);
        intersection.vNormal = alignZero(intersection.v.dotProduct(intersection.normal));
        return intersection.vNormal != 0;
    }

    /**
     * Perturbs {@code intersection.normal} using the material's {@code normalTexture}
     * (bump mapping), if one is set.
     * <p>
     * The texture is sampled as a grayscale height field at the intersection's UV
     * coordinates and at two small finite-difference steps along U and V, to estimate the
     * height gradient. The gradient is then applied to the normal along the surface's
     * local tangent/bitangent directions, scaled by {@code bumpStrength}, and renormalized.
     * Must run before {@code vNormal} is computed, since that depends on the (possibly
     * now-perturbed) normal.
     * </p>
     *
     * @param intersection the intersection being preprocessed; its {@code normal} must
     *                      already be set
     */
    private void applyNormalMap(Intersectable.Intersection intersection) {
        Texture normalTexture = intersection.material.normalTexture;
        if (normalTexture == null) return;

        final double epsilon = 0.001;
        UV uv = intersection.geometry.getUV(intersection.point);
        double height = normalTexture.sample(uv).luminance();
        double heightDu = normalTexture.sample(new UV(uv.u() + epsilon, uv.v())).luminance();
        double heightDv = normalTexture.sample(new UV(uv.u(), uv.v() + epsilon)).luminance();

        double du = (heightDu - height) / epsilon * intersection.material.bumpStrength;
        double dv = (heightDv - height) / epsilon * intersection.material.bumpStrength;
        if (isZero(du) && isZero(dv)) return;

        Vector normal = intersection.normal;
        Vector tangent = arbitraryPerpendicular(normal);
        Vector bitangent = normal.crossProduct(tangent);

        Vector perturbed = normal;
        if (!isZero(du)) perturbed = perturbed.subtract(tangent.scale(du));
        if (!isZero(dv)) perturbed = perturbed.subtract(bitangent.scale(dv));

        intersection.normal = perturbed.normalize();
    }

    /**
     * Returns an arbitrary unit vector perpendicular to the given unit vector, used as the
     * tangent direction for bump-map normal perturbation.
     *
     * @param normal the unit vector to find a perpendicular direction to
     * @return an arbitrary unit vector perpendicular to {@code normal}
     */
    private static Vector arbitraryPerpendicular(Vector normal) {
        Vector reference = Math.abs(normal.getX()) < 0.9 ? Vector.AXIS_X : Vector.AXIS_Y;
        return normal.crossProduct(reference).normalize();
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