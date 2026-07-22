package primitives;

/**
 * Immutable pair of texture coordinates.
 * <p>
 * Whether the components are normalized to {@code [0,1)} or are unbounded local
 * surface coordinates depends on the geometry that produced them (see
 * {@link geometries.api.Geometry#getUV(Point)}); {@link Texture} implementations are
 * responsible for wrapping/scaling raw values as needed.
 * </p>
 *
 * @param u the horizontal texture coordinate
 * @param v the vertical texture coordinate
 * @author mattkuperwasser
 * @author moshehanau
 */
public record UV(double u, double v) {
    /**
     * Maps a unit direction vector to normalized equirectangular (longitude/latitude)
     * texture coordinates, the same convention {@code Sphere.getUV} uses for a surface
     * point relative to its center - {@code u} wraps once around the equator, {@code v}
     * runs from the north pole ({@code 0}) to the south pole ({@code 1}) along the Y axis.
     * <p>
     * Used both by {@code Sphere.getUV} and, with the ray's own direction, to sample an
     * environment map/skybox for rays that hit nothing.
     * </p>
     *
     * @param direction a unit-length direction vector
     * @return the corresponding equirectangular texture coordinates
     */
    public static UV fromDirection(Vector direction) {
        double u = 0.5 + Math.atan2(direction.getZ(), direction.getX()) / (2 * Math.PI);
        double clampedY = Math.max(-1, Math.min(1, direction.getY()));
        double v = 0.5 - Math.asin(clampedY) / Math.PI;
        return new UV(u, v);
    }
}
