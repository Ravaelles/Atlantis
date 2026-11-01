package tests.unit;

import atlantis.combat.FightTarget;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.acceptance.WorldStubForTests;

import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #175, contact-aware bending: a chewed unit never bends its escape
 * step toward the squad (curving while a melee enemy is on top trades
 * separation for shape); the kite owns contact, the arc owns the open
 * field. Same chew veto as the formation hold. Decision pin - the
 * 400-tick arc sims own the geometry, this owns the gate.
 */
@Tag("combat")
public class BendAppliesTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void chewedDragoonKeepsStraightStep() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit chewer = fake(AUnitType.Zerg_Zergling, 21, 20);
        chewer.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(chewer), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FightTarget.bendApplies(dragoon),
                "melee on top: the arc stands down, the kite owns contact");
        });
    }

    @Test
    public void clearDragoonMayBend() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit chewer = fake(AUnitType.Zerg_Zergling, 40, 20);
        chewer.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(chewer), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(FightTarget.bendApplies(dragoon),
                "nothing close: the arc may bend the open-field step");
        });
    }
}
