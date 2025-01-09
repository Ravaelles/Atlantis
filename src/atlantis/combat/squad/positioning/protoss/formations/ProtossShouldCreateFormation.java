package atlantis.combat.squad.positioning.protoss.formations;

import atlantis.units.AUnit;
import atlantis.game.player.Enemy;

public class ProtossShouldCreateFormation {
    private static AUnit nearestEnemy;

    public static boolean check(AUnit unit) {
        return enoughEnemiesToConsiderABattle(unit)
            && !needsToStartBattleNow(unit);
    }

    private static boolean needsToStartBattleNow(AUnit unit) {
        nearestEnemy = unit.nearestGroundCombatEnemy();
        if (nearestEnemy == null) return false;

        AUnit leader = unit.squadLeader();
        if (leader == null) return false;

//        return leader.distTo(nearestEnemy) <= 7.8 || leader.lastAttackFrameLessThanAgo(30 * 3);
        return leader.lastUnderAttackLessThanAgo(40) || leader.lastAttackFrameLessThanAgo(30 * 3);
    }

    private static boolean enoughEnemiesToConsiderABattle(AUnit unit) {
        int enemies = unit.enemiesNear().groundUnits().combatUnits().inRadius(enemiesRadius(), unit).size();
//        return enemies >= 2 && enemies >= (unit.friendsNear().groundUnits().combatUnits().size() + 1) / 4;
        return enemies >= 2 && enemies >= (unit.friendsNear().groundUnits().combatUnits().size() + 1) / 4;
    }

    private static int enemiesRadius() {
        return Enemy.terran() ? 15 : 13;
    }
}
