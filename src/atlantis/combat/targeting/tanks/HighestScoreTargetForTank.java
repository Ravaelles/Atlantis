package atlantis.combat.targeting.tanks;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.util.Enemy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import static atlantis.units.AUnitType.*;

public class HighestScoreTargetForTank extends HasUnit {
    public HighestScoreTargetForTank(AUnit unit) {
        super(unit);
    }

    public AUnit targetWithBestScoreAmong(ArrayList<AUnit> enemies) {
        if (enemies.isEmpty()) return null;

        TreeMap<AUnit, Double> scores = new TreeMap<>();

        for (AUnit enemy : enemies) {
            scores.put(enemy, calculateScoreIfTargetIs(enemy));
        }

        double maxScore = Collections.max(scores.values());

        return scores.entrySet().stream()
            .filter(entry -> entry.getValue() >= maxScore)
            .findFirst()
            .map(java.util.Map.Entry::getKey)
            .orElse(null);
    }

    private double calculateScoreIfTargetIs(AUnit enemy) {
        double score = scoreBonusAgainstUnitType(enemy);

        // Direct damage to the unit targeted
        score += Math.min(enemy.hp(), 70);

        // All enemies affected
        score += enemy.enemiesNear().groundUnits().nonBuildings().inRadius(1.25, enemy).count() * 50;

        // Friendly fire
        score -= enemy.friendsNear().groundUnits().nonBuildings().inRadius(1.25, enemy).count() * 200;

        return score;
    }

    private double scoreBonusAgainstUnitType(AUnit enemy) {
        if (!enemy.isCrucialUnit()) return 0;

        if (enemy.is(Zerg_Defiler)) {
            return 2000;
        }
        else if (enemy.is(Zerg_Lurker)) {
            return 1000;
        }
        else if (enemy.is(Zerg_Ultralisk)) {
            return 400;
        }

        if (enemy.is(Protoss_Reaver)) {
            return 2000;
        }
        else if (enemy.is(Protoss_High_Templar)) {
            return 1000;
        }
        else if (enemy.is(Protoss_Archon)) {
            return 1000;
        }
        else if (enemy.is(Protoss_Dark_Templar)) {
            return 1000;
        }
        else if (enemy.is(Protoss_Dragoon)) {
            return 40; // Slight bonus for tanks to attack Goons, higher chance of avoiding friendly-fire
        }

        if (enemy.is(Terran_Siege_Tank_Siege_Mode)) {
            return 2000;
        }
        else if (enemy.is(Terran_Siege_Tank_Tank_Mode)) {
            return 1000;
        }

        return 0;
    }
}
