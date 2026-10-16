package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import bwapi.TechType;

public class HealMostWoundedInRange extends Manager {

    private AUnit nearestWoundedInfantry;

    public HealMostWoundedInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.energy(5)) return false;
        if (unit.lastActionLessThanAgo(10, Actions.HEAL)) return false;

        nearestWoundedInfantry = allowedToBeHealed()
            .notHavingHp(19)
            .inRadius(1.99, unit)
            .sortByHealth()
            .first();

        return nearestWoundedInfantry != null;
    }

    @Override
    public Manager handle() {
        healUnit(nearestWoundedInfantry);
        unit.setTooltip("MostWounded");
        return usedManager(this);
    }

    private void healUnit(AUnit unitToHeal) {
        if (unit != null && unitToHeal != null && !unitToHeal.equals(unit.target())) {
            unit.useTech(TechType.Healing, unitToHeal);
            unit.setTooltipTactical("Heal");
        }
    }

    private Selection allowedToBeHealed() {
        return Select.our()
            .organic()
            .exclude(unit)
            .exclude(TerranMedic.medicsToAssignments.values()) // Only heal units that have no medics assigned
            .notBeingHealed();
    }
}
