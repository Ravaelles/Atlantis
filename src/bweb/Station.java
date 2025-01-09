package bweb;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Station {
    private BWEM.Base base;
    private BWEM.Base partnerBase;
    private BWEM.ChokePoint choke;
    private BWAPI.Position resourceCentroid = new BWAPI.Position(0, 0);
    private BWAPI.Position defenseCentroid = new BWAPI.Position(0, 0);
    private BWAPI.Position anglePosition;
    private Set<BWAPI.TilePosition> secondaryLocations = new HashSet<>();
    private Set<BWAPI.TilePosition> defenses = new HashSet<>();
    private boolean main, natural;
    private double defenseAngle = 0.0;
    private double baseAngle = 0.0;
    private double chokeAngle = 0.0;
    private BWAPI.TilePosition mediumPosition, smallPosition;

    public Station(BWEM.Base _base, boolean _main, boolean _natural) {
        base = _base;
        main = _main;
        natural = _natural;
        resourceCentroid = new BWAPI.Position(0, 0);
        defenseCentroid = new BWAPI.Position(0, 0);
        // Skipping full logic for brevity; add as needed
    }
    public BWAPI.Position getResourceCentroid() { return resourceCentroid; }
    public Set<BWAPI.TilePosition> getSecondaryLocations() { return secondaryLocations; }
    public Set<BWAPI.TilePosition> getDefenses() { return defenses; }
    public BWAPI.TilePosition getMediumPosition() { return mediumPosition; }
    public BWAPI.TilePosition getSmallPosition() { return smallPosition; }
    public BWEM.Base getBase() { return base; }
    public BWEM.ChokePoint getChokepoint() { return choke; }
    public boolean isMain() { return main; }
    public boolean isNatural() { return natural; }
    public void draw() {
        // Drawing logic stub
    }
    public double getDefenseAngle() { return defenseAngle; }
    @Override
    public boolean equals(Object o) {
        if (o instanceof Station) {
            return base == ((Station)o).getBase();
        }
        return false;
    }
    @Override
    public int hashCode() {
        return Objects.hash(base);
    }
}