package atlantis.combat.targeting.tanks;

import atlantis.units.AUnit;
import atlantis.units.HasUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static bwapi.CoordinateType.Map;

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
        double score = 0;

        // Friendly fire
        score -= enemy.friendsNear().groundUnits().inRadius(1.25, enemy).count() * 50;

        // All enemies affected
        score += enemy.enemiesNear().groundUnits().inRadius(1.25, enemy).count() * 30;

        // Direct damage to the unit targeted
        int killBonus = 100;
        score += Math.min(70, enemy.hp()) + (enemy.hp() <= 70 ? killBonus : 0);

        return score;
    }
}