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
     * of nested {@link Geometries}, recursively split via the surface-area heuristic (SAH),
     * so {@code calcIntersectionsHelper}'s existing per-child bounding-box short-circuit
     * prunes whole subtrees instead of only individual leaves.
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
     * Recursively splits a list of items - all guaranteed to have a bounding box - via the
     * surface-area heuristic (SAH), bottoming out in a flat leaf once the list is small
     * enough.
     * <p>
     * For each of the 3 axes, items are sorted by bounding-box centroid and every split
     * position along that sorted order is scored in a single {@link #sahSplit} sweep; the
     * split with the lowest cost across all 3 axes wins. This produces tighter,
     * more balanced-by-cost subtrees than a fixed median split, especially for
     * non-uniformly distributed items (e.g. an imported mesh with a dense cluster of small
     * triangles alongside a few large ones).
     * </p>
     *
     * @param items the (mutable, locally-owned) items to organize; must all be bounded
     * @return the root of the resulting subtree
     */
    private static Geometries buildBoundedBVH(List<Intersectable> items) {
        if (items.size() <= BVH_LEAF_SIZE) {
            return new Geometries(items.toArray(new Intersectable[0]));
        }

        int bestAxis = 0;
        int bestIndex = items.size() / 2;
        double bestCost = Double.POSITIVE_INFINITY;

        for (int axis = 0; axis < 3; ++axis) {
            final int currentAxis = axis;
            List<Intersectable> sorted = new ArrayList<>(items);
            sorted.sort(Comparator.comparingDouble(item -> centroidCoordinate(item, currentAxis)));

            SahSplit split = sahSplit(sorted);
            if (split.cost() < bestCost) {
                bestCost = split.cost();
                bestAxis = axis;
                bestIndex = split.index();
            }
        }

        final int splitAxis = bestAxis;
        items.sort(Comparator.comparingDouble(item -> centroidCoordinate(item, splitAxis)));

        Geometries left = buildBoundedBVH(new ArrayList<>(items.subList(0, bestIndex)));
        Geometries right = buildBoundedBVH(new ArrayList<>(items.subList(bestIndex, items.size())));

        Geometries node = new Geometries();
        node.add(left, right);
        return node;
    }

    /**
     * The lowest-cost split position found by {@link #sahSplit} along one axis, and its
     * surface-area-heuristic cost.
     *
     * @param index the split position (items {@code [0, index)} left, {@code [index, size)}
     *              right)
     * @param cost  the SAH cost of splitting at {@code index}
     */
    private record SahSplit(int index, double cost) {
    }

    /**
     * Sweeps every split position of a centroid-sorted item list in O(n), scoring each via
     * the surface-area heuristic: the expected traversal cost of a node is proportional to
     * its bounding box's surface area times the number of items it contains, so the cost of
     * a split (items {@code [0, index)} left, {@code [index, size)} right) is the sum of
     * that product across both halves. Prefix and suffix bounding-box unions are
     * accumulated incrementally so every split position is scored in O(1) off the previous
     * one, instead of re-summing each half's box from scratch per candidate index.
     *
     * @param sorted items sorted by centroid coordinate along the candidate split axis
     * @return the lowest-cost split position along this axis and its cost
     */
    private static SahSplit sahSplit(List<Intersectable> sorted) {
        int n = sorted.size();
        double[] prefixArea = new double[n];
        double[] suffixArea = new double[n];

        BoundingBox box = sorted.get(0).getBoundingBox();
        prefixArea[0] = box.surfaceArea();
        for (int i = 1; i < n; ++i) {
            box = box.union(sorted.get(i).getBoundingBox());
            prefixArea[i] = box.surfaceArea();
        }

        box = sorted.get(n - 1).getBoundingBox();
        suffixArea[n - 1] = box.surfaceArea();
        for (int i = n - 2; i >= 0; --i) {
            box = box.union(sorted.get(i).getBoundingBox());
            suffixArea[i] = box.surfaceArea();
        }

        double bestCost = Double.POSITIVE_INFINITY;
        int bestIndex = n / 2;
        for (int index = 1; index < n; ++index) {
            double cost = prefixArea[index - 1] * index + suffixArea[index] * (n - index);
            if (cost < bestCost) {
                bestCost = cost;
                bestIndex = index;
            }
        }

        return new SahSplit(bestIndex, bestCost);
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
