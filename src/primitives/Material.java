package primitives;

/**
 * Represents the physical properties of a geometry's surface in the Phong lighting model.
 * <p>
 * Defines ambient, diffuse, and specular reflection coefficients, surface shininess,
 * recursive transparency ({@code kT}) and reflection ({@code kR}) coefficients, and
 * per-effect blur radii ({@code blurT} for diffuse glass, {@code blurR} for glossy
 * surfaces). This class follows the builder pattern, allowing method chaining during
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
     * Transparency coefficient (kT).
     * Fraction of incoming light that passes through the surface and continues
     * along the same direction (refraction is not modeled). A value of
     * {@code (0, 0, 0)} makes the surface fully opaque.
     * Initialized to {@code (0, 0, 0)} by default.
     */
    public Double3 kT = Double3.ZERO;

    /**
     * Reflection coefficient (kR).
     * Fraction of incoming light that is mirror-reflected by the surface.
     * A value of {@code (0, 0, 0)} disables the recursive reflection term.
     * Initialized to {@code (0, 0, 0)} by default.
     */
    public Double3 kR = Double3.ZERO;

    /**
     * Glossy reflection blur radius.
     * Radius of the sampling disk placed at
     * {@link renderer.BeamSampler#sampleBeam BeamSampler}'s target distance
     * past the surface, used to scatter reflection rays around the ideal
     * mirror direction. {@code 0} produces a perfectly sharp mirror;
     * larger values produce progressively blurrier (brushed-metal) reflections.
     * Has no effect when {@code kR} is {@code (0, 0, 0)}.
     * Initialized to {@code 0} by default.
     */
    public double blurR = 0;

    /**
     * Diffuse transparency blur radius.
     * Radius of the sampling disk placed at
     * {@link renderer.BeamSampler#sampleBeam BeamSampler}'s target distance
     * past the surface, used to scatter transparency rays around the ideal
     * through-direction. {@code 0} produces perfectly clear glass; larger
     * values produce progressively more frosted glass.
     * Has no effect when {@code kT} is {@code (0, 0, 0)}.
     * Initialized to {@code 0} by default.
     */
    public double blurT = 0;

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

    /**
     * Sets the transparency coefficient using a {@link Double3} triad.
     *
     * @param kT the transparency coefficient triad
     * @return this material for builder-style chaining
     */
    public Material setKT(Double3 kT) {
        this.kT = kT;
        return this;
    }

    /**
     * Sets the transparency coefficient to a uniform value across all components.
     *
     * @param kT the scalar transparency coefficient
     * @return this material for builder-style chaining
     */
    public Material setKT(double kT) {
        this.kT = new Double3(kT);
        return this;
    }

    /**
     * Sets the reflection coefficient using a {@link Double3} triad.
     *
     * @param kR the reflection coefficient triad
     * @return this material for builder-style chaining
     */
    public Material setKR(Double3 kR) {
        this.kR = kR;
        return this;
    }

    /**
     * Sets the reflection coefficient to a uniform value across all components.
     *
     * @param kR the scalar reflection coefficient
     * @return this material for builder-style chaining
     */
    public Material setKR(double kR) {
        this.kR = new Double3(kR);
        return this;
    }

    /**
     * Sets the glossy reflection blur radius.
     *
     * @param blurR the disk-sampling radius for reflection rays; {@code 0}
     *              for a perfectly sharp mirror
     * @return this material for builder-style chaining
     */
    public Material setBlurR(double blurR) {
        this.blurR = blurR;
        return this;
    }

    /**
     * Sets the diffuse transparency blur radius.
     *
     * @param blurT the disk-sampling radius for transparency rays; {@code 0}
     *              for perfectly clear glass
     * @return this material for builder-style chaining
     */
    public Material setBlurT(double blurT) {
        this.blurT = blurT;
        return this;
    }
}
