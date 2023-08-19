package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldAlwaysAvoidAsTerran extends Manager {
    public ShouldAlwaysAvoidAsTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerran();
    }

    public boolean shouldAlwaysAvoid() {
        if (asVulture()) return true;

        return false;
    }

    private boolean asVulture() {
        if (!unit.isVulture()) return false;

        return unit.hp() <= 30 && unit.enemiesNear().canAttack(unit, 2.1 + unit.woundPercent() / 50).notEmpty();
    }
}
