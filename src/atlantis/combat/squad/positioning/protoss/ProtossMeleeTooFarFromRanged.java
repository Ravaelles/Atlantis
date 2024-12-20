package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ProtossMeleeTooFarFromRanged extends Manager {
    private AUnit nearestGoon;

    public ProtossMeleeTooFarFromRanged(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (!unit.isCombatUnit()) return false;
        if (!unit.isMelee()) return false;
        if (Count.dragoons() == 0) return false;

        nearestGoon = unit.friendsNear().ranged().nearestTo(unit);

        if (nearestGoon == null) return false;
        if (unit.meleeEnemiesNearCount(3) >= 2) return false;

        double distToGoon = unit.distTo(nearestGoon);

        double baseDist = unit.enemiesNear().combatUnits().empty() ? 2.8 : 1.2;

        return distToGoon >= 6
            || (distToGoon < (baseDist + unit.hpPercent() / 60.0) && !isOvercrowded());
    }

    @Override
    public Manager handle() {
        if (!unit.isMoving()) {
            HasPosition moveTo = moveTo();
            if (unit.move(moveTo, Actions.MOVE_FORMATION, "ToCenter")) {
                return usedManager(this);
            }
        }

        return null;
    }

    private boolean isOvercrowded() {
        return unit.friendsNear().groundUnits().inRadius(1.5, unit).count() >= 5;
    }

    private HasPosition moveTo() {
        return nearestGoon.translateTilesTowards(unit, 2);
    }
}
