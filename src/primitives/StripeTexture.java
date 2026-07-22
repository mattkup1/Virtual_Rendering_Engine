package primitives;

/**
 * Procedural stripe pattern, alternating between two colors in bands along the
 * U texture axis.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class StripeTexture implements Texture {
    /**
     * The first stripe color
     */
    private final Color _colorA;
    /**
     * The second stripe color
     */
    private final Color _colorB;
    /**
     * The width of each stripe, in UV units
     */
    private final double _stripeWidth;

    /**
     * Constructs a stripe texture.
     *
     * @param colorA      the first stripe color
     * @param colorB      the second stripe color
     * @param stripeWidth the width of each stripe, in UV units
     * @throws IllegalArgumentException if {@code stripeWidth} is not positive
     */
    public StripeTexture(Color colorA, Color colorB, double stripeWidth) {
        if (stripeWidth <= 0) throw new IllegalArgumentException("Stripe width must be positive");
        _colorA = colorA;
        _colorB = colorB;
        _stripeWidth = stripeWidth;
    }

    @Override
    public Color sample(UV uv) {
        long stripe = (long) Math.floor(uv.u() / _stripeWidth);
        return Math.floorMod(stripe, 2) == 0 ? _colorA : _colorB;
    }
}
