package tests.e2e;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import tests.fakes.FakeUnit;

import java.util.List;

/**
 * The fight recorder for scenario tests: every N frames, one compact line per
 * unit with everything needed to understand the battle afterwards - position,
 * health, the manager that owns the unit right now, its target and its last
 * order.
 *
 * <p>Line format (stable, greppable):</p>
 * <pre>
 * OBS @135 O probe#12 40+20 @[9,10] mgr=WorkerDefenceFightCombatUnits tgt=Zergling#19 cmd=AttackUnit
 * </pre>
 * <p>Side is {@code O} (ours) or {@code E} (enemies); health is
 * {@code hp+shields}; {@code mgr=-} means no manager applied that frame
 * (DoNothing path); {@code tgt=-} means no attack target.</p>
 *
 * <p>Read it as a story, not a snapshot: pick one unit and follow its lines
 * through the fight (when it engages, what it targets, when its manager
 * changes, where it moves), then do the same for what it was fighting. The
 * questions this answers are the ones reheated in every lost scenario: who
 * fought, who fled, who stood still, and which manager decided each of those.
 * For harder future cases (real opponents, longer horizons) the same lines
 * work unchanged - only the cadence may need tightening around the interesting
 * frames.</p>
 */
public class ScenarioObserver {

    private final int everyNFrames;

    public ScenarioObserver(int everyNFrames) {
        this.everyNFrames = everyNFrames;
    }

    public void onFrame(int frame, List<FakeUnit> ours, List<FakeUnit> enemies) {
        if (frame % everyNFrames != 0) return;

        for (FakeUnit unit : ours) {
            print(frame, "O", unit);
        }
        for (FakeUnit unit : enemies) {
            print(frame, "E", unit);
        }
    }

    private void print(int frame, String side, FakeUnit unit) {
        Manager manager = unit.manager();
        AUnit target = unit.target();

        System.err.println("OBS @" + frame
            + " " + side + " " + unit.type().name() + "#" + unit.id()
            + " " + unit.hp() + "+" + unit.shields()
            + " @[" + unit.position() + "]"
            + " mgr=" + (manager == null ? "-" : manager.getClass().getSimpleName())
            + " tgt=" + (target == null ? "-" : target.type().name() + "#" + target.id())
            + " cmd=" + unit.lastCommand());
    }
}
