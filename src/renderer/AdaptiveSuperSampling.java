package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Calculates a pixel color using adaptive super sampling.
 */
class AdaptiveSuperSampling {
    /**
     * Camera location.
     */
    private final Point cameraLocation;
    /**
     * View-plane right direction.
     */
    private final Vector vRight;
    /**
     * View-plane up direction.
     */
    private final Vector vUp;
    /**
     * Ray tracer used to resolve sampled rays.
     */
    private final RayTracerBase rayTracer;
    /**
     * Maximum recursive subdivision depth.
     */
    private final int maxLevel;

    /**
     * Creates an adaptive super sampler for a camera view plane.
     *
     * @param cameraLocation camera location
     * @param vRight         view-plane right direction
     * @param vUp            view-plane up direction
     * @param rayTracer      ray tracer used to resolve rays
     * @param maxLevel       maximum recursive subdivision depth
     */
    AdaptiveSuperSampling(
            Point cameraLocation,
            Vector vRight,
            Vector vUp,
            RayTracerBase rayTracer,
            int maxLevel
    ) {
        this.cameraLocation = cameraLocation;
        this.vRight = vRight;
        this.vUp = vUp;
        this.rayTracer = rayTracer;
        this.maxLevel = maxLevel;
    }

    /**
     * Samples one pixel adaptively.
     *
     * @param pixelCenter center point of the pixel on the view plane
     * @param pixelWidth  pixel width
     * @param pixelHeight pixel height
     * @return averaged pixel color
     */
    Color sample(Point pixelCenter, double pixelWidth, double pixelHeight) {
        return sample(pixelCenter, pixelWidth, pixelHeight, maxLevel);
    }

    /**
     * Recursively samples a rectangular area on the view plane.
     *
     * @param center center of the sampled area
     * @param width  sampled area width
     * @param height sampled area height
     * @param level  remaining recursion depth
     * @return averaged color for the sampled area
     */
    private Color sample(Point center, double width, double height, int level) {
        Color centerColor = traceRayThroughPoint(center);

        double halfWidth = width / 2.0;
        double halfHeight = height / 2.0;

        Point topLeft = moveOnViewPlane(center, -halfWidth, halfHeight);
        Point topRight = moveOnViewPlane(center, halfWidth, halfHeight);
        Point bottomLeft = moveOnViewPlane(center, -halfWidth, -halfHeight);
        Point bottomRight = moveOnViewPlane(center, halfWidth, -halfHeight);

        Color topLeftColor = traceRayThroughPoint(topLeft);
        Color topRightColor = traceRayThroughPoint(topRight);
        Color bottomLeftColor = traceRayThroughPoint(bottomLeft);
        Color bottomRightColor = traceRayThroughPoint(bottomRight);

        if (level == 0 || centerColor.equalColors(topLeftColor, topRightColor, bottomLeftColor, bottomRightColor)) {
            return centerColor
                    .add(topLeftColor, topRightColor, bottomLeftColor, bottomRightColor)
                    .reduce(5);
        }

        double quarterWidth = width / 4.0;
        double quarterHeight = height / 4.0;

        Color topLeftSubPixel = sample(
                moveOnViewPlane(center, -quarterWidth, quarterHeight),
                halfWidth,
                halfHeight,
                level - 1
        );

        Color topRightSubPixel = sample(
                moveOnViewPlane(center, quarterWidth, quarterHeight),
                halfWidth,
                halfHeight,
                level - 1
        );

        Color bottomLeftSubPixel = sample(
                moveOnViewPlane(center, -quarterWidth, -quarterHeight),
                halfWidth,
                halfHeight,
                level - 1
        );

        Color bottomRightSubPixel = sample(
                moveOnViewPlane(center, quarterWidth, -quarterHeight),
                halfWidth,
                halfHeight,
                level - 1
        );

        return topLeftSubPixel
                .add(topRightSubPixel, bottomLeftSubPixel, bottomRightSubPixel)
                .reduce(4);
    }

    /**
     * Traces a ray from the camera location through a point on the view plane.
     *
     * @param point point on the view plane
     * @return traced color
     */
    private Color traceRayThroughPoint(Point point) {
        return rayTracer.traceRay(new Ray(cameraLocation, point.subtract(cameraLocation).normalize()));
    }

    /**
     * Moves a point along the view-plane axes.
     *
     * @param point source point
     * @param x     right-axis offset
     * @param y     up-axis offset
     * @return moved point
     */
    private Point moveOnViewPlane(Point point, double x, double y) {
        Point movedPoint = point;

        if (!isZero(x))
            movedPoint = movedPoint.add(vRight.scale(x));

        if (!isZero(y))
            movedPoint = movedPoint.add(vUp.scale(y));

        return movedPoint;
    }
}
