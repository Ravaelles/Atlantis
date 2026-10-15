package atlantis.production;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.A;
import atlantis.util.We;

/**
 * Runs {@link GoalBoardFeeder} on a 30-frame cadence (decisions do not
 * need frame precision; queue guards absorb repeats). Legacy list only -
 * the LIVE engine has its own goal sources.
 */
public class GoalBoardFeederCommander extends Commander {

    @Override
    public boolean applies() {
        return We.protoss();
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[0];
    }

    @Override
    protected boolean handle() {
        if (!A.everyNthGameFrame(30)) {
            return false;
        }
        GoalBoardFeeder.update();
        return false;
    }
}
