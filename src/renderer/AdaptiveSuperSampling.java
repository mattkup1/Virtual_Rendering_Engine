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
        double halfWidth = pixelWidth / 2.0;
        double halfHeight = pixelHeight / 2.0;

        Point tl = moveOnViewPlane(pixelCenter, -halfWidth,  halfHeight);
        Point tr = moveOnViewPlane(pixelCenter,  halfWidth,  halfHeight);
        Point bl = moveOnViewPlane(pixelCenter, -halfWidth, -halfHeight);
        Point br = moveOnViewPlane(pixelCenter,  halfWidth, -halfHeight);

        return sample(
                pixelCenter, traceRayThroughPoint(pixelCenter),
                tl, traceRayThroughPoint(tl),
                tr, traceRayThroughPoint(tr),
                bl, traceRayThroughPoint(bl),
                br, traceRayThroughPoint(br),
                maxLevel
        );
    }

    /**
     * Recursively samples a rectangular area on the view plane.
     * <p>
     * All five corner/center colors are passed in so that each ray is traced
     * exactly once: the parent computes a point, passes its color down to both
     * sub-quadrants that share it, and the sub-quadrants never re-trace it.
     * New midpoint rays are computed here and likewise forwarded to the two
     * children that share each midpoint, eliminating the redundant re-tracing
     * that caused the original implementation to miss edges between cells.
     *
     * @param center      center of the sampled area
     * @param centerColor already-traced color at {@code center}
     * @param tl          top-left corner
     * @param tlColor     already-traced color at {@code tl}
     * @param tr          top-right corner
     * @param trColor     already-traced color at {@code tr}
     * @param bl          bottom-left corner
     * @param blColor     already-traced color at {@code bl}
     * @param br          bottom-right corner
     * @param brColor     already-traced color at {@code br}
     * @param level       remaining recursion depth
     * @return averaged color for the sampled area
     */
    private Color sample(
            Point center, Color centerColor,
            Point tl, Color tlColor,
            Point tr, Color trColor,
            Point bl, Color blColor,
            Point br, Color brColor,
            int level
    ) {
        if (level == 0 || centerColor.equalColors(tlColor, trColor, blColor, brColor)) {
            return centerColor
                    .add(tlColor, trColor, blColor, brColor)
                    .reduce(5);
        }

        // Half-extents of this cell along each view-plane axis.
        double halfW = vRight.dotProduct(tr.subtract(tl)) / 2.0;
        double halfH = vUp.dotProduct(tl.subtract(bl))   / 2.0;
        double qW    = halfW / 2.0;
        double qH    = halfH / 2.0;

        // Compute the four edge midpoints — each is shared by two sub-quadrants.
        Point midTop    = moveOnViewPlane(center,  0,     halfH);
        Point midBottom = moveOnViewPlane(center,  0,    -halfH);
        Point midLeft   = moveOnViewPlane(center, -halfW,  0);
        Point midRight  = moveOnViewPlane(center,  halfW,  0);

        Color midTopColor    = traceRayThroughPoint(midTop);
        Color midBottomColor = traceRayThroughPoint(midBottom);
        Color midLeftColor   = traceRayThroughPoint(midLeft);
        Color midRightColor  = traceRayThroughPoint(midRight);

        // Recurse into the four quadrants, reusing all shared corner/midpoint colors.
        Color topLeftResult = sample(
                moveOnViewPlane(center, -qW,  qH), centerColor,
                tl,      tlColor,
                midTop,  midTopColor,
                midLeft, midLeftColor,
                center,  centerColor,
                level - 1
        );

        Color topRightResult = sample(
                moveOnViewPlane(center,  qW,  qH), centerColor,
                midTop,   midTopColor,
                tr,       trColor,
                center,   centerColor,
                midRight, midRightColor,
                level - 1
        );

        Color bottomLeftResult = sample(
                moveOnViewPlane(center, -qW, -qH), centerColor,
                midLeft,   midLeftColor,
                center,    centerColor,
                bl,        blColor,
                midBottom, midBottomColor,
                level - 1
        );

        Color bottomRightResult = sample(
                moveOnViewPlane(center,  qW, -qH), centerColor,
                center,    centerColor,
                midRight,  midRightColor,
                midBottom, midBottomColor,
                br,        brColor,
                level - 1
        );

        return topLeftResult
                .add(topRightResult, bottomLeftResult, bottomRightResult)
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