package atlantis.game;

import atlantis.Atlantis;
import atlantis.combat.missions.MissionChanger;
import atlantis.config.AtlantisConfig;
import atlantis.production.orders.production.CurrentProductionQueue;
import atlantis.units.AUnitType;
import bwapi.*;

import java.util.ArrayList;
import java.util.List;

import static atlantis.Atlantis.game;

/**
 * Represents various aspect of the game like time elapsed (in frames or approximated seconds), free supply
 * (from our point of view), game speed, enemy APlayer etc.<br />
 * <br /><b>It's worth to study this class carefully as it contains some really useful methods.</b>
 */
public class AGame {

    private static boolean umsMode = false; // Should be set to `true` on UMS (custom) maps
    private static APlayer _enemy = null; // Cached enemy APlayer
    private static APlayer _neutral = null; // Cached neutral APlayer
    private static APlayer _our = null; // Cached our APlayer

    // =========================================================
    
    /**
     * Quits the game gently, killing all processes and cleaning up.
     */
    public static void exit() {
        Atlantis.getInstance().onEnd(false);
    }

    public static void quit() {
        exit();
    }

    /**
     * Quits the game gently, killing all processes and cleaning up.
     */
    public static void exit(String message) {
        System.err.println(message);
        Atlantis.getInstance().exitGame();
    }

    public static void changeDisableUI(boolean disableUI) {
        AtlantisConfig.DISABLE_GUI = disableUI;
        Game game = game();
        if (game != null) {
            game.setGUI(AtlantisConfig.DISABLE_GUI);
        }
    }

    /**
     * Returns game speed.
     */
    public static int getGameSpeed() {
        return GameSpeed.gameSpeed;
    }

    /**
     * Returns approximate number of in-game seconds elapsed.
     */
    public static int timeSeconds() {
        return Atlantis.game().getFrameCount() / 30;
    }

    /**
     * Returns number of game frames elapsed.
     */
    public static int now() {
        return Atlantis.game().getFrameCount();
    }

    /**
     * Return how many frames ago this moment was.
     */
    public static int framesAgo(int frame) {
        return now() - frame;
    }

    /**
     * Returns true once per n game frames.
     */
    public static boolean everyNthGameFrame(int n) {
        return Atlantis.game().getFrameCount() % n == 0;
    }

    /**
     * Returns false once per n game frames.
     */
    public static boolean notNthGameFrame(int n) {
        return Atlantis.game().getFrameCount() % n != 0;
    }

    /**
     * Number of minerals.
     */
    public static int minerals() {
        return Atlantis.game().self().minerals();
    }

    /**
     * Number of gas.
     */
    public static int gas() {
        return Atlantis.game().self().gas();
    }

    /**
     * Number of free supply.
     */
    public static int supplyFree() {
        return supplyTotal() - supplyUsed();
    }

    /**
     * Number of supply used.
     */
    public static int supplyUsed() {
        return Atlantis.game().self().supplyUsed() / 2;
    }

    public static boolean hasSupply(int minSupply) {
        return supplyUsed() >= minSupply;
    }

    /**
     * Number of supply totally available.
     */
    public static int supplyTotal() {
        return Atlantis.game().self().supplyTotal() / 2;
    }

    /**
     * Returns current APlayer.
     */
    public static APlayer getPlayerUs() {
        if (_our == null) {
            _our = new APlayer(Atlantis.game().self());
        }

        return _our;
    }

    /**
     * Returns all APlayers.
     */
    public static List<APlayer> getPlayers() {
        List<APlayer> players = new ArrayList<>();
        for (Player p : game().getPlayers()){
            players.add(APlayer.create(p));
        }
        return players;
    }

    /**
     * Returns enemy APlayer.
     */
    public static APlayer enemy() {
        if (_enemy == null) {
            _enemy = new APlayer(Atlantis.game().enemies().iterator().next());
        }
        return _enemy;
    }

    /**
     * Returns neutral APlayer (minerals, geysers, critters).
     */
    public static APlayer neutralPlayer() {
        return new APlayer(Atlantis.game().neutral());
    }

    /**
     * UMS maps are custom made maps, which may be used to test micro-management. They can cause a lot of exceptions.
     */
    public static boolean isUms() {
        return umsMode;
    }

