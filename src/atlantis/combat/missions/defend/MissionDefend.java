package atlantis.combat.missions.defend;

import atlantis.combat.missions.AFocusPoint;
import atlantis.combat.missions.Mission;
import atlantis.game.AGame;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

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
        if (unit.isMelee() && enemy.isMelee() && "300".equals(unit.tooltip())) {
            return unit.distTo(enemy) <= 1;
//            return false; // 300 mode - stand in line, default to SC auto attacks
        }

        if (
//                (unit.isMelee() && unit.hasWeaponRangeToAttack(enemy, 0.1))
                (unit.isMelee() && unit.distToLessThan(enemy, 1.1))
                || (unit.isRanged() && unit.hasWeaponRangeToAttack(enemy, 2.2))
        ) {
            return true;
        }

        if (Have.main()) {
            if (Select.enemy().inRadius(18, Select.main()).atLeast(1)) {
                return true;
            }

            if (Select.enemy().inRadius(18, Select.naturalOrMain()).atLeast(1)) {
                return true;
            }
        }

        AFocusPoint focusPoint = focusPoint();
        AUnit base = Select.main();
        if (focusPoint != null && base != null) {
//            double unitDistToFocusPoint = unit.distTo(focusPoint());
            double focusPointDistToBase = focusPoint.distTo(base);
            double unitDistToBase = unit.distTo(base);
            double enemyDistToBase = enemy.distTo(base);

            if (focusPointDistToBase < enemyDistToBase || enemyDistToBase < unitDistToBase) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        if (unit.isDragoon() && enemies.onlyMelee() && unit.hp() >= 30 && unit.lastAttackFrameMoreThanAgo(35)) {
            return true;
        }

        if (unit.isHealthy() || unit.shieldDamageAtMost(14)) {
            return true;
        }

        if (unit.isMelee() && unit.friendsNearby().inRadius(1.3, unit).atLeast(3)) {
            return true;
        }

        return false;
//        return enemies.onlyMelee() && unit.hp() >= 18;
    }
}
