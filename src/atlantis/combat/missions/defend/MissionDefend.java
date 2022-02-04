package atlantis.combat.missions.defend;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.We;

public class MissionDefend extends Mission {

    public MissionDefend() {
        super("Defend");
        focusPointManager = new MissionDefendFocusPoint();
    }

    @Override
    public boolean update(AUnit unit) {
        if (AGame.isUms()) {
            return false;
        }

        // =========================================================

        AFocusPoint focusPoint = focusPoint();
        if (focusPoint == null) {
            if (!Have.base()) {
                return false;
            }

            System.err.println("Couldn't define choke point.");
            throw new RuntimeException("Couldn't define choke point.");
        }

        // Don't reposition if enemies nearby
        if (unit.enemiesNearby().inRadius(7, unit).atLeast(3)) {
            return false;
        }

        return MoveToDefendFocusPoint.move(unit, focusPoint);
    }

    // =========================================================

    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
        AFocusPoint focusPoint = focusPoint();
        AUnit base = Select.main();
        if (focusPoint == null || base == null) {
            return true;
        }

        // =========================================================

        double focusPointDistToBase = focusPoint.distTo(base);
        double unitDistToBase = unit.groundDist(base);
        double enemyDistToBase = enemy.groundDist(base);
        double enemyDistToFocus = enemy.groundDist(focusPoint);

//        if (unit.isMelee() && enemy.isMelee() && "300".equals(unit.tooltip())) {
//            return unit.distTo(enemy) <= 1;
////            return false; // 300 mode - stand in line, default to SC auto attacks
//        }

        // Zealots vs Zealot fix
        if (unit.isZealot() && enemy.isZealot()) {
            int ourZealots = unit.zealotsNearbyCount(0.4);
            if (ourZealots < unit.enemiesNearby().inRadius(0.5, unit).count()) {
                return false;
            }
        }

        if (enemyDistToFocus <= 1) {
            return true;
        }

        if (
//                (unit.isMelee() && unit.hasWeaponRangeToAttack(enemy, 0.1))
                (unit.isMelee() && enemyDistToFocus <= 1.3 && unit.distToLessThan(enemy, 1.1))
                || (unit.isRanged() && unit.hasWeaponRangeToAttack(enemy, 2.2))
        ) {
            return true;
        }

        if (unit.isMelee() && enemyDistToBase > unitDistToBase) {
            return false;
        }

        if (Have.main()) {
            if (Select.enemy().inRadius(18, Select.main()).atLeast(1)) {
                return true;
            }

            if (Select.enemy().inRadius(18, Select.naturalOrMain()).atLeast(1)) {
                return true;
            }
        }

        if (focusPointDistToBase < enemyDistToBase || enemyDistToBase < unitDistToBase) {
            return true;
        }

        return false;
    }

    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        if (We.terran()) {
            return false;
        }

        if (
            (unit.isMelee() || unit.hpMoreThan(40))
            && unit.friendsNearby().combatBuildings(false).inRadius(5, unit).notEmpty()
        ) {
            unit.addLog("ProtectBuilding");
            return true;
        }

        if (unit.hpLessThan(36) && unit.friendsNearbyCount() <= 2) {
            return false;
        }

        if (unit.isDragoon() && enemies.onlyMelee() && unit.hp() >= 30 && unit.lastAttackFrameMoreThanAgo(35)) {
            return true;
        }

        if (unit.isRanged() && (unit.isHealthy() || unit.shieldDamageAtMost(10))) {
            return true;
        }

//        if (unit.isMelee() && unit.friendsNearby().inRadius(1.3, unit).atLeast(3)) {
//            return true;
//        }

        return false;
//        return enemies.onlyMelee() && unit.hp() >= 18;
    }
}
