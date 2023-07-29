package atlantis.combat.squad.positioning;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class TooClustered extends Manager {
    public TooClustered(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.squad().size() >= 2 && unit.friendsNear().inRadius(0.3, unit).groundUnits().atLeast(3);
    }

    public Manager handle() {
        Selection ourCombatUnits = Select.ourCombatUnits().inRadius(5, unit);
        AUnit nearestBuddy = ourCombatUnits.clone().nearestTo(unit);
        double minDistBetweenUnits = minDistBetweenUnits();

        if (tooClustered(ourCombatUnits, nearestBuddy, minDistBetweenUnits)) {
            APosition goTo = unit.makeFreeOfAnyGroundUnits(4, 0.2, unit);
            if (goTo != null) {
                if (unit.move(goTo, Actions.MOVE_FORMATION, "SpreadOut", false)) {
                    return usedManager(this);
                }
            }
        }

        return null;
    }

    // =========================================================

    private boolean tooClustered(
        Selection ourCombatUnits,
        AUnit nearestBuddy,
        double minDistBetweenUnits
    ) {
        return nearestBuddy != null
            && ourCombatUnits.size() >= 5
            && nearestBuddy.distToLessThan(unit, minDistBetweenUnits)
            && unit.friendsInRadius(1.5).size() >= 4;
    }

    private double minDistBetweenUnits() {
        double baseDist = preferedBaseDistToNextUnit();
        int enemiesNear = unit.enemiesNearInRadius(4);

        if (enemiesNear <= 1 || unit.noCooldown()) {
            int highTemplars = unit.enemiesNear().ofType(AUnitType.Protoss_High_Templar).havingEnergy(75).count();
            if (highTemplars > 0) {
                return baseDist + 0.7 * highTemplars;
            }
        }

        if (enemiesNear <= 2 || unit.noCooldown()) {
            int lurkers = unit.enemiesNear().ofType(AUnitType.Zerg_Lurker).count();
            if (lurkers > 0) {
                return baseDist + 0.1 * lurkers;
            }
        }

        return baseDist;
    }

    private double preferedBaseDistToNextUnit() {
        if (unit.isTank()) {
            return 0.8;
        }

        return 0.4;
    }
}
