package atlantis.information.strategy.response.protoss;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.strategy.response.StrategyResponse;
import atlantis.production.constructions.cancelling.CriticalCancelPending;
import atlantis.production.dynamic.protoss.buildings.ProduceCannonAtNaturalOrMain;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.util.AConsole;

public class ProtossResponseEnemyHiddenUnits extends StrategyResponse {
    private int cannonNeeded;

    /**
     * Earliest any hidden unit can exist (seconds): DTs need Templar
     * Archives (4:30+), Lurkers need Lair + Den + research (5:00+).
     * Any hidden-units call before this is spurious recognition, not
     * defense - measured live GAME_4382BF70, the response gutted our
     * Core + 2 Gateways at 2:37 vs Zerg for phantom cannons.
     */
    static final int HIDDEN_EARLIEST_S = 240;

    /**
     * Pure time predicate (test seam): hidden units are impossible
     * before {@link #HIDDEN_EARLIEST_S}, so the emergency must not
     * fire. No game state - timing alone decides.
     */
    public static boolean isTooEarlyForHiddenUnits(int seconds) {
        return seconds < HIDDEN_EARLIEST_S;
    }

    private static int cannonNeeded() {
        return Enemy.zerg()
            ? 2
            : (Enemy.protoss() ? 3 : 2);
    }

    public boolean handle() {
        if (isTooEarlyForHiddenUnits(A.s)) return false;

        buildCannons();
        cancelBasesBeingConstructed();

        return true;
    }

    private void cancelBasesBeingConstructed() {
        if (Count.observers() == 0 && !Have.observatory()) {
            CriticalCancelPending.cancelBases();
            CriticalCancelPending.cancelGatewats();
        }
    }

    private void buildCannons() {
        cannonNeeded = cannonNeeded();
        cancelConstructionsIfNeededToGetMinerals();

        AConsole.println(atlantis.util.log.LogLevel.LOW, "cannonsNeeded = " + cannonNeeded);

        for (int i = 0; i < cannonNeeded; i++) {
            ProduceCannonAtNaturalOrMain.produce();
        }
    }

    private void cancelConstructionsIfNeededToGetMinerals() {
        int mineralsNeeded = cannonNeeded * 150;
        int mineralsMissing = mineralsNeeded - A.minerals();

        if (mineralsMissing > 90) {
            CriticalCancelPending.cancelToGetMinerals(mineralsMissing);
        }
    }
}
