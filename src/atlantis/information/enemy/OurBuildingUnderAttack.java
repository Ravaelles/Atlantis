package atlantis.information.enemy;

import atlantis.units.AUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;

public class OurBuildingUnderAttack {
    public static AUnit get() {
//        for (AUnit unit : Select.ourBasesWithUnfinished().list()) {
        Units underAttack = new Units();

        for (AUnit unit : Select.ourBases().list()) {
            if (unit.woundPercent() >= 3 && unit.enemiesNear().canAttack(unit, 1).count() >= 1) {
                return unit;
            }
        }

        for (AUnit unit : Select.ourBuildingsWithUnfinished().list()) {
            if (
                unit.woundPercent() >= 3
                    && unit.enemiesNear().combatUnits().canAttack(unit, 1).count() >= 1
            ) {
                underAttack.addUnit(unit);
            }
        }

        return underAttack.selection().nearestToMain();
    }

    public static boolean notNull() {
        return get() != null;
    }

    public static boolean none() {
        return get() == null;
    }

    public static boolean noBuildingUnderSeriousAttack() {
        AUnit building = get();

        return building == null || building.woundPercent() <= 25;
    }
}
