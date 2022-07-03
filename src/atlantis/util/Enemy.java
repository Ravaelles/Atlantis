package atlantis.util;

import atlantis.game.AGame;
import atlantis.game.APlayer;
import atlantis.util.cache.Cache;
import bwapi.Player;

import java.util.ArrayList;

public class Enemy {

    private static Cache<Object> cache = new Cache<>();

    // =========================================================

    public static boolean terran() {
        return AGame.isEnemyTerran();
    }

    public static boolean protoss() {
        return AGame.isEnemyProtoss();
    }

    public static boolean zerg() {
        return AGame.isEnemyZerg();
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
