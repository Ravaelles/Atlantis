package atlantis.combat.missions.defend.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.game.A;
import atlantis.map.base.Bases;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class ZergDefendFocus {
    public static AFocusPoint define() {
        if (!We.zerg()) return null;

        AFocusPoint focus;

        if ((focus = sunken()) != null) return focus;

        return null;
    }

    protected static AFocusPoint sunken() {
        if (We.zerg()) {
            AUnit mainBase = Select.main();
            AUnit sunken = Select.ourOfType(AUnitType.Zerg_Sunken_Colony).mostDistantTo(mainBase);
            if (sunken != null) {
                return new AFocusPoint(
                    sunken.translateTilesTowards(3.2, mainBase),
                    mainBase,
                    "Sunken"
                );
            }
        }
        return null;
    }
}
