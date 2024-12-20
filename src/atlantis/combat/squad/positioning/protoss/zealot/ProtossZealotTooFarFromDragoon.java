package atlantis.combat.squad.positioning.protoss.zealot;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.Enemy;

public class ProtossZealotTooFarFromDragoon extends Manager {

    private static final double PREFERED_MAX_DIST = 1.1;
    private static final double ABSOLUTE_MAX_DIST = 4.7;

    private AUnit dragoon;

    public ProtossZealotTooFarFromDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isZealot()) return false;

        dragoon = unit.friendsNear().dragoons().nearestTo(unit);
        if (dragoon == null) return false;

        double distToGoon = unit.distTo(dragoon);

        if (distToGoon <= 8 && A.supplyUsed() >= 180) return false;

        if (distToGoon <= 6 && allowAttackingZergWhenRelativelyOk()) return false;
        if (distToGoon <= 7 && unit.combatEvalRelative() >= 1.25) return false;

        if (
            distToGoon >= ABSOLUTE_MAX_DIST
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
            && unit.combatEvalRelative() >= 0.75
            && unit.hp() >= 60
            && unit.meleeEnemiesNearCount(1.2) <= 1;
    }

    @Override
    protected Manager handle() {
        if (moveTo()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean moveTo() {
        return unit.move(dragoon, Actions.MOVE_FORMATION, "TooFarFromGoon");
    }
}
