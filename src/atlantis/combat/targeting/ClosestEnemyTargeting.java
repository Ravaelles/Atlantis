package atlantis.combat.targeting;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class ClosestEnemyTargeting {
    private Selection enemies = null;

    public ClosestEnemyTargeting(Selection enemies) {
        this.enemies = enemies;
    }

    public AUnit nearestTarget(AUnit unit, double maxDistFromEnemy) {
        if (unit.isMelee()) return forMelee(unit);

        return forRanged(unit);
    }

    private AUnit forRanged(AUnit unit) {
//        AUnit inRange;

        return unit.enemiesNear().canBeAttackedBy(unit, 1).mostWoundedOrNearest(unit);

//        double baseExtraMargin = Enemy.zerg() ? -0.25 : -0.1;
//
//        inRange = enemies.wounded().canBeAttackedBy(unit, baseExtraMargin).mostWounded();
//        if (inRange != null) return inRange;
//
//        inRange = enemies.canBeAttackedBy(unit, baseExtraMargin).nearestTo(unit);
//        if (inRange != null) return inRange;
//
//        inRange = enemies.wounded().canBeAttackedBy(unit, 0.5).mostWounded();
//        if (inRange != null) return inRange;
//
//        HasPosition squadCenter = unit.squadCenter();
//        if (squadCenter == null) return null;
//
//        Selection squadCenterEnemiesNear = unit.squadCenterEnemiesNear();
//        if (squadCenterEnemiesNear == null) return null;
//
//        return squadCenterEnemiesNear.canBeAttackedBy(unit, 10).nearestTo(unit);
    }

    private AUnit forMelee(AUnit unit) {
        AUnit inRange = enemies.canBeAttackedBy(unit, 0).mostWoundedOrNearest(unit);

        if (inRange != null) return inRange;

        return enemies.canBeAttackedBy(unit, 10).nearestTo(unit);
    }
}
