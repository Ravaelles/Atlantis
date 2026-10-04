package tests.acceptance;

import atlantis.combat.eval.CombatEvalScale;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.units.select.Count;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Protoss_Photon_Cannon;
import static atlantis.units.AUnitType.Terran_Wraith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Protoss tweaks cannot push eval() below zero, in a world where they do.
 *
 * <p>This is the same class as {@code CombatEvaluatorTest}, except that the bot is
 * Protoss - because {@code ProtossJfapTweaksConsiderChokesEtc} only runs when
 * {@code We.protoss()}, so the whole class is the only place where the sign break of
 * _AI/BUGS.md B-2 can be measured at all.</p>
 *
 * <p>Measured before the floor (same world, same engine data): the Wraith scored
 * <b>-0.3789</b> - the raw ratio 0.0066 minus -0.1 for enemy buildings near and -0.3
 * for two anti-air combat buildings - while the cannons' own side scored 47.50. The
 * absolute score was -760 on both the Terran and the Protoss reading, so the raw
 * ratio is not the thing that changed.</p>
 */
public class ProtossCombatEvalScaleTest extends AbstractTestWithWorld {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void aWraithAgainstTwoDiscoveredCannonsScoresAtTheFloorNotBelowIt() {
        FakeUnit wraith = fake(Terran_Wraith, 90);
        FakeUnit cannon1 = fakeEnemy(Protoss_Photon_Cannon, 92);
        FakeUnit cannon2 = fakeEnemy(Protoss_Photon_Cannon, 93);

        world(1, fakeOurs(wraith), fakeEnemies(), () -> {
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon1);
            EnemyUnitsUpdater.weDiscoveredEnemyUnit(cannon2);

            assertEquals(2, wraith.enemiesNear().size(), "both cannons are near the wraith");
            assertEquals(-760.0, wraith.combatEvalAbsolute(), 0.5,
                "the raw scores are the same ones the Terran world produces");

            assertEquals(CombatEvalScale.FLOOR, wraith.eval(), 0.000001,
                "the tweaks would take this to -0.3789; the floor is the last thing "
                    + "that runs");
            assertTrue(cannon1.eval() > 40, "measured 47.50 - the enemy side is untouched");
        });
    }

    @Test
    public void theFloorIsNotAShortcutForTheNothingToFightValue() {
        FakeUnit wraith = fake(Terran_Wraith, 90);
        FakeUnit dragoon = fakeEnemy(atlantis.units.AUnitType.Protoss_Dragoon, 92).setStasised(true);

        world(1, fakeOurs(wraith), fakeEnemies(dragoon), () -> {
            assertEquals(-1.0, wraith.combatEvalAbsolute(), 0.001, "nothing can shoot back");
            assertTrue(wraith.eval() > 1000,
                "the 'no threat' shortcut stays huge, so every 'eval >= 2' guard in "
                    + "production still reads it as safe");
            assertEquals(0, Count.cannons(), "and this world really is the one it claims to be");
        });
    }

    @Override
    protected FakeUnit[] generateOur() {
        return fakeOurs(fake(Terran_Wraith, 90));
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return fakeEnemies();
    }
}