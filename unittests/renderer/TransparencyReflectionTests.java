package renderer;

import static java.awt.Color.BLUE;
import static java.awt.Color.RED;

import geometries.impl.Tube;
import org.junit.jupiter.api.Test;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.SpotLight;
import primitives.*;
import scene.Scene;

/**
 * Tests for reflection and transparency functionality, test for partial
 * shadows
 * (with transparency)
 * @author Dan Zilberstein
 */
class TransparencyReflectionTests {
   /** Default constructor to satisfy JavaDoc generator */
   TransparencyReflectionTests() { /* to satisfy JavaDoc generator */ }

   /** Scene for the tests */
   private final Scene          _scene         = new Scene("Test scene");
   /** Camera builder for the tests with triangles */
   private final Camera.Builder _cameraBuilder = Camera.getBuilder()     //
      .setRayTracer(_scene, RayTracerType.SIMPLE);

   /** Produce a picture of a sphere lighted by a spot light */
   @Test
   @SuppressWarnings("java:S109")
   void testTwoSpheres() {
      _scene.geometries.add( //
                            new Sphere(new Point(0, 0, -50), 50D).setEmission(new Color(BLUE)) //
                               .setMaterial(new Material().setKD(0.4).setKS(0.3).setShininess(100).setKT(0.3)), //
                            new Sphere(new Point(0, 0, -50), 25D).setEmission(new Color(RED)) //
                               .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(100))); //
      _scene.lights.add( //
                        new SpotLight(new Color(1000, 600, 0), new Point(-100, -100, 500), new Vector(-1, -1, -2)) //
                           .setKl(0.0004).setKq(0.0000006));

