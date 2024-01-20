package atlantis.combat.squad.squad_scout;

import atlantis.combat.squad.Squad;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class DefineSquadScout {
    private final Squad squad;

    public DefineSquadScout(Squad squad) {
        this.squad = squad;
    }

    public AUnit define() {
        if (!squad.isMainSquad()) return null;
        if (We.terran() && Select.ourCombatUnits().ranged().empty()) return null;

        // First try to define a ranged unit as a scout e.g. Dragoon, Marine
        Selection groundUnits = squad.units().groundUnits();
        AUnit ranged = groundUnits.ranged().nonTanks().healthy().notSpecialAction().mostDistantToBase();
        if (ranged != null) {
            return ranged;
        }

        // If no ranged unit is available, use melee (Zealot). Not perfect, but better than nothing.
        return groundUnits.melee().havingAtLeastHp(30).mostDistantToBase();
    }
}
