package renderer;

import lighting.DirectionalLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirectionalLightTests {

    private static final Vector V00N1 = new Vector(0, 0, -1);

    private static final Color LIGHT_COLOR_1 = new Color(100d, 100d, 100d);

    private static final DirectionalLight DIRECTIONAL_LIGHT_1 = new DirectionalLight(LIGHT_COLOR_1, V00N1);

    private static final Point P001 = new Point(0, 0, 1);

    @Test
    public void testGetL() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard test
        assertEquals(V00N1, DIRECTIONAL_LIGHT_1.getL(P001),
                "ERROR: getL produced Wrong Vector");
    }

    @Test
    public void testGetIntensity() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard test
        assertEquals(LIGHT_COLOR_1, DIRECTIONAL_LIGHT_1.getIntensity(P001),
                "ERROR: getIntensity produced Wrong Color");
    }
}
