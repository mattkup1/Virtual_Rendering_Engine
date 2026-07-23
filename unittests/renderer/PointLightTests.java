package renderer;

import lighting.PointLight;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link PointLight}.
 * <p>
 * Verifies the light direction vector and distance-based intensity attenuation,
 * including standard and boundary-value cases.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
@Tag("unit")
public class PointLightTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    PointLightTests() { /* to satisfy Javadoc generator */ }

    /**
     * A uniform white light intensity used in the tests.
     */
    private static final Color INTENSITY_1 = new Color(100d, 100d, 100d);

    /**
     * The position of the point light source.
     */
    private static final Point SOURCE = new Point(0, 0, 5);

    /**
     * A sample point in space used for light calculations.
     */
    private static final Point P001 = new Point(0, 0, 1);

    /**
     * Constant attenuation coefficient for {@link #POINT_LIGHT_1}.
     */
    private static final double kC_1 = 1d;

    /**
     * Linear attenuation coefficient for {@link #POINT_LIGHT_1}.
     */
    private static final double kL_1 = 0d;

    /**
     * Quadratic attenuation coefficient for {@link #POINT_LIGHT_1}.
     */
    private static final double kQ_1 = 0.0001;

    /**
     * A point light with intensity {@link #INTENSITY_1} at {@link #SOURCE}.
     */
    private static final PointLight POINT_LIGHT_1 = new PointLight(INTENSITY_1, SOURCE, kC_1, kL_1, kQ_1);

    /**
     * Error message used when {@link PointLight#getIntensity(Point)} fails.
     */
    private static final String ERR_INTENSITY = "ERROR: getIntensity produced Wrong intensity";

    /**
     * Error message used when {@link PointLight#getL(Point)} fails.
     */
    private static final String ERR_GETL = "ERROR: getL produced Wrong vector";

    /**
     * Tests {@link PointLight#getL(Point)} for a standard point and for the boundary
     * case where the query point coincides with the light source.
     */
    @Test
    public void testGetL() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Test the vector from the light source to a given point
        final Vector expectedV1 = new Vector(0, 0, -1);
        assertEquals(expectedV1, POINT_LIGHT_1.getL(P001), ERR_GETL);

        // =============== Boundary Values Tests ==================
        // BV01: Same point as light point
        assertThrows(IllegalArgumentException.class, () -> POINT_LIGHT_1.getL(SOURCE), ERR_GETL);
    }

    /**
     * Tests {@link PointLight#getIntensity(Point)} for a standard point and for the boundary
     * case where the query point coincides with the light source.
     */
    @Test
    public void testGetIntensity() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Test the light intensity at a given point
        final Color expectedC1 = new Color(99.84025559105, 99.84025559105, 99.84025559105);
        assertEquals(expectedC1, POINT_LIGHT_1.getIntensity(P001), ERR_INTENSITY);

        // =============== Boundary Values Tests ==================
        // BV01: Same point as light point
        assertEquals(INTENSITY_1, POINT_LIGHT_1.getIntensity(SOURCE), ERR_INTENSITY);
    }

    /**
     * Tests {@link PointLight#getRadius()}/{@link PointLight#setRadius(double)}: defaults
     * to a hard-shadow light (radius 0), settable to enable soft shadows, rejects negative
     * values.
     */
    @Test
    public void testRadius() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Default radius is 0 (hard shadow)
        assertEquals(0, new PointLight(INTENSITY_1, SOURCE).getRadius(), "ERROR: default radius should be 0");

        // EP02: Radius is settable
        PointLight soft = new PointLight(INTENSITY_1, SOURCE).setRadius(5);
        assertEquals(5, soft.getRadius(), "ERROR: getRadius should return the value set by setRadius");

        // =============== Boundary Values Tests ==================
        // BV01: Negative radius is invalid
        assertThrows(IllegalArgumentException.class, () -> new PointLight(INTENSITY_1, SOURCE).setRadius(-1),
                "ERROR: negative radius should be rejected");
    }
}
