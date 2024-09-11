package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.TechType;

public class HealAnyWoundedNear extends Manager {
    /**
     * Maximum allowed distance for a medic to heal wounded units that are not their assignment.
     * The idea is to disallow them to move away too much.
     */
    public int HEAL_OTHER_UNITS_MAX_DISTANCE = 10;

    private AUnit target;

    public HealAnyWoundedNear(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.energy(5)) return false;

        Selection potentialTargets = Select.our()
            .organic()
            .wounded()
            .inRadius(HEAL_OTHER_UNITS_MAX_DISTANCE, unit)
            .exclude(unit);

        target = potentialTargets
            .notRunning()
            .notBeingHealed()
            .nearestTo(unit);

        if (target == null && unit.idIsEven()) target = potentialTargets.nearestTo(unit);

        return target != null;
    }

    @Override
    public Manager handle() {
        healUnit(target);
        unit.setTooltip("AnyWounded");
        return usedManager(this);
    }

    private void healUnit(AUnit unitToHeal) {
        if (unit != null && unitToHeal != null && !unitToHeal.equals(unit.target())) {
            unit.useTech(TechType.Healing, unitToHeal);
            unit.setTooltipTactical("Heal");
        }
    }
}
