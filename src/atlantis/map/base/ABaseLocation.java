package atlantis.map.base;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import bwapi.TilePosition;
import bwem.Base;

public class ABaseLocation implements HasPosition {

    private Base base = null;
    private APosition position = null;

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
        return "BaseLocation at " + position + " ("
            + (isStartLocation() ? "start_loc" : "non_start_loc")
            + ")";
    }

    // =========================================================

//    public boolean isIsland() {
//        return base.isIsland();
//    }
//
//    public boolean isMineralOnly() {
//        return base.isMineralOnly();
//    }

    public boolean isStartLocation() {
        return base.isStartingLocation();
    }


}
