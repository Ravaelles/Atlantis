package atlantis.combat.micro.avoid.always;

import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;
import atlantis.util.Enemy;

import atlantis.architecture.Manager;

public class DragoonAlwaysAvoidEnemy extends Manager {
    public DragoonAlwaysAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isDragoon()) return false;

        if (Enemy.zerg()) return vsZerg();

        return false;
    }

    private boolean vsZerg() {
        if (unit.hp() <= 40 && unit.isMissionAttack() && unit.enemiesNear().inRadius(5.8, unit).notEmpty())
            return unit.setTooltip("AvoidVeryLowHP");

        if (
            unit.hp() <= 60
                && unit.shotSecondsAgo() <= 3.5
                && unit.enemiesThatCanAttackMe(1.85).ranged().atLeast(2)
        ) return true;

        if (
            unit.friendsNear().atMost(20)
                && unit.enemiesNearInRadius(OurDragoonRange.range() - 0.6) >= 1
        ) {
            return unit.lastAttackFrameLessThanAgo(30 * (unit.hp() >= 60 ? 4 : 7));
        }

        if (
            unit.cooldown() >= 15
                && unit.shieldDamageAtLeast(41)
                && unit.lastUnderAttackLessThanAgo(30 * 2)
                && unit.lastAttackFrameLessThanAgo(50)
                && unit.enemiesNearInRadius(OurDragoonRange.range() - 0.5) >= 2
        ) return true;

        if (unit.shieldDamageAtLeast(9)) {
            if (lonelyAndLotsOfZerglings()) return true;
            if (lonelyAndLotsOfHydras()) return true;
        }

        return false;
    }

    private boolean lonelyAndLotsOfHydras() {
        return unit.friendsNear().inRadius(2.5, unit).atMost(1)
            && unit.enemiesNear().hydras().inRadius(7.2, unit).atLeast(unit.almostDead() ? 1 : 2);
    }

    private boolean lonelyAndLotsOfZerglings() {
        return unit.friendsNear().inRadius(2.5, unit).atMost(1)
            && unit.enemiesNear().zerglings().inRadius(3.2, unit).atLeast(unit.almostDead() ? 2 : 3);
    }
}
