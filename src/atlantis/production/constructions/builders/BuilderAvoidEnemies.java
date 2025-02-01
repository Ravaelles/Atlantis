package atlantis.production.constructions.builders;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidSingleEnemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class BuilderAvoidEnemies extends Manager {
    private AUnit enemy;

    public BuilderAvoidEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isBuilder()
            && (enemy = unit.enemiesThatCanAttackMe(safetyMargin()).combatUnits().nearestTo(unit)) != null;
    }

    private double safetyMargin() {
        return 2.8
            + unit.woundPercent() / 30.0
            + Math.min(2.5, unit.distToBase() / 30.0);
    }

    @Override
    public Manager handle() {
        if ((new AvoidSingleEnemy(unit, enemy)).forceHandle() != null) {
            return usedManager(this);
        }

        if (unit.moveToSafety(Actions.MOVE_SAFETY)) {
            return usedManager(this);
        }

        return null;
    }
}
