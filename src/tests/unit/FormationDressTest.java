package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.Regroup;
import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.information.enemy.EnemyUnitsUpdater;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Formation dress (owner 2026-11-09): positioning owns overriding
 * priority - the line dresses at its slots before any
 * collapse/reinforce/hold leg is considered. Attack marches march
 * out, the buildings-only walk in, arrived lines hover, and quiet
 * boards keep the old legs.
 */
@Tag("combat")
public class FormationDressTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Zerg;
    }

    @AfterEach
    public void clean() {
        for (AUnit unit : Alpha.get().selection().list().toArray(new AUnit[0])) {
            Alpha.get().removeUnit(unit);
        }
        BattleCommander.clearForTests();
    }

    private static FakeUnit[] zealots(int count, int tx, int ty) {
        List<FakeUnit> units = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            units.add(fake(AUnitType.Protoss_Zealot, tx + i, ty));
        }

        return units.toArray(new FakeUnit[0]);
    }

    private static FakeUnit[] lings(int count, int tx, int ty) {
        List<FakeUnit> units = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, tx + i, ty);
            ling.setEnemy();
            units.add(ling);
        }

        return units.toArray(new FakeUnit[0]);
    }

    private static void addToAlpha(FakeUnit[] ours) {
        for (FakeUnit unit : ours) {
            if (unit.isCombatUnit()) {
                Alpha.get().addUnit(unit);
            }
        }
    }

    private static FakeUnit[] concat(FakeUnit first, FakeUnit[] rest) {
        List<FakeUnit> out = new ArrayList<>();
        out.add(first);
        for (FakeUnit u : rest) out.add(u);

        return out.toArray(new FakeUnit[0]);
    }

    @Test
    public void homeAnchorDressesBeforeCollapseLegs() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(4, 40, 22);
        FakeUnit[] foes = lings(3, 52, 24);
        APosition anchor = APosition.create(30, 20);

        world(3, concat(main, ball), foes, () -> {
            if (atlantis.game.A.now() != 2) return;
            for (FakeUnit foe : foes) EnemyUnitsUpdater.weDiscoveredEnemyUnit(foe);
            addToAlpha(ball);
            atlantis.combat.ArmyContact.noteFight(ball[0], foes[0], FightOrFlight.Tactic.FIGHT);

            FakeUnit unit = null;
            for (FakeUnit candidate : ball) {
                if (!atlantis.combat.strategy.GhostAdvance.isVanguard(candidate)) {
                    unit = candidate;
                    break;
                }
            }
            assertTrue(unit != null, "fixture needs a non-vanguard unit");
            HasPosition slot = ClusterRally.formationSlotFor(
                unit, ball[1], anchor, FightOrFlight.Tactic.FIGHT);
            assertTrue(slot != null, "fixture check: the line owns a slot");

            Regroup.march(unit, FightOrFlight.Tactic.FIGHT, anchor, ball[1], BattleStrategy.OPEN_FIELD);

            assertTrue(unit.targetPosition != null
                    && unit.targetPosition.distTo(slot) < 3,
                "dressed at the slot, not collapsed or reinforced: " + unit.targetPosition);
            assertTrue(unit.commandHistory().toString().contains("ClusterDress"),
                "the dress names itself: " + unit.commandHistory());
        });
    }

    @Test
    public void attackAnchorMarchesOutInsteadOfDressing() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(4, 22, 20);
        FakeUnit[] foes = lings(3, 32, 24);
        APosition anchor = APosition.create(90, 20);

        world(3, concat(main, ball), foes, () -> {
            if (atlantis.game.A.now() != 2) return;
            for (FakeUnit foe : foes) EnemyUnitsUpdater.weDiscoveredEnemyUnit(foe);
            addToAlpha(ball);
            atlantis.combat.ArmyContact.noteFight(ball[0], foes[0], FightOrFlight.Tactic.FIGHT);

            FakeUnit unit = ball[0];
            Regroup.march(unit, FightOrFlight.Tactic.FIGHT, anchor, ball[1], BattleStrategy.OPEN_FIELD);

            assertFalse(unit.commandHistory().toString().contains("ClusterDress"),
                "an assault column marches out: " + unit.commandHistory());
        });
    }

    @Test
    public void quietBoardKeepsOldLegs() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit[] ball = zealots(4, 22, 20);
        APosition anchor = APosition.create(30, 20);

        world(3, concat(main, ball), new FakeUnit[0], () -> {
            if (atlantis.game.A.now() != 2) return;
            addToAlpha(ball);

            FakeUnit unit = ball[0];
            Regroup.march(unit, FightOrFlight.Tactic.FIGHT, anchor, ball[1], BattleStrategy.OPEN_FIELD);

            assertFalse(unit.commandHistory().toString().contains("ClusterDress"),
                "no contact, no dress: " + unit.commandHistory());
        });
    }
}
