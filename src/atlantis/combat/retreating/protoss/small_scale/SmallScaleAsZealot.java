package atlantis.combat.retreating.protoss.small_scale;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.game.player.Enemy;

public class SmallScaleAsZealot extends HasUnit {
    public SmallScaleAsZealot(AUnit unit) {
        super(unit);

        assert unit.isZealot();
    }

    public boolean shouldSmallScaleRetreat() {
        if (unit.hp() <= 26) return true;

        int meleeEnemiesNear = unit.meleeEnemiesNearCount(1.8 + unit.woundPercent() / 66.0);

        if (Enemy.protoss()) {
            if (meleeEnemiesNear >= 1) {
                if (unit.hp() <= 35 && unit.eval() <= 2) return true;
            }

            if (meleeEnemiesNear >= 2) {
                int friendlyMelee = unit.friendsNear().melee().countInRadius(1.8, unit);

                if (friendlyMelee == 0) return true;
            }
        }

        if (Enemy.zerg()) {
            if (meleeEnemiesNear >= 2 && unit.cooldown() >= 7) return true;
            if (meleeEnemiesNear >= 3 && unit.friendsNear().inRadius(1.8, unit).atMost(2)) return true;
        }

        return false;
    }
}
