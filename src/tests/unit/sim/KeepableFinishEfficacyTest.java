package tests.unit.sim;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Efficacy verdict for the #184 keepable question (sim-first by design):
 * a dragoon holding a valid primary (zealot) while a sub-35hp ling chews
 * it - hold the primary or finish the ling first?
 *
 * <p>Measured answer: holding wins by construction (75hp kept, pack
 * cleared tick 140, 13 switches), while forcing the target onto the
 * wounded ling every frame DONATES the dragoon (0hp, pack never
 * cleared, 70 switches, target starved to null). The forcing churns
 * against the ladder's validity gate ({@code isValidTarget} bans
 * lings, so keepCurrent declines the forced target every tick and the
 * downstream rules never settle) - a faithful finish rule must live
 * INSIDE the ladder, next to MeleeChewerTarget which already bypasses
 * the focus bans for melee. That is ladder surgery, sequenced post-cut
 * per #184, so no behavior changes here: the experiment pins the
 * verdict, not a new rule.</p>
 *
 * <p>Note the ladder already finishes the wounded ling once the valid
 * primary is gone (keep arm: zealot dies tick 100, ling tick 140 via
 * the engage paths) - the open part was only ever the ORDER, and the
 * naive order dies proving it.</p>
 */
@Tag("combat")
public class KeepableFinishEfficacyTest extends tests.unit.AbstractTestWithUnits {

    private static final int TILE = 32;

    private static Outcome run(boolean finishFirst) {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 28, 20);
        dragoon.hp = 120;
        dragoon.shields = 0;
        dragoon.cooldown = 10;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 31, 20);
        zealot.setEnemy();
        zealot.hp = 60;
        zealot.shields = 0;
        FakeUnit w = fake(AUnitType.Zerg_Zergling, 29, 20);
        w.setEnemy();
        w.hp = 10;

        dragoon.target = zealot;

        Outcome out = new Outcome();
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[]{dragoon}, new FakeUnit[]{zealot, w}, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            final int[] lastTid = new int[]{-2};
            for (int i = 0; i < 800; i++) {
                final int tick = i;
                world.tick(() -> {
                    if (finishFirst && w.hp > 0) dragoon.target = w;
                    if (dragoon.hp > 0) {
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptChewer(zealot, dragoon);
                    scriptChewer(w, dragoon);
                    if (out.wDeathTick < 0 && w.hp <= 0) out.wDeathTick = tick;
                    if (out.zDeathTick < 0 && zealot.hp <= 0) out.zDeathTick = tick;
                    if (out.clearTick < 0 && w.hp <= 0 && zealot.hp <= 0) out.clearTick = tick;
                    int tid = dragoon.target == null ? -1 : dragoon.target.id();
                    if (tick > 0 && tid != lastTid[0]) out.targetSwitches++;
                    lastTid[0] = tid;
                });
                if (out.clearTick >= 0 || dragoon.hp <= 0) break;
            }

            out.dragoonHpEnd = Math.max(0, dragoon.hp);
            out.shots = world.shotsFired();

            return out;
        } finally {
            Alpha.get().removeUnit(dragoon);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    /**
     * Scripted chewer: nearest live dragoon, move into reach, then trade.
     */
    private static void scriptChewer(FakeUnit foe, FakeUnit dragoon) {
        if (foe.hp <= 0 || dragoon.hp <= 0) {
            foe.lastCommand = "Stop";

            return;
        }
        foe.lastCommand = foe.distTo(dragoon) <= SimWorld.reachTiles(foe, dragoon)
            ? "AttackUnit" : "Move";
        foe.target = dragoon;
        foe.targetPosition = dragoon.position;
    }

    @Test
    public void holdPrimaryClearsWhileForcedFinishDonates() {
        Outcome keep = run(false);
        Outcome finish = run(true);

        String score = "keep(hp=" + keep.dragoonHpEnd + ",clear=" + keep.clearTick
            + ",w@" + keep.wDeathTick + ",z@" + keep.zDeathTick
            + ",shots=" + keep.shots + ",switches=" + keep.targetSwitches
            + ") vs finish(hp=" + finish.dragoonHpEnd + ",clear=" + finish.clearTick
            + ",w@" + finish.wDeathTick + ",z@" + finish.zDeathTick
            + ",shots=" + finish.shots + ",switches=" + finish.targetSwitches + ")";
        assertTrue(keep.shots > 0 && finish.shots > 0,
            "both arms must trade shots, a silent kite proves nothing: " + score);
        assertTrue(keep.clearTick > 0 && keep.dragoonHpEnd > 0,
            "holding the valid primary clears the pack with the dragoon alive: " + score);
        assertTrue(finish.dragoonHpEnd < keep.dragoonHpEnd,
            "forcing the target onto the wounded ling ends worse, not better: " + score);
        assertTrue(finish.targetSwitches > keep.targetSwitches,
            "the forcing churns against the validity gate instead of holding: " + score);
    }

    private static final class Outcome {
        int dragoonHpEnd;
        int clearTick = -1;
        int wDeathTick = -1;
        int zDeathTick = -1;
        int shots;
        int targetSwitches;
    }
}
