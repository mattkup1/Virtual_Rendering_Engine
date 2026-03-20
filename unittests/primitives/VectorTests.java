package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static primitives.Util.isZero;

/**
 * Unit tests for class {@link Vector}
 * These tests verify:
 * <ul>
 * <li>Vector constructor validity</li>
 * <li>{@link Vector#add(Vector)}</li>
 * <li>{@link Vector#scale(double)}</li>
 * <li>{@link Vector#dotProduct(Vector)}</li>
 * <li>{@link Vector#crossProduct(Vector)}</li>
 * <li>{@link Vector#lengthSquared()}</li>
 * <li>{@link Vector#length()}</li>
 * <li>{@link Vector#normalize()}</li>
 * </ul>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
class VectorTests {

    private static final Vector V1 = new Vector(1, 2, 3);
    private static final Vector V2 = new Vector(-2, -4, -6);
    private static final Vector V3 = new Vector(0, 3, -2);
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link primitives.Vector#Vector(double, double, double)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct vector construction
        assertDoesNotThrow(() -> new Vector(1, 1, 1),
                "Failed to construct a valid Vector");

        // =============== Boundary Values Tests ==================
        // BV01: Zero vector construction should throw exception
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0),
                "Constructed a zero vector (0,0,0)");

        // BV02: Zero vector construction from Double3 should throw exception
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO),
                "Constructed a zero vector from Double3.ZERO");
    }

    /**
     * Test method for {@link primitives.Vector#add(primitives.Vector)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard vector addition
        assertEquals(new Vector(1, 5, 1), V1.add(V3),
                "add() produced wrong result");

        // =============== Boundary Values Tests ==================
        // BV01: Addition resulting in zero vector should throw exception
        Vector vOpposite = new Vector(-1, -2, -3);
        assertThrows(IllegalArgumentException.class, () -> V1.add(vOpposite),
                "Adding opposite vector did not throw exception for zero vector");
    }

    /**
     * Test method for {@link primitives.Vector#subtract(primitives.Point)}
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Regular subtraction
        assertEquals(new Vector(1, -1, 5), V1.subtract(V3), "Vectors should be equal");

        // =============== Boundary Values Tests ==================
        // BV01: Subtracting a vector from itself
    }

    /**
     * Test method for {@link primitives.Vector#scale(double)}.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard scaling
        assertEquals(new Vector(2, 4, 6), V1.scale(2),
                "scale() produced wrong result");

        // =============== Boundary Values Tests ==================
        // BV01: Scaling by zero should throw exception
        assertThrows(IllegalArgumentException.class, () -> V1.scale(0),
                "Scaling by zero did not throw exception");
    }

    /**
     * Test method for {@link primitives.Vector#dotProduct(primitives.Vector)}.
     */
    @Test
    void testDotProduct() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard dot product
        assertEquals(-28, V1.dotProduct(V2), DELTA,
                "dotProduct() result is wrong");

        // =============== Boundary Values Tests ==================
        // BV01: Dot product of orthogonal vectors should be zero
        assertEquals(0, V1.dotProduct(V3), DELTA,
                "dotProduct() for orthogonal vectors is not zero");
    }

    /**
     * Test method for {@link primitives.Vector#crossProduct(primitives.Vector)}.
     */
    @Test
    void testCrossProduct() {
        // ============ Equivalence Partitions Tests ==============
        Vector vr = V1.crossProduct(V3);

        // EP01 Check length of cross product result (area of parallelogram)
        assertEquals(V1.length() * V3.length(), vr.length(), DELTA,
                "crossProduct() wrong result length");

        // EP02: Check result is orthogonal to its operands
        assertTrue(isZero(vr.dotProduct(V1)), "crossProduct() result not orthogonal to v1");
        assertTrue(isZero(vr.dotProduct(V3)), "crossProduct() result not orthogonal to v3");

        // =============== Boundary Values Tests ==================
        // BV01: Cross product of parallel vectors should throw exception (zero vector)
        assertThrows(IllegalArgumentException.class, () -> V1.crossProduct(V2),
                "crossProduct() for parallel vectors did not throw exception");
    }

    /**
     * Test method for {@link primitives.Vector#lengthSquared()}.
     */
    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard length squared
        assertEquals(14, V1.lengthSquared(), DELTA,
                "lengthSquared() result is wrong");
    }

    /**
     * Test method for {@link primitives.Vector#length()}.
     */
    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard length
        assertEquals(Math.sqrt(14), V1.length(), DELTA,
                "length() result is wrong");
    }

    /**
     * Test method for {@link primitives.Vector#normalize()}.
     */
    @Test
    void testNormalize() {
        Vector u = V1.normalize();

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normalized vector is a unit vector (length = 1)
        assertEquals(1, u.length(), DELTA,
                "normalize() result is not a unit vector");

        // EP02: Normalized vector is parallel to original
        assertThrows(IllegalArgumentException.class, () -> V1.crossProduct(u),
                "normalize() result is not parallel to original vector");

        // EP03: Normalized vector points in the same direction
        assertTrue(V1.dotProduct(u) > 0,
                "normalize() result points in opposite direction");
    }
}