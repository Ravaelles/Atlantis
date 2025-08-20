package atlantis.information.enemy;

import atlantis.game.A;
import atlantis.map.bullets.ABullet;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.attacked_by.Bullets;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.select.Select;
import atlantis.util.Counter;

import java.util.HashMap;

public class UnitsArchive {
    protected static HashMap<Integer, AUnit> destroyedUnitIds = new HashMap<>();
    protected static Counter<AUnitType> enemyLostTypes = new Counter<>();
    protected static Counter<AUnitType> ourLostTypes = new Counter<>();
    protected static Counter<AUnitType> ourKilledResourcesPerUnitTypes = new Counter<>();
    protected static Counter<AUnitType> ourLostResourcesPerUnitTypes = new Counter<>();
    protected static Counter<AUnitType> ourKillCountersPerUnitTypes = new Counter<>();

    protected static int enemyBasesDestroyed = 0;
    protected static int lastTimeOurCombatUnitDied = -9999;

    // =========================================================

    public static void markUnitAsDestroyed(AUnit unit) {
        destroyedUnitIds.put(unit.id(), unit);

//        for (int id : destroyedUnitIds.keySet()) {
//            System.out.println("Destroyed unit: " + id + " / " + destroyedUnitIds.get(id));
//        }
//        System.out.println("------");

        if (unit.isEnemy()) {
            EnemyInfo.removeDiscoveredUnit(unit);
            enemyUnitDestroyed(unit);
        }
        else if (unit.isOur()) {
            ourUnitDestroyed(unit);
        }

//        System.out.println("Unit destroyed: " + unit
//            + " / isEnemy: " + unit.isEnemy()
//            + " / isOur: " + unit.isOur()
//            + " / hp: " + unit.hp()
//            + " / isDead: " + unit.isDead()
//            + " / isAlive: " + unit.isAlive()
//            + " / position: " + unit.position()
//        );
    }

    private static AUnit ourUnitThatKilledEnemy(AUnit enemy) {
        for (AUnit our : Select.our().list()) {
            if (enemy.equals(our.target()) || enemy.equals(our.lastTarget())) {
                return our;
            }
        }

        for (ABullet bullet : Bullets.against(enemy)) {
            if (bullet.attacker() != null) {
                return bullet.attacker();
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

    private static void print(Counter<AUnitType> types) {
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

            if (unit.isCombatUnit()) lastTimeOurCombatUnitDied = A.now();
        }
    }

    public static void enemyUnitDestroyed(AUnit enemy) {
        enemyLostTypes.incrementValueFor(enemy.type());

        modifyResourcesKilledForKilledEnemy(enemy);

        AbstractFoggedUnit fogged = enemy.foggedUnit();
        if (fogged != null) fogged.forceSetPositionNull();

        if (enemy.isBase()) enemyBasesDestroyed++;
    }

    private static void modifyResourcesKilledForKilledEnemy(AUnit enemy) {
        AUnit ourKiller = ourUnitThatKilledEnemy(enemy);
        if (ourKiller != null && (!enemy.isABuilding() || enemy.isCombatBuilding())) {
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

    public static int enemyDestroyedWorkers() {
        return enemyLostTypes.getValueFor(AUnitType.Terran_SCV)
            + enemyLostTypes.getValueFor(AUnitType.Protoss_Probe)
            + enemyLostTypes.getValueFor(AUnitType.Zerg_Drone);
    }

    public static boolean lastTimeOurCombatUnitDiedLessThanAgo(int frames) {
        return A.ago(lastTimeOurCombatUnitDied) < frames;
    }

    public static boolean lastTimeOurCombatUnitDiedMoreThanAgo(int frames) {
        return A.ago(lastTimeOurCombatUnitDied) > frames;
    }

    public static int enemyBasesDestroyed() {
        return enemyBasesDestroyed;
    }
}
