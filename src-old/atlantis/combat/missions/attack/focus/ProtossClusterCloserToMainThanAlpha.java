package atlantis.combat.missions.attack.focus;

import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.cache.Cache;

public class ProtossClusterCloserToMainThanAlpha {
    private static Cache<AUnit> cache = new Cache<>();

    private static double leaderDistToMain;

    public static AUnit detect() {
        if (!Enemy.protoss()) return null;
        if (A.s >= 10 * 60) return null;

        AUnit leader = Alpha.alphaLeader();
        if (leader == null) return null;

        return cache.get(
            "detect",
            17,
            () -> {
                if ((leaderDistToMain = leader.groundDistToMain()) <= 70) return null;

                for (AUnit enemy : Select.enemyCombatUnits().list()) {
                    if (enemy.friendsNear().buildings().notEmpty()) continue;

                    double enemyDistToMain = enemy.groundDistToMain();
                    if (enemyDistToMain + 5 < leaderDistToMain) return enemy;
                }

                return null;
            }
        );
    }
}
