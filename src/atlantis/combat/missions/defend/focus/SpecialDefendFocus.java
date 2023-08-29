package atlantis.combat.missions.defend.focus;

import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.missions.Missions;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class SpecialDefendFocus {
    public static AFocusPoint define() {
        if (!We.zerg()) return null;

        AFocusPoint focus;

        if ((focus = spartaFocus()) != null) return focus;

        return null;
    }

    /**
     * Sparta mission - as Zealots hold line in a narrow choke.
     */
    private static AFocusPoint spartaFocus() {
        if (Missions.isGlobalMissionSparta()) {
            Selection basesWithUnfinished = Select.ourWithUnfinished().bases();
            AChoke mainChoke = Chokes.mainChoke();
            if (basesWithUnfinished.count() <= 1) {
                if (mainChoke != null) {
                    return new AFocusPoint(
                        mainChoke,
                        Select.main(),
                        "Choke300"
                    );
                }
            }
        }
        return null;
    }
}
