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
 * Mid-game Gateway demand (owner: the missing 3rd Gateway while rich).
 * The BO owns rows @10/@12; after that V2 never asks for a Gateway, so
 * a banked mid-game with busy facilities starves on two Gates (measured
 * live 183253: 2 Gateways the whole game, bank to 7963 while Dragoon
 * demand waited on busy facilities). The goal fires only when rich,
 * busy, past the opening and capped - never ahead of the bank.
 *
 * <p>Pure snapshot state, no world: the counts ride the GameSnapshot
 * seam ({@code gatewaysExisting}/{@code gatewaysFree}), never Select.</p>
 */
@Tag("production")
public class GatewayDemandTest {

    private static DynamicGoals.GameSnapshot state(
            int workers, int supply, int minerals, int gateways, int freeGateways) {
        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
            workers, 1, supply, 5, supply + 5, minerals, false);
        snapshot.gatewaysExisting = gateways;
        snapshot.gatewaysFree = freeGateways;

        return snapshot;
    }

    private static boolean hasGatewayGoal(List<ProductionGoal> goals) {
        String id = UnitProducible.of(AUnitType.Protoss_Gateway).id();

        return goals.stream().anyMatch(goal -> goal.item().id().equals(id));
    }

    private static List<ProductionGoal> contribute(DynamicGoals.GameSnapshot snapshot) {
        return DynamicGoals.contribute(snapshot, ProductionRacePolicy.forRace("Protoss"));
    }

    @Test
    public void richAndBusyDemandsThirdGateway() {
        assertTrue(
            hasGatewayGoal(contribute(state(25, 45, 800, 2, 0))),
            "bank 800, both Gateways busy past the opening: the 3rd Gateway is wanted");
    }

    @Test
    public void poorBankNeverDemands() {
        assertFalse(
            hasGatewayGoal(contribute(state(25, 45, 150, 2, 0))),
            "bank 150 never funds a Gateway ahead of units and tech");
    }

    @Test
    public void freeGatewayTrainsFirst() {
        assertFalse(
            hasGatewayGoal(contribute(state(25, 45, 800, 2, 1))),
            "a free Gateway trains first - no new building while capacity idles");
    }

    @Test
    public void openingBelongsToTheBuildOrder() {
        assertFalse(
            hasGatewayGoal(contribute(state(12, 20, 800, 2, 0))),
            "at supply 20 the BO rows still own Gateway demand");
    }

    @Test
    public void fourthGatewayIsTheCeiling() {
        assertFalse(
            hasGatewayGoal(contribute(state(30, 60, 1200, 4, 0))),
            "four Gateways is enough for now - further expansion is a new decision");
    }
}
