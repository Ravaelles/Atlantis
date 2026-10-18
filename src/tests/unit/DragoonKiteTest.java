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
    public void zealotsNeverKite() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.cooldown = 15;

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20), zealot), fakeEnemies(foe), () -> {
            assertFalse(BattleCommander.shouldKite(zealot, foe), "Zealots want contact");
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

    private static java.util.List<tests.fakes.FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((tests.fakes.FakeOrderSink) unit.orderSink()).orders();
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
    public void walledInDragoonSidestepsInsteadOfIdling() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        dragoon.cooldown = 25;
        // Hurt so the margin rule fires at 2 tiles (a healthy Dragoon
        // would rightly hold and shoot here, no step needed).
        dragoon.hp = 20;
        dragoon.shields = 0;
        // Mineral wall directly behind the Dragoon: the straight step
        // lands next to minerals and is refused, the lateral ones are
        // free. Old code stood still (then fought point blank); the
        // compass must find the sidestep.
        neutralInWorld = new FakeUnit[] {
            fake(AUnitType.Resource_Mineral_Field, 26, 20).setNeutral()
        };
        atlantis.util.log.EventLog.clearForTests();

        try {
            world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                atlantis.battle.BattleCommander.act(
                    dragoon, atlantis.battle.FightOrFlight.Tactic.FIGHT, null, dragoon);
            });

            assertTrue(
                hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                "a wall behind must not mean standing still - sidestep: " + ordersOf(dragoon));
            assertFalse(
                hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                "no standing trade while a step exists: " + ordersOf(dragoon));
            assertTrue(dragoon.targetPosition.x() > 940,
                "straight back runs into the mineral wall - sidestep east instead: "
                    + dragoon.targetPosition);
        }
        finally {
            neutralInWorld = null;
            atlantis.util.log.EventLog.clearForTests();
        }
    }
}
