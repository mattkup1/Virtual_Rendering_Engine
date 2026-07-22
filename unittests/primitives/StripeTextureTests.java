package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link StripeTexture}.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class StripeTextureTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    StripeTextureTests() { /* to satisfy Javadoc generator */ }

    /**
     * The first stripe color
     */
    private static final Color COLOR_A = new Color(255, 0, 0);
    /**
     * The second stripe color
     */
    private static final Color COLOR_B = new Color(0, 0, 255);
    /**
     * Stripe texture with 1-unit-wide stripes, used across the tests
     */
    private static final StripeTexture TEXTURE = new StripeTexture(COLOR_A, COLOR_B, 1);

    /**
     * Error message for incorrect sampled color
     */
    private static final String ERR_SAMPLE = "ERROR: Incorrect stripe sample";

    /**
     * Test method for {@link StripeTexture} constructor.
     */
    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================
        // BV01: Non-positive stripe width is invalid
        assertThrows(IllegalArgumentException.class, () -> new StripeTexture(COLOR_A, COLOR_B, 0));
    }

    /**
     * Test method for {@link StripeTexture#sample(UV)}.
     */
    @Test
    void testSample() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: First stripe
        assertEquals(COLOR_A, TEXTURE.sample(new UV(0.5, 0.5)), ERR_SAMPLE);
        // EP02: Second stripe
        assertEquals(COLOR_B, TEXTURE.sample(new UV(1.5, 0.5)), ERR_SAMPLE);
        // EP03: V coordinate has no effect on the stripe axis
        assertEquals(COLOR_A, TEXTURE.sample(new UV(0.5, 99)), ERR_SAMPLE);

        // =============== Boundary Values Tests ==================
        // BV01: Negative U coordinate alternates correctly too
        assertEquals(COLOR_B, TEXTURE.sample(new UV(-0.5, 0)), ERR_SAMPLE);
    }
}
