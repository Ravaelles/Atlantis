package atlantis.config;

import atlantis.units.AUnitType;
import bwapi.Race;
import main.Main;

public class AtlantisConfigChanger {

    /**
     * Sets the race configuration from the CLIENT's own answer.
     *
     * <p>
     * {@code Main.ourRace()} is the single source of truth for the race (owner's
     * ruling, 2026-10-07). This used to read the race back from the GAME
     * ({@code Atlantis.game().self().getRace()}), which made bwapi.ini the real
     * authority: when the ini said Terran and Main said Protoss, the bot set
     * MY_RACE = Terran, every race branch followed it, and all Protoss code -
     * including the unit producer - silently never ran. Two sources of truth for
     * one fact, with the wrong one winning.
     * </p>
     *
     * <p>
     * The game's race is still checked, but only to WARN: if StarCraft started us
     * as something other than what we asked for, the launcher's ini is wrong and
     * the bot says so loudly instead of quietly playing the wrong race. See
     * {@code OnGameStarted.warnIfRequestedRaceDiffersFromTheGame()}.
     * </p>
     */
    public static void modifyRacesInConfigFileIfNeeded() {
        switch (Main.ourRace().toLowerCase()) {
            case "protoss":
                useConfigForProtoss();
                return;
            case "terran":
                useConfigForTerran();
                return;
            case "zerg":
                useConfigForZerg();
                return;
            default:
                throw new IllegalStateException(
                        "Main.ourRace() must name a race, got: " + Main.ourRace());
        }
    }

    /**
     * Helper method for using Terran race.
     */
    public static void useConfigForTerran() {
        AtlantisRaceConfig.MY_RACE = Race.Terran;
        AtlantisRaceConfig.BASE = AUnitType.Terran_Command_Center;
        AtlantisRaceConfig.WORKER = AUnitType.Terran_SCV;
        AtlantisRaceConfig.BARRACKS = AUnitType.Terran_Barracks;
        AtlantisRaceConfig.SUPPLY = AUnitType.Terran_Supply_Depot;
        AtlantisRaceConfig.GAS_BUILDING = AUnitType.Terran_Refinery;

        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Terran_Bunker;
        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Terran_Missile_Turret;
    }

    /**
     * Helper method for using Protoss race.
     */
    public static void useConfigForProtoss() {
        AtlantisRaceConfig.MY_RACE = Race.Protoss;
        AtlantisRaceConfig.BASE = AUnitType.Protoss_Nexus;
        AtlantisRaceConfig.WORKER = AUnitType.Protoss_Probe;
        AtlantisRaceConfig.BARRACKS = AUnitType.Protoss_Gateway;
        AtlantisRaceConfig.SUPPLY = AUnitType.Protoss_Pylon;
        AtlantisRaceConfig.GAS_BUILDING = AUnitType.Protoss_Assimilator;

        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Protoss_Photon_Cannon;
        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Protoss_Photon_Cannon;
    }

    /**
     * Helper method for using Zerg race.
     */
    public static void useConfigForZerg() {
        AtlantisRaceConfig.MY_RACE = Race.Zerg;
        AtlantisRaceConfig.BASE = AUnitType.Zerg_Hatchery;
        AtlantisRaceConfig.WORKER = AUnitType.Zerg_Drone;
        AtlantisRaceConfig.BARRACKS = AUnitType.Zerg_Spawning_Pool;
        AtlantisRaceConfig.SUPPLY = AUnitType.Zerg_Overlord;
        AtlantisRaceConfig.GAS_BUILDING = AUnitType.Zerg_Extractor;

        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Zerg_Creep_Colony;
        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Zerg_Creep_Colony;
    }

}
