package tests.acceptance;

import atlantis.combat.CombatUnitManager;
import atlantis.combat.generic.DoNothing;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Self-defense is not chasing.
 *
 * <p>Reported from a game vs Steamhammer: one zealot fought and died alone while its
 * twin stood by a building with DoNothing and never moved. Reproduced here as a world,
 * not an anecdote: two zealots under Sparta, one 2 tiles from a marine but 20 tiles
 * from its focus point and 18 from its leader, held {@code DoNothing} for 30 frames -
 * three independent leashes, each saying "don't go there", with nobody saying "but the
 * fight is here": {@code PreventAttacksInMissionDefend} (15 tiles from focus, 8 from
 * leader), the Protoss attack decision (enemy has ranged, leader 15+ away), and
 * {@code IsTargetOnWrongSideOfFocusPoint} - while the mission itself authorized the
 * fight (eval 7.7, threshold 1.3). Each got the same escape: shots incoming, or an
 * enemy inside 3 tiles, means the fight found the unit, and the leash still governs
 * everything farther away.</p>
 *
 * <p>The second assertion is the complement and is <i>also</i> the point: the twin by
 * the base, with nothing near it, still holds DoNothing. Under a defend mission that is
 * the doctrine working - it is not reinforced across the map. If that ever changes, it
 * is a strategy decision, and this test is where it will show up.</p>
 */
public class ZealotSelfDefenseTest extends WorldStubForTests {

    @Test
    public void anAdjacentEnemyClaimsTheFarZealotButNotTheIdleTwin() {
        FakeUnit homeZealot = fake(Protoss_Zealot, 10);
        FakeUnit fightingZealot = fake(Protoss_Zealot, 28);
        FakeUnit enemy = fakeEnemy(Terran_Marine, 30);

        MissionChanger.setGlobalMissionTo(Missions.SPARTA, "early-game defend posture");

        world(30, fakeOurs(
            fake(Protoss_Nexus, 10, 12),
            fake(Protoss_Pylon, 11, 12),
            homeZealot,
            fightingZealot
        ), fakeEnemies(enemy), () -> {
            for (AUnit unit : Select.ourCombatUnits().list()) {
                (new CombatUnitManager(unit)).invokeFrom(this);
            }
        });

        assertFalse(fightingZealot.manager() instanceof DoNothing,
            "a marine 2 tiles away is not 'far away' no matter what the focus point says");
        assertTrue(homeZealot.manager() instanceof DoNothing,
            "nothing is near the base twin, and under Sparta it holds - sending it 20 "
                + "tiles out would be the chase the leashes exist to prevent");
        assertTrue(A.now() >= 30, "sanity: the world ran its frames");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}