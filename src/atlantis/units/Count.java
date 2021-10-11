package atlantis.units;

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
}
