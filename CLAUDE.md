# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Java ray tracer built for the ISE (Introduction to Software Engineering) course. Project ID
`ISE5786_7270_8257`. Authors: Matt Kuperwasser and Moshe Hanau.

## Build / test / run

The project builds with Maven (`pom.xml`) as well as via the IntelliJ module
(`ISE5786_7270_8257.iml`) — both point at the same `src/` (main) and `unittests/` (test) source
roots, so no source directories needed to move.

Every test class carries a JUnit 5 `@Tag`: `unit` (single-class logic), `component` (parses/
assembles an object graph, e.g. a `SceneLoader`, without rendering), `integration` (spans
components, asserts on behavior, no image output), or `render` (calls `Camera.renderImage()` +
`writeToImage()` — the slow, visually-verified showcase scenes). `mvn test` excludes `render` by
default (see the `excludedGroups` property in `pom.xml`), so day-to-day runs stay fast; opt in
explicitly when you need to check a render:

- `mvn test` — compile and run every `unit`/`component`/`integration` test (fast, seconds)
- `mvn test -Dgroups=render -DexcludedGroups=` — run only the render/image-generation tests (slow,
  minutes — writes to `images/`)
- `mvn test -DexcludedGroups=` — run everything, including renders
- `mvn -Dtest=ClassName test` — run a single test class (e.g. `-Dtest=SphereTests`)
- `mvn -Dtest=ClassName#methodName test` — run a single test method
- `mvn compile` — compile only

`unittests-archive/` holds superseded/redundant test classes (original course-checkpoint tests
later covered by an equivalent JSON/XML scene test, plus a couple of already-disabled exploratory
ones) — kept for reference via git history, not part of either the Maven or IntelliJ source roots,
so they're never compiled or run.

Dependencies:
- `org.json:json:20240303` (JSON scene parsing)
- `org.junit.jupiter:junit-jupiter:5.10.2` (tests, via `maven-surefire-plugin`)

Rendered images are written to `images/`; XML/JSON scene definitions used by tests and manual
runs live in `sceneSourceFiles/{xml,json}/`, with larger external mesh packages (e.g. the Cornell
Box) under `sceneSourceFiles/packages/`. The `gui` package (`RenderApp`/`RenderWindow`, plain
Swing) is a desktop scene launcher/viewer front end for the same pipeline — run via
`mvn exec:java` or `RenderApp.main` from the IDE.

## Architecture

### Package layout

- `primitives` — `Point`, `Vector`, `Ray`, `Color`, `Double3`, `Material`, `Util`
- `geometries/api` — `Intersectable` (abstract; owns the nested `Intersection` PDS), `Geometry`
- `geometries/impl` — `Sphere`, `Plane`, `Triangle`, `Polygon`, `Cylinder`, `Tube`,
  `RadialGeometry`, `Geometries` (composite; flat linear scan over children, no acceleration
  structure)
- `lighting` — `LightSource` interface, `Light` base, `AmbientLight`, `DirectionalLight`,
  `PointLight`, `SpotLight`
- `scene` — `Scene` (fluent setters), `SceneLoader` (abstract, Template Method), `XmlSceneLoader`,
  `JsonSceneLoader`
- `renderer` — `Camera` (Builder pattern), `ImageWriter`, `RayTracerBase`, `SimpleRayTracer`,
  `BeamSampler`, `PixelManager`, `RayTracerType`

### Rendering pipeline

1. `Camera.renderImage()` iterates pixels and calls `castRay(x, y)`.
2. `castRay` calls `Camera.constructRay()` then `SimpleRayTracer.traceRay()`.
3. `SimpleRayTracer` implements recursive Phong shading: emission + ambient + diffuse + specular +
   shadow (transparency-aware) + reflection + refraction.
4. Global effects use `BeamSampler.sampleBeam()` at the top recursion level only (glossy
   reflections via `blurR`, diffuse glass via `blurT`).
5. `SimpleRayTracer.adaptiveBeam()` can reduce ray count in uniform beam regions, gated by the
   `adaptiveSuperSampling` constant (currently hardcoded `false`).
6. Multi-threading via `PixelManager`, which distributes pixels across worker threads (`-1`
   threads = all CPUs minus 2).

`AdaptiveSuperSampling.java` (per-pixel anti-aliasing) exists in `renderer/` but is not referenced
anywhere in `src/` or `unittests/` — treat it as unwired/legacy rather than part of the active
pipeline.

### Key design choices

- `Intersectable.Intersection` is a mutable PDS carrying `normal`, `v`, `l`, `light`, and
  dot-products, populated by `RayTracerBase.preprocessIntersection` /
  `preprocessLightSource` before color calculation.
- `Material` holds per-channel `Double3` coefficients (`kA`, `kD`, `kS`, `kT`, `kR`) plus
  `blurR`/`blurT` for glossy/diffuse effects.
- Camera rotation is supported via `Builder.rotate(degrees)`; `setDirection(Point)` auto-computes
  `vTo` from a target point.
- `SceneLoader` is a Template Method base class: subclasses (`XmlSceneLoader`, `JsonSceneLoader`)
  only extract raw `Map<String, String>` attribute data per light/geometry; `SceneLoader` itself
  owns all object construction (`buildGeometries`, `buildLight`, `buildMaterial`) and the
  `material.*`-namespaced attribute convention shared by both formats.
- Private fields across most classes use a leading-underscore naming convention (`_p0`, `_vTo`,
  etc.) — follow this convention when editing those classes for consistency.
