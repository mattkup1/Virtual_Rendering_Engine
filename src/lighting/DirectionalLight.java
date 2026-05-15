package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public class DirectionalLight extends Light implements LightSource {
    private final Vector _direction;

    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        _direction = direction.normalize();
    }

    public Vector getL(Point P) {
        return _direction;
    }

    public Color getIntensity(Point p) {
        return _intensity;
    }
}
