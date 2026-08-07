# ISE Ray Tracer

A Java ray tracer built for the ISE (Introduction to Software Engineering) course.

Project ID `ISE5786_7270_8257`. Authors: Matt Kuperwasser and Moshe Hanau.

## Features

- Recursive Phong shading (emission, ambient, diffuse, specular, shadows, reflection, refraction)
- Geometric primitives: sphere, plane, triangle, polygon, cylinder, tube
- BVH acceleration with SAH-based splitting
- Anti-aliasing / soft shadows / depth of field via adaptive beam sampling
- Scene definition via XML or JSON
- Multi-threaded rendering
- A simple Swing GUI for launching and viewing renders

## Build / test / run

The project builds with Maven (`pom.xml`).

- `mvn test` — compile and run the fast unit/component/integration tests
- `mvn test -Dgroups=render -DexcludedGroups=` — run only the slow render/image-generation tests
  (writes to `images/`)
- `mvn test -DexcludedGroups=` — run everything, including renders
- `mvn compile` — compile only
- `mvn exec:java` — launch the GUI (`gui.RenderApp`)

## Architecture

- `primitives` — `Point`, `Vector`, `Ray`, `Color`, `Double3`, `Material`, `Util`
- `geometries` — `Intersectable`, `Geometry`, and implementations (`Sphere`, `Plane`, `Triangle`,
  `Polygon`, `Cylinder`, `Tube`, `Geometries`)
- `lighting` — `LightSource`, `AmbientLight`, `DirectionalLight`, `PointLight`, `SpotLight`
- `scene` — `Scene`, `SceneLoader`, `XmlSceneLoader`, `JsonSceneLoader`
- `renderer` — `Camera`, `ImageWriter`, `RayTracerBase`, `SimpleRayTracer`, `BeamSampler`,
  `PixelManager`
- `gui` — `RenderApp`/`RenderWindow`, a Swing front end for launching and viewing scenes

Scene definitions live under `sceneSourceFiles/{xml,json}/`; rendered output is written to
`images/`.

## Dependencies

- [`org.json:json`](https://mvnrepository.com/artifact/org.json/json) — JSON scene parsing
- [`org.junit.jupiter:junit-jupiter`](https://junit.org/junit5/) — tests
