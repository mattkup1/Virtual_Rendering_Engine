package primitives;

public class Vector extends Point {
    Vector(double x, double y, double z) {
        super(x, y, z);

        if (Util.isZero(x) && Util.isZero(y) && Util.isZero(z))
            throw new IllegalArgumentException();
    }

    Vector(Double3 xyz) {
        super(xyz);

        if (xyz == Double3.ZERO)
            throw new IllegalArgumentException();
    }
}
