package atlantis.combat.missions.attack.focus;

import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;

import java.util.ArrayList;

public class EnemyExistingExpansion {
    private static Cache<AUnit> cache = new Cache<>();

    public static boolean found() {
        return get() != null;
    }

    public static boolean notFound() {
        return get() == null;
    }

    public static HasPosition get() {
        return cache.get(
            "expansion",
            37,
            () -> {
                Positions<HasPosition> mainAndNatural = new Positions<>();
                APosition enemyMain = EnemyInfo.enemyMain();
                mainAndNatural.addPosition(enemyMain);
                mainAndNatural.addPosition(EnemyInfo.enemyNatural());

                ArrayList<AUnit> found = new ArrayList<>();

                // =========================================================

                HasPosition vulnerable = getVulnerableBase(mainAndNatural);
                if (vulnerable != null) return vulnerable;

                // =========================================================

                return getAccordingToNotBeingEnemyMainNorNatural(mainAndNatural, found, enemyMain);
            }
        );
    }

    private static HasPosition getVulnerableBase(Positions<HasPosition> mainAndNatural) {
        if (Select.mainOrAnyBuilding() == null) return null;

        ArrayList<AUnit> found = new ArrayList<>();

        Selection enemyBuildings = EnemyUnits.discovered().buildings();
        for (AUnit base : enemyBuildings.bases().list()) {
            if (enemyBuildings.combatBuildingsAntiLand().countInRadius(10, base) <= 1) {
                found.add(base);
            }
        }

        return (new Positions<>(found)).groundNearestTo(Select.mainOrAnyBuilding());
    }

    private static AUnit getAccordingToNotBeingEnemyMainNorNatural(
        Positions<HasPosition> mainAndNatural, ArrayList<AUnit> found, APosition enemyMain
    ) {
        for (AUnit building : EnemyUnits.discovered().buildings().list()) {
            if (mainAndNatural.inGroundRadius(17, building).empty()) {
                found.add(building);
            }
        }

//                for (AUnit base : EnemyUnits.discovered().mainAndNatural().list()) {
//                    if (mainAndNatural.inRadius(15, base).empty()) {
//                        found.add(base);
//                    }
//                }

        if (found.isEmpty()) {
            return null;
        }
        else if (found.size() == 1) {
            return found.get(0);
        }
        return (Select.from(found, "expansions-found").groundFarthestTo(
            enemyMain != null ? enemyMain : Select.mainOrAnyBuilding())
        );
    }
}
