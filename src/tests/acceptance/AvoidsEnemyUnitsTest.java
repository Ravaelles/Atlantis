package tests.acceptance;

import atlantis.combat.micro.avoid.EnemyUnitsToAvoid;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Which enemies a unit wants to avoid.
 *
 * <p>The previous version of this test drove {@code CombatUnitManager} and
 * expected a marine to keep 1.1+ tiles away from a zealot. That behaviour is no
 * longer in the manager chain - the avoid managers were commented out of
 * {@code TerranCombatManagerMediumPriority}, and avoidance is now driven by
 * whoever assembles the enemy list ({@code DoAvoidEnemies} takes its
 * {@code Units} as a constructor argument). The test therefore drives the seam
 * that is actually live: {@link EnemyUnitsToAvoid}.</p>
 */
public class AvoidsEnemyUnitsTest extends AbstractTestWithWorld {

    @Test
    public void aZealotNextToOurMarineIsSomethingToAvoid() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 11);

        createWorld(1, fakeOurs(marine), fakeEnemies(zealot), () -> {
            Units toAvoid = new EnemyUnitsToAvoid(marine).unitsToAvoid(false);

            assertEquals(1, toAvoid.size(), "a melee zealot next to us is a threat");
            assertEquals(zealot, toAvoid.first());
        });
    }

    @Test
    public void aZealotOutOfReachIsNotEvenAPotentialEnemy() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 16);

        createWorld(1, fakeOurs(marine), fakeEnemies(zealot), () -> {
            // potentialEnemies() keeps only enemies that can reach us within a
            // 5 tile margin, and a zealot is melee: 6 tiles away it is out of
            // reach, so it never enters the list (measured, not assumed).
            assertEquals(0, new EnemyUnitsToAvoid(marine).unitsToAvoid(false).size(),
                "6 tiles is out of a zealot's reach, even with the 5 tile margin");
            assertEquals(0, new EnemyUnitsToAvoid(marine).unitsToAvoid(true).size());
        });
    }

    @Test
    public void aZealotWithinTheMarginIsPotentialButNotDangerouslyClose() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 14);

        createWorld(1, fakeOurs(marine), fakeEnemies(zealot), () -> {
            assertEquals(1, new EnemyUnitsToAvoid(marine).unitsToAvoid(false).size(),
                "4 tiles is inside the 5 tile reach margin");
            assertEquals(0, new EnemyUnitsToAvoid(marine).unitsToAvoid(true).size(),
                "but not close enough to be 'dangerously close'");
        });
    }

    @Test
    public void aUnitThatCannotAttackUsIsNotAPotentialEnemy() {
        FakeUnit marine = fake(AUnitType.Terran_Marine, 10);
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 11);

        createWorld(1, fakeOurs(marine), fakeEnemies(overlord), () -> {
            assertEquals(0, new EnemyUnitsToAvoid(marine).unitsToAvoid(false).size(),
                "an overlord cannot shoot back");
        });
    }

    @Override
    protected FakeUnit[] generateOur() {
        return fakeOurs(fake(AUnitType.Terran_Marine, 10));
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return fakeEnemies(fake(AUnitType.Protoss_Zealot, 14));
    }
}
