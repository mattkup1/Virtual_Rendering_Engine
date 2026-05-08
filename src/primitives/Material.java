package primitives;

/**
 * The Material class represents the physical properties of a geometry's surface.
 * It defines how light interacts with the object, specifically regarding
 * reflection, transparency, and shininess.
 * <p>
 * This class follows the Builder pattern, allowing for method chaining during
 * material definition.
 * </p>
 *
 * @author mattkuperwasser
 * @author moshehanau
 */
public class Material {
    /**
     * Ambient reflection coefficient (kA).
     * Represents the ratio of ambient light reflected by the material.
     * Initialized to (1,1,1) by default.
     */
    public Double3 kA = Double3.ONE;

    /**
     * Default constructor for the Material class.
     */
    public Material() { /* To satisfy Javadoc generator */ }

    /**
     * Sets the ambient reflection coefficient using a Double3 triad.
     *
     * @param kA The ambient reflection coefficient triad
     * @return The Material object itself for builder-style chaining
     */
    public Material setKA(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /**
     * Sets the ambient reflection coefficient to a uniform value across all components.
     * This creates a new Double3 where all three values are the same
     *
     * @param kA The scalar value for the ambient reflection coefficient
     * @return The Material object itself for builder-style chaining
     */
    public Material setKA(Double kA) {
        this.kA = new Double3(kA);
        return this;
    }
}
