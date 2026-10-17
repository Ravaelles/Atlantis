package atlantis.decisions.goals.protoss;

import atlantis.decisions.board.BoardState;
import atlantis.decisions.board.Goal;
import atlantis.units.AUnitType;

/**
 * Static base defense: how many Photon Cannons the bot wants in total.
 *
 * <p>
 * The count matrix moved here from {@code ShouldSecureProtossBase} unchanged
 * (supply tiers, mineral tiers, muta bonus, race caps): tuning strategy is
 * domain knowledge and now pins in millisecond stub tests instead of living
 * untested inside a producer. Per-base placement (which base, which tile)
 * stays execution-side - the board names the number, never the site.
 * Not an emergency: cannons are planned work.
 * </p>
 */
public final class CannonGoal implements Goal {

    @Override
    public String id() {
        return "cannon";
    }

    @Override
    public boolean active(BoardState state) {
        return state.cannonCount < wanted(state);
    }

    @Override
    public int rank() {
        return GoalRanks.CANNON;
    }

    @Override
    public int priceMinerals() {
        return AUnitType.Protoss_Photon_Cannon.mineralPrice();
    }

    @Override
    public int priceGas() {
        return AUnitType.Protoss_Photon_Cannon.gasPrice();
    }

    /**
     * Wanted cannon total for this state. Pure: supply, bank, matchup and
     * scouted mutas in, number out. Matches the legacy matrix row for row so
     * the delegation changes no behavior, only its address.
     */
    public static int wanted(BoardState state) {
        int minSupplyForSecondCannon = "Terran".equals(state.enemyRace) ? 100 : 85;

        int total = (state.supplyTotal >= minSupplyForSecondCannon ? 2 : 1)
            + (state.minerals >= 540 ? 1 : 0)
            + ("Zerg".equals(state.enemyRace) && state.supplyTotal >= 130 ? 1 : 0)
            + ("Zerg".equals(state.enemyRace) && state.supplyTotal >= 180 ? 1 : 0)
            + ("Zerg".equals(state.enemyRace) && state.supplyTotal >= 190 ? 1 : 0)
            + mutasBonus(state)
            + (state.minerals >= 640 ? 1 : 0)
            + (state.minerals >= 800 ? 1 : 0);

        if ("Protoss".equals(state.enemyRace)) total = Math.min(total, 5 + (state.minerals >= 700 ? 1 : 0));
        if ("Terran".equals(state.enemyRace)) total = Math.min(total, 2);
        if ("Zerg".equals(state.enemyRace)) total = Math.min(total, 6);

        if ("Zerg".equals(state.enemyRace) && (state.supplyUsed <= 150 && state.minerals <= 550)) {
            total = Math.min(3, total);
        }

        return total;
    }

    private static int mutasBonus(BoardState state) {
        if (!"Zerg".equals(state.enemyRace)) return 0;
        if (state.enemyMutalisks == 0) return 0;

        return 1 + (state.enemyMutalisks / 4) + (state.minerals / 500);
    }
}
