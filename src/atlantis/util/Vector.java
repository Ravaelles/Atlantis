package atlantis.util;

import javax.vecmath.Vector2d;

public class Vector extends Vector2d {

    public Vector(int x, int y) {
        super(x, y);
    }

    public Vector(double x, double y) {
        super(x, y);
    }

    public Vector(Vector v) {
        super(v.x, v.y);
    }

    public double toAngle() {
        return new Vector2d(5, 0).angle(this);
    }

    /**
     * Angle between vectors less than 0.50 radians.
     */
    public boolean isParallelTo(Vector otherVector) {
        return (Math.abs(this.angle(otherVector)) % 3.14) < 0.77;
    }

    /**
     * Angle between vectors less than N degrees.
     */
    public boolean isParallelTo(Vector otherVector, double degreeMargin) {
        return (Math.abs(this.angle(otherVector)) % 3.14) <= Angle.degreesToRadians(degreeMargin);
    }

    public Vector rotate(double angle) {
        double x1 = (double) (x * Math.cos(angle) - y * Math.sin(angle));
        double y1 = (double) (x * Math.sin(angle) + y * Math.cos(angle)) ;

        return new Vector(x1, y1);
    }

    public Vector addLength(int deltaLength) {
        double length = length();

        Vector vector = new Vector(this);
        vector.normalize();
        vector.scale(length + deltaLength);

        return vector;

//        return new Vector(x + extraLength * x * x / length, y);
    }
}
