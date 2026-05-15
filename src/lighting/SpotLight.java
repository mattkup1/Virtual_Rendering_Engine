package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public class SpotLight extends PointLight {
    private final Vector _direction;

    public SpotLight(Color intensity, Point position, double kC, double kL, double kQ, Vector direction) {
        super(intensity, position, kC, kL, kQ);
        _direction = direction;
    }


    public Vector getL(Point P) {
        return null;
    }

    public Color getIntensity(Point p) {
        return null;
    }
}
