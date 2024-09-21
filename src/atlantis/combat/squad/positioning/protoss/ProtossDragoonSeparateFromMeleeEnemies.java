package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ProtossDragoonSeparateFromMeleeEnemies extends Manager {
    private Selection enemiesNear;

    public ProtossDragoonSeparateFromMeleeEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

        return We.protoss()
            && unit.isDragoon()
            && unit.cooldown() >= 15
            && unit.hp() <= 35
//            && unit.shieldWounded()
            && unit.friendsNear().inRadius(5, unit).atMost(1)
            && (enemiesNear = defineEnemies()).notEmpty();
    }

    @Override
    protected Manager handle() {
        if (enemiesNear.notEmpty()) {
            if (movedAway()) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean movedAway() {
        HasPosition centerOfEnemies = unit.enemiesNear().inRadius(4, unit).nearestTo(unit);
        if (centerOfEnemies == null) centerOfEnemies = unit.enemiesNear().nearestTo(unit);

        double moveDist = unit.distTo(centerOfEnemies) >= 2.5 ? 0.3 : 1.45;

        return unit.moveAwayFrom(centerOfEnemies, moveDist, Actions.MOVE_DANCE_AWAY, "GoonSeparate");
    }

    private Selection defineEnemies() {
        return unit.enemiesNear()
            .melee()
            .havingPosition()
            .havingAntiGroundWeapon()
            .canAttack(unit, 2.8);
    }
}
