package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Forge timing: DT prediction plus matchup supply timings.
 *
 * <p>
 * Moved here row for row from {@code ProduceForge}: hidden-units
 * prediction (seen or going, with army/production gates), then the
 * supply/army gate, then the matchup build-at-supply table. Tuning
 * strategy pins in stub tests; the producer keeps only the already-have
 * check and the queue-once dedup. Not an emergency: a Forge is planned
 * tech, and DT defense answers through detection first.
 * </p>
 */
public final class ForgeGoal implements Goal {

    @Override
    public String id() {
        return "forge";
    }

    @Override
    public boolean active(BoardState state) {
        return !state.hasForge && wanted(state);
    }

    @Override
    public int rank() {
        return GoalRanks.FORGE;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Forge.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Forge.gasPrice();
    }

    /**
     * Forge wanted in this state. Pure mirror of the legacy producer:
     * DT prediction first, then the supply/army gate with the matchup
     * timing table.
     */
    public static boolean wanted(BoardState state) {
        // DT prediction queues at supply 40 in the legacy producer
        // (buildToHaveOne(40, Forge)) - the timing is part of the want.
        if (predictEnemyDT(state)) return state.supplyUsed >= 40;

        if (state.supplyUsed <= 60 && (state.combatUnits <= 11 || state.armyStrengthWithoutCB <= 180)) {
            return false;
        }

        return state.supplyUsed >= buildAtSupply(state);
    }

    private static boolean predictEnemyDT(BoardState state) {
        if (state.enemyHasHiddenUnits) return true;

        if (state.combatUnits <= 9) return false;
        if (state.armyStrengthWithoutCB <= 130 && state.supplyUsed < 60) return false;
        if (state.gatewaysUnfinished <= 3) return false;

        return (state.minerals >= 250 || state.armyStrength >= 150) && state.enemyGoingHidden;
    }

    private static int buildAtSupply(BoardState state) {
        if (state.enemyGoingHidden) return 30;

        if ("Terran".equals(state.enemyRace) && !state.enemyHasHiddenUnits) return 93;
        if ("Protoss".equals(state.enemyRace) && !state.enemyHasRanged) return 73;
        if ("Zerg".equals(state.enemyRace)) return 46 + (state.armyStrength <= 150 ? 10 : 0);

        return 62;
    }
}
