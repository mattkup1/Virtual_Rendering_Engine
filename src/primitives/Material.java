package primitives;

/**
 * Represents the physical properties of a geometry's surface in the Phong lighting model.
 * <p>
 * Defines ambient, diffuse, and specular reflection coefficients, as well as surface
 * shininess. This class follows the builder pattern, allowing method chaining during
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
     * Initialized to {@code (1, 1, 1)} by default.
     */
    public Double3 kA = Double3.ONE;

    /**
     * Specular reflection coefficient (kS).
     * Controls the intensity of mirror-like highlights.
     * Initialized to {@code (0, 0, 0)} by default.
     */
    public Double3 kS = Double3.ZERO;

    /**
     * Diffuse reflection coefficient (kD).
     * Controls how much incoming light is scattered evenly across the surface.
     * Initialized to {@code (0, 0, 0)} by default.
     */
    public Double3 kD = Double3.ZERO;

    /**
     * Shininess exponent (nShininess) used in specular calculations.
     * Higher values produce smaller, sharper highlights.
     * Initialized to {@code 0} by default.
     */
    public int nShininess = 0;

    /**
     * Default constructor for the {@link Material} class.
     */
    public Material() { /* To satisfy Javadoc generator */ }

    /**
     * Sets the ambient reflection coefficient using a {@link Double3} triad.
     *
     * @param kA the ambient reflection coefficient triad
     * @return this material for builder-style chaining
     */
    public Material setKA(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /**
     * Sets the ambient reflection coefficient to a uniform value across all components.
     *
     * @param kA the scalar ambient reflection coefficient
     * @return this material for builder-style chaining
     */
    public Material setKA(Double kA) {
        this.kA = new Double3(kA);
        return this;
    }

    /**
     * Sets the specular reflection coefficient using a {@link Double3} triad.
     *
     * @param kS the specular reflection coefficient triad
     * @return this material for builder-style chaining
     */
    public Material setKS(Double3 kS) {
        this.kS = kS;
        return this;
    }

    /**
     * Sets the specular reflection coefficient to a uniform value across all components.
     *
     * @param kS the scalar specular reflection coefficient
     * @return this material for builder-style chaining
     */
    public Material setKS(double kS) {
        this.kS = new Double3(kS);
        return this;
    }

    /**
     * Sets the diffuse reflection coefficient using a {@link Double3} triad.
     *
     * @param kD the diffuse reflection coefficient triad
     * @return this material for builder-style chaining
     */
    public Material setKD(Double3 kD) {
        this.kD = kD;
        return this;
    }

    /**
     * Sets the diffuse reflection coefficient to a uniform value across all components.
     *
     * @param kD the scalar diffuse reflection coefficient
     * @return this material for builder-style chaining
     */
    public Material setKD(double kD) {
        this.kD = new Double3(kD);
        return this;
    }

    /**
     * Sets the shininess exponent used in specular highlight calculations.
     *
     * @param nShininess the shininess exponent
     * @return this material for builder-style chaining
     */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
    }
}
