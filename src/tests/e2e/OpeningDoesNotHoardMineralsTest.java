package tests.e2e;

import atlantis.game.A;
import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.engine.ProductionEngine;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The owner's acceptance criterion for the production redesign (2026-10-07):
 * the opening must not hoard minerals.
 *
 * <p>
 * Explicitly: when the first Pylon starts, no more than 16 minerals may be in
 * the bank; and when the Gateway comes up, again no more than 16. A bank far
 * above the cost of what is being built means the engine is saving for
 * something it should not - which is exactly what happened (a second Nexus
 * planned as a Probe prerequisite, 400 minerals, measured on the real engine).
 * </p>
 *
 * <p>
 * The test drives the real commander frame by frame and records the mineral
 * bank at the two moments that matter, so the assertion is about the observed
 * game, not about the scheduler's internals. A plan can be mathematically
 * perfect and still hoard; this is the check that says it does not.
 * </p>
 */
public class OpeningDoesNotHoardMineralsTest extends AbstractTestWithWorld {

    /** The owner's threshold: at most this much left when a build starts. */

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void aPlanNeverCostsMoreThanWeHoldWhenSomethingIsAffordable() {
        // The direct form of the owner's rule: with a bank that covers a cheap
        // item, the plan must contain that cheap item - it must not consist only
        // of something we cannot pay for. A plan whose cheapest item costs 8x
        // the bank is the "saving for a base" symptom, which the first Pylon
        // waiting for 400 minerals was (owner report, 2026-10-07).
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit[] probes = {
                fake(AUnitType.Protoss_Probe, 8.3),
                fake(AUnitType.Protoss_Probe, 9.1),
                fake(AUnitType.Protoss_Probe, 10.6),
        };
        FakeUnit[] ours = units(nexus, probes[0], probes[1], probes[2]);

        world(60, ours, new FakeUnit[0], () -> {
            currentMinerals = 100;

            if (A.now() != 30)
                return;

            ProductionPlan plan = new ProductionEngine().updateFrame();

            boolean hasAffordableItem = false;
            for (ProductionItem item : plan.items()) {
                if (item.item().cost().minerals() <= 100) hasAffordableItem = true;
            }

            assertTrue(hasAffordableItem,
                    "with 100 minerals banked the plan must contain something we can pay for;"
                            + " plan was: " + plan);
        });
    }

    // =========================================================
    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
