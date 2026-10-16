package atlantis.combat.micro.avoid.terran.fight;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class TerranFightInsteadAvoidAsAir extends Manager {
    public TerranFightInsteadAvoidAsAir(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        Selection enemies;

        if (!unit.isAir() || !unit.hasAnyWeapon()) return false;
        if (!(enemies = unit.enemiesNear()).onlyAir()) return false;

        if (enemies.onlyOfType(unit.type()) && (enemies.size() == 1 || unit.eval() >= 0.85)) return true;

        return false;
    }

    @Override
    public Manager handle() {
        return this;
    }
}
