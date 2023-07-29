package atlantis.combat.missions.defend;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.AllowTimeToReposition;
import atlantis.combat.squad.positioning.MakeSpaceForNearbyWorkers;
import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.util.Enemy;
import atlantis.util.We;

public class MissionDefendManager extends MissionManager {
    public MissionDefendManager(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[] {
            MakeSpaceForNearbyWorkers.class,
            AllowTimeToReposition.class,
            AdvanceToDefendFocusPoint.class,
        };
    }

    // =========================================================

//    @Override
//    public boolean allowsToAttackEnemyUnit(AUnit unit, AUnit enemy) {
//        return allowsToAttack.allowsToAttackEnemyUnit(unit, enemy);
//    }

//    @Override
    public boolean forcesUnitToFight(AUnit unit, Units enemies) {
        if (We.terran()) {
            return false;
        }

        if (
            unit.isMelee()
                && unit.friendsNear().combatBuildings(false).inRadius(5, unit).notEmpty()
                && !"Sparta".equals(unit.tooltip())
        ) {
            if (unit.hp() <= 18) {
                if (Enemy.protoss()) return false;

                if (unit.hp() <= 10) return false;
            }

            unit.addLog("ProtectBuilding");
            return true;
        }

        if (unit.hpLessThan(36) && unit.friendsNearCount() <= 2 && unit.lastAttackFrameMoreThanAgo(30 * 4)) {
            return false;
        }

        if (unit.isHydralisk()) {
            if (unit.woundPercentMin(60) || unit.meleeEnemiesNearCount(2) >= 2) {
                return false;
            }
        }

        if (
            unit.isDragoon()
                && enemies.onlyMelee() && unit.hp() >= 40
                && unit.lastAttackFrameMoreThanAgo(30 * 4)
                && unit.nearestEnemyDist() >= 2.8
        ) {
            return true;
        }

        if (unit.isRanged() && (unit.isHealthy() || unit.shieldDamageAtMost(10))) {
            return true;
        }

//        if (unit.isMelee() && unit.friendsNear().inRadius(1.3, unit).atLeast(3)) {
//            return true;
//        }

        return false;
//        return enemies.onlyMelee() && unit.hp() >= 18;
    }

//    protected  boolean noFocusPoint() {
//        if (!Have.base()) {
//            return false;
//        }
//
//        System.err.println("Couldn't define choke point.");
//        throw new RuntimeException("Couldn't define choke point.");
//    }

//    @Override
//    public double optimalDist() {
//        return (new AdvanceToDefendFocusPoint()).optimalDist();
//    }
//
//    @Override
//    protected Manager managerClass(AUnit unit) {
//        return null;
//    }
}
