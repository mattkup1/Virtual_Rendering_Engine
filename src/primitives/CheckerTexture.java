package primitives;

/**
 * Procedural checkerboard pattern, alternating between two colors in square cells.
 * <p>
 * Works with either bounded (e.g. {@code Sphere}) or unbounded (e.g. {@code Plane}) UV
 * coordinates - cells simply keep tiling outward in every direction.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class CheckerTexture implements Texture {
    /**
     * The first checker color
     */
    private final Color _colorA;
    /**
     * The second checker color
     */
    private final Color _colorB;
    /**
     * The side length of each square cell, in UV units
     */
    private final double _cellSize;

    /**
     * Constructs a checkerboard texture.
     *
     * @param colorA   the first checker color
     * @param colorB   the second checker color
     * @param cellSize the side length of each square cell, in UV units
     * @throws IllegalArgumentException if {@code cellSize} is not positive
     */
    public CheckerTexture(Color colorA, Color colorB, double cellSize) {
        if (cellSize <= 0) throw new IllegalArgumentException("Checker cell size must be positive");
        _colorA = colorA;
        _colorB = colorB;
        _cellSize = cellSize;
    }

    @Override
    public Color sample(UV uv) {
        long cellU = (long) Math.floor(uv.u() / _cellSize);
        long cellV = (long) Math.floor(uv.v() / _cellSize);
        return Math.floorMod(cellU + cellV, 2) == 0 ? _colorA : _colorB;
    }
}
