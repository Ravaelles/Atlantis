package tests.unit;

import atlantis.combat.FrozenWatch;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Frozen-unit detection (owner 2026-10-26): a move/attack order riding
 * with no displacement is a stuck engine order. Trading blows (target
 * in weapon range) and cooldowns are excluded - standing still there
 * is a decision, not a freeze.
 */
@Tag("combat")
public class FrozenWatchTest extends WorldStubForTests {

    @BeforeEach
    public void clear() {
        FrozenWatch.clearForTests();
        atlantis.util.log.EventLog.clearForTests();
    }

    @AfterEach
    public void clean() {
        FrozenWatch.clearForTests();
        atlantis.util.log.EventLog.clearForTests();
    }

    @Test
    public void moveOrderGoingNowhereFreezes() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.cooldown = 0;

        world(FrozenWatch.FROZEN_FRAMES + 10,
            fakeOurs(dragoon), fakeEnemies(), () -> {
                int now = atlantis.game.A.now();
                if (now <= 4) {
                    // Bank deployment first: the opening march is not a
                    // freeze, a stop after moving is.
                    dragoon.position = atlantis.map.position.APosition.createFromPixels(
                        dragoon.position.x() + 32, dragoon.position.y());
                }
                dragoon.move(atlantis.map.position.APosition.create(40, 20),
                    Actions.MOVE_ENGAGE, "FormHold", true);
                FrozenWatch.note(dragoon);

                if (now == FrozenWatch.FROZEN_FRAMES + 6) {
                    assertTrue(FrozenWatch.isFrozen(dragoon),
                        "move order riding, no displacement, no cooldown: frozen");
                }
            });
    }

    @Test
    public void displacementResets() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.cooldown = 0;

        world(FrozenWatch.FROZEN_FRAMES + 5,
            fakeOurs(dragoon), fakeEnemies(), () -> {
                int now = atlantis.game.A.now();
                if (now > 1) {
                    dragoon.position = atlantis.map.position.APosition.createFromPixels(
                        dragoon.position.x() + 32, dragoon.position.y());
                }
                dragoon.move(atlantis.map.position.APosition.create(40, 20),
                    Actions.MOVE_ENGAGE, "FormHold", true);
                FrozenWatch.note(dragoon);

                if (now == FrozenWatch.FROZEN_FRAMES + 1) {
                    assertFalse(FrozenWatch.isFrozen(dragoon),
                        "displacing every frame is progress, never frozen");
                }
            });
    }

    @Test
    public void cooldownExcludes() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.cooldown = 15;

        world(FrozenWatch.FROZEN_FRAMES + 5,
            fakeOurs(dragoon), fakeEnemies(), () -> {
                dragoon.move(atlantis.map.position.APosition.create(40, 20),
                    Actions.MOVE_ENGAGE, "FormHold", true);
                FrozenWatch.note(dragoon);

                if (atlantis.game.A.now() == FrozenWatch.FROZEN_FRAMES + 1) {
                    assertFalse(FrozenWatch.isFrozen(dragoon),
                        "a running cooldown explains standing still");
                }
            });
    }

    @Test
    public void tradingBlowsIsNotFrozen() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();
        dragoon.cooldown = 0;

        world(FrozenWatch.FROZEN_FRAMES + 5,
            fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                dragoon.attackUnit(zealot);
                FrozenWatch.note(dragoon);

                if (atlantis.game.A.now() == FrozenWatch.FROZEN_FRAMES + 1) {
                    assertFalse(FrozenWatch.isFrozen(dragoon),
                        "target in weapon range: standing still is trading, not freezing");
                }
            });
    }

    @Test
    public void attackOrderOnFarTargetFreezes() {
        // The owner's dragoon shape: an attack mission riding at a target
        // it never reaches and never shoots.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 50, 20);
        zealot.setEnemy();
        dragoon.cooldown = 0;

        world(FrozenWatch.FROZEN_FRAMES + 10,
            fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                int now = atlantis.game.A.now();
                if (now <= 4) {
                    dragoon.position = atlantis.map.position.APosition.createFromPixels(
                        dragoon.position.x() + 32, dragoon.position.y());
                }
                dragoon.attackUnit(zealot);
                FrozenWatch.note(dragoon);

                if (now == FrozenWatch.FROZEN_FRAMES + 6) {
                    assertTrue(FrozenWatch.isFrozen(dragoon),
                        "attack order riding, no displacement, nothing in range: frozen");
                }
            });
    }

    @Test
    public void secondConsecutiveFreezeFilesAnError() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.cooldown = 0;

        world(FrozenWatch.FROZEN_FRAMES + 10,
            fakeOurs(dragoon), fakeEnemies(), () -> {
                int now = atlantis.game.A.now();
                if (now <= 4) {
                    dragoon.position = atlantis.map.position.APosition.createFromPixels(
                        dragoon.position.x() + 32, dragoon.position.y());
                }
                dragoon.move(atlantis.map.position.APosition.create(40, 20),
                    Actions.MOVE_ENGAGE, "FormHold", true);
                FrozenWatch.note(dragoon);

                if (now == FrozenWatch.FROZEN_FRAMES + 5) {
                    assertTrue(FrozenWatch.isFrozen(dragoon), "still frozen: " + now);
                    assertTrue(FrozenWatch.frozenStreak(dragoon) >= 2,
                        "consecutive frozen notes accumulate: " + FrozenWatch.frozenStreak(dragoon));
                    assertNotNull(FrozenWatch.frozenErrorFrame(dragoon),
                        "second consecutive freeze files the error");
                }
            });
    }

    @Test
    public void movementResetsTheStreak() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.cooldown = 0;

        world(FrozenWatch.FROZEN_FRAMES + 10,
            fakeOurs(dragoon), fakeEnemies(), () -> {
                int now = atlantis.game.A.now();
                if (now <= 4 || now == FrozenWatch.FROZEN_FRAMES + 3) {
                    dragoon.position = atlantis.map.position.APosition.createFromPixels(
                        dragoon.position.x() + 32, dragoon.position.y());
                }
                dragoon.move(atlantis.map.position.APosition.create(40, 20),
                    Actions.MOVE_ENGAGE, "FormHold", true);
                FrozenWatch.note(dragoon);

                if (now == FrozenWatch.FROZEN_FRAMES + 4) {
                    assertEquals(0, FrozenWatch.frozenStreak(dragoon),
                        "one step of progress resets the episode");
                }
            });
    }
}
