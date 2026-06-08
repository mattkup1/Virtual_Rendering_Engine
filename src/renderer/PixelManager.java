package renderer;

/**
 * Helper for distributing pixels between rendering threads and reporting
 * rendering progress.
 */
class PixelManager {
    /**
     * Immutable pixel coordinates.
     *
     * @param col pixel column number
     * @param row pixel row number
     */
    record Pixel(int col, int row) {
    }

    /** Maximum rows of pixels */
    private final int maxRows;
    /** Maximum columns of pixels */
    private final int maxCols;
    /** Total amount of pixels in the generated image */
    private final long totalPixels;

    /** Currently allocated row */
    private int cRow = 0;
    /** Currently allocated column */
    private int cCol = -1;
    /** Amount of pixels that have been processed */
    private long pixels = 0L;
    /** Last printed progress update percentage, in tenths of a percent */
    private int lastPrinted = 0;

    /** Progress percentage printing interval, in tenths of a percent */
    private final int printInterval;
    /** Printing format */
    private static final String PRINT_FORMAT = "%5.1f%%\r";

    /**
     * Initialize pixel manager data for multithreaded rendering.
     *
     * @param maxRows       amount of pixel rows
     * @param maxCols       amount of pixel columns
     * @param printInterval print interval, in percent; 0 disables printing
     */
    PixelManager(int maxRows, int maxCols, double printInterval) {
        this.maxRows = maxRows;
        this.maxCols = maxCols;
        this.totalPixels = (long) maxRows * maxCols;
        this.printInterval = (int) (printInterval * 10);

        if (this.printInterval > 0) {
            System.out.printf(PRINT_FORMAT, 0d);
        }
    }

    /**
     * Allocate the next available pixel to a worker thread.
     *
     * @return next pixel, or {@code null} when all pixels were allocated
     */
    synchronized Pixel nextPixel() {
        if (cRow == maxRows) {
            return null;
        }

        ++cCol;
        if (cCol < maxCols) {
            return new Pixel(cCol, cRow);
        }

        cCol = 0;
        ++cRow;
        if (cRow < maxRows) {
            return new Pixel(cCol, cRow);
        }

        return null;
    }

    /**
     * Mark one pixel as complete and print progress when needed.
     */
    synchronized void pixelDone() {
        ++pixels;
        if (printInterval <= 0) {
            return;
        }

        int percentage = (int) (1000L * pixels / totalPixels);
        if (percentage - lastPrinted >= printInterval) {
            lastPrinted = percentage;
            System.out.printf(PRINT_FORMAT, percentage / 10d);
        }
    }
}
