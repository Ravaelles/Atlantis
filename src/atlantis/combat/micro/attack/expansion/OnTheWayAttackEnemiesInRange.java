package atlantis.combat.micro.attack.expansion;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class OnTheWayAttackEnemiesInRange extends Manager {
    private AUnit enemy;

    public OnTheWayAttackEnemiesInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.woundPercent() >= 40 && unit.shotSecondsAgo(5)) return false;
        if (unit.enemiesThatCanAttackMe(2.6).count() > 0) return false;

        return unit.cooldown() <= 0
            && (enemy = enemy()) != null;
    }

    @Override
    public Manager handle() {
        if (unit.attackUnit(enemy)) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit enemy() {
        return unit.enemiesNear()
            .canBeAttackedBy(unit, -0.2)
            .realUnits()
            .notDeadMan()
            .nearestTo(unit);
    }
}
