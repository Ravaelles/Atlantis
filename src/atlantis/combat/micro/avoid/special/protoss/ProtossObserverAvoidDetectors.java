package atlantis.combat.micro.avoid.special.protoss;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.We;

public class ProtossObserverAvoidDetectors extends Manager {
    private HasPosition enemyDetectorCenter;

    public ProtossObserverAvoidDetectors(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isObserver()
            && (enemyDetectorCenter = enemyDetector()) != null
            && inDangerFromAnyEnemy();
    }

    @Override
    public Manager handle() {
        if (unit.moveAwayFrom(enemyDetectorCenter, 3, Actions.MOVE_AVOID, "AvoidDetector")) {
            return usedManager(this);
        }

        return null;
    }

    private HasPosition enemyDetector() {
        return unit.enemiesNear()
            .detectors()
            .inRadius(13, unit)
            .center();
    }

    private boolean inDangerFromAnyEnemy() {
        return unit.enemiesNear()
            .havingAntiAirWeapon()
            .canAttack(unit, 1 + unit.woundPercent() / 22)
            .notEmpty();
    }
}
