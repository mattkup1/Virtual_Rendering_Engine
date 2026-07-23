package primitives;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link ImageTexture}.
 * <p>
 * Driven by {@code sceneSourceFiles/textures/testSwatch.png}, a 2x2 test image with a
 * distinct known color in each quadrant (red top-left, green top-right, blue
 * bottom-left, white bottom-right).
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@Tag("unit")
class ImageTextureTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    ImageTextureTests() { /* to satisfy Javadoc generator */ }

    /**
     * Path to the 2x2 test swatch image
     */
    private static final String SWATCH_PATH = "sceneSourceFiles/textures/testSwatch.png";

    /**
     * Error message for incorrect sampled color
     */
    private static final String ERR_SAMPLE = "ERROR: Incorrect image sample";

    /**
     * Test method for {@link ImageTexture} constructor.
     */
    @Test
    void testConstructor() {
        // =============== Boundary Values Tests ==================
        // BV01: Non-positive repeat is invalid
        assertThrows(IllegalArgumentException.class, () -> new ImageTexture(SWATCH_PATH, 0, 1));
        // BV02: Non-existent file
        assertThrows(RuntimeException.class, () -> new ImageTexture("no/such/file.png", 1, 1));
    }

    /**
     * Test method for {@link ImageTexture#sample(UV)}: verifies each quadrant of the
     * swatch is sampled correctly, including V being flipped so v near 1 reads the
     * image's top row.
     */
    @Test
    void testSample() {
        ImageTexture texture = new ImageTexture(SWATCH_PATH, 1, 1);

        // ============ Equivalence Partitions Tests ==============
        // EP01: Top-left quadrant (low U, high V) -> red
        assertEquals(new Color(255, 0, 0), texture.sample(new UV(0.25, 0.75)), ERR_SAMPLE);
        // EP02: Top-right quadrant (high U, high V) -> green
        assertEquals(new Color(0, 255, 0), texture.sample(new UV(0.75, 0.75)), ERR_SAMPLE);
        // EP03: Bottom-left quadrant (low U, low V) -> blue
        assertEquals(new Color(0, 0, 255), texture.sample(new UV(0.25, 0.25)), ERR_SAMPLE);
        // EP04: Bottom-right quadrant (high U, high V... low V) -> white
        assertEquals(new Color(255, 255, 255), texture.sample(new UV(0.75, 0.25)), ERR_SAMPLE);
    }

    /**
     * Test method for {@link ImageTexture#sample(UV)} with a fractional repeat, verifying
     * the image tiles (wraps) rather than only ever showing a single instance.
     */
    @Test
    void testSampleWithRepeat() {
        // Half-unit tiles: the same UV that fell in the "green" quadrant at repeat=1
        // now lands back at the tile-local equivalent of (0.5,0.5) -> the white quadrant.
        ImageTexture texture = new ImageTexture(SWATCH_PATH, 0.5, 0.5);
        assertEquals(new Color(255, 255, 255), texture.sample(new UV(0.75, 0.75)), ERR_SAMPLE);
    }
}
