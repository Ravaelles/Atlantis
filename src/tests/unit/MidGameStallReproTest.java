package tests.unit;

import atlantis.game.A;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.engine.ProductionEngine;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Mid-game stall reproduction (owner live 2026-10-29, Cere game): 59/65
 * supply, 900 minerals banked, and no Pylons, no Zealots, no Dragoons, no
 * Nexus coming. Either the engine refuses everything from this state
 * (scheduler/dispatcher gates - reproducible here) or the live stall is
 * elsewhere (busy facilities, a supply lie, the engine not running -
 * needs live twin rows, not this harness).
 *
 * <p>Scene mirrors the report: saturated one-base economy, Core standing,
 * idle Gateways, gas for Dragoons. Pylons are deliberately NOT asserted:
 * at 6 free the worry gate (4) holds by design - trains and the Nexus
 * are the uncontroversial demand.</p>
 */
@Tag("production")
public class MidGameStallReproTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        EventLog.clearForTests();
    }

    @Test
    public void richMidGameWithIdleGatewaysTrains() {
        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 12, 10),
            fake(AUnitType.Protoss_Cybernetics_Core, 14, 10),
            fake(AUnitType.Protoss_Gateway, 16, 10),
            fake(AUnitType.Protoss_Gateway, 16, 12),
            fake(AUnitType.Protoss_Probe, 11, 10));

        options.set("supplyUsed", 59);
        options.set("supplyTotal", 65);
        initSupply(59, 65);
        currentMinerals = 900;
        currentGas = 150;

        EventLog.clearForTests();
        ProductionEngine engine = new ProductionEngine(ProductionRacePolicy.forRace("Protoss"));

        world(90, ours, fakeEnemies(), () -> engine.updateFrame());

        String log = EventLog.toJsonLines();
        assertTrue(log.contains("\"item\":\"Dragoon\"") || log.contains("\"item\":\"Zealot\""),
            "idle Gateways + Core + gas must train something within 90 frames: " + log);
    }

    @Test
    public void richSaturatedOneBaseExpands() {
        FakeUnit[] probes = new FakeUnit[26];
        for (int i = 0; i < probes.length; i++) {
            probes[i] = fake(AUnitType.Protoss_Probe, 8 + (i % 6), 8 + (i / 6));
        }
        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 12, 10),
            fake(AUnitType.Protoss_Cybernetics_Core, 14, 10),
            fake(AUnitType.Protoss_Gateway, 16, 10),
            fake(AUnitType.Protoss_Gateway, 16, 12));
        FakeUnit[] all = new FakeUnit[ours.length + probes.length];
        System.arraycopy(ours, 0, all, 0, ours.length);
        System.arraycopy(probes, 0, all, ours.length, probes.length);

        options.set("supplyUsed", 59);
        options.set("supplyTotal", 65);
        initSupply(59, 65);
        currentMinerals = 900;
        currentGas = 150;

        world(10, all, fakeEnemies(), () -> {
            if (A.now() != 5) return;

            atlantis.production.v2.engine.GameStateSnapshot snap =
                new atlantis.production.v2.engine.GameStateSnapshot(
                    ProductionRacePolicy.forRace("Protoss"));

            assertTrue(snap.dynamicGoals(false).stream()
                .anyMatch(g -> g.item().id().equals(
                    atlantis.production.v2.UnitProducible.of(AUnitType.Protoss_Nexus).id())),
                "26 workers saturate the base - the natural must be wanted");
        });
    }

    @Test
    public void unsaturatedOneBaseDoesNotExpand() {
        FakeUnit[] ours = fakeOurs(
            fake(AUnitType.Protoss_Nexus, 10, 10),
            fake(AUnitType.Protoss_Pylon, 12, 10),
            fake(AUnitType.Protoss_Cybernetics_Core, 14, 10),
            fake(AUnitType.Protoss_Gateway, 16, 10),
            fake(AUnitType.Protoss_Gateway, 16, 12),
            fake(AUnitType.Protoss_Probe, 11, 10));

        options.set("supplyUsed", 59);
        options.set("supplyTotal", 65);
        initSupply(59, 65);
        currentMinerals = 900;
        currentGas = 150;

        world(10, ours, fakeEnemies(), () -> {
            if (A.now() != 5) return;

            atlantis.production.v2.engine.GameStateSnapshot snap =
                new atlantis.production.v2.engine.GameStateSnapshot(
                    ProductionRacePolicy.forRace("Protoss"));

            assertTrue(snap.dynamicGoals(false).stream()
                .noneMatch(g -> g.item().id().equals(
                    atlantis.production.v2.UnitProducible.of(AUnitType.Protoss_Nexus).id())),
                "below 25 workers the base is not saturated - workers first, no Nexus");
        });
    }
}
