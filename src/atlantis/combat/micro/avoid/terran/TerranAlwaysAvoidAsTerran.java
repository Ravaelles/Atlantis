package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class TerranAlwaysAvoidAsTerran extends Manager {
    public TerranAlwaysAvoidAsTerran(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isTerran();
    }

    public boolean shouldAlwaysAvoid() {
        if (asVulture()) return true;
        if (asInfantry()) return true;

        return false;
    }

    private boolean asInfantry() {
        if (!unit.isTerranInfantry()) return false;

        return MarineCanAttackNearEnemy.allowedForThisUnit(unit);
    }

    private boolean asVulture() {
        if (!unit.isVulture()) return false;

        return unit.hp() <= 30 && unit.enemiesNear().canAttack(unit, 2.1 + unit.woundPercent() / 50).notEmpty();
    }
}
