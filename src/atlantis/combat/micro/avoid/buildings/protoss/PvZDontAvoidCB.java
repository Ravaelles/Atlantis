package atlantis.combat.micro.avoid.buildings.protoss;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class PvZDontAvoidCB {
    public static boolean dontAvoid(AUnit unit, AUnit combatBuilding) {
        if (!Enemy.zerg()) return false;
        if (A.resourcesBalance() < 0) return false;
        if (Count.ourCombatUnits() <= 7) return false;
        if (Army.strengthWithoutOurCB() <= 400) return false;
        if (unit.shields() <= 15 && A.supplyUsed() <= 150) return false;
        if (unit.eval() <= 4) return false;
        if (unit.friendsNear().combatUnits().countInRadius(10, unit) <= 6) return false;

        return true;
    }
}
