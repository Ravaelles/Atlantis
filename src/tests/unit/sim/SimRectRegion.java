package tests.unit.sim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.region.ARegion;

/**
 * One rectangle of the sim fake map (NEXT #191): a fixed center and a
 * fixed reachable set, no BWEM anywhere. Identity equality: two
 * regions are the same only when they are the same object, so the
 * installed list and the source answer stay consistent by
 * construction.
 */
public final class SimRectRegion extends ARegion {

    private final String name;
    private final APosition center;
    private final List<ARegion> reachable = new ArrayList<>();

    public SimRectRegion(String name, APosition center) {
        this.name = name;
        this.center = center;
    }

    void link(SimRectRegion other) {
        if (!reachable.contains(other)) reachable.add(other);
        if (!other.reachable.contains(this)) other.reachable.add(this);
    }

    @Override
    public APosition position() {
        return center;
    }

    @Override
    public APosition center() {
        return center;
    }

    @Override
    public List<ARegion> getReachableRegions() {
        return Collections.unmodifiableList(reachable);
    }

    @Override
    public List<AChoke> chokes() {
        return new ArrayList<>();
    }

    @Override
    public List<atlantis.map.base.ABaseLocation> getBaseLocations() {
        return new ArrayList<>();
    }

    @Override
    public boolean isReachable(ARegion otherRegion) {
        return reachable.contains(otherRegion);
    }

    @Override
    public double apprxWidth() {
        return 20;
    }

    @Override
    public boolean equals(HasPosition o) {
        return this == o;
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "SimRectRegion{" + name + "}";
    }
}
