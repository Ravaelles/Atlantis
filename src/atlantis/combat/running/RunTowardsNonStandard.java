package atlantis.combat.running;

import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class RunTowardsNonStandard {
    private final atlantis.combat.running.ARunningManager ARunningManager;

    public RunTowardsNonStandard(atlantis.combat.running.ARunningManager ARunningManager) {
        this.ARunningManager = ARunningManager;
    }

    protected HasPosition shouldRunTowardsBunker() {
        if (!We.terran() || !GamePhase.isEarlyGame() || Count.bunkers() == 0) {
            return null;
        }

        if (ARunningManager.unit.isTerranInfantry()) {
            AUnit bunker = Select.ourOfType(AUnitType.Terran_Bunker).nearestTo(ARunningManager.unit);
            if (
                bunker != null
                    && ARunningManager.unit.enemiesNearInRadius(2) == 0
                    && bunker.distToMoreThan(ARunningManager.unit, 5 + ARunningManager.unit.woundPercent() / 12)
            ) {
                return bunker.position();
            }
        }

        return null;
    }

    /**
     * Running behavior which will make unit run toward main base.
     */
//    protected boolean shouldRunTowardsBase() {
//        if (ARunningManager.unit.isFlying()) return false;
//
//        AUnit main = Select.main();
//
//        if (main == null) return false;
//
//        if (ARunningManager.unit.isAir() && main.distTo(ARunningManager.unit) < 12) return true;
//
//        if (OurStrategy.get().isRushOrCheese() && A.seconds() <= 300) return false;
//
//        if (!ARunningManager.unit.hasPathTo(main)) return false;
//
//        double distToMain = ARunningManager.unit.distTo(main);
//
//        if (main == null) return false;
//
//        int meleeEnemiesNearCount = ARunningManager.unit.meleeEnemiesNearCount(4);
//        if (distToMain >= 40 || (distToMain > 15 && meleeEnemiesNearCount == 0 && ARunningManager.unit.isMissionDefend()))
//            return true;
//
//        if (A.seconds() >= 380) return false;
//
//        if (ARunningManager.unit.isScout()) return false;
//
//        // If already close to the base, don't run towards it, no point
//        if (distToMain < 50) return false;
//
//        if (meleeEnemiesNearCount >= 1) return false;
//
//        // Only run towards our main if our army isn't too numerous, otherwise units gonna bump upon each other
//        if (Count.ourCombatUnits() > 10) return false;
//
//        if (ARunningManager.unit.lastStartedRunningLessThanAgo(30) && ARunningManager.unit.lastStoppedRunningLessThanAgo(30))
//            return false;
//
//        if (Count.ourCombatUnits() <= 10 || ARunningManager.unit.isNearEnemyBuilding()) {
//            if (ARunningManager.unit.meleeEnemiesNearCount(3) == 0) {
//                return true;
//            }
//        }
//
//        return false;
//    }
}
