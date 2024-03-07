package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.production.dynamic.expansion.protoss.ProtossExpansionCommander;
import atlantis.production.dynamic.expansion.terran.TerranExpansionCommander;
import atlantis.production.dynamic.expansion.zerg.ZergExpansionCommander;

public class ExpansionCommander extends Commander {
    private static int _lastExpandedAt = -1;

    @Override
    protected Class<? extends Commander>[] subcommanders() {
        return new Class[]{
            ProtossExpansionCommander.class,
            TerranExpansionCommander.class,
            ZergExpansionCommander.class,
        };
    }

    public static void justExpanded() {
        _lastExpandedAt = A.now();
    }

    public static boolean lastExpandedLessThanSecondsAgo(int seconds) {
        return (A.now() - _lastExpandedAt) <= 30 * seconds;
    }
}
