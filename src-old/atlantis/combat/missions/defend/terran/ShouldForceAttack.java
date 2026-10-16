package atlantis.combat.missions.defend.terran;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.MissionHistory;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.units.select.Count;

public class ShouldForceAttack {
    public static boolean check() {
        int strength = Army.strength();

        int combatUnits = Count.ourCombatUnits();
        if (combatUnits <= 4) return false;

        if (combatUnits >= 35) {
            return MissionChanger.forceMissionAttack("ManyCombatUnits(" + combatUnits + ")");
        }

        if (strength >= 800) {
            return MissionChanger.forceMissionAttack("HugeStrength");
        }

        if (
            Strategy.get().isRushOrCheese()
//                && A.resourcesBalance() >= -250
                && combatUnits >= 5
                && strength >= 111
                && (strength >= 131 || EnemyUnits.dragoons() <= 2)
        ) {
            return MissionChanger.forceMissionAttack("Rush or cheese");
        }

        if (A.minerals() >= 2000 && A.supplyUsed() >= 90) {
            MissionChanger.reason = "Abundance of minerals";
            return true;
        }

        if (combatUnits >= 20 && strength >= 90 && Count.tanks() >= 3 && MissionHistory.numOfChanges() <= 3) {
            MissionChanger.reason = "Try to engage at least once";
            return true;
        }

        return false;
    }
}
