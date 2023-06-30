package atlantis.combat.missions.attack;

import atlantis.combat.missions.ProtossMissionAdjustments;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class MissionAttackVsEnemyUnit {

    public static boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
//        if (A.supplyUsed() <= 40) {
//            // Zealots vs Zealot fix
//            if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
//                return true;
//            }
//        }

        return true;
    }
}
