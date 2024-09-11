package atlantis.combat.squad.squad_scout;

import atlantis.combat.squad.Squad;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class DefineSquadScout {
    private final Squad squad;

    public DefineSquadScout(Squad squad) {
        this.squad = squad;
    }

    public AUnit define() {
        if (squad == null || A.isUms() || Count.ourCombatUnits() <= 4) return null;

        if (!squad.isMainSquad()) return null;
        if (We.terran() && Select.ourCombatUnits().ranged().empty()) return null;

        // First try to define a ranged unit as a scout e.g. Dragoon, Marine
        Selection groundUnits = squad.units().groundUnits().notScout();
        AUnit ranged = groundUnits.ranged().nonTanks().healthy().notSpecialAction().mostDistantToBase();
        if (ranged != null) {
            return defineNewSquadScout(ranged);
        }

        ranged = groundUnits.ranged().nonTanks().mostDistantToBase();
        if (ranged != null) {
            return defineNewSquadScout(ranged);
        }

        // If no ranged unit is available, use melee (Zealot). Not perfect, but better than nothing.
        return defineNewSquadScout(
            groundUnits.melee().notScout().havingAtLeastHp(30).mostDistantToBase()
        );
    }

    private static AUnit defineNewSquadScout(AUnit squadScout) {
        if (squadScout == null) return null;

        squadScout.setSquadScout();
        return squadScout;
    }
}
