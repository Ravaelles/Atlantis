package atlantis.config;

import atlantis.Atlantis;
import atlantis.units.AUnitType;
import bwapi.Race;

public class AtlantisConfigChanger {

    public static void modifyRacesInConfigFileIfNeeded() {
        Race racePlayed = Atlantis.game().self().getRace();
        if (racePlayed.equals(Race.Protoss)) {
            useConfigForProtoss();
        } else if (racePlayed.equals(Race.Terran)) {
            useConfigForTerran();
        } else if (racePlayed.equals(Race.Zerg)) {
            useConfigForZerg();
        }
    }

    /**
     * Helper method for using Terran race.
     */
    public static void useConfigForTerran() {
        AtlantisConfig.MY_RACE = Race.Terran;
        AtlantisConfig.BASE = AUnitType.Terran_Command_Center;
        AtlantisConfig.WORKER = AUnitType.Terran_SCV;
        AtlantisConfig.BARRACKS = AUnitType.Terran_Barracks;
        AtlantisConfig.SUPPLY = AUnitType.Terran_Supply_Depot;
        AtlantisConfig.GAS_BUILDING = AUnitType.Terran_Refinery;

        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Terran_Bunker;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Terran_Missile_Turret;
    }

    /**
     * Helper method for using Protoss race.
     */
    public static void useConfigForProtoss() {
        AtlantisConfig.MY_RACE = Race.Protoss;
        AtlantisConfig.BASE = AUnitType.Protoss_Nexus;
        AtlantisConfig.WORKER = AUnitType.Protoss_Probe;
        AtlantisConfig.BARRACKS = AUnitType.Protoss_Gateway;
        AtlantisConfig.SUPPLY = AUnitType.Protoss_Pylon;
        AtlantisConfig.GAS_BUILDING = AUnitType.Protoss_Assimilator;

        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Protoss_Photon_Cannon;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Protoss_Photon_Cannon;
    }

    /**
     * Helper method for using Zerg race.
     */
    public static void useConfigForZerg() {
        AtlantisConfig.MY_RACE = Race.Zerg;
        AtlantisConfig.BASE = AUnitType.Zerg_Hatchery;
        AtlantisConfig.WORKER = AUnitType.Zerg_Drone;
        AtlantisConfig.BARRACKS = AUnitType.Zerg_Spawning_Pool;
        AtlantisConfig.SUPPLY = AUnitType.Zerg_Overlord;
        AtlantisConfig.GAS_BUILDING = AUnitType.Zerg_Extractor;

        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Zerg_Creep_Colony;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Zerg_Creep_Colony;
    }

}