    /**
     * UMS maps are custom made maps, which may be used to test micro-management.
     */
    public static void setUmsMode() {
        if (!AGame.umsMode) {
            AGame.umsMode = true;
            System.out.println("### UMS mode enabled! ###");

            MissionChanger.forceMissionAttack("UmsAlwaysAttack");
        }
    }
    
    // =========================================================
    // Auxiliary
    /**
     * Returns random int number from range [min, max], both inclusive.
     */
    public static int rand(int min, int max) {
        return A.rand(min, max);
    }

    /**
     * Returns true if user plays as Terran.
     */
    public static boolean isPlayingAsTerran() {
        return AtlantisConfig.MY_RACE.equals(Race.Terran);
    }

    /**
     * Returns true if user plays as Protoss.
     */
    public static boolean isPlayingAsProtoss() {
        return AtlantisConfig.MY_RACE.equals(Race.Protoss);
    }

    /**
     * Returns true if user plays as Zerg.
     */
    public static boolean isPlayingAsZerg() {
        return AGame.getPlayerUs().getRace().equals(Race.Zerg);
//        return AtlantisConfig.MY_RACE.equals(Race.Zerg);
    }

    /**
     * Returns true if enemy plays as Terran.
     */
    public static boolean isEnemyTerran() {
        return AGame.enemy().getRace().equals(Race.Terran);
    }

    /**
     * Returns true if enemy plays as Protoss.
     */
    public static boolean isEnemyProtoss() {
        return AGame.enemy().getRace().equals(Race.Protoss);
    }

    /**
     * Returns true if enemy plays as Zerg.
     */
    public static boolean isEnemyZerg() {
        return AGame.enemy().getRace().equals(Race.Zerg);
    }

    /**
     * Returns true if we can afford given amount of minerals.
     */
    public static boolean hasMinerals(int mineralsToAfford) {
        return minerals() >= mineralsToAfford;
    }

    /**
     * Returns true if we can afford given amount of gas.
     */
    public static boolean hasGas(int gasToAfford) {
        return gas() >= gasToAfford;
    }

    /**
     * Returns true if we can afford minerals and gas for given unit type.
     */
    public static boolean canAfford(AUnitType unitType) {
        return hasMinerals(unitType.getMineralPrice()) && hasGas(unitType.getGasPrice());
    }

    /**
     * Returns true if we can afford minerals and gas for given upgrade.
     */
    public static boolean canAfford(UpgradeType upgrade) {
        //TODO: check whether we need to pass level 0 to match getMineral/GasPriceBase()
        return hasMinerals(upgrade.mineralPrice()) && hasGas(upgrade.gasPrice());
    }

    public static boolean canAfford(TechType tech) {
        return hasMinerals(tech.mineralPrice()) && hasGas(tech.gasPrice());
    }

    /**
     * Returns true if we can afford both so many minerals and gas at the same time.
     */
    public static boolean canAfford(int minerals, int gas) {
        return hasMinerals(minerals) && hasGas(gas);
    }

    /**
     * Returns true if we can afford both so many minerals and gas at the same time.
     * Takes into account planned constructions and orders.
     */
    public static boolean canAffordWithReserved(int minerals, int gas) {
//        int[] reservedConstructions = ConstructionRequests.resourcesNeededForNotStartedConstructions();
        int[] reservedInQueue = CurrentProductionQueue.resourcesReserved();

        return canAfford(
                minerals + reservedInQueue[0],
                gas + reservedInQueue[1]
        );
    }

    public static boolean canAffordWithReserved(AUnitType type) {
        return canAffordWithReserved(type.getMineralPrice(), type.getGasPrice());
    }

    public static boolean canAffordWithReserved(TechType type) {
        return canAffordWithReserved(type.mineralPrice(), type.gasPrice());
    }

    public static boolean canAffordWithReserved(UpgradeType type) {
        return canAffordWithReserved(type.mineralPrice(), type.gasPrice());
    }

    public static int killsLossesResourceBalance() {
        return Atlantis.KILLED_RESOURCES - Atlantis.LOST_RESOURCES;
    }

    public static String getMapName() {
        return Atlantis.game().mapName();
    }

    // =========================================================
    // Utility
    /**
     * Sends in-game message that will be visible by other APlayers.
     */
    public static void sendMessage(String message) {
        if (game() != null) {
            game().sendText(message);
        }
    }
}
