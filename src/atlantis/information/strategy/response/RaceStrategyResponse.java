package atlantis.information.strategy.response;

import atlantis.information.strategy.AStrategy;
import atlantis.information.strategy.EnemyStrategy;
import atlantis.units.AUnit;

public abstract class RaceStrategyResponse {
//    public boolean update() {
//        if (AGame.notNthGameFrame(17)) return false;
//
//        // Anti-LAND

    /// /        if (antiLandManager().requestToBuildNewAntiLandCombatBuilding()) return true;
//
//        // Anti-AIR
//        if (antiAirManager().requestToBuildNewAntiAirCombatBuilding()) return true;
//
//        return false;
//    }
    public static RaceStrategyResponse get() {
        return RaceStrategyResponseFactory.forOurRace();
    }

    // =========================================================

    public abstract boolean requestDetection(AUnit enemyUnit);

    // =========================================================

    public void updateEnemyStrategyChanged() {
        AStrategy enemyStrategy = EnemyStrategy.get();

//        if (Strategy.get().isRushOrCheese() && GamePhase.isEarlyGame()) {
//            return;
//        }

        // === Rush ========================================
        // (ported: the defend half lives in PostureReasons as
        // RUSH_DEFENCE now - one decider, every frame; the response
        // chain below it was a no-op (ignored return, zero callers
        // for the building counters) and is deleted, not ported)

        // === Expansion ========================================
        // (ported: the engage half lives in PostureReasons as
        // ENEMY_EXPANDING_SO_ENGAGE now - one decider, every frame,
        // no flap between change ticks; see the comment above)

        // === Tech ========================================

        if (enemyStrategy.isGoingHiddenUnits()) {
            onEnemyGoesHiddenUnits();
        }

        // === Air units ===========================================

        if (enemyStrategy.isAirUnits()) {
            handleAirUnitsDefence();
        }
    }

    public abstract void onEnemyGoesHiddenUnits();

    // =========================================================

    public void handleAirUnitsDefence() {
    }

    // =========================================================

}
