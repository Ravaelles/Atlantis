package atlantis.combat.micro.avoid.terran;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class TerranFightInsteadAvoidAsAir extends Manager {
    public TerranFightInsteadAvoidAsAir(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        System.out.println("z");
        if (!unit.isAir() || !unit.hasAnyWeapon()) return false;
        System.out.println("x");
        Selection enemies = unit.enemiesNear();
        System.out.println("c");
        if (!enemies.onlyAir()) return false;

        System.out.println("a");
        if (enemies.onlyOfType(unit.type()) && (enemies.size() == 1 || unit.combatEvalRelative() >= 1.1)) {
            return true;
        }
        System.out.println("b");

        return false;
    }

    @Override
    public Manager handle() {
        return this;
    }
}
