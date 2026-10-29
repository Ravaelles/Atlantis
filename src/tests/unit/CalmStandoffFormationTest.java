package tests.unit;

import atlantis.battle.DuelMemory;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.FormationHold;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Calm standoff at a choke (owner 2026-10-29, Cere game): the army formed
 * up facing the wall while the target sat in another region, and never
 * attacked. No cluster damage in 15s + enemy farther than 15 + we within
 * 5 of a choke + no enemy land combat unit means march, not formation.
 * Workers and weaponless enemies never count.
 */
@Tag("combat")
public class CalmStandoffFormationTest extends WorldStubForTests {

    private static FakeUnit dragoon(double tx, double ty) {
        return fake(AUnitType.Protoss_Dragoon, tx, ty);
    }

    private static FakeUnit farLing() {
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 50, 20);
        ling.setEnemy();

        return ling;
    }

    private static FakeUnit farOverlord() {
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 50, 20);
        overlord.setEnemy();

        return overlord;
    }

    private static MockedStatic<Chokes> chokeAt(APosition center) {
        AChoke choke = Mockito.mock(AChoke.class);
        Mockito.when(choke.center()).thenReturn(center);

        MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class);
        chokes.when(() -> Chokes.nearestChoke(Mockito.any())).thenReturn(choke);

        return chokes;
    }

    @AfterEach
    public void clearAlpha() {
        for (AUnit u : Alpha.get().selection().list()) {
            Alpha.get().removeUnit(u);
        }
    }

    @Test
    public void calmFarAndAtChokeMarchesInsteadOfForming() {
        FakeUnit dragoon = dragoon(30, 20);
        FakeUnit overlord = farOverlord();

        try (MockedStatic<Chokes> chokes = chokeAt(APosition.create(32, 20))) {
            world(3, fakeOurs(dragoon), fakeEnemies(overlord), () -> {
                assertTrue(FormationHold.calmStandoffAtChoke(dragoon),
                    "calm, enemy 20 out, choke 2.8 out, no land combat - march");
                assertFalse(FormationHold.shouldHold(
                    dragoon, overlord, FightOrFlight.Tactic.FIGHT),
                    "the hold declines, the march owns the unit");
            });
        }
    }

    @Test
    public void landCombatAnywhereKeepsTheFormation() {
        FakeUnit dragoon = dragoon(30, 20);
        FakeUnit overlord = farOverlord();
        FakeUnit ling = farLing();

        try (MockedStatic<Chokes> chokes = chokeAt(APosition.create(32, 20))) {
            world(3, fakeOurs(dragoon), fakeEnemies(overlord, ling), () -> {
                assertFalse(FormationHold.calmStandoffAtChoke(dragoon),
                    "a land combat unit anywhere vetoes the standoff");
            });
        }
    }

    @Test
    public void nearEnemyKeepsTheFormation() {
        FakeUnit dragoon = dragoon(30, 20);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 40, 20);
        ling.setEnemy();

        try (MockedStatic<Chokes> chokes = chokeAt(APosition.create(32, 20))) {
            world(3, fakeOurs(dragoon), fakeEnemies(ling), () -> {
                assertFalse(FormationHold.calmStandoffAtChoke(dragoon),
                    "enemy 10 out is not far - the standoff does not apply");
            });
        }
    }

    @Test
    public void freshDamageKeepsTheFormation() {
        FakeUnit dragoon = dragoon(30, 20);
        FakeUnit overlord = farOverlord();
        Alpha.get().addUnit(dragoon);

        try (MockedStatic<Chokes> chokes = chokeAt(APosition.create(32, 20))) {
            world(3, fakeOurs(dragoon), fakeEnemies(overlord), () -> {
                dragoon.hp = 100;
                DuelMemory.noteBattleFrame(dragoon);
                dragoon.hp = 80;
                DuelMemory.noteBattleFrame(dragoon);

                assertFalse(FormationHold.calmStandoffAtChoke(dragoon),
                    "damage now means contact is near, not calm");
            });
        }
    }

    @Test
    public void farFromChokeKeepsTheFormation() {
        FakeUnit dragoon = dragoon(30, 20);
        FakeUnit overlord = farOverlord();

        try (MockedStatic<Chokes> chokes = chokeAt(APosition.create(60, 60))) {
            world(3, fakeOurs(dragoon), fakeEnemies(overlord), () -> {
                assertFalse(FormationHold.calmStandoffAtChoke(dragoon),
                    "open field is not a choke standoff");
            });
        }
    }
}
