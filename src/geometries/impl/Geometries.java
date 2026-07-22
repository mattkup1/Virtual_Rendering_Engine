package geometries.impl;

import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import primitives.BoundingBox;
import primitives.Point;
import primitives.Ray;

import static primitives.Util.alignZero;

/**
 * Representation of a collection of geometric shapes
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Geometries extends Intersectable {

    /**
     * Collection of geometric shapes
     */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Cached result of {@link #getBoundingBox()}, lazily computed and invalidated by
     * {@link #add(Intersectable...)}. A composite's box is otherwise recomputed as the
     * union of every child's own box on every call, which - unlike a leaf shape's O(1)
     * box - is O(size of subtree) and would make any tree built out of nested
     * {@code Geometries} (e.g. a BVH) cost O(n) per traversal step instead of O(1) per node.
     */
    private BoundingBox _cachedBoundingBox;
    /**
     * Whether {@link #_cachedBoundingBox} holds a valid (possibly {@code null}) cached value
     */
    private boolean _boundingBoxCached = false;

    /**
     * Parameterized constructor
     *
     * @param geometries the geometric shapes in the collection
     */
    public Geometries(Intersectable... geometries) {
        this.add(geometries);
    }

    /**
     * Add geometric shapes to the collection
     *
     * @param geometries the geometric shapes to add to the collection
     */
    public void add(Intersectable... geometries) {
        this.geometries.addAll(Arrays.asList(geometries));
        _boundingBoxCached = false;
    }

    /**
     * Find and return the intersection points with a given ray in a list
     * return only points that are within a given distance from the ray's origin
     *
     * @param ray the intersecting ray
     * @param maxDistance the maximum distance from the ray origin
     *                    which the intersection point must be within
     *                    to be added to the intersections list
     * @return the list of intersection points with the ray
     */
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> intersections = null;
        for (Intersectable geometry : geometries) {
            final BoundingBox box = geometry.getBoundingBox();
            if (box != null && !box.intersectsRay(ray, maxDistance)) {
                continue; // Ray misses this geometry's bounding box - skip the exact math
            }

            final var geometry_intersection = geometry.calcIntersections(ray);
            if (geometry_intersection != null) {
                for (var intersection : geometry_intersection) {
                    if (alignZero(intersection.point.distance(ray.getOrigin())) <= maxDistance) {
                        if (intersections == null) {
                            intersections = new ArrayList<>();
                        }
                        intersections.add(intersection);
                    }
                }
            }
        }
        return intersections; // Null if no intersection points
    }

    @Override
    public BoundingBox getBoundingBox() {
        if (!_boundingBoxCached) {
            _cachedBoundingBox = computeBoundingBox();
            _boundingBoxCached = true;
        }
        return _cachedBoundingBox;
    }

    /**
     * Computes the union of every child's bounding box.
     *
     * @return the union box, or {@code null} if this collection is empty or any child is unbounded
     */
    private BoundingBox computeBoundingBox() {
        BoundingBox union = null;
        for (Intersectable geometry : geometries) {
            final BoundingBox box = geometry.getBoundingBox();
            if (box == null) return null; // An unbounded child makes the whole group unbounded
            union = union == null ? box : union.union(box);
        }
        return union; // Null if this collection is empty
    }

    /**
     * Maximum number of items in a BVH leaf before it is split further.
     */
    private static final int BVH_LEAF_SIZE = 4;

    /**
     * Builds a bounding-volume hierarchy from a flat list of items: a balanced binary tree
     * of nested {@link Geometries}, recursively median-split along each subtree's longest
     * centroid-extent axis, so {@code calcIntersectionsHelper}'s existing per-child
     * bounding-box short-circuit prunes whole subtrees instead of only individual leaves.
     * <p>
     * Items with no bounding box (e.g. an infinite {@link Plane}) can't be spatially sorted,
     * so they are kept as direct children of the returned root alongside the tree of bounded
     * items, instead of being included in the split.
     * </p>
     *
     * @param items the items to organize; may be empty
     * @return the root of the resulting tree
     */
    public static Geometries buildBVH(List<Intersectable> items) {
        List<Intersectable> bounded = new ArrayList<>();
        List<Intersectable> unbounded = new ArrayList<>();
        for (Intersectable item : items) {
            if (item.getBoundingBox() == null) unbounded.add(item);
            else bounded.add(item);
        }

        Geometries root = new Geometries();
        if (!bounded.isEmpty()) root.add(buildBoundedBVH(bounded));
        for (Intersectable item : unbounded) root.add(item);
        return root;
    }

    /**
     * Recursively median-splits a list of items - all guaranteed to have a bounding box -
     * along their longest centroid-extent axis, bottoming out in a flat leaf once the list
     * is small enough.
     *
     * @param items the (mutable, locally-owned) items to organize; must all be bounded
     * @return the root of the resulting subtree
     */
    private static Geometries buildBoundedBVH(List<Intersectable> items) {
        if (items.size() <= BVH_LEAF_SIZE) {
            return new Geometries(items.toArray(new Intersectable[0]));
        }

        int axis = longestCentroidAxis(items);
        items.sort(Comparator.comparingDouble(item -> centroidCoordinate(item, axis)));

        int mid = items.size() / 2;
        Geometries left = buildBoundedBVH(new ArrayList<>(items.subList(0, mid)));
        Geometries right = buildBoundedBVH(new ArrayList<>(items.subList(mid, items.size())));

        Geometries node = new Geometries();
        node.add(left, right);
        return node;
    }

    /**
     * Determines which axis (0=X, 1=Y, 2=Z) has the largest spread among the items'
     * bounding-box centers, used to pick the BVH split axis.
     *
     * @param items the items to measure; must all be bounded
     * @return the axis index with the largest centroid spread
     */
    private static int longestCentroidAxis(List<Intersectable> items) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (Intersectable item : items) {
            Point center = item.getBoundingBox().center();
            minX = Math.min(minX, center.getX());
            maxX = Math.max(maxX, center.getX());
            minY = Math.min(minY, center.getY());
            maxY = Math.max(maxY, center.getY());
            minZ = Math.min(minZ, center.getZ());
            maxZ = Math.max(maxZ, center.getZ());
        }

        double spreadX = maxX - minX, spreadY = maxY - minY, spreadZ = maxZ - minZ;
        if (spreadX >= spreadY && spreadX >= spreadZ) return 0;
        return spreadY >= spreadZ ? 1 : 2;
    }

    /**
     * Returns an item's bounding-box center coordinate along the given axis.
     *
     * @param item the item; must be bounded
     * @param axis 0=X, 1=Y, 2=Z
     * @return the requested coordinate of the item's bounding-box center
     */
    private static double centroidCoordinate(Intersectable item, int axis) {
        Point center = item.getBoundingBox().center();
        return switch (axis) {
            case 0 -> center.getX();
            case 1 -> center.getY();
            default -> center.getZ();
        };
    }
}
