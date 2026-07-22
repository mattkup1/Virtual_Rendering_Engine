package geometries.impl;

import geometries.api.Geometry;
import java.util.List;
import java.util.Objects;
import primitives.BoundingBox;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a bounded flat elliptical (or, with equal radii, circular disk) patch
 * in a 3D Cartesian coordinate system.
 * <p>
 * The patch lies on a supporting {@link Plane} and is bounded to the set of points
 * within {@code radiusX} along one in-plane axis and {@code radiusY} along the
 * perpendicular in-plane axis, centered at a given point.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Ellipse extends Geometry {
    /**
     * The center point of the ellipse
     */
    private final Point _center;
    /**
     * The plane supporting the ellipse
     */
    private final Plane _plane;
    /**
     * Unit vector along the ellipse's {@code radiusX} in-plane axis
     */
    private final Vector _uAxis;
    /**
     * Unit vector along the ellipse's {@code radiusY} in-plane axis, perpendicular to {@link #_uAxis}
     */
    private final Vector _vAxis;
    /**
     * The ellipse's radius along {@link #_uAxis}
     */
    private final double _radiusX;
    /**
     * The ellipse's radius along {@link #_vAxis}
     */
    private final double _radiusY;

    /**
     * Constructs an ellipse from a center point, a supporting-plane normal, an in-plane
     * axis direction, and the two radii measured along that axis and its perpendicular.
     * <p>
     * {@code axisDirection} does not need to be exactly perpendicular to {@code normal};
     * it is projected onto the plane and normalized.
     * </p>
     *
     * @param center        the center point of the ellipse
     * @param normal        the normal vector of the supporting plane
     * @param axisDirection the direction of the {@code radiusX} axis, projected onto the plane
     * @param radiusX       the radius along {@code axisDirection}
     * @param radiusY       the radius along the perpendicular in-plane direction
     * @throws IllegalArgumentException if either radius is not positive, or if
     *                                  {@code axisDirection} is parallel to {@code normal}
     */
    public Ellipse(Point center, Vector normal, Vector axisDirection, double radiusX, double radiusY) {
        if (radiusX <= 0 || radiusY <= 0)
            throw new IllegalArgumentException("Ellipse radii must be positive");

        _center = center;
        final Vector unitNormal = normal.normalize();
        _plane = new Plane(center, unitNormal);
        final double projectionOnNormal = axisDirection.dotProduct(unitNormal);
        final Vector inPlaneDirection = isZero(projectionOnNormal)
                ? axisDirection
                : axisDirection.subtract(unitNormal.scale(projectionOnNormal));
        _uAxis = inPlaneDirection.normalize();
        _vAxis = unitNormal.crossProduct(_uAxis).normalize();
        _radiusX = radiusX;
        _radiusY = radiusY;
    }

    /**
     * Constructs a circular disk from a center point, a supporting-plane normal, and a radius.
     *
     * @param center the center point of the disk
     * @param normal the normal vector of the supporting plane
     * @param radius the disk's radius
     * @throws IllegalArgumentException if radius is not positive
     */
    public Ellipse(Point center, Vector normal, double radius) {
        this(center, normal, arbitraryPerpendicular(normal.normalize()), radius, radius);
    }

    /**
     * Returns an arbitrary unit vector perpendicular to the given unit vector.
     *
     * @param normal the unit vector to find a perpendicular direction to
     * @return an arbitrary unit vector perpendicular to {@code normal}
     */
    private static Vector arbitraryPerpendicular(Vector normal) {
        final Vector reference = Math.abs(normal.getX()) < 0.9 ? Vector.AXIS_X : Vector.AXIS_Y;
        return normal.crossProduct(reference).normalize();
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        final var planeIntersection = _plane.findIntersections(ray);
        if (planeIntersection == null) return null;

        final Point p = planeIntersection.getFirst();

        if (!p.equals(_center)) {
            final Vector fromCenter = p.subtract(_center);
            final double u = fromCenter.dotProduct(_uAxis);
            final double v = fromCenter.dotProduct(_vAxis);
            if (alignZero(u * u / (_radiusX * _radiusX) + v * v / (_radiusY * _radiusY) - 1) > 0)
                return null;
        }

        if (alignZero(p.distance(ray.getOrigin()) - maxDistance) > 0) return null;

        return List.of(new Intersection(this, p));
    }

    @Override
    public Vector getNormal(Point point) {
        return _plane.getNormal(point);
    }

    @Override
    public BoundingBox getBoundingBox() {
        final double ax = _radiusX * _uAxis.getX(), bx = _radiusY * _vAxis.getX();
        final double ay = _radiusX * _uAxis.getY(), by = _radiusY * _vAxis.getY();
        final double az = _radiusX * _uAxis.getZ(), bz = _radiusY * _vAxis.getZ();
        final double ex = Math.sqrt(ax * ax + bx * bx);
        final double ey = Math.sqrt(ay * ay + by * by);
        final double ez = Math.sqrt(az * az + bz * bz);
        return new BoundingBox(
                _center.getX() - ex, _center.getY() - ey, _center.getZ() - ez,
                _center.getX() + ex, _center.getY() + ey, _center.getZ() + ez);
    }

    @Override
    public String toString() {
        return "Ellipse: Center: " + _center + ", Normal: " + _plane.getNormal(_center)
                + ", RadiusX: " + _radiusX + ", RadiusY: " + _radiusY;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ellipse other = (Ellipse) obj;
        return _center.equals(other._center) && _uAxis.equals(other._uAxis) && _vAxis.equals(other._vAxis)
                && alignZero(_radiusX - other._radiusX) == 0 && alignZero(_radiusY - other._radiusY) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_center, _uAxis, _vAxis, _radiusX, _radiusY);
    }
}
