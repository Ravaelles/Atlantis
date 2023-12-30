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

    public boolean isAngleAlmostIdentical(Vector otherVector) {
        return angleBetweenOtherVector(otherVector) < 0.87;
    }

    public boolean isAngleAlmostOpposite(Vector otherVector) {
//        System.err.println("angleBetweenOtherVector(otherVector) = " + angleBetweenOtherVector(otherVector));
        return Math.abs(angleBetweenOtherVector(otherVector) - Math.PI) <= 0.97;
    }

    private double angleBetweenOtherVector(Vector otherVector) {
        return Math.abs(this.angle(otherVector)) % (2 * Math.PI);
    }

    /**
     * Angle between vectors less than N degrees.
     */
    public boolean isAngleAlmostIdentical(Vector otherVector, double degreeMargin) {
        return (Math.abs(this.angle(otherVector)) % 3.14) <= Angle.degreesToRadians(degreeMargin);
    }

    public Vector rotate(double angle) {
        double x1 = (double) (x * Math.cos(angle) - y * Math.sin(angle));
        double y1 = (double) (x * Math.sin(angle) + y * Math.cos(angle));

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

    public Vector normalizeTo1() {
        normalize();
        return this;
    }

    public Vector multiplyVector(double factor) {
        this.scale(factor);
        return this;
    }

    public void print(String vector) {
        System.err.println(vector + ": [" + x + ", " + y + "]");
    }
}
