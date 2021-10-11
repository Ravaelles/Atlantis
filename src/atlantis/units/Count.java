package atlantis.units;

import atlantis.constructing.AConstructionRequests;

/**
 * Quick auxiliary class for counting our units.
 */
public class Count {

    public static int ourCombatUnits() {
        return Select.ourCombatUnits().count();
    }

    public static int ofType(AUnitType type) {
        return Select.ourOfType(type).count();
    }

    public static int ofTypeFree(AUnitType type) {
        return Select.ourOfType(type).free().count();
    }

    /**
     * Some buildings like Sunken Colony are morphed into from Creep Colony. When counting Creep Colonies, we
     * need to count sunkens as well.
     */
    public static int unitsOfGivenTypeOrSimilar(AUnitType type) {
        if (type.equals(AUnitType.Zerg_Creep_Colony)) {
            return Select.ourIncludingUnfinished().ofType(type).count()
                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Spore_Colony).count()
                    + Select.ourIncludingUnfinished().ofType(AUnitType.Zerg_Sunken_Colony).count();
        }
        else if (type.isPrimaryBase()) {
            return Select.ourIncludingUnfinished().bases().count()
                    + AConstructionRequests.countNotStartedConstructionsOfType(type)
                    + AConstructionRequests.countNotStartedConstructionsOfType(AUnitType.Zerg_Lair)
                    + AConstructionRequests.countNotStartedConstructionsOfType(AUnitType.Zerg_Hive);
        }
        else if (type.isBase() && !type.isPrimaryBase()) {
            return Select.ourIncludingUnfinished().ofType(type).count()
                    + AConstructionRequests.countNotStartedConstructionsOfType(type);
        }
        else {
            return Select.ourIncludingUnfinished().ofType(type).count();
        }
    }
}
