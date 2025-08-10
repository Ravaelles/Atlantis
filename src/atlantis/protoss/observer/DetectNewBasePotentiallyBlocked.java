package atlantis.protoss.observer;

import atlantis.architecture.Manager;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;

import java.util.ArrayList;

public class DetectNewBasePotentiallyBlocked extends Manager {
    private HasPosition baseConstruction;

    public DetectNewBasePotentiallyBlocked(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (Enemy.protoss()) return false;
        if (shouldNotLeaveAlphaSquad()) return false;

        ArrayList<Construction> basesNotStarted = ConstructionRequests.notStartedOfType(AUnitType.Protoss_Nexus);
        if (basesNotStarted.isEmpty()) {
            return false;
        }

        baseConstruction = basesNotStarted.get(0).buildPosition();

        return baseConstruction != null
            && baseConstruction.hasPosition()
            && Select.ourBasesWithUnfinished().countInRadius(8, baseConstruction) == 0
            && isNearestObserverToConstructionPosition();
    }

    private boolean shouldNotLeaveAlphaSquad() {
        if (Count.observers() <= 1) return true;

        AUnit leader = Alpha.alphaLeader();
        if (leader == null) return false;

        return leader.friendsNear().detectors().empty()
            && leader.enemiesNear().effUndetected().notEmpty();
    }

    private boolean isNearestObserverToConstructionPosition() {
        AUnit nearestUnit = Select.ourOfType(AUnitType.Protoss_Observer).nearestTo(baseConstruction);

        return unit.equals(nearestUnit);
    }

    @Override
    public Manager handle() {
        if (unit.move(baseConstruction, Actions.MOVE_REVEAL, null)) {
            return usedManager(this, "DetectNewBasePotentiallyBlocked");
        }

        return null;
    }
}
