package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public class PointLight extends Light implements LightSource {
    protected final Point _position;
    private double _kC, _kL, _kQ;

    public PointLight(Color intensity, Point position, double kC, double kL, double kQ) {
        super(intensity);
        _position = position;
        kC = kC;
        kL = kL;
        kQ = kQ;
    }

    public Vector getL(Point P) {
        return null;
    }

    public Color getIntensity(Point p) {
        return null;
    }
}
