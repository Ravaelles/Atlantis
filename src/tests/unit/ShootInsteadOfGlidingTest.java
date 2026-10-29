package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.FightTarget;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Shoot instead of gliding (owner 2026-10-29): a Dragoon that re-engaged
 * into the Zerg pack stood next to enemies without firing and died
 * trying to withdraw. Rules, in order: a ready weapon plus a shootable
 * foe stops the naked glide - a standing attack order on the ladder
 * focus issues instead, so the shot fires on arrival; kiting keeps
 * absolute precedence, so threat response never waits for the shot.
 * The subject stays the focus: retargeting onto the nearest foe here
 * splits squad focus fire (pincer pin), the ladder owns who to shoot.
 *
 * <p>Every world builds FRESH units: per-unit caches (enemiesNear,
 * melee counts) are frame-keyed, and frame numbers restart in each
 * world - reusing a unit across two worlds reads the previous world's
 * stale selections.
 */
@Tag("combat")
public class ShootInsteadOfGlidingTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void cleanLog() {
        EventLog.clearForTests();
        BattleCommander.clearForTests();
        FightTarget.clearForTests();
    }

    @AfterEach
    public void cleanLogAfter() {
        EventLog.clearForTests();
        BattleCommander.clearForTests();
        FightTarget.clearForTests();
    }

    private static FakeUnit dragoon(double tx, double ty, int cooldown) {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, tx, ty);
        dragoon.cooldown = cooldown;

        return dragoon;
    }

    private static FakeUnit zealotFoe(double tx, double ty) {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, tx, ty);
        zealot.setEnemy();

        return zealot;
    }

    private static FakeUnit ling(double tx, double ty) {
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, tx, ty);
        ling.setEnemy();

        return ling;
    }

    @Test
    public void attackOrderStandsInsteadOfNakedGlide() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = dragoon(30, 20, 0);
        FakeUnit focus = zealotFoe(45, 20);
        FakeUnit chewer = ling(33.5, 20);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(focus, chewer), () -> {
            if (atlantis.game.A.now() != 2) return;

            FightTarget.fight(dragoon, focus, BattleStrategy.OPEN_FIELD, null, null);

            assertEquals("AttackUnit", dragoon.lastCommand,
                "a ready gun with a foe in range stands an attack order instead of a naked glide");
            assertTrue(dragoon.target == focus,
                "the subject stays the ladder focus - the gate owns the glide, not the target");
        });
    }

    @Test
    public void coolingGlideStillMarchesWithNothingInRange() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = dragoon(30, 20, 22);
        FakeUnit focus = zealotFoe(50, 20);

        world(5, fakeOurs(nexus, dragoon), fakeEnemies(focus), () -> {
            if (atlantis.game.A.now() != 2) return;

            FightTarget.fight(dragoon, focus, BattleStrategy.OPEN_FIELD, null, null);

            assertEquals("Move", dragoon.lastCommand,
                "cooling weapon and nothing in range: the glide still marches");
        });
    }

    @Test
    public void kitePreemptsGlideOnNextThreat() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = dragoon(30, 20, 22);
        FakeUnit focus = zealotFoe(50, 20);
        FakeUnit presser = ling(31, 20);

        world(3, fakeOurs(nexus, dragoon), fakeEnemies(focus, presser), () -> {
            if (atlantis.game.A.now() != 2) return;

            FightTarget.fight(dragoon, focus, BattleStrategy.OPEN_FIELD, null, null);

            assertTrue(EventLog.toJsonLines().contains("\"result\":\"stepped-aside\""),
                "a threat arriving mid-glide kites on the next frame - the glide"
                    + " never suppresses the kite: " + EventLog.toJsonLines());
        });
    }
}
