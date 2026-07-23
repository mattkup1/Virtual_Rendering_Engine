package renderer;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for methods {@link Camera#constructRay(int, int)}
 * and {@link geometries.api.Intersectable#findIntersections(Ray)}.
 * These tests verify the interaction between the camera ray generation
 * and the intersection logic for spheres, planes, and triangles.
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@Tag("integration")
public class CameraIntersectionIntegration {

    /**
     * Default constructor to satisfy Javadoc generator.
     */
    CameraIntersectionIntegration() { /* to satisfy Javadoc generator */ }

    /**
     * Direction vector from the camera towards the view plane (Negative Z axis).
     */
    private static final Vector V_TO = new Vector(0, 0, -1);

    /**
     * Distance from the camera location to the view plane.
     */
    private static final double VP_DISTANCE = 1d;

    /**
     * Number of horizontal pixels in the test resolution (3x3 grid).
     */
    private static final int nX = 3;

    /**
     * Number of vertical pixels in the test resolution (3x3 grid).
     */
    private static final int nY = 3;

    /**
     * Total width of the view plane.
     */
    private static final double width = 3d;

    /**
     * Total height of the view plane.
     */
    private static final double height = 3d;

    /**
     * Common point used for positioning geometries in front of the camera.
     */
    private static final Point P00N2 = new Point(0, 0, -2);

    /**
     * Error message constant for intersection count mismatches.
     */
    private static final String ERR_INCORRECT_NUM_INTERSECTIONS = "ERROR: Incorrect number of intersections";

    /**
     * Base camera instance located at the origin (0,0,0).
     * Used as a standard starting point for most integration cases.
     */
    final Camera camera1 = baseBuilder()
            .setLocation(Point.ZERO)
            .build();

    /**
     * Creates a pre-configured camera builder with the default resolution,
     * view-plane size, and direction vectors used across all integration tests.
     *
     * @return a builder initialized with standard test parameters
     */
    private Camera.Builder baseBuilder() {
        return Camera.getBuilder()
                .setDirection(V_TO, Vector.AXIS_Y)
                .setVpDistance(VP_DISTANCE)
                .setResolution(nX, nY)
                .setVpSize(width, height);
    }

    /**
     * Helper function to iterate through all pixels in the 3x3 grid, construct rays,
     * and sum up all intersections with the given geometric shape.
     *
     * @param camera        the camera instance to generate rays
     * @param intersectable the geometric shape to check for intersections
     * @param expectedCount the expected total number of intersection points
     * @param errorMessage  message to display if the actual count differs from expected
     */
    private void assertIntersectionsCount(Camera camera, Intersectable intersectable, int expectedCount, String errorMessage) {
        int count = 0;
        for (int i = 0; i < nX; ++i) {
            for (int j = 0; j < nY; ++j) {
                var intersections = intersectable.findIntersections(camera.constructRay(i, j));
                if (intersections != null)
                    count += intersections.size();
            }
        }
        assertEquals(expectedCount, count, errorMessage);
    }

    /**
     * Integration tests for camera rays intersecting a {@link Sphere}.
     * Covers cases where the sphere is small, large, containing the camera, or behind the camera.
     */
    @Test
    void testCameraRaySphereIntegration() {
        // Camera moved slightly forward to test sphere proximity
        final Camera camera2 = baseBuilder()
                .setLocation(new Point(0, 0, 0.5))
                .build();

        // TC01: Small sphere in front of the center pixel (2 points)
        final Sphere sphereTC01 = new Sphere(new Point(0, 0, -3), 1d);
        assertIntersectionsCount(camera1, sphereTC01, 2, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC02: Large sphere where every ray hits twice (18 points)
        final Sphere sphereTC02 = new Sphere(new Point(0, 0, -2.5), 2.5);
        assertIntersectionsCount(camera2, sphereTC02, 18, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC03: Medium sphere intersecting center and side pixels (10 points)
        final Sphere sphereTC03 = new Sphere(P00N2, 2d);
        assertIntersectionsCount(camera2, sphereTC03, 10, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC04: Camera is inside the sphere - all rays hit once from the inside (9 points)
        final Sphere sphereTC04 = new Sphere(new Point(1, 0, 0), 4d);
        assertIntersectionsCount(camera1, sphereTC04, 9, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC05: Sphere is behind the camera location (0 points)
        final Sphere sphereTC05 = new Sphere(new Point(0, 0, 1), 0.5);
        assertIntersectionsCount(camera1, sphereTC05, 0, ERR_INCORRECT_NUM_INTERSECTIONS);
    }

    /**
     * Integration tests for camera rays intersecting a {@link Plane}.
     * Covers parallel planes and planes at various tilted angles.
     */
    @Test
    void testCameraRayPlaneIntegration() {
        // TC01: Plane parallel to the view plane (9 points)
        final Plane planeTC01 = new Plane(P00N2, Vector.AXIS_Z);
        assertIntersectionsCount(camera1, planeTC01, 9, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC02: Plane tilted slightly - all rays still intersect (9 points)
        final Plane planeTC02 = new Plane(P00N2, new Vector(0, 1, -2));
        assertIntersectionsCount(camera1, planeTC02, 9, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC03: Plane tilted sharply - upper rows of rays miss the plane (6 points)
        final Plane planeTC03 = new Plane(P00N2, new Vector(0, 2, -1));
        assertIntersectionsCount(camera1, planeTC03, 6, ERR_INCORRECT_NUM_INTERSECTIONS);
    }

    /**
     * Integration tests for camera rays intersecting a {@link Triangle}.
     * Verifies intersections with small and large triangles positioned in front of the camera.
     */
    @Test
    void testCameraRayTriangleIntegration() {
        // TC01: Small triangle in front of the center pixel only (1 point)
        final Triangle triangleTC01 = new Triangle(new Point(0, 1, -2)
                , new Point(1, -1, -2)
                , new Point(-1, -1, -2));
        assertIntersectionsCount(camera1, triangleTC01, 1, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC02: Tall triangle intersecting the center and top-middle pixel (2 points)
        final Triangle triangleTC02 = new Triangle(new Point(0, 10, -2)
                , new Point(1, -1, -2)
                , new Point(-1, -1, -2));
        assertIntersectionsCount(camera1, triangleTC02, 2, ERR_INCORRECT_NUM_INTERSECTIONS);
    }
}