package renderer;

import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link BeamSampler}.
 * <p>
 * {@link BeamSampler#sampleBeam} is randomized (no seed control), so these tests avoid
 * asserting exact ray directions and instead check deterministic invariants: the
 * zero-blur/single-sample fast paths, and that every sampled direction still points
 * toward the same general side as the ideal direction.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class BeamSamplerTests {
    /**
     * Default constructor to satisfy Javadoc generator
     */
    BeamSamplerTests() { /* to satisfy Javadoc generator */ }

    /**
     * Test method for the zero-blur-radius fast path of both {@link BeamSampler#sampleBeam}
     * overloads: collapses to a single ray along the (normalized) ideal direction,
     * regardless of target distance.
     */
    @Test
    void testSampleBeamZeroBlurRadius() {
        Point origin = new Point(0, 0, 0);
        Vector direction = new Vector(0, 0, -1);
        Vector normal = new Vector(0, 0, 1);

        List<Ray> defaultDistance = BeamSampler.sampleBeam(origin, direction, normal, 0, 10);
        assertEquals(1, defaultDistance.size(), "ERROR: zero blur radius should collapse to one ray");
        assertEquals(direction, defaultDistance.getFirst().getDirection(), "ERROR: incorrect ideal direction");

        List<Ray> explicitDistance = BeamSampler.sampleBeam(origin, direction, normal, 0, 10, 50);
        assertEquals(1, explicitDistance.size(), "ERROR: zero blur radius should collapse to one ray");
        assertEquals(direction, explicitDistance.getFirst().getDirection(), "ERROR: incorrect ideal direction");
    }

    /**
     * Test method for the {@code sampleCount <= 1} fast path.
     */
    @Test
    void testSampleBeamSingleSample() {
        Point origin = new Point(0, 0, 0);
        Vector direction = new Vector(0, 0, -1);
        Vector normal = new Vector(0, 0, 1);

        List<Ray> beam = BeamSampler.sampleBeam(origin, direction, normal, 5, 1, 50);
        assertEquals(1, beam.size(), "ERROR: sampleCount <= 1 should collapse to one ray");
        assertEquals(direction, beam.getFirst().getDirection(), "ERROR: incorrect ideal direction");
    }

    /**
     * Test method verifying every sampled ray in a jittered beam still points toward the
     * same general side as the ideal direction, for both the default and an explicit
     * (much closer) target distance - a scaled-down target distance means a
     * proportionally scaled-down blur radius produces the same disk geometry, so the
     * invariant should hold identically either way.
     */
    @Test
    void testSampleBeamStaysOnIdealSide() {
        Point origin = new Point(0, 0, 0);
        Vector direction = new Vector(0, 0, -1);
        Vector normal = new Vector(0, 0, 1);

        List<Ray> nearBeam = BeamSampler.sampleBeam(origin, direction, normal, 2, 40, 10);
        for (Ray ray : nearBeam) {
            assertTrue(ray.getDirection().dotProduct(direction) > 0,
                    "ERROR: sampled direction should stay on the ideal direction's side");
        }

        List<Ray> farBeam = BeamSampler.sampleBeam(origin, direction, normal, 2, 40, 1000);
        for (Ray ray : farBeam) {
            assertTrue(ray.getDirection().dotProduct(direction) > 0,
                    "ERROR: sampled direction should stay on the ideal direction's side");
        }
    }
}
