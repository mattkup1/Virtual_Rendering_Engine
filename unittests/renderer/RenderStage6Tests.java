package renderer;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Test rendering a basic image
 * @author Dan
 */
@SuppressWarnings("java:S109")
class RenderStage6Tests {
   /** Default constructor to satisfy JavaDoc generator */
   RenderStage6Tests() { /* to satisfy JavaDoc generator */ }

   /** Z axis location of triangles */
   private static final double Z          = -100D;
   /** Left, Top point */
   private static final Point  P_LT       = new Point(-100, 100, Z);
   /** Left, Middle point */
   private static final Point  P_LM       = new Point(-100, 0, Z);
   /** Left, Bottom point */
   private static final Point  P_LB       = new Point(-100, -100, Z);
   /** Middle, Top point */
   private static final Point  P_MT       = new Point(0, 100, Z);
   /** Middle, Bottom point */
   private static final Point  P_MB       = new Point(0, -100, Z);
   /** Right, Middle point */
   private static final Point  P_RM       = new Point(100, 0, Z);
   /** Right, Bottom point */
   private static final Point  P_RB       = new Point(100, -100, Z);
   /** Sphere center point */
   private static final Point  O          = new Point(0, 0, Z);
   /** Sphere radius */
   private static final double RADIUS     = 50D;

   private static int          RESOLUTION = 1000;

   /**
    * Build camera and render image with grid
    * @param scene    the scene to be used for the image
    * @param fileName the name of the image file
    */
   private static void createImage(Scene scene, String fileName) {
      Camera.getBuilder() //
         .setResolution(RESOLUTION, RESOLUTION) //
         .setLocation(Point.ZERO).setDirection(new Point(0, 0, -1), Vector.AXIS_Y) //
         .setVpDistance(100).setVpSize(500, 500) //
         .setRayTracer(scene, RayTracerType.SIMPLE) //
         .build() //
         .renderImage() //
         .printGrid(100, new Color(WHITE)) //
         .writeToImage(fileName);
   }

   /**
    * Produce a scene with basic 3D model - including individual emission lights of
    * the
    * bodies and render it into a png image with a grid
    */
   @Test
   void testRenderEmissionColor() {
      Scene scene = new Scene("Emission color").setAmbientLight(new AmbientLight(new Color(51, 51, 51)));
      scene.geometries //
         .add(// center
              new Sphere(O, RADIUS), // no emission
              // up left
              new Triangle(P_LM, P_MT, P_LT).setEmission(new Color(GREEN)),
              // down left
              new Triangle(P_LM, P_MB, P_LB).setEmission(new Color(RED)),
              // down right
              new Triangle(P_RM, P_MB, P_RB).setEmission(new Color(BLUE)));
      createImage(scene, "emission render test");
   }

   /**
    * Produce a scene with basic 3D model - including ambient light attenuation
    * factors of the
    * bodies and render it into a png image with a grid
    */
   @Test
   @Disabled("To be updated and enabled by students")
   void testRenderAmbientColor() {
      Scene scene = new Scene("Ambient colors"); // TODO by students
      scene.geometries //
         .add(// center
              new Sphere(O, RADIUS), // TODO by students
              // up left
              new Triangle(P_LM, P_MT, P_LT), // TODO by students
              // down left
              new Triangle(P_LM, P_MB, P_LB), // TODO by students
              // down right
              new Triangle(P_RM, P_MB, P_RB) // TODO by students
         );// TODO by students
      createImage(scene, "ambient render test");
   }
}
