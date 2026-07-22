package primitives;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Texture backed by an image file, tiled across the surface at a configurable
 * repeat frequency.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class ImageTexture implements Texture {
    /**
     * The loaded image
     */
    private final BufferedImage _image;
    /**
     * The width of one image tile, in UV units
     */
    private final double _repeatU;
    /**
     * The height of one image tile, in UV units
     */
    private final double _repeatV;

    /**
     * Constructs an image texture from an image file.
     *
     * @param path    the path to the image file (any format supported by {@link ImageIO})
     * @param repeatU the width of one image tile, in UV units
     * @param repeatV the height of one image tile, in UV units
     * @throws RuntimeException        if the image cannot be read
     * @throws IllegalArgumentException if {@code repeatU} or {@code repeatV} is not positive
     */
    public ImageTexture(String path, double repeatU, double repeatV) {
        if (repeatU <= 0 || repeatV <= 0)
            throw new IllegalArgumentException("Image texture repeat must be positive");
        try {
            _image = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Could not load texture image from: " + path, e);
        }
        if (_image == null)
            throw new RuntimeException("Unrecognized image format: " + path);
        _repeatU = repeatU;
        _repeatV = repeatV;
    }

    @Override
    public Color sample(UV uv) {
        double wrappedU = wrap(uv.u() / _repeatU);
        double wrappedV = wrap(uv.v() / _repeatV);

        int x = Math.min((int) (wrappedU * _image.getWidth()), _image.getWidth() - 1);
        // Flip V so increasing v moves up the image, matching conventional texture orientation.
        int y = Math.min((int) ((1 - wrappedV) * _image.getHeight()), _image.getHeight() - 1);

        return new Color(new java.awt.Color(_image.getRGB(x, y)));
    }

    /**
     * Wraps a value into the {@code [0,1)} range, tiling repeatedly in both directions.
     *
     * @param value the value to wrap
     * @return the wrapped value, in {@code [0,1)}
     */
    private static double wrap(double value) {
        double wrapped = value % 1.0;
        return wrapped < 0 ? wrapped + 1.0 : wrapped;
    }
}
