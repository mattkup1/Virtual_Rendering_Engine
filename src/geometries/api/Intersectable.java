package geometries.api;

import java.util.List;
import primitives.Point;
import primitives.Ray;

abstract public class Intersectable {
    abstract public List<Point> findIntersections(Ray r);
}
