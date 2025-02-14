package atlantis.combat.missions.defend.terran;

import atlantis.combat.missions.MissionChanger;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.Army;
import atlantis.information.strategy.Strategy;
import atlantis.units.select.Count;

import static atlantis.combat.missions.MissionChanger.forceMissionAttack;

public class ShouldForceAttack {
    public static boolean check() {
        int strength = Army.strength();

        if (Count.ourCombatUnits() <= 4) return false;

        if (strength >= 800) {
            return forceMissionAttack("HugeStrength");
        }

        if (
            Strategy.get().isRushOrCheese()
                && A.resourcesBalance() >= -250
                && strength >= 115
                && (strength >= 131 || EnemyUnits.dragoons() <= 2)
        ) {
            return forceMissionAttack("Rush or cheese");
        }

        if (A.minerals() >= 2000 && A.supplyUsed() >= 90) {
            if (MissionChanger.DEBUG) MissionChanger.reason = "Abundance of minerals";
            return true;
        }


        return false;
    }
}
