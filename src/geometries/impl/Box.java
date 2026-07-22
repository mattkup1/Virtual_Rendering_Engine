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
 * Represents an axis-aligned box (cuboid) in a 3D Cartesian coordinate system.
 * <p>
 * The box is defined by two opposite corner points; the constructor normalizes
 * them into a minimum and a maximum corner so the box's faces are parallel to
 * the coordinate axes.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public final class Box extends Geometry {
    /**
     * The corner of the box with the smallest x/y/z coordinates
     */
    private final Point _min;
    /**
     * The corner of the box with the largest x/y/z coordinates
     */
    private final Point _max;

    /**
     * Constructs an axis-aligned box from two opposite corner points.
     * <p>
     * The corners do not need to be given in any particular order; the smaller
     * and larger coordinate on each axis are derived automatically.
     * </p>
     *
     * @param corner1 one corner of the box
     * @param corner2 the corner diagonally opposite to {@code corner1}
     * @throws IllegalArgumentException if the box has zero extent on any axis
     */
    public Box(Point corner1, Point corner2) {
        _min = new Point(
                Math.min(corner1.getX(), corner2.getX()),
                Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ()));
        _max = new Point(
                Math.max(corner1.getX(), corner2.getX()),
                Math.max(corner1.getY(), corner2.getY()),
                Math.max(corner1.getZ(), corner2.getZ()));

        if (isZero(_max.getX() - _min.getX())
                || isZero(_max.getY() - _min.getY())
                || isZero(_max.getZ() - _min.getZ()))
            throw new IllegalArgumentException("Box must have positive extent on every axis");
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        final Point origin = ray.getOrigin();
        final Vector dir = ray.getDirection();

        double tEntry = Double.NEGATIVE_INFINITY;
        double tExit = Double.POSITIVE_INFINITY;

        // X slab
        if (isZero(dir.getX())) {
            if (origin.getX() < _min.getX() || origin.getX() > _max.getX()) return null;
        } else {
            double t0 = (_min.getX() - origin.getX()) / dir.getX();
            double t1 = (_max.getX() - origin.getX()) / dir.getX();
            if (t0 > t1) { double tmp = t0; t0 = t1; t1 = tmp; }
            tEntry = Math.max(tEntry, t0);
            tExit = Math.min(tExit, t1);
            if (tEntry > tExit) return null;
        }

        // Y slab
        if (isZero(dir.getY())) {
            if (origin.getY() < _min.getY() || origin.getY() > _max.getY()) return null;
        } else {
            double t0 = (_min.getY() - origin.getY()) / dir.getY();
            double t1 = (_max.getY() - origin.getY()) / dir.getY();
            if (t0 > t1) { double tmp = t0; t0 = t1; t1 = tmp; }
            tEntry = Math.max(tEntry, t0);
            tExit = Math.min(tExit, t1);
            if (tEntry > tExit) return null;
        }

        // Z slab
        if (isZero(dir.getZ())) {
            if (origin.getZ() < _min.getZ() || origin.getZ() > _max.getZ()) return null;
        } else {
            double t0 = (_min.getZ() - origin.getZ()) / dir.getZ();
            double t1 = (_max.getZ() - origin.getZ()) / dir.getZ();
            if (t0 > t1) { double tmp = t0; t0 = t1; t1 = tmp; }
            tEntry = Math.max(tEntry, t0);
            tExit = Math.min(tExit, t1);
            if (tEntry > tExit) return null;
        }

        final boolean entryValid = tEntry > 0 && alignZero(tEntry - maxDistance) <= 0;
        final boolean exitValid = tExit > 0 && alignZero(tExit - maxDistance) <= 0;

        if (entryValid && exitValid) {
            return List.of(new Intersection(this, ray.getPoint(tEntry)), new Intersection(this, ray.getPoint(tExit)));
        }
        if (entryValid) return List.of(new Intersection(this, ray.getPoint(tEntry)));
        if (exitValid) return List.of(new Intersection(this, ray.getPoint(tExit)));
        return null;
    }

    @Override
    public Vector getNormal(Point point) {
        if (isZero(point.getX() - _min.getX())) return new Vector(-1, 0, 0);
        if (isZero(point.getX() - _max.getX())) return new Vector(1, 0, 0);
        if (isZero(point.getY() - _min.getY())) return new Vector(0, -1, 0);
        if (isZero(point.getY() - _max.getY())) return new Vector(0, 1, 0);
        if (isZero(point.getZ() - _min.getZ())) return new Vector(0, 0, -1);
        return new Vector(0, 0, 1);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(_min.getX(), _min.getY(), _min.getZ(), _max.getX(), _max.getY(), _max.getZ());
    }

    @Override
    public String toString() {
        return "Box: Min: " + _min + ", Max: " + _max;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Box other = (Box) obj;
        return _min.equals(other._min) && _max.equals(other._max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_min, _max);
    }
}
