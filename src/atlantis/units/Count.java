package atlantis.units;

public class Count {

    public static int countOurCombatUnits() {
        return Select.ourCombatUnits().count();
    }

}
