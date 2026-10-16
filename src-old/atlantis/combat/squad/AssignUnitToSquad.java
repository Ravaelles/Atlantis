package atlantis.combat.squad;

import atlantis.units.AUnit;

public class AssignUnitToSquad {
    public static void assignTo(AUnit unit, Squad squad) {
        Squad oldSquad = unit.squad();
        if (oldSquad != null) oldSquad.removeUnit(unit);

        squad.addUnit(unit);
        unit.forceSetSquad(squad);
    }
}
