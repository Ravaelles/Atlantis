package atlantis.util;

import javax.vecmath.Vector2d;

public class Vector extends Vector2d {

    public Vector(double x, double y) {
        super(x, y);
    }

    public double toAngle() {
        return new Vector2d(5, 0).angle(this);
    }

    public boolean isParallelTo(Vector otherVector, double maxDiffInRadians) {
        return Math.abs(this.angle(otherVector) % 3.14) < maxDiffInRadians;
    }

    /**
     * Angle between vectors less than ~32 degrees (0.55 radians).
     */
    public boolean isParallelTo(Vector otherVector) {
        return (Math.abs(this.angle(otherVector)) % 3.14) < 0.55;
    }

}
