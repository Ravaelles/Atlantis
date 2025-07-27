package atlantis.combat.squad.positioning.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.game.player.Enemy;

public class ProtossZealotTooFarFromDragoon extends Manager {

    private static final double MIN_DIST = 1.3;
    private static final double PREFERED_MAX_DIST = 1.3;
    private static final double ABSOLUTE_MAX_DIST = 4.7;

    private AUnit dragoon;
    private double distToGoon;

    public ProtossZealotTooFarFromDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isZealot()) return false;

        dragoon = unit.friendsNear().dragoons().nearestTo(unit);
        if (dragoon == null) return false;

        distToGoon = unit.distTo(dragoon);
        if (distToGoon <= 8 && A.supplyUsed() >= 180) return false;
        if (distToGoon <= 6 && allowAttackingZergWhenRelativelyOk()) return false;

        double eval = unit.eval();

        if (distToGoon <= 7 && eval >= 1.25) return false;

        if (
            eval >= 0.65
                && unit.enemiesNear().groundUnits().canBeAttackedBy(unit, 0.3).notEmpty()
        ) {
            return false;
        }

        if (
            eval <= 2 && distToGoon >= ABSOLUTE_MAX_DIST
//                && !unit.enemiesNear().combatUnits().mostlyRanged()
        ) {
            return true;
        }

        return distToGoon >= PREFERED_MAX_DIST
            && unit.enemiesNear().inRadius(4.2, unit).empty()
            && (
            unit.shieldWound() >= 16
                || unit.enemiesNear().combatBuildingsAntiLand().inRadius(8.3, unit).empty()
        );
    }

    private boolean allowAttackingZergWhenRelativelyOk() {
        if (!Enemy.zerg()) return false;

        return unit.cooldown() <= 4
            && unit.eval() >= 0.75
            && unit.hp() >= 60
            && unit.meleeEnemiesNearCount(1.2) <= 1;
    }

    @Override
    protected Manager handle() {
        if (distToGoon <= 1.3) {
            if (unit.moveAwayFrom(dragoon, 0.2, Actions.MOVE_FORMATION)) {
                return usedManager(this, "TooCloseToGoon");
            }
        }

        if (moveTo()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean moveTo() {
        return unit.move(dragoon, Actions.MOVE_FORMATION, "TooFarFromGoon");
    }
}
