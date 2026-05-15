package renderer;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import static java.awt.Color.BLUE;

/**
 * End-to-end rendering tests with multiple simultaneous light sources.
 * <p>
 * Verifies that diffuse and specular contributions from directional, point,
 * and spot lights are combined correctly on the same geometry.
 */
class MultipleLightSourceTests {
    /** Image resolution for all tests in this class */
    private static final int RESOLUTION = 500;

    /** Default constructor to satisfy Javadoc generator */
    MultipleLightSourceTests() { /* to satisfy Javadoc generator */ }

    /** Shininess value for most of the geometries in the tests */
    private static final int SHININESS = 301;
    /** Diffusion attenuation factor for the sphere */
    private static final double KD = 0.5;
    /** Diffusion attenuation factor for the triangles */
    private static final Double3 KD3 = new Double3(0.2, 0.6, 0.4);
    /** Specular attenuation factor for the sphere */
    private static final double KS = 0.5;
    /** Specular attenuation factor for the triangles */
    private static final Double3 KS3 = new Double3(0.2, 0.4, 0.3);

    /** Color of the sphere (copied from {@link LightsTests}) */
    private static final Color SPHERE_COLOR = new Color(BLUE).reduce(2);
    /** Center of the sphere (copied from {@link LightsTests}) */
    private static final Point SPHERE_CENTER = new Point(0, 0, -50);
    /** Radius of the sphere (copied from {@link LightsTests}) */
    private static final double SPHERE_RADIUS = 50D;

    /**
     * The triangles' vertices (copied from {@link LightsTests})
     */
    private static final Point[] VERTICES = {
            new Point(-110, -110, -150),
            new Point(95, 100, -150),
            new Point(110, -110, -150),
            new Point(-75, 78, 100)
    };

    /**
     * Creates a camera builder aimed at the origin for the given scene.
     *
     * @param scene     the scene to render
     * @param vpWidth   view-plane width
     * @param vpHeight  view-plane height
     * @return a configured camera builder
     */
    private static Camera.Builder baseCamera(Scene scene, double vpWidth, double vpHeight) {
        return Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(vpWidth, vpHeight)
                .setVpDistance(1000);
    }

    /**
     * Creates the sphere geometry with a fresh material instance.
     *
     * @return the sphere used in the light tests
     */
    private static Geometry createSphere() {
        return new Sphere(SPHERE_CENTER, SPHERE_RADIUS)
                .setEmission(SPHERE_COLOR)
                .setMaterial(new Material().setKD(KD).setKS(KS).setShininess(SHININESS));
    }

    /**
     * Creates the first triangle with a fresh material instance.
     *
     * @return the first triangle used in the light tests
     */
    private static Geometry createTriangle1() {
        return new Triangle(VERTICES[0], VERTICES[1], VERTICES[2])
                .setMaterial(new Material().setKD(KD3).setKS(KS3).setShininess(SHININESS));
    }

    /**
     * Creates the second triangle with a fresh material instance.
     *
     * @return the second triangle used in the light tests
     */
    private static Geometry createTriangle2() {
        return new Triangle(VERTICES[0], VERTICES[1], VERTICES[3])
                .setMaterial(new Material().setKD(KD3).setKS(KS3).setShininess(SHININESS));
    }

    /**
     * Renders the given scene and writes the result to an image file.
     *
     * @param scene     the scene to render
     * @param vpWidth   view-plane width
     * @param vpHeight  view-plane height
     * @param imageName output image name (without extension)
     */
    private void render(Scene scene, double vpWidth, double vpHeight, String imageName) {
        baseCamera(scene, vpWidth, vpHeight)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage(imageName);
    }

