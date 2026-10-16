package atlantis.combat.retreating.terran;

import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.generic.Army;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class TerranShouldFullRetreat {
    private static AUnit unit;

    public static boolean shouldFullRetreat(AUnit unit) {
//        if (A.isUms() && Count.bases() == 0) return false;
        TerranShouldFullRetreat.unit = unit;

        if (unit.enemiesNear().combatUnits().empty()) return false;

        double eval = unit.eval();
        if (unit.isMissionAttack() && eval <= evalThresholdDuringMissionAttack()) return true;

        if (eval >= 0.4 && (Army.strength() >= 600 && A.supplyUsed() >= 60) || A.minerals() >= 2000) return false;

        if (unit.distToBunker() <= 2.5) return false;

        return unit.eval() <= 0.9;
    }

    private static double evalThresholdDuringMissionAttack() {
        double base = A.s <= 60 * 7 ? 1.2 : 1;

        base += baseThresholdModifier() / (5 + Count.ourCombatUnits());

        return base;
    }

    private static double baseThresholdModifier() {
        return EnemyInfo.hasRanged() ? 7.0 : 5.2;
    }

}
