package renderer;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CameraIntersectionIntegration {

    /**
     * Default constructor to satisfy Javadoc generator.
     */
    CameraIntersectionIntegration() { /* to satisfy Javadoc generator */ }

    /**
     * Default vTo vector used in test cameras
     */
    private static final Vector V_TO = new Vector(0, 0, -1);
    /**
     * Default view-plane distance used in tests.
     */
    private static final double VP_DISTANCE = 1d;

    /**
     * Default number of pixel columns in view plane
     */
    private static final int nX = 3;

    /**
     * Default number of pixel rows in view plane
     */
    private static final int nY = 3;
    /**
     * Default view plane width
     */
    private static final double width = 3d;

    /**
     * Default view plane height
     */
    private static final double height = 3d;

    /**
     * Common point at z = -2 used for plane and sphere integration test cases.
     */
    private static final Point P00N2 = new Point(0,0,-2);
    /**
     * Error message for invalid argument in camera build.
     */
    private static final String ERR_INCORRECT_NUM_INTERSECTIONS = "ERROR: Incorrect number of intersections";
    /**
     * Default camera1 located at the origin, used in the integration test cases.
     */
    final Camera camera1 = baseBuilder()
            .setLocation(Point.ZERO)
            .build();

    /**
     * Creates a basic builder with valid location and view-plane distance.
     *
     * @return initialized camera builder
     */
    private Camera.Builder baseBuilder() {
        return Camera.getBuilder()
                .setDirection(V_TO, Vector.AXIS_Y)
                .setVpDistance(VP_DISTANCE)
                .setResolution(nX, nY)
                .setVpSize(width, height);
    }

    /**
     * Helper function to assert correct intersections count in test cases
     *
     * @param camera        the camera
     * @param intersectable the intersectable geometric shape
     * @param expectedCount the expected number of intersection points between the camera rays and the intersectable
     * @param errorMessage  error message in case of test failure
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

        assertEquals(count, expectedCount, errorMessage);
    }

    @Test
    void testCameraRaySphereIntegration() {

        final Camera camera2 = baseBuilder()
                .setLocation(new Point(0, 0, 0.5))
                .build();

        // TC01: Intersection through center pixel only - 2 intersection points
        final Sphere sphereTC01 = new Sphere(new Point(0, 0, -3), 1d);
        assertIntersectionsCount(camera1, sphereTC01, 2, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC02: All pixel rays intersect the sphere twice - 18 intersection points
        final Sphere sphereTC02 = new Sphere(new Point(0, 0, -2.5), 2.5);
        assertIntersectionsCount(camera2, sphereTC02, 18, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC03: Intersection through non-corner pixels only - 10 intersection points
        final Sphere sphereTC03 = new Sphere(P00N2, 2d);
        assertIntersectionsCount(camera2, sphereTC03, 10, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC04: Camera inside sphere - 9 Intersection points
        final Sphere sphereTC04 = new Sphere(new Point(1, 0, 0), 4d);
        assertIntersectionsCount(camera1, sphereTC04, 9, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC05: Camera is in front the sphere - 0 Intersection points
        final Sphere sphereTC05 = new Sphere(new Point(0,0,1) , 0.5);
        assertIntersectionsCount(camera1, sphereTC05, 0, ERR_INCORRECT_NUM_INTERSECTIONS);

    }

    @Test
    void testCameraRayPlaneIntegration() {

        // TC01: All pixel rays intersect the plane - 9 Intersection points
        final Plane planeTC01 = new Plane(P00N2, Vector.AXIS_Z);
        assertIntersectionsCount(camera1 ,planeTC01, 9, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC02: All pixel rays intersect the tilted plane - 9 Intersection points
        final Plane planeTC02 = new Plane(P00N2, new Vector(0,1,-2));
        assertIntersectionsCount(camera1, planeTC02, 9, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC03: Only 6 pixel rays intersect the tilted plane - 6 Intersection points
        final Plane planeTC03 = new Plane(P00N2, new Vector(0,2,-1));
        assertIntersectionsCount(camera1, planeTC03, 6, ERR_INCORRECT_NUM_INTERSECTIONS);
    }

    @Test
    void testCameraRayTriangleIntegration() {

        // TC01: Intersection through center pixel only - 1 Intersection points
        final Triangle triangleTC01 = new Triangle(new Point(0,1,-2)
                ,new Point(1,-1,-2)
                ,new Point(-1,-1,-2));
        assertIntersectionsCount(camera1, triangleTC01, 1, ERR_INCORRECT_NUM_INTERSECTIONS);

        // TC02: Intersection through center and upper-middle pixels - 2 intersection points
        final Triangle triangleTC02 = new Triangle(new Point(0,10,-2)
                ,new Point(1,-1,-2)
                ,new Point(-1,-1,-2));
        assertIntersectionsCount(camera1, triangleTC02, 2,  ERR_INCORRECT_NUM_INTERSECTIONS);
    }
}
