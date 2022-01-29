package atlantis.map;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
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
