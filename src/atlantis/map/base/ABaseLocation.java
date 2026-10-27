package atlantis.map.base;

import atlantis.map.analysis.BaseRoles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.TilePosition;
import bwem.Base;

public class ABaseLocation implements HasPosition, Comparable<ABaseLocation> {
    private Base base = null;
    private APosition position = null;
    private BaseRoles.Role role = BaseRoles.Role.EXPANSION;

    public static ABaseLocation create(Base base) {
        ABaseLocation baseLocation = new ABaseLocation();
        baseLocation.base = base;
        baseLocation.position = APosition.create(base.getLocation().toPosition());

        return baseLocation;
    }

    public static ABaseLocation create(TilePosition tilePosition) {
        ABaseLocation aBaseLocation = new ABaseLocation();
        aBaseLocation.position = APosition.create(tilePosition.toPosition());

        return aBaseLocation;
    }

    public static HasPosition mineralsCenter(AUnit base) {
        APosition center = Select.minerals().inRadius(10, base).center();

        if (center == null) {
            return base;
        }

        return center;
    }

    // =========================================================

    @Override
    public APosition position() {
        if (position != null) {
            return position;
        }

        return APosition.create(base.getCenter());
    }

    @Override
    public int x() {
        return position.getX();
    }

    @Override
    public int y() {
        return position.getY();
    }

    @Override
    public String toString() {
        return "BaseLocation at " + position() + " ("
            + (isStartLocation() ? "start_loc" : "non_start_loc")
            + ")";
    }

    // =========================================================

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ABaseLocation)) return false;

        ABaseLocation location = (ABaseLocation) o;

        return position().equals(location.position());
    }

    @Override
    public int hashCode() {
        return position().hashCode();
    }

    @Override
    public int compareTo(ABaseLocation o) {
        return position().compareTo(o);
    }

    // =========================================================

    public BaseRoles.Role role() {
        return role;
    }

    public void setRole(BaseRoles.Role role) {
        if (role != null) this.role = role;
    }

    public boolean isIsland() {
        return role == BaseRoles.Role.ISLAND;
    }

    public boolean isMineralOnly() {
        return role == BaseRoles.Role.MINERAL_ONLY;
    }

    public boolean isStartLocation() {
        return base != null && base.isStartingLocation();
    }

    /**
     * Mineral patches at this base; 0 when the location carries no base.
     */
    public int minerals() {
        return base == null ? 0 : base.getMinerals().size();
    }

    /**
     * Geysers at this base; 0 when the location carries no base.
     */
    public int gas() {
        return base == null ? 0 : base.getGeysers().size();
    }
}
