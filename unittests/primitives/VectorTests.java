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
    /**
     * Vector -> (1,2,3) used in vector tests
     */
    private static final Vector V1 = new Vector(1, 2, 3);
    /**
     * Vector -> (-2,-4,-6) used in vector tests
     */
    private static final Vector V2 = new Vector(-2, -4, -6);
    /**
     * Vector -> (0,3,-2) used in vector tests
     */
    private static final Vector V3 = new Vector(0, 3, -2);
    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    private static final String ERROR_EXCEPTION_EXPECTED = "ERROR: Exception expected";
    private static final String ERROR_EXCEPTION_THROWN = "ERROR: Exception thrown";
    private static final String ERROR_INCORRECT_RESULT = "ERROR: Incorrect result";

    /**
     * Test method for {@link primitives.Vector#Vector(double, double, double)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Correct vector construction
        assertDoesNotThrow(() -> new Vector(1, 1, 1), ERROR_EXCEPTION_THROWN);

        // =============== Boundary Values Tests ==================
        // BV01: Zero vector construction should throw exception
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0),
                ERROR_EXCEPTION_EXPECTED);

        // BV02: Zero vector construction from Double3 should throw exception
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO),
                ERROR_EXCEPTION_EXPECTED);
    }

    /**
     * Test method for {@link primitives.Vector#add(primitives.Vector)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard vector addition
        assertEquals(new Vector(1, 5, 1), V1.add(V3),
                ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Addition resulting in zero vector should throw exception
        Vector vOpposite = new Vector(-1, -2, -3);
        assertThrows(IllegalArgumentException.class, () -> V1.add(vOpposite),
                ERROR_EXCEPTION_EXPECTED);
    }

    /**
     * Test method for {@link primitives.Vector#subtract(primitives.Point)}
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Regular subtraction
        assertEquals(new Vector(1, -1, 5), V1.subtract(V3), ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Subtracting a vector from itself
        assertThrows(IllegalArgumentException.class, () -> V1.subtract(V1), ERROR_EXCEPTION_EXPECTED);
    }

    /**
     * Test method for {@link primitives.Vector#scale(double)}.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard scaling
        assertEquals(new Vector(2, 4, 6), V1.scale(2), ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Scaling by zero should throw exception
        assertThrows(IllegalArgumentException.class, () -> V1.scale(0),
                ERROR_EXCEPTION_EXPECTED);
    }

    /**
     * Test method for {@link primitives.Vector#dotProduct(primitives.Vector)}.
     */
    @Test
    void testDotProduct() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard dot product
        assertEquals(-28, V1.dotProduct(V2), DELTA, ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Dot product of orthogonal vectors should be zero
        assertEquals(0, V1.dotProduct(V3), DELTA, ERROR_INCORRECT_RESULT);
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
                ERROR_INCORRECT_RESULT);

        // EP02: Check result is orthogonal to its operands
        assertTrue(isZero(vr.dotProduct(V1)), ERROR_INCORRECT_RESULT);
        assertTrue(isZero(vr.dotProduct(V3)), ERROR_INCORRECT_RESULT);

        // =============== Boundary Values Tests ==================
        // BV01: Cross product of parallel vectors should throw exception (zero vector)
        assertThrows(IllegalArgumentException.class, () -> V1.crossProduct(V2),
                ERROR_EXCEPTION_EXPECTED);
    }

    /**
     * Test method for {@link primitives.Vector#lengthSquared()}.
     */
    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard length squared
        assertEquals(14, V1.lengthSquared(), DELTA, ERROR_INCORRECT_RESULT);
    }

    /**
     * Test method for {@link primitives.Vector#length()}.
     */
    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Standard length
        assertEquals(Math.sqrt(14), V1.length(), DELTA, ERROR_INCORRECT_RESULT);
    }

    /**
     * Test method for {@link primitives.Vector#normalize()}.
     */
    @Test
    void testNormalize() {
        Vector u = V1.normalize();

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normalized vector is a unit vector (length = 1)
        assertEquals(1, u.length(), DELTA, ERROR_INCORRECT_RESULT);

        // EP02: Normalized vector is parallel to original
        assertThrows(IllegalArgumentException.class, () -> V1.crossProduct(u), ERROR_EXCEPTION_EXPECTED);

        // EP03: Normalized vector points in the same direction
        assertTrue(V1.dotProduct(u) > 0, ERROR_INCORRECT_RESULT);
    }
}