      _cameraBuilder
         .setLocation(new Point(0, 0, 1000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpDistance(1000).setVpSize(150, 150) //
         .setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("refractionTwoSpheres");
   }

   /** Produce a picture of a sphere lighted by a spot light */
   @Test
   @SuppressWarnings("java:S109")
   void testTwoSpheresOnMirrors() {
      _scene.geometries.add( //
                            new Sphere(new Point(-950, -900, -1000), 400D).setEmission(new Color(0, 50, 100)) //
                               .setMaterial(new Material().setKD(0.25).setKS(0.25).setShininess(20) //
                                  .setKT(new Double3(0.5, 0, 0))), //
                            new Sphere(new Point(-950, -900, -1000), 200D).setEmission(new Color(100, 50, 20)) //
                               .setMaterial(new Material().setKD(0.25).setKS(0.25).setShininess(20)), //
                            new Triangle(new Point(1500, -1500, -1500), new Point(-1500, 1500, -1500), //
                                         new Point(670, 670, 3000)) //
                               .setEmission(new Color(20, 20, 20)) //
                               .setMaterial(new Material().setKR(1)), //
                            new Triangle(new Point(1500, -1500, -1500), new Point(-1500, 1500, -1500), //
                                         new Point(-1500, -1500, -2000)) //
                               .setEmission(new Color(20, 20, 20)) //
                               .setMaterial(new Material().setKR(new Double3(0.5, 0, 0.4))));
      _scene.setAmbientLight(new AmbientLight(new Color(26, 26, 26)));
      _scene.lights.add(new SpotLight(new Color(1020, 400, 400), new Point(-750, -750, -150), new Vector(-1, -1, -4)) //
         .setKl(0.00001).setKq(0.000005));

      _cameraBuilder
         .setLocation(new Point(0, 0, 10000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpDistance(10000).setVpSize(2500, 2500) //
         .setResolution(500, 500) //
         .build() //
         .renderImage() //
         .writeToImage("reflectionTwoSpheresMirrored");
   }

   /**
    * Produce a picture of a two triangles lighted by a spot light with a
    * partially
    * transparent Sphere producing partial shadow
    */
   @Test
   @SuppressWarnings("java:S109")
   void testTrianglesTransparentSphere() {
      _scene.geometries.add(
                            new Triangle(new Point(-150, -150, -115), new Point(150, -150, -135),
                                         new Point(75, 75, -150))
                               .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(60)),
                            new Triangle(new Point(-150, -150, -115), new Point(-70, 70, -140), new Point(75, 75, -150))
                               .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(60)),
                            new Sphere(new Point(60, 50, -50), 30D).setEmission(new Color(BLUE))
                               .setMaterial(new Material().setKD(0.2).setKS(0.2).setShininess(30).setKT(0.6)));
      _scene.setAmbientLight(new AmbientLight(new Color(38, 38, 38)));
      _scene.lights.add(
                        new SpotLight(new Color(700, 400, 400), new Point(60, 50, 0), new Vector(0, 0, -1))
                           .setKl(4E-5).setKq(2E-7));

      _cameraBuilder
         .setLocation(new Point(0, 0, 1000)) //
         .setDirection(Point.ZERO, Vector.AXIS_Y) //
         .setVpDistance(1000).setVpSize(200, 200) //
         .setResolution(600, 600) //
         .build() //
         .renderImage() //
         .writeToImage("refractionShadow");
   }


   /**
    * Original scene combining transparency, reflection, partial shadows
    * and an infinite cylinder.
    */
   @Test
   @SuppressWarnings("java:S109")
   void testMyTransparencyReflectionScene() {
      _scene.geometries.add(
              // Floor - simple dark stage
              new Triangle(new Point(-230, -120, -190),
                      new Point(230, -120, -190),
                      new Point(230, 150, -190))
                      .setEmission(new Color(28, 30, 42))
                      .setMaterial(new Material()
                              .setKD(0.65)
                              .setKS(0.18)
                              .setShininess(60)),

              new Triangle(new Point(-230, -120, -190),
                      new Point(230, 150, -190),
                      new Point(-230, 150, -190))
                      .setEmission(new Color(28, 30, 42))
                      .setMaterial(new Material()
                              .setKD(0.65)
                              .setKS(0.18)
                              .setShininess(60)),

              // One mirror triangle in the background
              // Demonstrates reflection without cluttering the scene
              new Triangle(new Point(-145, 90, -185),
                      new Point(-20, 90, -185),
                      new Point(-85, 90, 40))
                      .setEmission(new Color(42, 28, 48))
                      .setMaterial(new Material()
                              .setKD(0.10)
                              .setKS(0.55)
                              .setShininess(180)
                              .setKR(0.58)),

              // Transparent sphere on the right
              // Demonstrates transparency and partial shadow
              new Sphere(new Point(70, -15, -130), 36D)
                      .setEmission(new Color(35, 115, 150))
                      .setMaterial(new Material()
                              .setKD(0.14)
                              .setKS(0.60)
                              .setShininess(180)
                              .setKT(0.55)),

              // Infinite cylinder / tube in the foreground
              // Smaller and lower so it does not dominate the scene
              new Tube(11D,
                      new Ray(new Point(-115, -92, -178),
                              new Vector(1.3, 0.35, 0.08)))
                      .setEmission(new Color(70, 45, 88))
                      .setMaterial(new Material()
                              .setKD(0.45)
                              .setKS(0.35)
                              .setShininess(100))
      );

      _scene.setBackground(new Color(20, 36, 48));
      _scene.setAmbientLight(new AmbientLight(new Color(14, 16, 20)));

      _scene.lights.add(
              new SpotLight(new Color(310, 230, 160),
                      new Point(-130, -220, 150),
                      new Vector(0.8, 1.4, -1.8))
                      .setKl(0.00055)
                      .setKq(0.0000018));

      _scene.lights.add(
              // Weak cool fill light
              new SpotLight(new Color(45, 70, 110),
                      new Point(160, -170, 110),
                      new Vector(-1.2, 1.0, -1.3))
                      .setKl(0.0008)
                      .setKq(0.0000028));

      _cameraBuilder
              .setLocation(new Point(0, -285, 75))
              .setDirection(new Point(0, -5, -130), Vector.AXIS_Z)
              .setVpDistance(430)
              .setVpSize(245, 245)
              .setResolution(800, 800)
              .build()
              .renderImage()
              .writeToImage("TransparencyReflectionScene");
   }
}
