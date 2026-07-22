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
}
