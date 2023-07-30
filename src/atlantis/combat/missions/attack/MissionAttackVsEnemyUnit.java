package atlantis.combat.missions.attack;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class MissionAttackVsEnemyUnit extends HasUnit {
    public MissionAttackVsEnemyUnit(AUnit unit) {
        super(unit);
    }

    public boolean allowsToAttackEnemyUnit(AUnit enemy) {
//        if (A.supplyUsed() <= 40) {
//            // Zealots vs Zealot fix
//            if (ProtossMissionAdjustments.allowsToAttackEnemyUnits(unit, enemy)) {
//                return true;
//            }
//        }

        return true;
    }
}
