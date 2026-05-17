package renderer;

import lighting.DirectionalLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link DirectionalLight}.
 * <p>
 * Verifies that the light direction and intensity behave as expected for a
 * directional source with constant illumination at every point in space.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class DirectionalLightTests {

    /**
     * Default constructor to satisfy Javadoc generator
     */
    DirectionalLightTests() { /* to satisfy Javadoc generator */ }

    /**
     * A normalized direction vector along the negative Z-axis.
     */
    private static final Vector V00N1 = new Vector(0, 0, -1);

    /**
     * A uniform white light intensity used in the tests.
     */
    private static final Color LIGHT_COLOR_1 = new Color(100d, 100d, 100d);

    /**
     * Directional light used in some tests
     */
    private static final DirectionalLight DIRECTIONAL_LIGHT_1 = new DirectionalLight(LIGHT_COLOR_1, V00N1);

    /**
     * A sample point in space used for light calculations.
     */
    private static final Point P001 = new Point(0, 0, 1);

    /**
     * Tests that {@link DirectionalLight#getL(Point)} returns a constant direction
     * independent of the query point.
     */
    @Test
    public void testGetL() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard test
        assertEquals(V00N1, DIRECTIONAL_LIGHT_1.getL(P001),
                "ERROR: getL produced Wrong Vector");
    }

    /**
     * Tests that {@link DirectionalLight#getIntensity(Point)} returns the same
     * intensity at every point in space.
     */
    @Test
    public void testGetIntensity() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard test
        assertEquals(LIGHT_COLOR_1, DIRECTIONAL_LIGHT_1.getIntensity(P001),
                "ERROR: getIntensity produced Wrong Color");
    }
}
