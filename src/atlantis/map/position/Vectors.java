package atlantis.map.position;

import atlantis.util.Vector;

/**
 * Vector arithmetic on positions.
 *
 * <p>Lives here, next to the type it reads, rather than in {@code atlantis.util}:
 * a shared kernel that takes domain types as parameters is not a kernel, and the
 * two overloads that took {@code AUnit} (three call sites in {@code AUnit} itself)
 * made a geometry helper know what a unit is. Callers pass
 * {@code unit.position()}, which is one word longer and one dependency
 * lighter.</p>
 */
public class Vectors {

    /**
     * The direction that leads from {@code from} to {@code to}: the vector
     * {@code to.position() - from.position()}.
     *
     * <p>Named for what it returns rather than for the order of the arguments,
     * because the facing helpers depend on both and the difference is a radian
     * window wide. A unit standing at {@code from} and pointing along this vector
     * is looking at {@code to}; a unit pointing along the opposite vector has its
     * back to it.</p>
     *
     * <p>This replaces {@code fromPositionsBetween(p1, p2)}, which returned
     * {@code p1 - p2} - the reverse of what its name said. Every call site read
     * correctly only because of that inversion, twice over, which is exactly the
     * kind of thing that survives a decade and then gets "fixed" into a bug. See
     * {@code _AI/BUGS.md} B-4.</p>
     */
    public static Vector directionTowards(APosition from, APosition to) {
        return new Vector(to.x - from.x, to.y - from.y);
    }

    public static Vector vectorFromAngle(double angleInRadians, double radius) {
        return new Vector(
            radius * Math.cos(angleInRadians),
            radius * Math.sin(angleInRadians)
        );
    }
}