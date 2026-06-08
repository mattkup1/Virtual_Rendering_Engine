package renderer;

/**
 * Iterates over image pixels and delegates the actual pixel rendering.
 */
class ImageRenderer {
    /**
     * Pixel rendering callback.
     */
    @FunctionalInterface
    interface PixelRenderer {
        /**
         * Render a single pixel.
         *
         * @param x pixel column
         * @param y pixel row
         */
        void renderPixel(int x, int y);
    }

    /**
     * Number of pixel columns.
     */
    private final int nX;
    /**
     * Number of pixel rows.
     */
    private final int nY;
    /**
     * Number of worker threads. Zero means sequential rendering.
     */
    private final int threadsCount;
    /**
     * Progress print interval in percent. Zero disables printing.
     */
    private final double printInterval;
    /**
     * Callback that renders a single pixel.
     */
    private final PixelRenderer pixelRenderer;

    /**
     * Creates an image renderer.
     *
     * @param nX            number of pixel columns
     * @param nY            number of pixel rows
     * @param threadsCount  number of worker threads
     * @param printInterval progress print interval in percent
     * @param pixelRenderer pixel rendering callback
     */
    ImageRenderer(int nX, int nY, int threadsCount, double printInterval, PixelRenderer pixelRenderer) {
        this.nX = nX;
        this.nY = nY;
        this.threadsCount = threadsCount;
        this.printInterval = printInterval;
        this.pixelRenderer = pixelRenderer;
    }

    /**
     * Renders all pixels sequentially or with worker threads.
     */
    void render() {
        if (threadsCount > 0) {
            renderWithThreads();
        } else {
            renderSequentially();
        }
    }

    /**
     * Renders all pixels sequentially.
     */
    private void renderSequentially() {
        for (int x = 0; x < nX; ++x) {
            for (int y = 0; y < nY; ++y) {
                pixelRenderer.renderPixel(x, y);
            }
        }
    }

    /**
     * Renders all pixels using worker threads.
     */
    private void renderWithThreads() {
        final PixelManager pixelManager = new PixelManager(nY, nX, printInterval);
        final Thread[] threads = new Thread[threadsCount];

        for (int i = 0; i < threadsCount; ++i) {
            threads[i] = new Thread(() -> {
                PixelManager.Pixel pixel;
                while ((pixel = pixelManager.nextPixel()) != null) {
                    pixelRenderer.renderPixel(pixel.col(), pixel.row());
                    pixelManager.pixelDone();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Rendering was interrupted", e);
            }
        }
    }
}
