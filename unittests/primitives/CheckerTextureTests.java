package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link CheckerTexture}.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class CheckerTextureTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    CheckerTextureTests() { /* to satisfy Javadoc generator */ }

    /**
     * The first checker color
     */
    private static final Color COLOR_A = new Color(255, 0, 0);
    /**
     * The second checker color
     */
    private static final Color COLOR_B = new Color(0, 0, 255);
    /**
     * Checker texture with 1-unit cells, used across the tests
     */
    private static final CheckerTexture TEXTURE = new CheckerTexture(COLOR_A, COLOR_B, 1);

    /**
     * Error message for incorrect sampled color
     */
    private static final String ERR_SAMPLE = "ERROR: Incorrect checker sample";

    /**
     * Test method for {@link CheckerTexture} constructor.
     */
    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================
        // BV01: Non-positive cell size is invalid
        assertThrows(IllegalArgumentException.class, () -> new CheckerTexture(COLOR_A, COLOR_B, 0));
    }

    /**
     * Test method for {@link CheckerTexture#sample(UV)}.
     */
    @Test
    void testSample() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Origin cell
        assertEquals(COLOR_A, TEXTURE.sample(new UV(0.5, 0.5)), ERR_SAMPLE);
        // EP02: Adjacent cell along U only (odd cell sum)
        assertEquals(COLOR_B, TEXTURE.sample(new UV(1.5, 0.5)), ERR_SAMPLE);
        // EP03: Diagonally adjacent cell (even cell sum)
        assertEquals(COLOR_A, TEXTURE.sample(new UV(1.5, 1.5)), ERR_SAMPLE);

        // =============== Boundary Values Tests ==================
        // BV01: Negative coordinates alternate correctly too
        assertEquals(COLOR_B, TEXTURE.sample(new UV(-0.5, 0.5)), ERR_SAMPLE);
    }
}
