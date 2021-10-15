package atlantis.util;

import javax.vecmath.Vector2d;

public class Vector extends Vector2d {

    public Vector(double x, double y) {
        super(x, y);
    }

    public double toAngle() {
        return new Vector2d(5, 0).angle(this);
    }

    /**
     * Angle between vectors less than 0.50 radians.
     */
    public boolean isParallelTo(Vector otherVector) {
        return (Math.abs(this.angle(otherVector)) % 3.14) < 0.55;
    }

    /**
     * Angle between vectors less than N degrees.
     */
    public boolean isParallelTo(Vector otherVector, double degreeMargin) {
        return (Math.abs(this.angle(otherVector)) % 3.14) <= Angle.degreesToRadians(degreeMargin);
    }

}
