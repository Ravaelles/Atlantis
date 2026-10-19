package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A Dragoon steps back only while the threat sits inside its safety
 * margin (PurpleWave BeDragoonVsSlowlot, owner 2026-10-19, Wine
 * 1Drag_v_1Zeal). A healthy Dragoon holds to about one tile and keeps
 * shooting through the cooldown; a hurt one yields from farther out.
 * Attack orders issue once per target (no re-spam), mid-animation
 * units get no orders at all. Dragoon-only: Zealots want contact.
 */
public class DragoonKiteTest extends AbstractTestWithWorld {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public FakeUnit[] generateOur() {
        return null;
    }

    @Override
    public FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void healthyDragoonHoldsFireAtTwoTiles() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(BattleCommander.shouldKite(dragoon, zealot),
                "healthy Dragoon holds to ~1 tile and shoots through the cooldown - "
                + "the Zealot may come much closer than 3.5 tiles");
        });
    }

    @Test
    public void gluedDragoonStepsBack() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        dragoon.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertTrue(BattleCommander.shouldKite(dragoon, zealot),
                "dist ~0 is inside any margin - step back");
        });
    }

    @Test
    public void hurtDragoonYieldsFromFartherOut() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;
        dragoon.hp = 20;
        dragoon.shields = 0;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertTrue(BattleCommander.shouldKite(dragoon, zealot),
                "a dying Dragoon demands ~4 tiles of margin, not ~1");
        });
    }

    @Test
    public void readyWeaponKeepsShooting() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 5;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(BattleCommander.shouldKite(dragoon, zealot),
                "weapon almost ready: stand and fire instead of stepping back");
        });
    }

    @Test
    public void facingZealotTriggersEarlierYield() {
        // Total 132: margin required 1.94 tiles. At dist 2 the base
        // margin (2.0) holds - unless the Zealot faces us (-0.5),
        // which is PurpleWave's commitment read (targeted + facing-dot):
        // the Dragoon yields as the Zealot commits to the swing.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit away = fake(AUnitType.Protoss_Zealot, 32, 20);
        FakeUnit toward = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;
        dragoon.hp = 52;
        away.setAngle(0);
        toward.setAngle(Math.PI);

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(away, toward), () -> {
            assertFalse(BattleCommander.shouldKite(dragoon, away),
                "Zealot facing away is not committing - hold and shoot");
            assertTrue(BattleCommander.shouldKite(dragoon, toward),
                "Zealot facing us commits to the swing - yield half a tile early");
        });
    }

    @Test
    public void distantEnemyIsApproachedNotKited() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 36, 20);
        dragoon.cooldown = 20;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(BattleCommander.shouldKite(dragoon, zealot),
                "6 tiles away is approach territory, not the 5-tile threat envelope");
        });
    }

    @Test
    public void zealotsKiteOnlyOnSpentWeapon() {
        // Owner direction (was: never kite): a Zealot with 7+ frames of
        // cooldown left steps back instead of standing through it - the
        // shot is gone either way, distance is free. Fresh weapon holds
        // contact as before.
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 30.5, 20);

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), zealot), fakeEnemies(foe), () -> {
            zealot.cooldown = 6;
            assertFalse(BattleCommander.shouldKite(zealot, foe), "fresh weapon holds contact");
            zealot.cooldown = 7;
            assertTrue(BattleCommander.shouldKite(zealot, foe), "spent weapon steps back");
            assertFalse(BattleCommander.shouldKite(null, foe));
            assertFalse(BattleCommander.shouldKite(zealot, null));
        });
    }

    @Test
    public void coolingDragoonGlidesToFiringPosition() {
        // Dist 4.5, cooldown 20, healthy, unhunted: margin holds, but
        // standing still would donate the whole approach to the chaser.
        // Instead glide to the max-range firing position (3.5 tiles),
        // so the shot fires the instant the weapon is ready (PurpleWave
        // BeDragoonVsSlowlot: advance while safe and cooling).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34.5, 20);
        dragoon.cooldown = 20;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "glide to the firing position while cooling: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "no standing trade from out of range: " + ordersOf(dragoon));
        });
    }

    @Test
    public void gluedReadyDragoonShootsInsteadOfStepping() {
        // Glued at dist 0.3 with a ready weapon: a step cannot outrun
        // the swing already in progress, and stepping every frame means
        // zero shots until death (Wine 1Drag_v_1Zeal: 190 frames glued,
        // 17 hits, no shot). Shoot - the ready shot is free damage.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30.3, 20);
        dragoon.cooldown = 0;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "glued + ready: trade, do not step in place: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "no futile step from glue with a ready weapon: " + ordersOf(dragoon));
        });
    }

    @Test
    public void gluedCoolingDragoonKeepsStepping() {
        // Same glue, weapon cooling: the step is the only thing that
        // can open distance, so it still issues.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30.3, 20);
        dragoon.cooldown = 20;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "glued + cooling: keep stepping: " + ordersOf(dragoon));
        });
    }

    @Test
    public void coolingDragoonLetsItsStepRide() {
        // Dist 0.9, cooldown 15: frame 1 steps (margin broken),
        // frame 2 the margin has recovered (stub Dragoon totals above
        // 186 effective HP, so healthy required is 1.0) but the unit is
        // still gliding - no countermanding attack order issues, the
        // reposition rides (PurpleWave sleep after move). Without this
        // the unit alternates move with attack-stop every frame and the
        // threat closes for free while it stands cooling.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30.9, 20);
        dragoon.cooldown = 15;
        atlantis.battle.BattleCommander.clearForTests();

        try {
            world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                atlantis.battle.BattleCommander.act(
                    dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);
            });

            int moves = 0;
            int attacks = 0;
            for (tests.fakes.FakeOrderSink.Record record : ordersOf(dragoon)) {
                if (record.actorId == dragoon.id() && record.operation.equals("move")) moves++;
                if (record.actorId == dragoon.id() && record.operation.equals("attackUnit")) attacks++;
            }
            assertTrue(moves == 1 && attacks == 0,
                "one step, then ride it - no attack-stop the next frame: "
                    + ordersOf(dragoon));
        }
        finally {
            atlantis.battle.BattleCommander.clearForTests();
        }
    }

    @Test
    public void shortStepIsNoStepSoDragoonFights() {
        // A 0.2-tile "step" opens 0.2 < 0.3 gain: not a step, so no
        // move issues and the Dragoon fights instead of stepping in
        // place (Wine 1Drag_v_1Zeal f310-329: 20 stepped rows, zero
        // pixels moved). Direct fromBest pin, no geometry games.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            assertTrue(
                atlantis.units.actions.move.MoveAway.bestDestination(dragoon, zealot, 0.2) == null,
                "a 0.2-tile hop is not a step");
            assertTrue(
                atlantis.units.actions.move.MoveAway.bestDestination(dragoon, zealot, 2.0) != null,
                "a 2-tile step opens real distance");
        });
    }

    @Test
    public void noRealStepMeansFightNotIdle() {
        // Every compass point mineral-covered: fromBest finds nothing,
        // so the Dragoon attacks instead of standing still.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;
        dragoon.hp = 20;
        dragoon.shields = 0;
        neutralInWorld = new FakeUnit[] {
            fake(AUnitType.Resource_Mineral_Field, 29, 20).setNeutral()
        };
        atlantis.util.log.EventLog.clearForTests();

        try {
            world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                atlantis.battle.BattleCommander.act(
                    dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);
            });

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "no step anywhere: fight in place: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "no futile moves: " + ordersOf(dragoon));
        }
        finally {
            neutralInWorld = null;
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    @Test
    public void frozenDragoonStopsToResetTheQueue() {
        // Steps issue but the unit never moves (order queue wedged -
        // the vanilla engine bug both reference bots unstick: PurpleWave
        // Unstick STOPs past 24 failing frames, Stardust resets past 48
        // motionless ones). After 48 motionless frames the Dragoon
        // issues STOP and names it, instead of stepping in place
        // forever. UNIT_SPEED_MODIFIER 0 pins the body; orders still
        // flow.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;
        dragoon.hp = 20;
        dragoon.shields = 0;
        atlantis.battle.BattleCommander.clearForTests();
        atlantis.util.log.EventLog.clearForTests();
        double oldModifier = tests.acceptance.FakeOnFrameEnd.UNIT_SPEED_MODIFIER_PER_FRAME;
        tests.acceptance.FakeOnFrameEnd.UNIT_SPEED_MODIFIER_PER_FRAME = 0;

        try {
            world(70, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                atlantis.battle.BattleCommander.act(
                    dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);
            });

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "stop"),
                "48 motionless frames with steps issued: STOP to reset the queue: "
                    + ordersOf(dragoon));
            assertTrue(
                atlantis.util.log.EventLog.toJsonLines().contains("\"frozen-reset\""),
                "the reset names itself");
        }
        finally {
            tests.acceptance.FakeOnFrameEnd.UNIT_SPEED_MODIFIER_PER_FRAME = oldModifier;
            atlantis.battle.BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }



    private static boolean hasOrder(java.util.List<tests.fakes.FakeOrderSink.Record> orders,
                                    int unitId, String operation) {
        for (tests.fakes.FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    @Test
    public void kiteStepOpensTheMostDistance() {
        // Open field, hurt Dragoon, Zealot east: the straight step
        // back opens 4 tiles, laterals ~2.8 - fromBest issues the
        // straight one (PurpleWave: retreat where it is safest, not
        // first-validated into a corner). Positions are pixels (x32).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;
        dragoon.hp = 20;
        dragoon.shields = 0;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "margin broken - step: " + ordersOf(dragoon));
            assertTrue(dragoon.targetPosition.x() < 920,
                "straight back west opens the most distance, laterals stay ~960: "
                    + dragoon.targetPosition);
        });
    }

    @Test
    public void fleeingWithoutAnchorKitesInsteadOfIdling() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;
        // Hurt so the margin rule fires at 2 tiles (a healthy Dragoon
        // would rightly hold and shoot here).
        dragoon.hp = 20;
        dragoon.shields = 0;

        // No Nexus: defendAnchor() is null, so FLEE has nowhere to go.
        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FLEE, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "nowhere to run to must still move (kite), never DoNothing: "
                    + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "a Dragoon inside its margin steps back instead of trading: "
                    + ordersOf(dragoon));
        });
    }

    @Test
    public void coolingDragoonHoldsItsOrderInsteadOfRespamming() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        // Healthy at 2 tiles: margin holds, so the Dragoon stands and
        // shoots instead of stepping back. Two acts, one order: the
        // standing attack order fires on its own when the weapon is
        // ready (PurpleWave: order on new target, else sleep).
        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            int attacks = 0;
            for (tests.fakes.FakeOrderSink.Record record : ordersOf(dragoon)) {
                if (record.actorId == dragoon.id() && record.operation.equals("attackUnit")) attacks++;
            }
            assertTrue(attacks == 1,
                "one acquisition, one order - no per-frame re-spam: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "margin holds: stand and shoot, do not step back: " + ordersOf(dragoon));
        });
    }

    @Test
    public void fleeingWithAnchorRunsThere() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FLEE, null, null);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "a real anchor still means run: " + ordersOf(dragoon));
        });
    }

    @Test
    public void midAnimationShotIsNeverInterrupted() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 25;

        // Baseless world, FLEE tactic, close cooling threat.
        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            // A shot already in flight: attack first (sets lastCommand
            // exactly like the real order path does), arm the animation
            // flag, then forget the setup order and ask for the next act.
            dragoon.attackUnit(zealot);
            dragoon.attackFrame = true;
            ((tests.fakes.FakeOrderSink) dragoon.orderSink()).clear();
            if (!dragoon.isAttackFrame()) return;

            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FLEE, null, dragoon);

            assertTrue(ordersOf(dragoon).isEmpty(),
                "mid-animation: hold the shot, issue nothing");
        });
    }

    @Test
    public void readyDragoonAttacksInsteadOfKiting() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 5;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "weapon almost ready: shoot, do not step back: " + ordersOf(dragoon));
        });
    }

    @Test
    public void midSwingThreatGetsTradedWithInsteadOfDodged() {
        // The Zealot's swing is already in flight (attack order +
        // attack frame): a step cannot dodge locked-in damage, but our
        // ready shot still lands. Trade instead of stepping (symmetric
        // animation discipline - the same reason we never interrupt
        // our own shot).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 0;
        zealot.attackUnit(dragoon);
        zealot.attackFrame = true;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "swing in flight: take the trade, do not step: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "a step now only cancels our own shot: " + ordersOf(dragoon));
        });
    }


    @Test
    public void readyDragoonGlidesInsteadOfAttackChasing() {
        // Out of range with a ready weapon: an attack order would chase
        // and get cancelled by the next kite step before firing (Wine
        // 8Drag_v_8Drag: cd stuck at 0, 156 orders, zero shots landed).
        // Glide to the firing position with a move; the attack issues
        // on arrival, when the target is actually in range.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34.5, 20);
        dragoon.cooldown = 0;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "out of range: glide in with a move: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "no attack order that a step would cancel: " + ordersOf(dragoon));
        });
    }

    private static java.util.List<tests.fakes.FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((tests.fakes.FakeOrderSink) unit.orderSink()).orders();
    }

}
