package primitives;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link RingTexture}.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@Tag("unit")
class RingTextureTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    RingTextureTests() { /* to satisfy Javadoc generator */ }

    /**
     * The first ring color
     */
    private static final Color COLOR_A = new Color(255, 0, 0);
    /**
     * The second ring color
     */
    private static final Color COLOR_B = new Color(0, 0, 255);
    /**
     * Ring texture with 1-unit-wide rings, used across the tests
     */
    private static final RingTexture TEXTURE = new RingTexture(COLOR_A, COLOR_B, 1);

    /**
     * Error message for incorrect sampled color
     */
    private static final String ERR_SAMPLE = "ERROR: Incorrect ring sample";

    /**
     * Test method for {@link RingTexture} constructor.
     */
    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================
        // BV01: Non-positive ring width is invalid
        assertThrows(IllegalArgumentException.class, () -> new RingTexture(COLOR_A, COLOR_B, 0));
    }

    /**
     * Test method for {@link RingTexture#sample(UV)}.
     */
    @Test
    void testSample() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Center ring (distance 0)
        assertEquals(COLOR_A, TEXTURE.sample(new UV(0, 0)), ERR_SAMPLE);
        // EP02: Second ring (distance 1.5, a 3-4-5 triangle: u=0.9,v=1.2 -> distance 1.5)
        assertEquals(COLOR_B, TEXTURE.sample(new UV(0.9, 1.2)), ERR_SAMPLE);
        // EP03: Fifth ring (distance 5.0, a 3-4-5 triangle: u=3,v=4)
        assertEquals(COLOR_B, TEXTURE.sample(new UV(3, 4)), ERR_SAMPLE);

        // =============== Boundary Values Tests ==================
        // BV01: Negative coordinates use the same radial distance
        assertEquals(COLOR_A, TEXTURE.sample(new UV(-0.5, 0)), ERR_SAMPLE);
    }
}
