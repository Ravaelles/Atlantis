package atlantis.combat.micro.avoid.buildings;

import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class EnemyCombatBuildingsTooStrong {
    public static boolean tooStrong(AUnit unit) {
        if (unit.squadSize() >= 40) return false;

        Selection combatBuildings = EnemyUnits.discovered().buildings().combatBuildingsAnti(unit);
        if (combatBuildings.empty()) return false;

        double ourStrength = unit.combatEvalRelative();
        double enemyStrength = enemyStrength(unit, ourStrength, combatBuildings);

        return ourStrength >= enemyStrength;
    }

    private static double enemyStrength(AUnit unit, double ourStrength, Selection combatBuildings) {
        int basePenalty = unit.squadSize() <= 25 ? 4 : 1;

        AUnit squadLeader = unit.squadLeader();
        if (squadLeader != null) basePenalty += squadLeader.lastRetreatedAgo() <= 30 * 3 ? 2 : 0;

        return basePenalty + combatBuildings.inRadius(17, unit).count() / 1.5;
    }
}
