package primitives;

/**
 * A surface pattern or image sampled by texture coordinates, used in place of a
 * geometry's flat emission color when a {@link Material} has one attached.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public interface Texture {
    /**
     * Samples the texture at the given coordinates.
     *
     * @param uv the texture coordinates to sample at
     * @return the color at that point in the texture
     */
    Color sample(UV uv);
}
