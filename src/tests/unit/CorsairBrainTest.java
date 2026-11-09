package tests.unit;

import atlantis.combat.behaviors.protoss.corsair.CorsairBrain;
import atlantis.combat.behaviors.protoss.corsair.CorsairDanceToOverlord;
import atlantis.combat.behaviors.protoss.corsair.CorsairExploreEnemyMain;
import atlantis.combat.behaviors.protoss.corsair.CorsairHuntKnownOverlords;
import atlantis.combat.behaviors.protoss.corsair.CorsairHuntMutas;
import atlantis.combat.behaviors.protoss.corsair.CorsairRelocateWhenIdle;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * The restored Corsair brain (owner 2026-11-09, port of the old
 * Corsair manager chain): muta packs, overlord duels, known-board
 * overlord sweeps, scouting and anti-idle relocation - instead of
 * the #NO_COMMAND stand the narrowed hunt left behind.
 */
@Tag("combat")
public class CorsairBrainTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Zerg;
    }

    @AfterEach
    public void clean() {
        CorsairExploreEnemyMain.clearForTests();
    }

    @Test
    public void healthyCorsairAnswersMutas() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        FakeUnit muta = fake(AUnitType.Zerg_Mutalisk, 30, 20);
        muta.setEnemy();

        world(2, fakeOurs(corsair), fakeEnemies(muta), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CorsairHuntMutas.applies(corsair),
                "healthy Corsair answers a muta pack");
            assertTrue(CorsairBrain.act(corsair),
                "the brain issues the hunt, not silence");
        });
    }

    @Test
    public void weakCorsairSkipsMutas() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        corsair.hp = 40;
        FakeUnit muta = fake(AUnitType.Zerg_Mutalisk, 30, 20);
        muta.setEnemy();

        world(2, fakeOurs(corsair), fakeEnemies(muta), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CorsairHuntMutas.applies(corsair),
                "42 hp or less: no trading with Mutalisks");
        });
    }

    @Test
    public void knownOverlordGetsVisited() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 40, 20);
        overlord.setEnemy();

        world(2, fakeOurs(corsair), fakeEnemies(overlord), () -> {
            if (atlantis.game.A.now() != 2) return;
            atlantis.information.enemy.EnemyUnitsUpdater.weDiscoveredEnemyUnit(overlord);

            assertTrue(CorsairHuntKnownOverlords.applies(corsair),
                "an uncovered known Overlord gets visited");
            assertTrue(CorsairBrain.act(corsair),
                "the brain issues the visit, not silence");
        });
    }

    @Test
    public void guardedOverlordIsLeftAlone() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 40, 20);
        overlord.setEnemy();
        FakeUnit hydra = fake(AUnitType.Zerg_Hydralisk, 42, 20);
        hydra.setEnemy();

        world(2, fakeOurs(corsair), fakeEnemies(overlord, hydra), () -> {
            if (atlantis.game.A.now() != 2) return;
            atlantis.information.enemy.EnemyUnitsUpdater.weDiscoveredEnemyUnit(overlord);
            atlantis.information.enemy.EnemyUnitsUpdater.weDiscoveredEnemyUnit(hydra);

            assertFalse(CorsairHuntKnownOverlords.applies(corsair),
                "anti-air cover vetoes the visit");
        });
    }

    @Test
    public void coolingCorsairDancesIn() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        corsair.cooldown = 12;
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 30, 20);
        overlord.setEnemy();

        world(2, fakeOurs(corsair), fakeEnemies(overlord), () -> {
            if (atlantis.game.A.now() != 2) return;

            corsair.attackUnit(overlord);
            assertTrue(CorsairDanceToOverlord.applies(corsair),
                "cooling mid-duel: close in, don't trade at range");
        });
    }

    @Test
    public void idleCorsairRelocatesInsteadOfStanding() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        corsair.cooldown = 20;

        world(2, fakeOurs(corsair), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertTrue(CorsairRelocateWhenIdle.applies(corsair),
                "nothing known, nothing shot: relocate, never #NO_COMMAND");
            assertTrue(CorsairBrain.act(corsair),
                "the brain moves the idle Corsair instead of standing");
        });
    }

    @Test
    public void brainIgnoresNonCorsairs() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);

        world(2, fakeOurs(dragoon), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            assertFalse(CorsairBrain.act(dragoon),
                "the brain is a Corsair job");
        });
    }

    @Test
    public void relocationThrottlesItself() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);

        world(2, fakeOurs(corsair), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            if (CorsairBrain.act(corsair)) {
                assertFalse(CorsairRelocateWhenIdle.applies(corsair),
                    "a relocation just went out: let it play, no spam");
            }
        });
    }
}
