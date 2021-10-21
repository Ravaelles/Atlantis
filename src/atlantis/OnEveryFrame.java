package atlantis;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.A;
import atlantis.util.CappedList;

public class OnEveryFrame {

    private static CappedList<Integer> frames = new CappedList<>(4);

    public static void update() {
        for (AUnit unit : Select.ourCombatUnits().list()) {
            if (unit.isUnderAttack(2) && unit.hpPercent() < 48) {
                AGameSpeed.changeSpeedTo(30);
            }
        }
    }

}
