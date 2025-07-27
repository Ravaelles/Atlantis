package atlantis.game.player;

import atlantis.game.AGame;
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

    public static APlayer player() {
        return AGame.enemy();
    }

    public static int protossElse(int ifEnemyProtoss, int ifEnemyNotProtoss) {
        if (Enemy.protoss()) return ifEnemyProtoss;

        return ifEnemyNotProtoss;
    }

    public static int terranElse(int ifEnemyTerran, int ifEnemyNotTerran) {
        if (Enemy.terran()) return ifEnemyTerran;

        return ifEnemyNotTerran;
    }

    public static int zergElse(int ifEnemyZerg, int ifEnemyNotZerg) {
        if (Enemy.zerg()) return ifEnemyZerg;

        return ifEnemyNotZerg;
    }

    public static String name() {
        return AGame.enemyName();
    }
}
