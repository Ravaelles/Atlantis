package atlantis.util;

import atlantis.game.AGame;
import atlantis.game.APlayer;
import atlantis.game.race.EnemyRace;
import atlantis.util.cache.Cache;
import bwapi.Race;

import java.util.ArrayList;

public class Enemy {

    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static Race race() {
        return EnemyRace.enemyRace();
    }

    public static boolean terran() {
        return EnemyRace.isEnemyTerran();
    }

    public static boolean protoss() {
        return EnemyRace.isEnemyProtoss();
    }

    public static boolean zerg() {
        return EnemyRace.isEnemyZerg();
    }

    public static ArrayList<APlayer> players() {
        return (ArrayList<APlayer>) cache.get(
            "players",
            -1,
            () -> {
                ArrayList<APlayer> players = new ArrayList<>();

                APlayer playerUs = AGame.getPlayerUs();
                for (APlayer player : AGame.getPlayers()) {
                    if (player.isEnemy(playerUs)) {
                        players.add(player);
                    }
                }

                return players;
            }
        );
    }

}
