package atlantis.combat.micro.avoid.terran.avoid;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranAlwaysAvoidEnemy extends Manager {
    public TerranAlwaysAvoidEnemy(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerran();
    }

    public boolean shouldAlwaysAvoid() {
        if (asTank()) return true;
        if (asVulture()) return true;
        if (AlwaysAvoidAsTerranInfantry.asInfantry(unit)) return true;

        return false;
    }

    private boolean asTank() {
        if (!unit.isTankUnsieged()) return false;

        return unit.hp() <= 65
            && unit.enemiesNear().canAttack(unit, 3.1).notEmpty();
    }

    private boolean asVulture() {
        if (!unit.isVulture()) return false;

        return unit.hp() <= 30 && unit.enemiesNear().canAttack(unit, 2.1 + unit.woundPercent() / 50).notEmpty();
    }
}
