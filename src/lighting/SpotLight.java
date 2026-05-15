package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

public class SpotLight extends PointLight {
    private final Vector _direction;

    public SpotLight(Color intensity, Point position, double kC, double kL, double kQ, Vector direction) {
        super(intensity, position, kC, kL, kQ);
        _direction = direction.normalize();
    }

    public Vector getL(Point p) {
        return super.getL(p);
    }

    public Color getIntensity(Point p) {
        if (p.equals(_position)) return _intensity;
        final Color point_intensity = super.getIntensity(p);
        return point_intensity.scale(Math.max(0d, _direction.dotProduct(getL(p))));
    }

    @Override
    public SpotLight setKC(double kC) {
        return (SpotLight) super.setKC(kC);
    }

    @Override
    public SpotLight setKL(double kL) {
        return (SpotLight) super.setKL(kL);
    }

    @Override
    public SpotLight setKQ(double kQ) {
        return (SpotLight) super.setKQ(kQ);
    }
}
