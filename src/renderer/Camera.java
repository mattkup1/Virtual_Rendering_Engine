package renderer;

import java.awt.image.BufferedImage;
import java.util.MissingResourceException;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.CameraSettings;
import scene.Scene;

import static primitives.Util.isZero;

/**
 * Camera class representing a camera in a 3D space
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Camera implements Cloneable {
    /**
     * {@link Point} representing the camera location
     */
    private Point _p0;
    /**
     * {@link Vector} representing the direction in which the camera is pointing
     */
    private Vector _vTo;
    /**
     * {@link Vector} representing the upward direction relative to the camera
     */
    private Vector _vUp;
    /**
     * {@link Vector} representing the right hand side direction relative to the camera
     */
    private Vector _vRight;
    /**
     * View plane width
     */
    private double _width;
    /**
     * View plane height
     */
    private double _height;
    /**
     * Distance from the camera to the view plane
     */
    private double _distance;
    /**
     * Number of pixel columns in the view plane
     */
    private int _nX = 1;
    /**
     * Number of pixel rows in the view plane
     */
    private int _nY = 1;
    /**
     * {@link Point} representing the center of the view plane
     */
    private Point _vpCenter;
    /**
     * Individual pixel width
     */
    private double _pixelWidth;
    /**
     * Individual pixel height
     */
    private double _pixelHeight;
    /**
     * Image writer object
     */
    private ImageWriter _imageWriter;
    /**
     * Ray tracer object
     */
    private RayTracerBase _rayTracerBase;
    /**
     * Number of rendering threads; zero means render sequentially.
     */
    private int _threadsCount = 0;
    /**
     * Progress print interval in percent; zero disables progress printing.
     */
    private double _printInterval = 0;
    /**
     * Anti-aliasing samples per pixel axis; {@code 1} (the default) casts a single ray
     * through the pixel center, {@code n > 1} averages an {@code n x n} sub-pixel grid.
     */
    private int _antiAliasing = 1;
    /**
     * Thin-lens aperture radius; {@code 0} (the default) is a pinhole camera with
     * everything in sharp focus. A positive value enables depth of field, blurring
     * anything not at {@link #_focalDistance}.
     */
    private double _aperture = 0;
    /**
     * Distance from the camera at which the thin lens is in perfect focus; only
     * meaningful when {@link #_aperture} is positive.
     */
    private double _focalDistance;
    /**
     * Number of lens samples averaged per pixel when {@link #_aperture} is positive.
     * Each sample also uses an independently jittered sub-pixel offset, so this doubles
     * as anti-aliasing in depth-of-field mode rather than combining with {@link #_antiAliasing}.
     */
    private static final int DOF_SAMPLES = 64;

    /**
     * Empty camera constructor
     */
    private Camera() {
    }

    /**
     * Camera builder getter
     *
     * @return the camera builder object
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Construct a {@link Ray} from the camera to a given pixel in the view plane
     *
     * @param xIndex the pixel column number (zero indexed)
     * @param yIndex the pixel row number (zero indexed)
     * @return the ray
     */
    public Ray constructRay(int xIndex, int yIndex) {
        return constructRay(xIndex, yIndex, 0, 0);
    }

    /**
     * Construct a {@link Ray} from the camera to a jittered sub-pixel sample point within
     * a given pixel, for anti-aliasing.
     *
     * @param xIndex  the pixel column number (zero indexed)
     * @param yIndex  the pixel row number (zero indexed)
     * @param offsetX horizontal offset from the pixel center, as a fraction of the pixel
     *                width (typically in {@code [-0.5,0.5]}); {@code 0} is the center
     * @param offsetY vertical offset from the pixel center, as a fraction of the pixel
     *                height (typically in {@code [-0.5,0.5]}); {@code 0} is the center
     * @return the ray
     */
    private Ray constructRay(int xIndex, int yIndex, double offsetX, double offsetY) {
        Point pixelCenter = getPixelCenter(xIndex, yIndex, offsetX, offsetY);
        return new Ray(_p0, pixelCenter.subtract(_p0).normalize());
    }

    /**
     * Calculates a (possibly sub-pixel-jittered) point on the view plane within a pixel.
     *
     * @param xIndex  the pixel column number
     * @param yIndex  the pixel row number
     * @param offsetX horizontal offset from the pixel center, as a fraction of the pixel width
     * @param offsetY vertical offset from the pixel center, as a fraction of the pixel height
     * @return the sample point on the view plane
     */
    private Point getPixelCenter(int xIndex, int yIndex, double offsetX, double offsetY) {
        final double xJ = (xIndex - (_nX - 1) / 2.0 + offsetX) * _pixelWidth;
        final double yI = -(yIndex - (_nY - 1) / 2.0 + offsetY) * _pixelHeight;

        Point pixelCenter = _vpCenter;

        if (!isZero(xJ))
            pixelCenter = pixelCenter.add(_vRight.scale(xJ));

        if (!isZero(yI))
            pixelCenter = pixelCenter.add(_vUp.scale(yI));

        return pixelCenter;
    }

    /**
     * Renders the image by tracing rays through all pixels in the view plane.
     * <p>
     * Rendering is performed either sequentially or using multiple worker
     * threads depending on the value configured via
     * {@link Builder#setMultithreading(int)}.
     * </p>
     *
     * @return this camera instance for method chaining
     */
    public Camera renderImage() {
        if (_threadsCount > 1) {
            renderWithThreads();
        } else {
            renderSequentially();
        }

        return this;
    }

    /**
     * Renders all pixels sequentially on the calling thread.
     * <p>
     * Each pixel is processed by invoking {@link #castRay(int, int)} in
     * row-column order until the entire image is rendered.
     * </p>
     */
    private void renderSequentially() {
        for (int x = 0; x < _nX; ++x) {
            for (int y = 0; y < _nY; ++y) {
                castRay(x, y);
            }
        }
    }

    /**
     * Renders all pixels using multiple worker threads.
     * <p>
     * Work distribution is coordinated through {@link PixelManager}, which
     * assigns pixels dynamically to threads and optionally reports rendering
     * progress. The method blocks until all worker threads complete.
     * </p>
     *
     * @throws IllegalStateException if rendering is interrupted while waiting
     *                               for worker threads to finish
     */
    private void renderWithThreads() {
        final PixelManager pixelManager =
                new PixelManager(_nY, _nX, _printInterval);

        final Thread[] threads =
                new Thread[_threadsCount];

        for (int i = 0; i < _threadsCount; ++i) {

            threads[i] = new Thread(() -> {

                PixelManager.Pixel pixel;

                while ((pixel = pixelManager.nextPixel()) != null) {

                    castRay(
                            pixel.col(),
                            pixel.row());

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

                throw new IllegalStateException(
                        "Rendering was interrupted",
                        e);
            }
        }
    }

    /**
     * Helper function to cast a ray (or, with anti-aliasing enabled, an averaged grid of
     * sub-pixel rays) through a specific pixel, calculate the resulting color, and write
     * it to the image.
     *
     * @param xIndex the pixel's column index
     * @param yIndex the pixel's row index
     */
    private void castRay(int xIndex, int yIndex) {
        Color color = _aperture > 0
                ? castDepthOfFieldRay(xIndex, yIndex)
                : _antiAliasing <= 1
                ? _rayTracerBase.traceRay(constructRay(xIndex, yIndex))
                : castAntiAliasedRay(xIndex, yIndex);

        _imageWriter.writePixel(xIndex, yIndex, color);
    }

    /**
     * Averages an {@code n x n} grid of jittered sub-pixel samples (n = {@link #_antiAliasing})
     * within a pixel, to smooth out aliasing on high-contrast edges (e.g. procedural texture
     * boundaries, geometry silhouettes) that a single ray through the pixel center would miss.
     *
     * @param xIndex the pixel's column index
     * @param yIndex the pixel's row index
     * @return the averaged color across the sample grid
     */
    private Color castAntiAliasedRay(int xIndex, int yIndex) {
        final double step = 1.0 / _antiAliasing;
        Color color = Color.BLACK;

        for (int i = 0; i < _antiAliasing; ++i) {
            for (int j = 0; j < _antiAliasing; ++j) {
                final double offsetX = (i + 0.5) * step - 0.5;
                final double offsetY = (j + 0.5) * step - 0.5;
                color = color.add(_rayTracerBase.traceRay(constructRay(xIndex, yIndex, offsetX, offsetY)));
            }
        }

        return color.reduce(_antiAliasing * _antiAliasing);
    }

    /**
     * Averages {@link #DOF_SAMPLES} thin-lens samples for a pixel to produce depth of
     * field: each sample jitters both the sub-pixel position (giving free anti-aliasing)
     * and the ray's origin across a disk of radius {@link #_aperture} on the lens plane,
     * then aims the ray through the point on the sharp (pinhole) ray where it crosses the
     * focal plane at distance {@link #_focalDistance}. Points exactly at the focal
     * distance stay sharp regardless of the sub-pixel/lens jitter, since every sample's
     * ray still passes through the same focal-plane point; points off the focal plane
     * scatter across the lens radius, producing blur proportional to their distance from
     * focus.
     *
     * @param xIndex the pixel's column index
     * @param yIndex the pixel's row index
     * @return the averaged color across the lens samples
     */
    private Color castDepthOfFieldRay(int xIndex, int yIndex) {
        Color color = Color.BLACK;

        for (int i = 0; i < DOF_SAMPLES; ++i) {
            double offsetX = Math.random() - 0.5;
            double offsetY = Math.random() - 0.5;
            Ray sharpRay = constructRay(xIndex, yIndex, offsetX, offsetY);

            Point focalPoint = sharpRay.getOrigin().add(sharpRay.getDirection()
                    .scale(_focalDistance / sharpRay.getDirection().dotProduct(_vTo)));

            double[] disk = BeamSampler.concentricDiskMap(Math.random(), Math.random());
            Point lensPoint = _p0;
            if (!isZero(disk[0])) lensPoint = lensPoint.add(_vRight.scale(disk[0] * _aperture));
            if (!isZero(disk[1])) lensPoint = lensPoint.add(_vUp.scale(disk[1] * _aperture));

            Ray lensRay = new Ray(lensPoint, focalPoint.subtract(lensPoint).normalize());
            color = color.add(_rayTracerBase.traceRay(lensRay));
        }

        return color.reduce(DOF_SAMPLES);
    }

    /**
     * Prints a grid of lines over the image at specified intervals.
     * This is primarily used for debugging and visualizing pixel alignment.
     *
     * @param interval the gap between grid lines (in pixels)
     * @param color    the color of the grid lines
     * @return the camera object itself for builder-like chaining
     */
    public Camera printGrid(int interval, Color color) {
        for (int x = 0; x < _nX; ++x) {
            for (int y = 0; y < _nY; ++y) {
                if (x % interval == 0 || y % interval == 0)
                    this._imageWriter.writePixel(x, y, color);
            }
        }
        return this;
    }

    /**
     * Delegates the final image file creation to the image writer.
     * This method triggers the actual saving of the pixel data to a file
     * on the disk with the specified name.
     *
     * @param filename the name of the output image file (without extension)
     */
    public void writeToImage(String filename) {
        this._imageWriter.writeToImage(filename);
    }

    /**
     * Returns the rendered image buffer directly, without writing it to disk.
     * <p>
     * The returned instance is live: pixels {@link #renderImage()} writes after this call
     * are reflected in it, so it may be polled (e.g. from a GUI) to preview a render in
     * progress rather than only inspected once complete.
     * </p>
     *
     * @return the image buffer
     */
    public BufferedImage getImage() {
        return _imageWriter.getImage();
    }

    /**
     * {@link Camera} builder class
     */
    public static class Builder {

        /**
         * Default constructor to satisfy Javadoc generator
         */
        public Builder() { /* To satisfy Javadoc generator */ }

        /**
         * Initialize {@link Camera} object
         */

        private final Camera _camera = new Camera();
        /**
         * A {@link Point} which the camera is facing
         */
        private Point _pTarget;

        /**
         * Set the {@link Camera} location
         *
         * @param p0 the camera location
         * @return the builder object
         */
        public Builder setLocation(Point p0) {
            _camera._p0 = p0;
            return this;
        }

        /**
         * Sets the {@link Camera} direction vectors.
         * <p>
         * Both vectors are normalized internally. Orthogonality is not enforced here;
         * it is resolved during {@link #build()} by re-computing the right and up vectors.
         * </p>
         *
         * @param vTo the direction in which the camera is pointing
         * @param vUp the upward direction relative to the camera
         * @return the builder object
         */
        public Builder setDirection(Vector vTo, Vector vUp) {
            _camera._vTo = vTo.normalize();
            _camera._vUp = vUp.normalize();
            this._pTarget = null;

            return this;
        }

        /**
         * Initializes the {@link Camera} direction vectors based on a given target point and up vector
         *
         * @param pTarget the target point at which the camera is pointing
         * @param vUp     the upward direction vector relative to the camera
         * @return the builder object
         */
        public Builder setDirection(Point pTarget, Vector vUp) {
            this._pTarget = pTarget;
            _camera._vUp = vUp.normalize();

            return this;
        }

        /**
         * Sets the {@link Camera} direction based on a single target point.
         * The upward direction is assumed to be the y-axis.
         * <p>
         * Orthogonality is not enforced here; it is resolved during {@link #build()}
         * by re-computing the right and up vectors.
         * </p>
         *
         * @param p the point at which the camera is pointing
         * @return the builder object
         */
        public Builder setDirection(Point p) {
            this._pTarget = p;
            _camera._vUp = Vector.AXIS_Y;

            return this;
        }

        /**
         * Set the distance between the camera and the view plane
         *
         * @param distance the distance
         * @return the builder objects
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Set the measurements of the view plane
         *
         * @param width  the view plane width
         * @param height the view plane height
         * @return the builder object
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;

            return this;
        }

        /**
         * Set the resolution (number of pixels in the view plane)
         *
         * @param nX number of pixel columns
         * @param nY number of pixel rows
         * @return the builder object
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;

            return this;
        }

        /**
         * Configures location, direction, view-plane geometry, and resolution from
         * {@link CameraSettings} loaded from a scene source file.
         * <p>
         * Execution/session settings such as multithreading or debug-print interval are
         * not part of {@link CameraSettings} and must still be set separately.
         * </p>
         *
         * @param settings the camera settings to apply
         * @return the builder object
         */
        public Builder loadFrom(CameraSettings settings) {
            setLocation(settings.location);
            setDirection(settings.direction, settings.up);
            setVpDistance(settings.vpDistance);
            setVpSize(settings.vpWidth, settings.vpHeight);
            setResolution(settings.resolutionX, settings.resolutionY);

            return this;
        }

        /**
         * Sets the number of threads used by {@link Camera#renderImage()}.
         * <p>
         * Use 0 to render sequentially, a positive value for an exact thread
         * count, or -1 to use all available processors except two.
         * </p>
         *
         * @param threadsCount number of rendering threads
         * @return the builder object
         */
        public Builder setMultithreading(int threadsCount) {
            if (threadsCount < -1)
                throw new IllegalArgumentException("Multithreading must be -1, 0, or a positive number");

            if (threadsCount == -1) {
                int availableThreads = Runtime.getRuntime().availableProcessors() - 2;
                _camera._threadsCount = Math.max(1, availableThreads);
            } else {
                _camera._threadsCount = threadsCount;
            }

            return this;
        }

        /**
         * Sets the anti-aliasing quality: the number of jittered sample rays per pixel
         * axis, averaged into a single pixel color.
         * <p>
         * {@code 1} (the default) disables anti-aliasing, casting a single ray through
         * each pixel's center. Higher values trade render time (which grows with the
         * square of this value) for smoother edges - {@code 3} (9 samples/pixel) is a
         * reasonable default quality level; {@code 4}-{@code 5} for a final/high-quality
         * render.
         * </p>
         *
         * @param samplesPerAxis number of samples per pixel axis
         * @return the builder object
         * @throws IllegalArgumentException if {@code samplesPerAxis} is not positive
         */
        public Builder setAntiAliasing(int samplesPerAxis) {
            if (samplesPerAxis <= 0)
                throw new IllegalArgumentException("Anti-aliasing samples per axis must be positive");

            _camera._antiAliasing = samplesPerAxis;
            return this;
        }

        /**
         * Enables depth of field by configuring a thin-lens aperture and focal distance.
         * <p>
         * Points at {@code focalDistance} from the camera render in sharp focus; points
         * nearer or farther blur proportionally to their distance from that plane and to
         * {@code aperture}. Leaving this unset (or passing {@code aperture == 0}) keeps
         * the default pinhole camera, where everything is in sharp focus.
         * </p>
         *
         * @param aperture      the lens radius; {@code 0} disables depth of field
         * @param focalDistance the distance from the camera at which the lens is in
         *                      perfect focus; only meaningful when {@code aperture > 0}
         * @return the builder object
         * @throws IllegalArgumentException if {@code aperture} is negative, or if
         *                                   {@code aperture > 0} and {@code focalDistance}
         *                                   is not positive
         */
        public Builder setDepthOfField(double aperture, double focalDistance) {
            if (aperture < 0)
                throw new IllegalArgumentException("Aperture must not be negative");
            if (aperture > 0 && focalDistance <= 0)
                throw new IllegalArgumentException("Focal distance must be positive when aperture is positive");

            _camera._aperture = aperture;
            _camera._focalDistance = focalDistance;
            return this;
        }

        /**
         * Sets progress printing interval for rendering.
         *
         * @param interval print interval in percent; 0 disables progress printing
         * @return the builder object
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0)
                throw new IllegalArgumentException("Debug print interval must be non-negative");

            _camera._printInterval = interval;
            return this;
        }

        /**
         * Rotates the camera around its viewing direction by the given angle in degrees.
         * The rotation updates the up and right vectors while keeping the viewing direction unchanged.
         * Positive angles rotate clockwise around the viewing direction.
         *
         * @param angle the rotation angle in degrees
         * @return the builder object
         * @throws MissingResourceException if the camera direction vectors have not been initialized yet
         */
        public Builder rotate(double angle) {

            // in case the angle is zero then there is no rotate
            if (isZero(angle % 360)) return this;

            if (_camera._vUp == null)
                throw new MissingResourceException("Camera vUp vector must be initialized before rotation",
                        "Camera.Builder", "vUp");

            if (_camera._vTo == null && this._pTarget == null)
                throw new MissingResourceException(
                        "Camera vTo vector or pTarget point must be initialized before rotation",
                        "Camera.Builder", "vTo or pTarget");

            if (_camera._p0 == null)
                throw new MissingResourceException("Camera location must be initialized before rotation",
                        "Camera.Builder", "p0");

            if (_camera._vTo == null)
                _camera._vTo = this._pTarget.subtract(_camera._p0).normalize();

            if (_camera._vRight == null)
                _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();

            final double radians = Math.toRadians(angle);
            final double cos = Math.cos(radians);
            final double sin = Math.sin(radians);

            Vector oldVUp = _camera._vUp;
            Vector oldVRight = _camera._vRight;
            Vector newVUp;

            if (isZero(cos)) {
                newVUp = oldVRight.scale(-sin);
            } else if (isZero(sin)) {
                newVUp = oldVUp.scale(cos);
            } else {
                newVUp = oldVUp.scale(cos).add(oldVRight.scale(-sin));
            }

            _camera._vUp = newVUp.normalize();
            _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();

            return this;
        }

        /**
         * Helper function to calculate the camera direction vectors
         */
        private void calcVectors() {
            if (_camera._vTo == null)
                _camera._vTo = this._pTarget.subtract(_camera._p0).normalize();

            _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();
        }

        /**
         * Helper function to calculate the view plane center point
         */
        private void calcVpCenter() {
            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
        }

        /**
         * Validate number of pixels in the rows and columns of the view plane
         * and construct the camera image writer
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0)
                throw new IllegalArgumentException("Number of pixels must be positive");

            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
        }

        /**
         * Validate the location and direction of the camera
         *
         * @throws MissingResourceException if p0 is missing
         * @throws IllegalArgumentException if vTo is parallel to vUp
         */
        private void checkLocationAndDirection() {
            if (_camera._p0 == null)
                throw new MissingResourceException("Camera location must be initialized", "Camera", "p0");

            if (_camera._vUp == null)
                throw new MissingResourceException("Camera vUp vector must be initialized", "Camera", "vUp");

            if (_camera._vTo == null && this._pTarget == null)
                throw new MissingResourceException(
                        "Camera vTo vector or pTarget point must be initialized", "Camera", "vTo or pTarget");

            calcVectors();
        }

        /**
         * Helper function to validate the width and height of the view plane and compute the pixel measurements
         *
         * @throws IllegalArgumentException if the view plane width or height are non-positive or if the distance
         *                                  between the camera and the view plane is non-positive
         */
        private void checkViewPlane() {
            if (_camera._width <= 0)
                throw new IllegalArgumentException("view plane width must be positive");
            if (_camera._height <= 0)
                throw new IllegalArgumentException("view plane height must be positive");
            if (_camera._distance <= 0)
                throw new IllegalArgumentException("distance must be positive");

            calcVpCenter();
            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;
        }

        /**
         * Configures the ray tracer for the camera by specifying the scene and the
         * type of tracer to be used.
         *
         * @param scene the scene to be rendered by the camera
         * @param type  the type of ray tracer to instantiate (must be SIMPLE)
         * @return the builder object itself for fluent chaining
         * @throws IllegalArgumentException if an unsupported ray tracer type is provided
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            if (type == RayTracerType.SIMPLE) {
                _camera._rayTracerBase = new SimpleRayTracer(scene);
            } else {
                throw new IllegalArgumentException("RayTracer type must be SIMPLE");
            }
            return this;
        }

        /**
         * Final camera build method
         *
         * @return the initialized camera object
         * @throws IllegalArgumentException if illegal camera parameters are given
         * @throws MissingResourceException if any crucial camera parameters are missing
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();

            if (_camera._rayTracerBase == null)
                setRayTracer(new Scene("test"), RayTracerType.SIMPLE);

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException("Camera clone should always succeed", e);
            }
        }
    }
}