    /**
     * Required test: sphere lit simultaneously by directional, point, and spot lights.
     * Each light has a different color, position, and/or direction.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testSphereAllLights() {
        Scene scene = new Scene("Sphere multiple lights");

        scene.geometries.add(createSphere());

        scene.lights.add(new DirectionalLight(
                new Color(380, 360, 300),
                new Vector(-1, -1, 0.5)));

        scene.lights.add(new PointLight(
                new Color(120, 420, 90),
                new Point(0, 0, 50))
                .setKl(0.001).setKq(0.00015));

        scene.lights.add(new SpotLight(
                new Color(460, 90, 90),
                new Point(-70, 20, 0),
                new Vector(1.3, -0.5, -1))
                .setKl(0.01).setKq(0.000001));

        render(scene, 150, 150, "multiLightSphereAll");
    }

    /**
     * Required test: two triangles lit simultaneously by directional, point, and spot lights.
     * Each light has a different color, position, and/or direction.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testTrianglesAllLights() {
        Scene scene = new Scene("Triangles multiple lights")
                .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

        scene.geometries.add(createTriangle1(), createTriangle2());

        scene.lights.add(new DirectionalLight(
                new Color(340, 130, 130),
                new Vector(-2, 0, -1)));

        scene.lights.add(new PointLight(
                new Color(220, 320, 150),
                new Point(30, 0, -50))
                .setKl(0.002).setKq(0.00025));

        scene.lights.add(new SpotLight(
                new Color(110, 110, 420),
                new Point(60, 10, -80),
                new Vector(0, 0, -1))
                .setKl(0.009).setKq(0.00001));

        render(scene, 200, 200, "multiLightTrianglesAll");
    }

    /**
     * Sphere lit by a directional light and a point light with distinct colors and directions.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testSphereDirectionalAndPoint() {
        Scene scene = new Scene("Sphere directional and point");

        scene.geometries.add(createSphere());

        scene.lights.add(new DirectionalLight(
                new Color(320, 280, 220),
                new Vector(1, 1, -0.5)));

        scene.lights.add(new PointLight(
                new Color(90, 380, 120),
                new Point(-50, -50, 25))
                .setKl(0.001).setKq(0.0002));

        render(scene, 150, 150, "multiLightSphereDirectionalPoint");
    }

    /**
     * Sphere lit by a directional light and a spotlight with distinct colors and directions.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testSphereDirectionalAndSpot() {
        Scene scene = new Scene("Sphere directional and spot");

        scene.geometries.add(createSphere());

        scene.lights.add(new DirectionalLight(
                new Color(300, 300, 260),
                new Vector(-1, -1, 0.5)));

        scene.lights.add(new SpotLight(
                new Color(420, 110, 110),
                new Point(-70, 20, 0),
                new Vector(1.3, -0.5, -1))
                .setKl(0.01).setKq(0.000001));

        render(scene, 150, 150, "multiLightSphereDirectionalSpot");
    }

    /**
     * Sphere lit by a point light and a spotlight at different positions.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testSpherePointAndSpot() {
        Scene scene = new Scene("Sphere point and spot");

        scene.geometries.add(createSphere());

        scene.lights.add(new PointLight(
                new Color(110, 400, 100),
                new Point(0, 0, 50))
                .setKl(0.001).setKq(0.00015));

        scene.lights.add(new SpotLight(
                new Color(480, 120, 80),
                new Point(-50, -50, 25),
                new Vector(1, 1, -0.5))
                .setKl(0.001).setKq(0.0001));

        render(scene, 150, 150, "multiLightSpherePointSpot");
    }

    /**
     * Triangles lit by a directional light and a point light with distinct colors and directions.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testTrianglesDirectionalAndPoint() {
        Scene scene = new Scene("Triangles directional and point")
                .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

        scene.geometries.add(createTriangle1(), createTriangle2());

        scene.lights.add(new DirectionalLight(
                new Color(360, 200, 160),
                new Vector(-2, -2, -2)));

        scene.lights.add(new PointLight(
                new Color(200, 300, 180),
                new Point(30, 10, -100))
                .setKl(0.001).setKq(0.0002));

        render(scene, 200, 200, "multiLightTrianglesDirectionalPoint");
    }

    /**
     * Triangles lit by a directional light and a spotlight with distinct colors and directions.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testTrianglesDirectionalAndSpot() {
        Scene scene = new Scene("Triangles directional and spot")
                .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

        scene.geometries.add(createTriangle1(), createTriangle2());

        scene.lights.add(new DirectionalLight(
                new Color(320, 150, 150),
                new Vector(-2, -2, -2)));

        scene.lights.add(new SpotLight(
                new Color(140, 140, 460),
                new Point(30, 10, -100),
                new Vector(-2, -2, -2))
                .setKl(0.001).setKq(0.0001));

        render(scene, 200, 200, "multiLightTrianglesDirectionalSpot");
    }

    /**
     * Triangles lit by a point light and a spotlight at different positions and directions.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testTrianglesPointAndSpot() {
        Scene scene = new Scene("Triangles point and spot")
                .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

        scene.geometries.add(createTriangle1(), createTriangle2());

        scene.lights.add(new PointLight(
                new Color(240, 320, 170),
                new Point(30, 10, -100))
                .setKl(0.001).setKq(0.0002));

        scene.lights.add(new SpotLight(
                new Color(500, 180, 120),
                new Point(60, 10, -80),
                new Vector(0, 0, -1))
                .setKl(0.009).setKq(0.00001));

        render(scene, 200, 200, "multiLightTrianglesPointSpot");
    }

    /**
     * Sphere lit by three lights with alternate positions and lower intensities
     * to emphasize color mixing without saturation.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testSphereAllLightsSoft() {
        Scene scene = new Scene("Sphere multiple lights soft");

        scene.geometries.add(createSphere());

        scene.lights.add(new DirectionalLight(
                new Color(260, 240, 200),
                new Vector(-0.5, -1, -0.3)));

        scene.lights.add(new PointLight(
                new Color(80, 300, 200),
                new Point(40, -30, 10))
                .setKl(0.0015).setKq(0.0003));

        scene.lights.add(new SpotLight(
                new Color(300, 120, 260),
                new Point(-30, 40, 15),
                new Vector(0.8, -1.2, -0.7))
                .setKl(0.008).setKq(0.000002));

        render(scene, 150, 150, "multiLightSphereAllSoft");
    }

    /**
     * Triangles lit by three lights with alternate positions and lower intensities
     * to emphasize color mixing without saturation.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testTrianglesAllLightsSoft() {
        Scene scene = new Scene("Triangles multiple lights soft")
                .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

        scene.geometries.add(createTriangle1(), createTriangle2());

        scene.lights.add(new DirectionalLight(
                new Color(250, 180, 140),
                new Vector(1, -1, -0.5)));

        scene.lights.add(new PointLight(
                new Color(170, 260, 220),
                new Point(-20, 30, -80))
                .setKl(0.0025).setKq(0.0003));

        scene.lights.add(new SpotLight(
                new Color(200, 160, 360),
                new Point(80, -20, -60),
                new Vector(-1.5, 0.5, -1))
                .setKl(0.012).setKq(0.00002));

        render(scene, 200, 200, "multiLightTrianglesAllSoft");
    }
}
