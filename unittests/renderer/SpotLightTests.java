package renderer;

import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link SpotLight}.
 * <p>
 * Verifies the light direction vector and spotlight intensity, including points
 * inside the beam, outside the beam, and on beam boundaries.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@Tag("unit")
public class SpotLightTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    SpotLightTests() { /* to satisfy Javadoc generator */ }

    /**
     * A uniform white light intensity used in the tests.
     */
    private static final Color INTENSITY_1 = new Color(100d, 100d, 100d);

    /**
     * The position of the spotlight source.
     */
    private static final Point SOURCE = new Point(0, 0, 5);

    /**
     * A sample point in front of the spotlight along its beam axis.
     */
    private static final Point P001 = new Point(0, 0, 1);

    /**
     * A sample point behind the spotlight, outside the beam.
     */
    private static final Point P006 = new Point(0, 0, 6);

    /**
     * A sample point perpendicular to the spotlight beam (90° from the axis).
     */
    private static final Point P105 = new Point(1, 0, 5);

    /**
     * The direction in which the spotlight is aimed (along the negative Z-axis).
     */
    private static final Vector DIRECTION = new Vector(0, 0, -1);

    /**
     * Constant attenuation coefficient for {@link #SPOT_LIGHT_1}.
     */
    private static final double kC_1 = 1d;

    /**
     * Linear attenuation coefficient for {@link #SPOT_LIGHT_1}.
     */
    private static final double kL_1 = 0d;

    /**
     * Quadratic attenuation coefficient for {@link #SPOT_LIGHT_1}.
     */
    private static final double kQ_1 = 0.0001;

    /**
     * A spotlight with intensity {@link #INTENSITY_1} at {@link #SOURCE} aimed along {@link #DIRECTION}.
     */
    private static final PointLight SPOT_LIGHT_1 =
            new SpotLight(INTENSITY_1, SOURCE, kC_1, kL_1, kQ_1, DIRECTION);

    /**
     * Error message used when {@link SpotLight#getIntensity(Point)} fails.
     */
    private static final String ERR_INTENSITY = "ERROR: getIntensity produced Wrong intensity";

    /**
     * Error message used when {@link SpotLight#getL(Point)} fails.
     */
    private static final String ERR_GETL = "ERROR: getL produced Wrong vector";

    /**
     * Tests {@link SpotLight#getL(Point)} for a standard point and for the boundary
     * case where the query point coincides with the light source.
     */
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

    /**
     * Tests {@link SpotLight#getIntensity(Point)} for points inside the beam, outside the beam,
     * and on beam boundaries.
     */
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
