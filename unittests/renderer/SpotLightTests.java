package renderer;

import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpotLightTests {

    private static final Color INTENSITY_1 = new Color(100d, 100d, 100d);

    private static final Point SOURCE = new Point(0, 0, 5);

    private static final Point P001 = new Point(0, 0, 1);

    private static final Point P006 = new Point(0, 0, 6);

    private static final Point P105 = new Point(1, 0, 5);

    private static final Vector DIRECTION = new Vector(0, 0, -1);

    private static final double kC_1 = 1d;

    private static final double kL_1 = 0d;

    private static final double kQ_1 = 0.0001;

    private static final PointLight SPOT_LIGHT_1 =
            new SpotLight(INTENSITY_1, SOURCE, kC_1, kL_1, kQ_1, DIRECTION);

    private static final String ERR_INTENSITY = "ERROR: getIntensity produced Wrong intensity";

    private static final String ERR_GETL = "ERROR: getL produced Wrong vector";

    @Test
    public void testGetL() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Test the vector from the the light source to a given point
        final Vector expectedV1 = new Vector(0, 0, -1);
        assertEquals(expectedV1, SPOT_LIGHT_1.getL(P001), ERR_GETL);

        // =============== Boundary Values Tests ==================
        // BV01: Point is the same as the spot light point
        assertThrows(IllegalArgumentException.class, () -> SPOT_LIGHT_1.getL(SOURCE), ERR_GETL);
    }

    @Test
    public void testGetIntensity() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Point in front of the spotlight
        final Color expectedC1 = new Color(99.84025559105, 99.84025559105, 99.84025559105);
        assertEquals(expectedC1, SPOT_LIGHT_1.getIntensity(P001), ERR_INTENSITY);

        // EP02: Point behind the spotlight
        assertEquals(Color.BLACK, SPOT_LIGHT_1.getIntensity(P006), ERR_INTENSITY);

        // =============== Boundary Values Tests ==================
        // BV01: Point is the same as the spot light point
        assertEquals(INTENSITY_1, SPOT_LIGHT_1.getIntensity(SOURCE), ERR_INTENSITY);
        // BV02: Point is 90 degrees relative to the light source in the light direction
        assertEquals(Color.BLACK, SPOT_LIGHT_1.getIntensity(P105), ERR_INTENSITY);
    }
}
