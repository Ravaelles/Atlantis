package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.FormationHold;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Formation hold (owner 2026-10-26, Stardust doctrine): while the enemy
 * is melee-only and nobody is in contact, the line holds instead of
 * dissolving into a charge. Contact means 2.5 tiles, damage taken, or
 * own ranged able to fire.
 */
@Tag("combat")
public class FormationHoldTest extends WorldStubForTests {

    @Test
    public void meleeOnlyEnemyFarAwayHolds() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 40, 20);
        zealot.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(FormationHold.shouldHold(dragoon, zealot, FightOrFlight.Tactic.FIGHT),
                "melee-only enemy, nobody in contact: hold the line");
        });
    }

    @Test
    public void breachedBaseNeverHolds() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 30);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 24, 20);
        ling.setEnemy();

        world(1, fakeOurs(nexus, zealot), fakeEnemies(ling), () -> {
            assertTrue(atlantis.combat.cluster.ClusterBreach.breachForcesFight(),
                "raider within 8 of the main: the test board is breached");
            assertFalse(FormationHold.shouldHold(zealot, ling, FightOrFlight.Tactic.FIGHT),
                "breach means everybody attacks: no forming up while raiders chew the base");
        });
    }

    @Test
    public void breachedBaseTwinsBreachNotHold() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 30);
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 24, 20);
        ling.setEnemy();

        world(1, fakeOurs(nexus, zealot), fakeEnemies(ling), () -> {
            assertFalse(FormationHold.holdIfCalm(
                    zealot, ling, FightOrFlight.Tactic.FIGHT, zealot, null),
                "breach declines the hold before any slot is considered");
            String rows = atlantis.util.log.EventLog.toJsonLines();
            assertTrue(rows.contains("\"verdict\":\"BREACH\""),
                "the twin names the deciding gate: " + rows);
        });
    }

    @Test
    public void rangedEnemyNeverHolds() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit foeZealot = fake(AUnitType.Protoss_Zealot, 40, 20);
        foeZealot.setEnemy();
        FakeUnit foeDragoon = fake(AUnitType.Protoss_Dragoon, 42, 20);
        foeDragoon.setEnemy();

        world(2, fakeOurs(zealot), fakeEnemies(foeZealot, foeDragoon), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(zealot, foeZealot, FightOrFlight.Tactic.FIGHT),
                "a ranged enemy means the arc would march into fire: fight as before");
        });
    }

    @Test
    public void fleeNeverHolds() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 40, 20);
        zealot.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(dragoon, zealot, FightOrFlight.Tactic.FLEE),
                "retreats own the unit above the hold");
        });
    }

    @Test
    public void ownRangedAbleToFireBreaks() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 23, 20);
        zealot.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(dragoon, zealot, FightOrFlight.Tactic.FIGHT),
                "dragoon reaches 3 tiles: the guns own the unit now");
        });
    }

    @Test
    public void contactBreaks() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 22, 20);
        foe.setEnemy();

        world(2, fakeOurs(zealot), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(zealot, foe, FightOrFlight.Tactic.FIGHT),
                "2 tiles is contact: the phalanx becomes the fight");
        });
    }

    @Test
    public void chewedDragoonBreaksBeforeContact() {
        // Owner 2026-10-26: kiting outranks holding, unconditionally.
        // A chewer closing inside 3 tiles breaks the hold even at full
        // health on a fresh weapon - waiting for damage (HP history
        // misses shields) or contact let dragoons hold while shredded.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit presser = fake(AUnitType.Protoss_Zealot, 33, 20);
        presser.setEnemy();
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 38, 20);
        far.setEnemy();
        dragoon.cooldown = 0;

        world(2, fakeOurs(dragoon), fakeEnemies(presser, far), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(FormationHold.needsKite(dragoon), "chewer closing: kite");
            assertFalse(FormationHold.shouldHold(dragoon, far, FightOrFlight.Tactic.FIGHT),
                "no holding the slot while chewed, whatever the cooldown");
        });
    }

    @Test
    public void holdStandsWithNobodyClose() {
        // Same board without the presser: the hold stands, the line
        // marches in formation instead of charging one by one.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 38, 20);
        far.setEnemy();
        dragoon.cooldown = 0;

        world(2, fakeOurs(dragoon), fakeEnemies(far), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.needsKite(dragoon), "nothing close: no preemption");
            assertTrue(FormationHold.shouldHold(dragoon, far, FightOrFlight.Tactic.FIGHT),
                "hold the line while the enemy is still far");
        });
    }

    @Test
    public void meleeOnTopNeverHolds() {
        // Owner 2026-10-26, hard veto: a melee enemy within 3 tiles ends
        // every formation discussion - literally never hold into chews.
        // Live dragoons stood in slots while zealots shredded them; the
        // contact, damage and kite gates all failed open on that board,
        // so the raw distance answers first with no plumbing to fail.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit chewer = fake(AUnitType.Protoss_Zealot, 21.5, 20);
        chewer.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(chewer), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(FormationHold.hasCloseMeleeEnemy(dragoon), "veto sees the chewer");
            assertFalse(FormationHold.shouldHold(dragoon, chewer, FightOrFlight.Tactic.FIGHT),
                "melee at 1.5 tiles: literally never hold");
            assertFalse(FormationHold.holdIfCalm(dragoon, chewer, FightOrFlight.Tactic.FIGHT, dragoon, null),
                "the calm hold honors the veto too");
        });
    }

    @Test
    public void vetoEndsPastThreeTiles() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 23.5, 20);
        zealot.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.hasCloseMeleeEnemy(dragoon),
                "3.5 tiles is pressure, not melee on top - other gates decide");
        });
    }

    @Test
    public void vetoBreaksAtTwoPointNine() {
        // Owner doctrine: melee within 3 tiles breaks the hold, no exceptions.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit chewer = fake(AUnitType.Protoss_Zealot, 22.9, 20);
        chewer.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(chewer), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(FormationHold.hasCloseMeleeEnemy(dragoon), "veto sees the chewer at 2.9");
            assertFalse(FormationHold.shouldHold(dragoon, chewer, FightOrFlight.Tactic.FIGHT),
                "melee at 2.9 tiles: literally never hold");
        });
    }

    @Test
    public void damageBreaksAnOtherwiseCalmHold() {
        // NEXT #183: the battle-hook latch, not the manager-updated HP
        // history, breaks the hold. Same board, same far enemy: the only
        // difference between the frames is one wound.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit intact = fake(AUnitType.Protoss_Dragoon, 30, 22);
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 38, 20);
        far.setEnemy();
        dragoon.cooldown = 0;
        intact.cooldown = 0;

        world(4, fakeOurs(dragoon, intact), fakeEnemies(far), () -> {
            int now = atlantis.game.A.now();
            if (now == 1) {
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                BattleCommander.act(intact, FightOrFlight.Tactic.FIGHT, null, intact);
                assertTrue(FormationHold.shouldHold(dragoon, far, FightOrFlight.Tactic.FIGHT),
                    "no damage yet: hold the line");
                assertTrue(FormationHold.shouldHold(intact, far, FightOrFlight.Tactic.FIGHT),
                    "control holds too");
            }
            if (now == 2) {
                dragoon.hp -= 60;
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                assertFalse(FormationHold.shouldHold(dragoon, far, FightOrFlight.Tactic.FIGHT),
                    "one wound breaks the hold even with the enemy 8 tiles out");
            }
            if (now == 3) {
                BattleCommander.act(intact, FightOrFlight.Tactic.FIGHT, null, intact);
                assertTrue(FormationHold.shouldHold(intact, far, FightOrFlight.Tactic.FIGHT),
                    "unwounded twin keeps holding: the break was the damage, not the frame");
            }
        });
    }

    @Test
    public void fightKitePreemptsHoldInMarginBand() {
        // Owner doctrine: FormHold never outranks kiting. A wounded dragoon
        // (hp 48) with a ling at 3.12 tiles sits in the band the raw gate
        // cannot see: outside veto (3.0), contact (2.5) and canAttackMe+3,
        // yet inside the fight's own margin (required 3.25 at this hp). The hold must decline exactly when the fight would
        // kite - same threat, same predicate, no second opinion.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.hp = 48;
        dragoon.shields = 0;
        dragoon.cooldown = 0;
        FakeUnit hunter = fake(AUnitType.Zerg_Zergling, 33.12, 20);
        hunter.setEnemy();
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 38, 20);
        far.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(hunter, far), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.hasCloseMeleeEnemy(dragoon),
                "3.12 tiles: outside the veto, the band this test pins");
            assertFalse(FormationHold.needsKite(dragoon),
                "raw 3-tile check is blind here - old code held the slot");
            assertFalse(FormationHold.shouldHold(dragoon, far, FightOrFlight.Tactic.FIGHT),
                "the fight would kite this threat, so the hold must decline");
        });
    }

    @Test
    public void unthreatenedGhostDeclinesHold() {
        // Live game 200532: a dragoon held formation slots for ~500f on
        // a fogged contact 8 tiles out (UNSTICK x5, army parked) instead
        // of hunting forward. A ghost contact holds nothing: with no
        // attacker in 3.2 tiles the hunt owns the unit, not the slot.
        // Owner rule: fall back only when really threatened.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit ghost = fake(AUnitType.Protoss_Zealot, 38, 20);
        ghost.setEnemy();
        ghost.isVisibleUnitOnMap = false;

        world(2, fakeOurs(dragoon), fakeEnemies(ghost), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.shouldHold(dragoon, ghost, FightOrFlight.Tactic.FIGHT),
                "fogged contact 8 tiles out, nothing can hit us: hunt, don't hold");
        });
    }

    @Test
    public void threatenedGhostStillFallsBack() {
        // Same ghost, but a real ling at 3.1 tiles can hit us (threat
        // radius 3.2): falling back to the cluster under pressure stays
        // allowed, so the hold marches (none of the other gates fires
        // on this board either).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.hp = 180;
        dragoon.shields = 80;
        dragoon.cooldown = 0;
        FakeUnit ghost = fake(AUnitType.Protoss_Zealot, 38, 20);
        ghost.setEnemy();
        ghost.isVisibleUnitOnMap = false;
        FakeUnit presser = fake(AUnitType.Zerg_Zergling, 33.1, 20);
        presser.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(ghost, presser), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.hasCloseMeleeEnemy(dragoon), "3.57 tiles: no veto");
            assertTrue(FormationHold.shouldHold(dragoon, ghost, FightOrFlight.Tactic.FIGHT),
                "threatened with a ghost contact: the fallback still holds");
        });
    }

    @Test
    public void visibleFarEnemyStillHolds() {
        // No regression for the designed shape: a VISIBLE far enemy keeps
        // the arc march (the ghost gate only fires on unseen contacts).
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.cooldown = 0;
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 38, 20);
        far.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(far), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertTrue(FormationHold.shouldHold(dragoon, far, FightOrFlight.Tactic.FIGHT),
                "visible enemy 8 tiles out: the arc still marches");
        });
    }

    @Test
    public void holdStandsWhenFightWouldNotKite() {
        // No overcorrection: full-hp dragoon, same geometry - the fight's
        // margin is satisfied (required 1.0 at full hp), so the hold still
        // marches. Old and new code agree here by construction.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        dragoon.hp = 180;
        dragoon.shields = 80;
        dragoon.cooldown = 0;
        FakeUnit hunter = fake(AUnitType.Zerg_Zergling, 33.12, 20);
        hunter.setEnemy();
        FakeUnit far = fake(AUnitType.Protoss_Zealot, 38, 20);
        far.setEnemy();

        world(2, fakeOurs(dragoon), fakeEnemies(hunter, far), () -> {
            if (atlantis.game.A.now() != 1) return;

            assertFalse(FormationHold.needsKite(dragoon), "same raw input as the band test");
            assertTrue(FormationHold.shouldHold(dragoon, far, FightOrFlight.Tactic.FIGHT),
                "fight would not kite: the hold still marches");
        });
    }
}
