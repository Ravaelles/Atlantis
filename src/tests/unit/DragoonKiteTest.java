package tests.unit;


import atlantis.battle.kite.Kite;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
@Tag("combat")
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
    public void coolingDragoonKitesBeforeProjectedContact() {
        // Projected-contact doctrine (owner; the old hold-at-2-tiles is
        // withdrawn as dangerous): at 1.5 tiles with 15 frames of
        // cooldown the Zealot closes ~1.9 tiles before the weapon is
        // ready - contact is projected, so the Dragoon kites while
        // cooling (costs no DPS) instead of standing into the swing.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 31.5, 20);
        dragoon.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertTrue(Kite.shouldKite(dragoon, zealot),
                "contact projected before the shot is ready - kite while cooling");
        });

        // Same pair, weapon nearly ready: the Zealot closes ~0.6 tiles
        // in 5 frames, no contact before the shot - hold and fire.
        dragoon.cooldown = 5;
        zealot.position = atlantis.map.position.APosition.createFromPixels(1040, 640);
        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(Kite.shouldKite(dragoon, zealot),
                "no contact projected - stand and fire instead of stepping back");
        });
    }

    @Test
    public void pinchedHurtDragoonRetreatsInsteadOfStanding() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.setHp(70);
        dragoon.setShields(0);
        dragoon.cooldown = 3;
        FakeUnit lingA = fake(AUnitType.Zerg_Zergling, 31, 20);
        FakeUnit lingB = fake(AUnitType.Zerg_Zergling, 29, 20);

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(lingA, lingB), () -> {
            assertTrue(Kite.shouldKite(dragoon, lingA),
                "hp 70 with two melee inside 2 tiles: the pocket is closing, "
                + "yield ground instead of standing into it - even with the shot ready");
        });
    }

    @Test
    public void loneHarasserDoesNotTriggerPinch() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.setHp(70);
        dragoon.setShields(0);
        dragoon.cooldown = 3;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 31, 20);
        FakeUnit far = fake(AUnitType.Zerg_Zergling, 60, 20);

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(ling, far), () -> {
            assertFalse(Kite.shouldKite(dragoon, far),
                "one melee inside 2 tiles is harassment, not a pinch - "
                + "the far threat reads through the normal margin");
        });
    }

    @Test
    public void gluedDragoonStepsBack() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        dragoon.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertTrue(Kite.shouldKite(dragoon, zealot),
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
            assertTrue(Kite.shouldKite(dragoon, zealot),
                "a dying Dragoon demands ~4 tiles of margin, not ~1");
        });
    }

    @Test
    public void readyWeaponKeepsShooting() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 5;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(Kite.shouldKite(dragoon, zealot),
                "weapon almost ready: stand and fire instead of stepping back");
        });
    }

    @Test
    public void longCooldownKitesRegardlessOfFacing() {
        // Projected-contact doctrine (owner): at 15 frames of cooldown
        // the Zealot turns in 1 frame and closes ~1.9 tiles before the
        // shot - current facing is noise, projected contact decides.
        // Healthy band, dist 1.8: the margin would still hold
        // (1.8/1.3 vs required ~1.19), so a kite here pins the cooling
        // rule, not the margin.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit away = fake(AUnitType.Protoss_Zealot, 31.8, 20);
        FakeUnit toward = fake(AUnitType.Protoss_Zealot, 31.8, 20);
        dragoon.cooldown = 15;
        away.setAngle(0);
        toward.setAngle(Math.PI);

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(away, toward), () -> {
            assertTrue(Kite.shouldKite(dragoon, away),
                "Zealot facing away still closes before the shot - kite, facing is noise at long cooldown");
            assertTrue(Kite.shouldKite(dragoon, toward),
                "Zealot facing us commits to the swing - kite");
        });

        // Wounded reach: at 2.4 tiles the healthy radius (2.0) stays
        // silent - only the wounded radius (2.6) kites, so hurt guns
        // yield from farther out while they cannot shoot anyway.
        FakeUnit hurtDragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 32.4, 20);
        hurtDragoon.cooldown = 15;
        hurtDragoon.hp = 80;
        hurtDragoon.shields = 0;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), hurtDragoon), fakeEnemies(foe), () -> {
            assertTrue(Kite.shouldKite(hurtDragoon, foe),
                "wounded at 2.4: yield early, every tile kept is a swing avoided");
        });
    }

    @Test
    public void shortCooldownStillReadsFacing() {
        // Same distance, weapon nearly ready: the Zealot closes ~0.6
        // tiles in 5 frames, so contact is NOT projected - the cooling
        // rule stays silent, the margin gate decides, and facing still
        // discriminates there. Band just above wounded (140/16 = 8.75
        // hits left, required margin ~1.81): away holds at 2.0, toward
        // yields at 1.5.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit away = fake(AUnitType.Protoss_Zealot, 32, 20);
        FakeUnit toward = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 5;
        dragoon.hp = 140;
        dragoon.shields = 0;
        away.setAngle(0);
        toward.setAngle(Math.PI);

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(away, toward), () -> {
            assertFalse(Kite.shouldKite(dragoon, away),
                "Zealot facing away is not committing and contact is not projected - hold and shoot");
            assertTrue(Kite.shouldKite(dragoon, toward),
                "Zealot facing us commits - the faced margin yields even with the shot near");
        });
    }

    @Test
    public void screenedDragoonStillKitesByMargin() {
        // Owner order (meat-screen stand withdrawn): friendly zealots
        // screening inside 8 tiles no longer hold the guns - a healthy
        // dragoon kites by the same margin as everyone else, health
        // plays no role. Below 40 hp the old yield answers as before.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit screen = fake(AUnitType.Protoss_Zealot, 31, 21);
        FakeUnit toward = fake(AUnitType.Protoss_Zealot, 32, 20);
        toward.setEnemy();
        toward.setAngle(Math.PI);
        dragoon.cooldown = 15;
        dragoon.hp = 52;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon, screen),
            fakeEnemies(toward), () -> {
                assertTrue(Kite.shouldKite(dragoon, toward),
                    "screened but healthy: the margin kites anyway");

                dragoon.hp = 20;
                dragoon.shields = 0;
                assertTrue(Kite.shouldKite(dragoon, toward),
                    "below 40 hp the old yield answers - dying trades nothing");
            });
    }

    @Test
    public void chewedDragoonKites() {
        // Contact voids every stand (game 185100, sim 333/333 before the
        // doctrine): with a melee enemy already on the guns the dragoon
        // kites by margin, regardless of health or company. The approach
        // (chewer outside hitting range) still holds or kites by the
        // regular gates - pinned next door.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit screen = fake(AUnitType.Protoss_Zealot, 31, 21);
        FakeUnit chewer = fake(AUnitType.Protoss_Zealot, 30.5, 20);
        chewer.setEnemy();
        dragoon.cooldown = 15;
        dragoon.hp = 52;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon, screen),
            fakeEnemies(chewer), () -> {
                assertTrue(Kite.isBeingChewed(dragoon), "0.5 tiles: already hitting");
                assertTrue(Kite.shouldKite(dragoon, chewer),
                    "chewed dragoon kites, screen or no screen");
            });
    }

    @Test
    public void chewedDragoonVoidsTheSurroundedStand() {
        // Owner order: kiting has absolute precedence. Three melee around
        // (surrounded shape) but one already chewing at 0.5 tiles: the
        // stand-and-shoot rule voids, the margin answers. The plain
        // surrounded stand (chewers at 1.0+, no contact) still holds -
        // pinned next door by surroundedDragoonStandsAndShoots.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit lingA = fake(AUnitType.Protoss_Zealot, 61, 20);
        FakeUnit lingB = fake(AUnitType.Protoss_Zealot, 61, 21);
        FakeUnit chewer = fake(AUnitType.Protoss_Zealot, 60.5, 20);
        lingA.setEnemy();
        lingB.setEnemy();
        chewer.setEnemy();
        dragoon.hp = 100;
        dragoon.shields = 0;
        dragoon.cooldown = 4;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon),
            fakeEnemies(lingA, lingB, chewer), () -> {
                assertTrue(Kite.isBeingChewed(dragoon), "0.5 tiles: already hitting");
                assertTrue(Kite.shouldKite(dragoon, chewer),
                    "chewed while surrounded: the kite voids the stand");
            });
    }

    @Test
    public void chewedLowHpDragoonKitesDespiteImminentShot() {
        // Low hp, weapon ready in 3 frames, chewer on the guns: the
        // imminent-shot hold voids too - the shot is not worth contact.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit chewer = fake(AUnitType.Protoss_Zealot, 60.5, 20);
        chewer.setEnemy();
        dragoon.hp = 30;
        dragoon.shields = 0;
        dragoon.cooldown = 3;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon),
            fakeEnemies(chewer), () -> {
                assertTrue(Kite.shouldKite(dragoon, chewer),
                    "chewed at low hp: kite even with the shot almost ready");
            });
    }

    @Test
    public void distantEnemyIsApproachedNotKited() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 36, 20);
        dragoon.cooldown = 20;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            assertFalse(Kite.shouldKite(dragoon, zealot),
                "6 tiles away is approach territory, not the 5-tile threat envelope");
        });
    }

    @Test
    public void zealotsKiteOnlyWhenWoundedOnSpentWeapon() {
        // Owner direction, NEXT #178 (was: kite on spent weapon alone): a
        // Zealot with 7+ frames of cooldown left steps back ONLY when
        // wounded - the shot is gone either way, but an unwounded line
        // trading contact must not string itself out. Fresh weapon holds
        // contact as before; the wound matrix lives in ZealotWithdrawTest.
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 30.5, 20);

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), zealot), fakeEnemies(foe), () -> {
            zealot.cooldown = 6;
            assertFalse(Kite.shouldKite(zealot, foe), "fresh weapon holds contact");
            zealot.cooldown = 7;
            assertFalse(Kite.shouldKite(zealot, foe),
                "spent but unwounded: trade contact, don't string the line");
            zealot.setHp(zealot.maxHp() - 20);
            assertTrue(Kite.shouldKite(zealot, foe), "spent and wounded: step back");
            assertFalse(Kite.shouldKite(null, foe));
            assertFalse(Kite.shouldKite(zealot, null));
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
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "glide to the firing position while cooling: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "no standing trade from out of range: " + ordersOf(dragoon));
        });
    }

    @Test
    public void gluedReadyDragoonStepsInsteadOfTrading() {
        // Owner 2026-10-26, kiting outranks trading: glued at dist 0.3
        // with a ready weapon, the old rule stood and traded while
        // being chewed. Safety of the single unit comes first - step
        // out (the margin already says so), shoot once clear. The old
        // 1Drag reading (trade at point blank) stays valid one-on-one;
        // against anything that outnumbers the line, standing is dying.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30.3, 20);
        dragoon.cooldown = 0;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "glued + ready: step out instead of trading in place: " + ordersOf(dragoon));
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
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

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
                    dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
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
        // Every compass point mineral-covered at short range: the old
        // contract fought in place, but a full-length sidestep exists
        // (NEXT #173: long-vector-first), so the Dragoon steps out and
        // kites instead of trading a lost fight at 20 hp.
        // Standing still stays forbidden - the step replaces the
        // attack, not the idle.
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
                    dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
            });

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "mineral-boxed short step retries long instead of trading: "
                    + ordersOf(dragoon));
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
                    dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
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
        // Open field, hurt Dragoon, Zealot east: the straight step back
        // opens the most distance, laterals stay closer - fromBest issues
        // the straight one (PurpleWave: retreat where it is safest, not
        // first-validated into a corner). The step *length* is the Stardust
        // hold point, not a fixed 2-tile shove - positions are pixels (x32).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;
        dragoon.hp = 20;
        dragoon.shields = 0;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "margin broken - step: " + ordersOf(dragoon));
            assertTrue(
                dragoon.targetPosition.x() < 940
                    && Math.abs(dragoon.targetPosition.y() - 640) <= 4,
                "straight back west, on axis - not a diagonal lateral: "
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
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FLEE, null, dragoon);

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
        dragoon.cooldown = 5;

        // Weapon nearly ready at 2 tiles: no contact projected, the
        // cooling rule stays silent and the margin holds, so the
        // Dragoon stands and shoots instead of stepping back.
        // Two acts, one order: the standing attack order fires on its
        // own when the weapon is ready (PurpleWave: order on new
        // target, else sleep).
        // Based on purpose: order discipline, not kiting.
        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

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
    public void coolingDragoonKeepsItsTargetWhenAnotherComesNearer() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealotA = fake(AUnitType.Protoss_Zealot, 33, 20);
        FakeUnit zealotB = fake(AUnitType.Protoss_Zealot, 34, 20);
        dragoon.cooldown = 15;

        // First acquisition takes the nearer A. When B steps nearer, a
        // healthy committed Dragoon keeps shooting A while A lives and
        // stays in range (Stardust isReady refusal, PurpleWave
        // new/overdue discipline) - per-frame re-picking restarts the
        // attack animation faster than it completes (Wine ZealDrag:
        // 519 acquisitions for 19 kills). Only A's death re-arms the pick.
        // Based on purpose: target stickiness, not kiting. Both zealots
        // stay outside the projected-contact radius (A at 3.0, B steps
        // to 2.8 - nearer, still beyond 2.6), so the cooling rule stays
        // silent and the standoff is about the pick, not the step.
        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealotA, zealotB), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
            assertTrue(attackOrders(dragoon) == 1, "acquires A: " + ordersOf(dragoon));

            zealotB.position = atlantis.map.position.APosition.createFromPixels(1050, 20 * 32);
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
            assertTrue(attackOrders(dragoon) == 1,
                "B nearer but A alive and in range - no switch, no re-order: " + ordersOf(dragoon));

            zealotA.hp = 0;
            dragoon.cooldown = 0;
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);
            assertTrue(attackOrders(dragoon) == 2,
                "A dead - the pick re-arms onto B: " + ordersOf(dragoon));
        });
    }

    @Test
    public void zealotHoldsFormationInsteadOfStepping() {
        // NEXT #179 (was: trade contact): with Dragoons behind, a Zealot
        // holds instead of initiating - the old pin below assumed the
        // attack it now withholds. Pinned so reviving it stays conscious.
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 60, 20);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 62, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 61, 20);
        zealot.cooldown = 10;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), zealot, dragoon), fakeEnemies(foe), () -> {
            atlantis.battle.BattleCommander.act(
                zealot, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, zealot);

            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "no sidestep, the line stays packed: " + ordersOf(zealot));
            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "attackUnit"),
                "no ranged shot yet: hold, don't initiate: " + ordersOf(zealot));
        });
    }

    @Test
    public void lonelyZealotAlsoTradesContact() {
        // Same Zealot, no Dragoons behind: still contact, still no step
        // (see above - the cooldown step cannot issue for range-0).
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 60, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 61, 20);
        zealot.cooldown = 10;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), zealot), fakeEnemies(foe), () -> {
            atlantis.battle.BattleCommander.act(
                zealot, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, zealot);

            assertFalse(
                hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "contact, not steps - lonely either: " + ordersOf(zealot));
            assertTrue(
                hasOrder(ordersOf(zealot), zealot.id(), "attackUnit"),
                "lonely Zealot still trades: " + ordersOf(zealot));
        });
    }

    @Test
    public void lowHpDragoonStandsForAnImminentShot() {
        // Below 45 health the Dragoon holds fire unless the shot is safe -
        // with 3 frames to go it stands and shoots instead of stepping and
        // delaying the one shot that still matters (owner; Stardust attacks
        // instead of stepping when cooldown <= latency + 2).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 62, 20);
        dragoon.hp = 20;
        dragoon.shields = 0;
        dragoon.cooldown = 3;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "imminent shot lands - no step: " + ordersOf(dragoon));
            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "stands and shoots: " + ordersOf(dragoon));
        });
    }

    @Test
    public void lowHpDragoonKitesInsteadOfStandingThroughCooldown() {
        // Same duel with a long cooldown left: holding fire means stepping,
        // never standing through it.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 62, 20);
        dragoon.hp = 20;
        dragoon.shields = 0;
        dragoon.cooldown = 10;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "holding fire at dist 2 with cd 10 steps back: " + ordersOf(dragoon));
        });
    }

    @Test
    public void lowHpDragoonShootsFromSafeDistance() {
        // 3.5 tiles of room: the shot is safe, hold and shoot.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 63.5, 20);
        dragoon.hp = 20;
        dragoon.shields = 0;
        dragoon.cooldown = 10;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "3.5 tiles is room enough - hold: " + ordersOf(dragoon));
            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "shoots from safe distance: " + ordersOf(dragoon));
        });
    }

    @Test
    public void surroundedDragoonStandsAndShoots() {
        // Three melee enemies on top: escape is impossible, so a nearly
        // ready weapon stands and shoots instead of donating the retreat.
        // (West stays open here, so standing is a choice, not a wall.)
        // Health is set explicitly: hp() already includes shields, so the
        // default full bar reads 260 and the margin gate sits exactly on
        // its boundary; 100+0 pins hitsLeft 6.25, where the margin WOULD
        // kite and only the stand rule holds.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit lingA = fake(AUnitType.Protoss_Zealot, 61, 20);
        FakeUnit lingB = fake(AUnitType.Protoss_Zealot, 61, 21);
        FakeUnit lingC = fake(AUnitType.Protoss_Zealot, 61, 19);
        dragoon.hp = 100;
        dragoon.shields = 0;
        dragoon.cooldown = 4;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(lingA, lingB, lingC), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "surrounded - no escape to kite into: " + ordersOf(dragoon));
            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "stands and shoots: " + ordersOf(dragoon));
        });
    }

    @Test
    public void surroundedDragoonWithLongCooldownStillKites() {
        // Same surround but 10 frames to go: no imminent shot, so the
        // margin kite still tries west into the open (the stand right
        // needs cd <= 6).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 60, 20);
        FakeUnit lingA = fake(AUnitType.Protoss_Zealot, 61, 20);
        FakeUnit lingB = fake(AUnitType.Protoss_Zealot, 61, 21);
        FakeUnit lingC = fake(AUnitType.Protoss_Zealot, 61, 19);
        dragoon.hp = 100;
        dragoon.shields = 0;
        dragoon.cooldown = 10;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(lingA, lingB, lingC), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "nothing imminent - the kite still tries west: " + ordersOf(dragoon));
        });
    }

    private static int attackOrders(FakeUnit unit) {
        int attacks = 0;
        for (tests.fakes.FakeOrderSink.Record record : ordersOf(unit)) {
            if (record.actorId == unit.id() && record.operation.equals("attackUnit")) attacks++;
        }
        return attacks;
    }

    @Test
    public void fleeingWithAnchorRunsThere() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), dragoon), fakeEnemies(zealot), () -> {
            atlantis.battle.BattleCommander.act(
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FLEE, null, null);

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
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FLEE, null, dragoon);

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
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

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
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

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
                dragoon, atlantis.battle.cluster.FightOrFlight.Tactic.FIGHT, null, dragoon);

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "out of range: glide in with a move: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "no attack order that a step would cancel: " + ordersOf(dragoon));
        });
    }

    @Test
    public void boxedShortStepRetriesLongInsteadOfStanding() {
        // NEXT #173, owner doctrine: the mineral line eats every short aim
        // (straight vetoed, laterals gain-vetoed), so short validates
        // nowhere - instead of fighting in place, one full-length step
        // out. Micro-gaps below one tile keep the old null: leaping 5
        // for 0.5 tiles of needed ground would donate the standoff.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.setHp(40);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 20, 20);
        neutralInWorld = new FakeUnit[] {
            fake(AUnitType.Resource_Mineral_Field, 31, 20).setNeutral()
        };

        try {
            world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                assertNull(
                    atlantis.units.actions.move.MoveAway.bestDestination(dragoon, zealot, 0.5),
                    "micro-gaps keep the old null - no 5-tile leaps for 0.5 tiles of need");
                atlantis.map.position.APosition dest =
                    atlantis.units.actions.move.MoveAway.bestDestination(dragoon, zealot, 1.5);
                assertNotNull(dest, "boxed short step retries long instead of standing");
                assertTrue(Math.abs(dragoon.distTo(dest) - 5.0) < 0.15,
                    "full-length sidestep past the mineral line: " + dest);
            });
        }
        finally {
            neutralInWorld = null;
        }
    }

    private static java.util.List<tests.fakes.FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((tests.fakes.FakeOrderSink) unit.orderSink()).orders();
    }
}
