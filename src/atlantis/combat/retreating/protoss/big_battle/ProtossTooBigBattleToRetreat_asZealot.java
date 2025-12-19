package atlantis.combat.retreating.protoss.big_battle;

import atlantis.game.player.Enemy;
import atlantis.units.AUnit;

public class ProtossTooBigBattleToRetreat_asZealot {
    public static boolean doNotRetreat(AUnit unit) {
        if (!Enemy.protoss()) return false;
        if (!unit.isZealot()) return false;

        if (zealotShouldSupportDragoons(unit)) return true;
        if (zealotShouldKeepZealotLine(unit)) return true;

        return false;
    }

    private static boolean zealotShouldKeepZealotLine(AUnit unit) {
        return (unit.hp() >= 35 || unit.cooldown() <= 15)
            && unit.friendsNear().zealots().inRadius(2.5, unit).atLeast(3);
    }

    private static boolean zealotShouldSupportDragoons(AUnit unit) {
        return unit.eval() > 0.85
            && unit.friendsNear().dragoons().inRadius(4, unit).atLeast(1);
    }
}
