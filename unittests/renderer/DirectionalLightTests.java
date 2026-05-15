package renderer;

import lighting.DirectionalLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

public class DirectionalLightTests {

    private static final DirectionalLight DIRECTIONAL_LIGHT_1 =
            new DirectionalLight(new Color(100d, 100d, 100d), Vector.AXIS_Z.scale(-1));

    private static final Point P001 = new Point(0, 0, 1);

    @Test
    public void testGetL() {
        // ============ Equivalence Partitions Tests ==============


    }

    @Test
    public void testGetIntensity() {
        // ============ Equivalence Partitions Tests ==============

    }
}
