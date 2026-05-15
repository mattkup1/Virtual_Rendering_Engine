package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public class PointLight extends Light implements LightSource {
    protected final Point _position;
    private double _kC = 1, _kL = 0, _kQ = 0;

    public PointLight(Color intensity, Point position, double kC, double kL, double kQ) {
        super(intensity);
        _position = position;
        _kC = kC;
        _kL = kL;
        _kQ = kQ;
    }

    public Vector getL(Point p) {
        return p.subtract(_position).normalize();
    }

    public Color getIntensity(Point p) {
        if (_position.equals(p)) {
            return _intensity;
        }
        final double d = _position.distance(p);
        return _intensity.scale(1d / (_kC + (_kL * d) + (_kQ * d * d)));
    }

    public PointLight setKC(double kC) {
        _kC = kC;
        return this;
    }

    public PointLight setKL(double kL) {
        _kL = kL;
        return this;
    }

    public PointLight setKQ(double kQ) {
        _kQ = kQ;
        return this;
    }
}
