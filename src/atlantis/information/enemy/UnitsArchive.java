package atlantis.information.enemy;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.MappingCounter;

import java.util.HashMap;

public class UnitsArchive {

    protected static HashMap<Integer, AUnit> destroyedUnitIds = new HashMap<>();
    protected static MappingCounter<AUnitType> enemyLostTypes = new MappingCounter<>();
    protected static MappingCounter<AUnitType> ourLostTypes = new MappingCounter<>();
    protected static MappingCounter<AUnitType> ourKilledResourcesPerUnitTypes = new MappingCounter<>();
    protected static MappingCounter<AUnitType> ourLostResourcesPerUnitTypes = new MappingCounter<>();
    protected static MappingCounter<AUnitType> ourKillCountersPerUnitTypes = new MappingCounter<>();

    // =========================================================

    public static void markUnitAsDestroyed(AUnit unit) {
        destroyedUnitIds.put(unit.id(), unit);

        if (unit.isEnemy()) {
            EnemyInfo.removeDiscoveredUnit(unit);
            enemyUnitDestroyed(unit);
        }
        else if (unit.isOur()) {
            ourUnitDestroyed(unit);
        }
    }

    private static AUnit ourUnitThatKilledEnemy(AUnit enemy) {
        for (AUnit our : Select.our().list()) {
            if (enemy.equals(our.target())) {
                return our;
            }
        }
        return null;
    }

    // =========================================================

    public static void paintKillLossResources() {
        System.out.println();
        System.out.println("--- Unit kill/loss in resources ---");
        for (AUnitType type : ourKilledResourcesPerUnitTypes.map().keySet()) {
            if (!type.isRealUnit() || type.hasNoWeaponAtAll()) {
                continue;
            }

            int balance = ourKilledResourcesPerUnitTypes.getValueFor(type) - ourLostResourcesPerUnitTypes.getValueFor(type);
            String balanceString = balance > 0 ? ("+" + balance) : ("" + balance);
            String balancePercent = balancePercentFor(type, balance);

            System.out.println(
                    type + ": " + balanceString + ", " + balancePercent
                    + "  (kills: " + ourKillCountersPerUnitTypes.getValueFor(type)
                    + ", lost: " + ourLostTypes.getValueFor(type) + ")"
            );
        }



    }

    private static String balancePercentFor(AUnitType type, int balance) {
        if (balance >= 0) {
            return ourLostResourcesPerUnitTypes.getValueFor(type) == 0
                    ? "+++%"
                    : ("+" + (ourKilledResourcesPerUnitTypes.getValueFor(type) * 100 / ourLostResourcesPerUnitTypes.getValueFor(type) - 100) + "%");
        }

        return ourKilledResourcesPerUnitTypes.getValueFor(type) == 0
                ? "---%"
                : (-ourLostResourcesPerUnitTypes.getValueFor(type) * 100 / ourKilledResourcesPerUnitTypes.getValueFor(type) + 100) + "%";
    }

    public static void paintLostUnits() {
        System.out.println("--- Lost ---");
        print(ourLostTypes);
    }

    public static void paintKilledUnits() {
        System.out.println("--- Killed ---");
        print(enemyLostTypes);
    }

    private static void print(MappingCounter<AUnitType> types) {
        for (AUnitType type : types.map().keySet()) {
            if (!type.isRealUnit()) {
                continue;
            }
            System.out.println(type + ":  " + types.getValueFor(type));
        }
    }

    // =========================================================

    public static void ourUnitDestroyed(AUnit unit) {
        ourLostTypes.incrementValueFor(unit.type());

        if (!unit.isABuilding()) {
            ourLostResourcesPerUnitTypes.changeValueBy(unit.type(), unit.totalCost());
        }
    }

    public static void enemyUnitDestroyed(AUnit enemy) {
        enemyLostTypes.incrementValueFor(enemy.type());

        AUnit ourKiller = ourUnitThatKilledEnemy(enemy);
        if (ourKiller != null && !enemy.isABuilding()) {

            ourKillCountersPerUnitTypes.incrementValueFor(ourKiller.type());
            ourKilledResourcesPerUnitTypes.changeValueBy(ourKiller.type(), enemy.totalCost());
        }
    }

    public static boolean isDestroyed(AUnit unit) {
        return destroyedUnitIds.containsKey(unit.id());
    }

    public static boolean isDestroyed(int unitId) {
        return destroyedUnitIds.containsKey(unitId);
    }

}
