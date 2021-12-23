package atlantis;

import atlantis.units.AUnitType;
import bwapi.Race;

public class AtlantisConfigChanger {

    protected static void modifyRacesInConfigFileIfNeeded() {
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
//        AtlantisConfig.DEFAULT_BUILD_ORDER = TerranBuildOrder.TERRAN_1_Base_Vultures;
//        AtlantisConfig.DEFAULT_BUILD_ORDER = TerranBuildOrder.TERRAN_BBS;

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
//        AtlantisConfig.DEFAULT_BUILD_ORDER = ProtossBuildOrder.PROTOSS_2_GATEWAY_ZEALOT;
//        AtlantisConfig.DEFAULT_BUILD_ORDER = ProtossBuildOrder.PROTOSS_2_GATE_RANGE_EXPAND;

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
//        AtlantisConfig.DEFAULT_BUILD_ORDER = ZergBuildOrder.ZERG_13_POOL_MUTA;

        AtlantisConfig.MY_RACE = Race.Zerg;
        AtlantisConfig.BASE = AUnitType.Zerg_Hatchery;
        AtlantisConfig.WORKER = AUnitType.Zerg_Drone;
        AtlantisConfig.BARRACKS = AUnitType.Zerg_Spawning_Pool;
        AtlantisConfig.SUPPLY = AUnitType.Zerg_Overlord;
        AtlantisConfig.GAS_BUILDING = AUnitType.Zerg_Extractor;

        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_LAND = AUnitType.Zerg_Creep_Colony;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Zerg_Creep_Colony;
    }

    /**
     * This method could be used to overwrite user's race in bwapi.ini file.
     * <b>CURRENTLY NOT IMPLEMENTED</b>.
     */
    private static void overrideBwapiIniRace(String raceString) {
        System.out.println("@NotImplemented overrideBwapiIniRace");
        System.exit(-1);
//        overrideBwapiIniRace(raceString);
    }

}
