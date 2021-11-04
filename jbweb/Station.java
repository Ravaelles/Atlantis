package jbweb;

import bwapi.*;
import bwem.*;

import java.util.List;

public class Station {
    private final Base base;
    private final List<TilePosition> defenses;
    private final Position resourceCentroid;
    private final boolean main;
    private final boolean natural;

    Station(Position _resourceCentroid, List<TilePosition> _defenses, Base _base, boolean _main, boolean _natural) {
        resourceCentroid = _resourceCentroid;
        defenses = _defenses;
        base = _base;
        main = _main;
        natural = _natural;
    }

    public boolean equals(Station s) {
        return base == s.base;
    }

    public Position getResourceCentroid() {
        return resourceCentroid;
    }

    public List<TilePosition> getDefenseLocations() {
        return defenses;
    }

    public Base getBWEMBase() {
        return base;
    }

    public boolean isMain() {
        return main;
    }

    public boolean isNatural() {
        return natural;
    }

    public int getGroundDefenseCount() {
        int count = 0;
        for (TilePosition defense : defenses) {
            UnitType type = JBWEB.isUsed(defense, 1, 1);
            if (type == UnitType.Protoss_Photon_Cannon
                    || type == UnitType.Zerg_Sunken_Colony
                    || type == UnitType.Terran_Bunker) {
                count++;
            }
        }
        return count;
    }

    public int getAirDefenseCount() {
        int count = 0;
        for (TilePosition defense : defenses) {
            UnitType type = JBWEB.isUsed(defense, 1, 1);
            if (type == UnitType.Protoss_Photon_Cannon
                    || type == UnitType.Zerg_Spore_Colony
                    || type == UnitType.Terran_Missile_Turret) {
                count++;
            }
        }
        return count;
    }

    public void draw() {
        Color color = JBWEB.game.self().getColor();
        Text textColor = color.id == 185 ? Text.DarkGreen : JBWEB.game.self().getTextColor();

        // Draw boxes around each feature
        for (TilePosition tile : defenses) {
            JBWEB.game.drawBoxMap(new Position(tile), new Position(tile.toPosition().x + 65, tile.toPosition().y + 65), color);
            JBWEB.game.drawTextMap(new Position(tile.toPosition().x + 4, tile.toPosition().y + 52), "%cS", textColor);
        }
        JBWEB.game.drawBoxMap(new Position(base.getLocation()), new Position(base.getLocation().toPosition().x + 129,
                base.getLocation().toPosition().y + 97), color);
    }
}
