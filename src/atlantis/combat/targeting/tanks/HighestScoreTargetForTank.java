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
        score -= enemy.friendsNear().groundUnits().nonBuildings().inRadius(1.25, enemy).count() * 200;

        // All enemies affected
        score += enemy.enemiesNear().groundUnits().nonBuildings().inRadius(1.25, enemy).count() * 50;

        // Direct damage to the unit targeted
        int lowHpMinPenalty = 50;
        int lowHpPenaltyBaseHp = 70;
        score -= (enemy.hp() <= lowHpPenaltyBaseHp ? (lowHpMinPenalty + lowHpPenaltyBaseHp - enemy.hp()) : 0);

        return score;
    }
}