package primitives;

/**
 * Procedural concentric-ring pattern, alternating between two colors in bands at
 * increasing distance from the UV origin - a "target"/"bullseye" pattern on a plane,
 * or latitude-independent bands radiating from a sphere's pole.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class RingTexture implements Texture {
    /**
     * The first ring color
     */
    private final Color _colorA;
    /**
     * The second ring color
     */
    private final Color _colorB;
    /**
     * The width of each ring, in UV units
     */
    private final double _ringWidth;

    /**
     * Constructs a ring texture.
     *
     * @param colorA    the first ring color
     * @param colorB    the second ring color
     * @param ringWidth the width of each ring, in UV units
     * @throws IllegalArgumentException if {@code ringWidth} is not positive
     */
    public RingTexture(Color colorA, Color colorB, double ringWidth) {
        if (ringWidth <= 0) throw new IllegalArgumentException("Ring width must be positive");
        _colorA = colorA;
        _colorB = colorB;
        _ringWidth = ringWidth;
    }

    @Override
    public Color sample(UV uv) {
        double distance = Math.sqrt(uv.u() * uv.u() + uv.v() * uv.v());
        long ring = (long) Math.floor(distance / _ringWidth);
        return Math.floorMod(ring, 2) == 0 ? _colorA : _colorB;
    }
}
