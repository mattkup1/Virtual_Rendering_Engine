package lighting;

import primitives.Point;

public class PointLight extends Light implements LightSource {
    protected Point _position;
    private double _kC, _kL, kQ;
}
