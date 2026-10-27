package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.FormationHold;
import atlantis.combat.Regroup;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The march without a target: with no strategy point and no ghost the
 * formation fallback owns the unit - except with melee on top, when
 * the hard veto skips the slots (same rule as the calm hold: formation
 * never stands into chews, with or without a target).
 */
@Tag("combat")
public class RegroupTest extends WorldStubForTests {

    @Test
    public void meleeOnTopSkipsFormationSlots() {
        // The chewer is invisible: visible enemies get acquired and the
        // fight owns the unit, so the no-target march only ever meets
        // melee on top through fog - exactly the shape the veto covers.
        // Pivot (the fogged chewer) sits 5.5 tiles from the leader,
        // so slots ARE offered - but 1.5 from the marching unit, so the
        // veto must skip them. (Chewer within 3.3 of the leader refuses
        // slots on pivot radius alone and would prove nothing.)
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 24, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 20, 21);
        FakeUnit chewer = fake(AUnitType.Protoss_Zealot, 25.5, 20);
        chewer.setEnemy();
        chewer.isVisibleUnitOnMap = false;

        world(30, fakeOurs(a, b, c), fakeEnemies(chewer), () -> {
            if (atlantis.game.A.now() != 25) return;

            Alpha.get().addUnit(a);
            Alpha.get().addUnit(b);
            Alpha.get().addUnit(c);
            try {
                Regroup.march(b, FightOrFlight.Tactic.FIGHT,
                    atlantis.map.position.APosition.create(60, 20), a, BattleStrategy.OPEN_FIELD);

                String journal = b.commandHistory().toString();
                assertTrue(FormationHold.hasCloseMeleeEnemy(b), "veto sees the fogged chewer");
                assertFalse(journal.contains("ClusterForm"),
                    "no formation slot with melee on top: " + journal);
                assertTrue(journal.contains("ClusterGhost") || journal.contains("ClusterMove"),
                    "the unit still marches, just never in formation: " + journal);
            } finally {
                Alpha.get().removeUnit(a);
                Alpha.get().removeUnit(b);
                Alpha.get().removeUnit(c);
            }
        });
    }
}
