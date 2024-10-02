package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class ProtossZealotTooFarFromDragoon extends Manager {
    private AUnit dragoon;

    public ProtossZealotTooFarFromDragoon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZealot()
            && (Count.dragoons() >= 1 || unit.enemiesNear().ranged().notEmpty())
            && unit.enemiesNear().havingWeapon().notEmpty()
            && (dragoon = nearestDragoon()) != null
            && dragoon.distTo(unit) > 2.7
            && noEnemiesVeryNearToAttack();
//        );
    }

    private boolean noEnemiesVeryNearToAttack() {
        if (unit.combatEvalRelative() <= 1.8) return true;

        int maxAllowedDist = 3 + (unit.lastUnderAttackLessThanAgo(70) ? 3 : 0);
        Selection enemiesNear = unit.enemiesNear().groundUnits().effVisible().inRadius(maxAllowedDist, unit);

        return enemiesNear.ranged().canBeAttackedBy(unit, maxAllowedDist).empty()
            && enemiesNear.workers().canBeAttackedBy(unit, maxAllowedDist).empty()
            && enemiesNear.buildings().canBeAttackedBy(unit, maxAllowedDist).empty();
    }

    private AUnit nearestDragoon() {
        Selection dragoons = unit.friendsNear().dragoons();
        if (dragoons.empty()) dragoons = Select.ourOfType(AUnitType.Protoss_Dragoon);

        return dragoons.nearestTo(unit);
    }

    @Override
    public Manager handle() {
        if (unit.move(dragoon, Actions.MOVE_FORMATION, "ClosaToDragoon")) return usedManager(this);

        return null;
    }
}
