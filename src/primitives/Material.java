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
     * Texture sampled in place of the geometry's flat emission color, for geometries
     * that support UV mapping (see {@link geometries.api.Geometry#getUV}).
     * {@code null} by default, meaning the geometry's own emission is used unchanged.
     */
    public Texture texture;

    /**
     * Texture sampled as a grayscale height field to perturb the surface normal (bump
     * mapping), for geometries that support UV mapping. {@code null} by default, meaning
     * the geometry's own normal is used unperturbed. Independent of {@link #texture} -
     * a material can have a color texture, a normal texture, both, or neither.
     */
    public Texture normalTexture;

    /**
     * Strength of the {@link #normalTexture} perturbation: a multiplier on the raw
     * UV-space height gradient before it's applied to the normal. Has no effect when
     * {@link #normalTexture} is {@code null}.
     */
    public double bumpStrength = 0.1;

    /**
     * Default constructor for the {@link Material} class.
     */
    public Material() { /* To satisfy Javadoc generator */ }

    /** Sets the ambient reflection coefficient using a {@link Double3} triad. */
    public Material setKA(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /** Sets the ambient reflection coefficient to a uniform value across all components. */
    public Material setKA(Double kA) {
        this.kA = new Double3(kA);
        return this;
    }

    /** Sets the specular reflection coefficient using a {@link Double3} triad. */
    public Material setKS(Double3 kS) {
        this.kS = kS;
        return this;
    }

    /** Sets the specular reflection coefficient to a uniform value across all components. */
    public Material setKS(double kS) {
        this.kS = new Double3(kS);
        return this;
    }

    /** Sets the diffuse reflection coefficient using a {@link Double3} triad. */
    public Material setKD(Double3 kD) {
        this.kD = kD;
        return this;
    }

    /** Sets the diffuse reflection coefficient to a uniform value across all components. */
    public Material setKD(double kD) {
        this.kD = new Double3(kD);
        return this;
    }

    /** Sets the shininess exponent used in specular highlight calculations. */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
    }

    /** Sets the transparency coefficient using a {@link Double3} triad. */
    public Material setKT(Double3 kT) {
        this.kT = kT;
        return this;
    }

    /** Sets the transparency coefficient to a uniform value across all components. */
    public Material setKT(double kT) {
        this.kT = new Double3(kT);
        return this;
    }

    /** Sets the reflection coefficient using a {@link Double3} triad. */
    public Material setKR(Double3 kR) {
        this.kR = kR;
        return this;
    }

    /** Sets the reflection coefficient to a uniform value across all components. */
    public Material setKR(double kR) {
        this.kR = new Double3(kR);
        return this;
    }

    /** Sets the glossy reflection blur radius; {@code 0} is a perfectly sharp mirror. */
    public Material setBlurR(double blurR) {
        this.blurR = blurR;
        return this;
    }

    /** Sets the diffuse transparency blur radius; {@code 0} is perfectly clear glass. */
    public Material setBlurT(double blurT) {
        this.blurT = blurT;
        return this;
    }

    /** Sets the texture sampled in place of the geometry's flat emission color. */
    public Material setTexture(Texture texture) {
        this.texture = texture;
        return this;
    }

    /** Sets the texture sampled as a grayscale height field to perturb the surface normal. */
    public Material setNormalTexture(Texture normalTexture) {
        this.normalTexture = normalTexture;
        return this;
    }

    /** Sets the bump-mapping perturbation strength. */
    public Material setBumpStrength(double bumpStrength) {
        this.bumpStrength = bumpStrength;
        return this;
    }
}
