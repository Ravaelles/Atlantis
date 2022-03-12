package atlantis.util;

import atlantis.game.AGame;
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

    public static ArrayList<Player> players() {
        return (ArrayList<Player>) cache.get(
                "players",
                -1,
                () -> {
                    ArrayList<Player> players = new ArrayList<>();

                    Player playerUs = AGame.getPlayerUs();
                    for (Player player : AGame.getPlayers()) {
                        if (player.isEnemy(playerUs)) {
                            players.add(player);
                        }
                    }

                    return players;
                }
        );
    }

}
