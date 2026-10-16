package atlantis.battle;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;

/**
 * Owns every army unit each frame. Phase 0 of the combat cutover
 * (_AI/__PLAN-COMBAT.md): the entry point exists and is invoked, the
 * behavior (one cluster, mission anchors, evaluator fight/flee) lands
 * next. Until then the army holds - no per-unit manager chains run.
 */
public class BattleCommander extends Commander {
    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{};
    }

    @Override
    protected boolean handle() {
        return false;
    }
}
