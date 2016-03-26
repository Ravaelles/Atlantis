package bwapi;

import java.lang.Override;
import java.util.HashMap;
import java.util.Map;

public class WalkPosition extends AbstractPoint<WalkPosition>{

    private int x, y;

    public WalkPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public native boolean isValid();

    public native WalkPosition makeValid();

    public native int getApproxDistance(WalkPosition position);

    public native double getLength();

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static WalkPosition Invalid;

    public static WalkPosition None;

    public static WalkPosition Unknown;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WalkPosition)) return false;

        WalkPosition position = (WalkPosition) o;

        if (x != position.x) return false;
        if (y != position.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }


    private static Map<Long, WalkPosition> instances = new HashMap<Long, WalkPosition>();

    private WalkPosition(long pointer) {
        this.pointer = pointer;
    }

    private static WalkPosition get(long pointer) {
        WalkPosition instance = instances.get(pointer);
        if (instance == null) {
            instance = new WalkPosition(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    public WalkPosition getPoint(){
        return this;
    }
}