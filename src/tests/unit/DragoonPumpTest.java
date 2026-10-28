package tests.unit;

import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mid-game Dragoon pump (owner: no combat units past the mid game). The
 * baseline army goal is a floor, not a ceiling - but no strategic layer
 * raises it, so a banked mid-game with idle Gateways starves on
 * workers/4 Dragoons while minerals pile to five digits (measured in the
 * sim past 50: 61 Pylons, bank 10k, Dragoons 6->16 in 12k frames, GW#3
 * never fires because demand never saturates capacity). Past the opening
 * a rich bank with a free Gateway trains Dragoons continuously: the bank
 * governs, the free Gateway signals. Symmetric with GatewayDemand.
 *
 * <p>Pure snapshot state, no world.</p>
 */
@Tag("production")
public class DragoonPumpTest {

    private static DynamicGoals.GameSnapshot state(
            int workers, boolean inEarlyGame, int dragoons, int gas, int minerals, int freeGateways) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            workers, 2, 55, 10, 65, minerals, inEarlyGame, 10, 2,
            0, false, false, 0,
            gas, true, dragoons, 1, false, 300);
        snapshot.gatewaysExisting = 2;
        snapshot.gatewaysFree = freeGateways;

        return snapshot;
    }

    private static boolean hasDragoonPump(List<ProductionGoal> goals) {
        String id = UnitProducible.of(AUnitType.Protoss_Dragoon).id();

        // The pump carries its own priority band: the floor goal (same unit,
        // MAINARMYBASE) may fire alongside, so counting goals cannot tell
        // them apart.
        return goals.stream().anyMatch(goal ->
            goal.item().id().equals(id)
                && goal.priority() == atlantis.production.v2.ProductionGoal.PRIORITY_MAINARMY);
    }

    private static List<ProductionGoal> contribute(DynamicGoals.GameSnapshot snapshot) {
        return DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
    }

    @Test
    public void richWithFreeGatewayPumpsDragoons() {
        assertTrue(
            hasDragoonPump(contribute(state(30, false, 7, 200, 800, 1))),
            "bank 800, a free Gateway past the opening: Dragoons keep coming");
    }

    @Test
    public void busyGatewaysDoNotPump() {
        assertFalse(
            hasDragoonPump(contribute(state(30, false, 7, 200, 800, 0))),
            "no free Gateway: the pump waits for capacity instead of queuing behind busy facilities");
    }

    @Test
    public void poorBankNeverPumps() {
        assertFalse(
            hasDragoonPump(contribute(state(30, false, 7, 200, 150, 1))),
            "bank 150 never funds a Dragoon ahead of the bank rule");
    }

    @Test
    public void openingBelongsToTheBuildOrder() {
        assertFalse(
            hasDragoonPump(contribute(state(10, true, 0, 100, 800, 1))),
            "in the opening the BO owns unit demand, never the pump");
    }

    @Test
    public void noGasMeansNoDragoonPump() {
        assertFalse(
            hasDragoonPump(contribute(state(30, false, 7, 0, 800, 1))),
            "Dragoons cost gas: without it the pump stays silent");
    }
}
