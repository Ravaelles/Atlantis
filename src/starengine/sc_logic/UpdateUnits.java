package starengine.sc_logic;


import atlantis.units.AUnit;
import atlantis.units.select.Select;
import tests.unit.FakeUnit;

public class UpdateUnits {
    public static void update() {
        for (AUnit unit : Select.all().list()) {
            if (unit.isDead() || !unit.isCompleted()) continue;

            updateUnit((FakeUnit) unit);
        }
    }

    private static boolean updateUnit(FakeUnit unit) {
        boolean order = ProcessAttackUnit.update(unit) || ProcessMoveUnit.update(unit);

        if (unit.cooldown > 0) unit.cooldown--;

        return true;
    }
}
