package atlantis.config;

import atlantis.Atlantis;
import atlantis.units.AUnitType;
import bwapi.Race;

public class AtlantisConfigChanger {

    public static void modifyRacesInConfigFileIfNeeded() {
        Race racePlayed = Atlantis.game().self().getRace();
        if (racePlayed.equals(Race.Protoss)) {
            useConfigForProtoss();
        }
        else if (racePlayed.equals(Race.Terran)) {
            useConfigForTerran();
        }
        else if (racePlayed.equals(Race.Zerg)) {
            useConfigForZerg();
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
        AtlantisRaceConfig.MY_RACE = Race.Terran;
        AtlantisRaceConfig.BASE = AUnitType.Zerg_Hatchery;
        AtlantisRaceConfig.WORKER = AUnitType.Zerg_Drone;
        AtlantisRaceConfig.BARRACKS = AUnitType.Zerg_Spawning_Pool;
        AtlantisRaceConfig.SUPPLY = AUnitType.Zerg_Overlord;
        AtlantisRaceConfig.GAS_BUILDING = AUnitType.Zerg_Extractor;

        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Zerg_Creep_Colony;
        AtlantisRaceConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Zerg_Creep_Colony;
    }

}
