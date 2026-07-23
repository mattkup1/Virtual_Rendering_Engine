package renderer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import primitives.Color;

/**
 * Unit tests for class {@link ImageWriter}
 */
@Tag("unit")
public class ImageWriterTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    ImageWriterTests() { /* To satisfy Javadoc generator */ }

    // ================== CONSTANTS ==================

    // Basic image: Grid and background
    /**
     * Number of pixel columns
     */
    private static final int X_PIXELS = 800;
    /**
     * Number of pixel rows
     */
    private static final int Y_PIXELS = 500;
    /**
     * Size of a square in the grid
     */
    private static final int GRID_SQUARE_DIMENSIONS = 50;
    /**
     * Image background color (Yellow)
     */
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 0);
    /**
     * Image grid color (black)
     */
    private static final Color GRID_COLOR = Color.BLACK;

    /**
     * Test method for {@link ImageWriter}
     */
    @Test
    public void testImageWriter() {
        final ImageWriter IW = new ImageWriter(X_PIXELS, Y_PIXELS);

        // Write each pixel
        for (int y = 0; y < Y_PIXELS; y++) {
            for (int x = 0; x < X_PIXELS; x++) {
                IW.writePixel(x, y,
                        x % GRID_SQUARE_DIMENSIONS == 0 || y % GRID_SQUARE_DIMENSIONS == 0 ? GRID_COLOR : BACKGROUND_COLOR);
            }
        }

        IW.writeToImage("our_first_image");
    }
}
