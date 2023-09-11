package atlantis.units;

import atlantis.util.Counter;
import atlantis.util.cache.Cache;
import bwapi.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Wrapper for JBWAPI UnitType class that makes it much easier to use.<br /><br />
 * Atlantis uses wrappers for BWAPI  classes which aren't extended.<br /><br />
 * <b>AUnitType</b> class contains numerous helper methods, but if you think some methods are missing you can
 * create missing method here and you can reference original UnitType class via ut() method.
 */
public class AUnitType implements Comparable<Object> {

    public static boolean disableErrorReporting = false;
    private static final HashMap<UnitType, AUnitType> instances = new HashMap<>();
    private static int firstFreeID = 1000;

    private final int ID;
    private Cache<Object> cache;

    // =========================================================

    public static Collection<AUnitType> getAllUnitTypes() {
        if (instances.size() < 30) {
            for (UnitType type : UnitType.values()) {
                from(type);
            }
        }

        return instances.values();
    }

    private final UnitType ut;

    // =========================================================

    private AUnitType(UnitType ut) {
        if (ut == null) {
            throw new RuntimeException("AUnitType constructor: type is null");
        }
        this.ut = ut;
        this.ID = firstFreeID++;
        this.cache = new Cache<>();
    }

    /**
     * Atlantis uses wrapper for BWAPI  classes which aren't extended.<br />
     * <b>AUnitType</b> class contains numerous helper methods, but if you think some methods are missing you
     * can create missing method here and you can reference original UnitType class via ut() method.
     */
    public static AUnitType from(UnitType ut) {
        if (ut == null) {
            throw new RuntimeException("AUnitType constructor: type is null");
        }

        if (ut.name().equals(UnitType.None.name())) {
            return null;
        }

        if (instances.containsKey(ut)) {
            return instances.get(ut);
        }
        else {
            AUnitType unitType = new AUnitType(ut);
            instances.put(ut, unitType);

            return unitType;
        }
//        AUnitType unitType;
//        if ((unitType = getJBWAPIUnitType(ut)) != null) {
//            return unitType;
//        }
//        else {
//            unitType = new AUnitType(ut);
//            instances.add(unitType);
//            return unitType;
//        }
    }

    // =========================================================

    /**
     * <b>AVOID USAGE AS MUCH AS POSSIBLE</b> outside AUnitType class. AUnitType class should be used always
     * in place of UnitType. Use only to access constant of UnitType class.
     */
    public UnitType ut() {
        return ut;
    }

//    private static AUnitType getJBWAPIUnitType(UnitType ut) {
//        for (AUnitType unitType : instances.values()) {
//            if (unitType.ut.equals(ut)) {
//                return unitType;
//            }
//        }
//        return null;
//    }

    // =========================================================
    // =========================================================
    // =========================================================
    public static final AUnitType Terran_Firebat = new AUnitType(UnitType.Terran_Firebat);
    public static final AUnitType Terran_Ghost = new AUnitType(UnitType.Terran_Ghost);
    public static final AUnitType Terran_Goliath = new AUnitType(UnitType.Terran_Goliath);
    public static final AUnitType Terran_Marine = new AUnitType(UnitType.Terran_Marine);
    public static final AUnitType Terran_Medic = new AUnitType(UnitType.Terran_Medic);
    public static final AUnitType Terran_SCV = new AUnitType(UnitType.Terran_SCV);
    public static final AUnitType Terran_Siege_Tank_Siege_Mode = new AUnitType(UnitType.Terran_Siege_Tank_Siege_Mode);
    public static final AUnitType Terran_Siege_Tank_Tank_Mode = new AUnitType(UnitType.Terran_Siege_Tank_Tank_Mode);
    public static final AUnitType Terran_Vulture = new AUnitType(UnitType.Terran_Vulture);
    public static final AUnitType Terran_Vulture_Spider_Mine = new AUnitType(UnitType.Terran_Vulture_Spider_Mine);
    public static final AUnitType Terran_Battlecruiser = new AUnitType(UnitType.Terran_Battlecruiser);
    public static final AUnitType Terran_Dropship = new AUnitType(UnitType.Terran_Dropship);
    public static final AUnitType Terran_Nuclear_Missile = new AUnitType(UnitType.Terran_Nuclear_Missile);
    public static final AUnitType Terran_Science_Vessel = new AUnitType(UnitType.Terran_Science_Vessel);
    public static final AUnitType Terran_Valkyrie = new AUnitType(UnitType.Terran_Valkyrie);
    public static final AUnitType Terran_Wraith = new AUnitType(UnitType.Terran_Wraith);
    public static final AUnitType Hero_Alan_Schezar = new AUnitType(UnitType.Hero_Alan_Schezar);
    public static final AUnitType Hero_Alexei_Stukov = new AUnitType(UnitType.Hero_Alexei_Stukov);
    public static final AUnitType Hero_Arcturus_Mengsk = new AUnitType(UnitType.Hero_Arcturus_Mengsk);
    public static final AUnitType Hero_Edmund_Duke_Tank_Mode = new AUnitType(UnitType.Hero_Edmund_Duke_Tank_Mode);
    public static final AUnitType Hero_Edmund_Duke_Siege_Mode = new AUnitType(UnitType.Hero_Edmund_Duke_Siege_Mode);
    public static final AUnitType Hero_Gerard_DuGalle = new AUnitType(UnitType.Hero_Gerard_DuGalle);
    public static final AUnitType Hero_Gui_Montag = new AUnitType(UnitType.Hero_Gui_Montag);
    public static final AUnitType Hero_Hyperion = new AUnitType(UnitType.Hero_Hyperion);
    public static final AUnitType Hero_Jim_Raynor_Marine = new AUnitType(UnitType.Hero_Jim_Raynor_Marine);
    public static final AUnitType Hero_Jim_Raynor_Vulture = new AUnitType(UnitType.Hero_Jim_Raynor_Vulture);
    public static final AUnitType Hero_Magellan = new AUnitType(UnitType.Hero_Magellan);
    public static final AUnitType Hero_Norad_II = new AUnitType(UnitType.Hero_Norad_II);
    public static final AUnitType Hero_Samir_Duran = new AUnitType(UnitType.Hero_Samir_Duran);
    public static final AUnitType Hero_Sarah_Kerrigan = new AUnitType(UnitType.Hero_Sarah_Kerrigan);
    public static final AUnitType Hero_Tom_Kazansky = new AUnitType(UnitType.Hero_Tom_Kazansky);
    public static final AUnitType Terran_Civilian = new AUnitType(UnitType.Terran_Civilian);
    public static final AUnitType Terran_Academy = new AUnitType(UnitType.Terran_Academy);
    public static final AUnitType Terran_Armory = new AUnitType(UnitType.Terran_Armory);
    public static final AUnitType Terran_Barracks = new AUnitType(UnitType.Terran_Barracks);
    public static final AUnitType Terran_Bunker = new AUnitType(UnitType.Terran_Bunker);
    public static final AUnitType Terran_Command_Center = new AUnitType(UnitType.Terran_Command_Center);
    public static final AUnitType Terran_Engineering_Bay = new AUnitType(UnitType.Terran_Engineering_Bay);
    public static final AUnitType Terran_Factory = new AUnitType(UnitType.Terran_Factory);
    public static final AUnitType Terran_Missile_Turret = new AUnitType(UnitType.Terran_Missile_Turret);
    public static final AUnitType Terran_Refinery = new AUnitType(UnitType.Terran_Refinery);
    public static final AUnitType Terran_Science_Facility = new AUnitType(UnitType.Terran_Science_Facility);
    public static final AUnitType Terran_Starport = new AUnitType(UnitType.Terran_Starport);
    public static final AUnitType Terran_Supply_Depot = new AUnitType(UnitType.Terran_Supply_Depot);
    public static final AUnitType Terran_Comsat_Station = new AUnitType(UnitType.Terran_Comsat_Station);
    public static final AUnitType Terran_Control_Tower = new AUnitType(UnitType.Terran_Control_Tower);
    public static final AUnitType Terran_Covert_Ops = new AUnitType(UnitType.Terran_Covert_Ops);
    public static final AUnitType Terran_Machine_Shop = new AUnitType(UnitType.Terran_Machine_Shop);
    public static final AUnitType Terran_Nuclear_Silo = new AUnitType(UnitType.Terran_Nuclear_Silo);
    public static final AUnitType Terran_Physics_Lab = new AUnitType(UnitType.Terran_Physics_Lab);
    public static final AUnitType Special_Crashed_Norad_II = new AUnitType(UnitType.Special_Crashed_Norad_II);
    public static final AUnitType Special_Ion_Cannon = new AUnitType(UnitType.Special_Ion_Cannon);
    public static final AUnitType Special_Power_Generator = new AUnitType(UnitType.Special_Power_Generator);
    public static final AUnitType Special_Psi_Disrupter = new AUnitType(UnitType.Special_Psi_Disrupter);
    public static final AUnitType Protoss_Archon = new AUnitType(UnitType.Protoss_Archon);
    public static final AUnitType Protoss_Dark_Archon = new AUnitType(UnitType.Protoss_Dark_Archon);
    public static final AUnitType Protoss_Dark_Templar = new AUnitType(UnitType.Protoss_Dark_Templar);
    public static final AUnitType Protoss_Dragoon = new AUnitType(UnitType.Protoss_Dragoon);
    public static final AUnitType Protoss_High_Templar = new AUnitType(UnitType.Protoss_High_Templar);
    public static final AUnitType Protoss_Probe = new AUnitType(UnitType.Protoss_Probe);
    public static final AUnitType Protoss_Reaver = new AUnitType(UnitType.Protoss_Reaver);
    public static final AUnitType Protoss_Scarab = new AUnitType(UnitType.Protoss_Scarab);
    public static final AUnitType Protoss_Zealot = new AUnitType(UnitType.Protoss_Zealot);
    public static final AUnitType Protoss_Arbiter = new AUnitType(UnitType.Protoss_Arbiter);
    public static final AUnitType Protoss_Carrier = new AUnitType(UnitType.Protoss_Carrier);
    public static final AUnitType Protoss_Corsair = new AUnitType(UnitType.Protoss_Corsair);
    public static final AUnitType Protoss_Interceptor = new AUnitType(UnitType.Protoss_Interceptor);
    public static final AUnitType Protoss_Observer = new AUnitType(UnitType.Protoss_Observer);
    public static final AUnitType Protoss_Scout = new AUnitType(UnitType.Protoss_Scout);
    public static final AUnitType Protoss_Shuttle = new AUnitType(UnitType.Protoss_Shuttle);
    public static final AUnitType Hero_Aldaris = new AUnitType(UnitType.Hero_Aldaris);
    public static final AUnitType Hero_Artanis = new AUnitType(UnitType.Hero_Artanis);
    public static final AUnitType Hero_Danimoth = new AUnitType(UnitType.Hero_Danimoth);
    public static final AUnitType Hero_Dark_Templar = new AUnitType(UnitType.Hero_Dark_Templar);
    public static final AUnitType Hero_Fenix_Dragoon = new AUnitType(UnitType.Hero_Fenix_Dragoon);
    public static final AUnitType Hero_Fenix_Zealot = new AUnitType(UnitType.Hero_Fenix_Zealot);
    public static final AUnitType Hero_Gantrithor = new AUnitType(UnitType.Hero_Gantrithor);
    public static final AUnitType Hero_Mojo = new AUnitType(UnitType.Hero_Mojo);
    public static final AUnitType Hero_Raszagal = new AUnitType(UnitType.Hero_Raszagal);
    public static final AUnitType Hero_Tassadar = new AUnitType(UnitType.Hero_Tassadar);
    public static final AUnitType Hero_Tassadar_Zeratul_Archon = new AUnitType(UnitType.Hero_Tassadar_Zeratul_Archon);
    public static final AUnitType Hero_Warbringer = new AUnitType(UnitType.Hero_Warbringer);
    public static final AUnitType Hero_Zeratul = new AUnitType(UnitType.Hero_Zeratul);
    public static final AUnitType Protoss_Arbiter_Tribunal = new AUnitType(UnitType.Protoss_Arbiter_Tribunal);
    public static final AUnitType Protoss_Assimilator = new AUnitType(UnitType.Protoss_Assimilator);
    public static final AUnitType Protoss_Citadel_of_Adun = new AUnitType(UnitType.Protoss_Citadel_of_Adun);
    public static final AUnitType Protoss_Cybernetics_Core = new AUnitType(UnitType.Protoss_Cybernetics_Core);
    public static final AUnitType Protoss_Fleet_Beacon = new AUnitType(UnitType.Protoss_Fleet_Beacon);
    public static final AUnitType Protoss_Forge = new AUnitType(UnitType.Protoss_Forge);
    public static final AUnitType Protoss_Gateway = new AUnitType(UnitType.Protoss_Gateway);
    public static final AUnitType Protoss_Nexus = new AUnitType(UnitType.Protoss_Nexus);
    public static final AUnitType Protoss_Observatory = new AUnitType(UnitType.Protoss_Observatory);
    public static final AUnitType Protoss_Photon_Cannon = new AUnitType(UnitType.Protoss_Photon_Cannon);
    public static final AUnitType Protoss_Pylon = new AUnitType(UnitType.Protoss_Pylon);
    public static final AUnitType Protoss_Robotics_Facility = new AUnitType(UnitType.Protoss_Robotics_Facility);
    public static final AUnitType Protoss_Robotics_Support_Bay = new AUnitType(UnitType.Protoss_Robotics_Support_Bay);
    public static final AUnitType Protoss_Shield_Battery = new AUnitType(UnitType.Protoss_Shield_Battery);
    public static final AUnitType Protoss_Stargate = new AUnitType(UnitType.Protoss_Stargate);
    public static final AUnitType Protoss_Templar_Archives = new AUnitType(UnitType.Protoss_Templar_Archives);
    public static final AUnitType Special_Khaydarin_Crystal_Form = new AUnitType(UnitType.Special_Khaydarin_Crystal_Form);
    public static final AUnitType Special_Protoss_Temple = new AUnitType(UnitType.Special_Protoss_Temple);
    public static final AUnitType Special_Stasis_Cell_Prison = new AUnitType(UnitType.Special_Stasis_Cell_Prison);
    public static final AUnitType Special_Warp_Gate = new AUnitType(UnitType.Special_Warp_Gate);
    public static final AUnitType Special_XelNaga_Temple = new AUnitType(UnitType.Special_XelNaga_Temple);
    public static final AUnitType Zerg_Broodling = new AUnitType(UnitType.Zerg_Broodling);
    public static final AUnitType Zerg_Defiler = new AUnitType(UnitType.Zerg_Defiler);
    public static final AUnitType Zerg_Drone = new AUnitType(UnitType.Zerg_Drone);
    public static final AUnitType Zerg_Egg = new AUnitType(UnitType.Zerg_Egg);
    public static final AUnitType Zerg_Hydralisk = new AUnitType(UnitType.Zerg_Hydralisk);
    public static final AUnitType Zerg_Infested_Terran = new AUnitType(UnitType.Zerg_Infested_Terran);
    public static final AUnitType Zerg_Larva = new AUnitType(UnitType.Zerg_Larva);
    public static final AUnitType Zerg_Lurker = new AUnitType(UnitType.Zerg_Lurker);
    public static final AUnitType Zerg_Lurker_Egg = new AUnitType(UnitType.Zerg_Lurker_Egg);
    public static final AUnitType Zerg_Ultralisk = new AUnitType(UnitType.Zerg_Ultralisk);
    public static final AUnitType Zerg_Zergling = new AUnitType(UnitType.Zerg_Zergling);
    public static final AUnitType Zerg_Cocoon = new AUnitType(UnitType.Zerg_Cocoon);
    public static final AUnitType Zerg_Devourer = new AUnitType(UnitType.Zerg_Devourer);
    public static final AUnitType Zerg_Guardian = new AUnitType(UnitType.Zerg_Guardian);
    public static final AUnitType Zerg_Mutalisk = new AUnitType(UnitType.Zerg_Mutalisk);
    public static final AUnitType Zerg_Overlord = new AUnitType(UnitType.Zerg_Overlord);
    public static final AUnitType Zerg_Queen = new AUnitType(UnitType.Zerg_Queen);
    public static final AUnitType Zerg_Scourge = new AUnitType(UnitType.Zerg_Scourge);
    public static final AUnitType Hero_Devouring_One = new AUnitType(UnitType.Hero_Devouring_One);
    public static final AUnitType Hero_Hunter_Killer = new AUnitType(UnitType.Hero_Hunter_Killer);
    public static final AUnitType Hero_Infested_Duran = new AUnitType(UnitType.Hero_Infested_Duran);
    public static final AUnitType Hero_Infested_Kerrigan = new AUnitType(UnitType.Hero_Infested_Kerrigan);
    public static final AUnitType Hero_Kukulza_Guardian = new AUnitType(UnitType.Hero_Kukulza_Guardian);
    public static final AUnitType Hero_Kukulza_Mutalisk = new AUnitType(UnitType.Hero_Kukulza_Mutalisk);
    public static final AUnitType Hero_Matriarch = new AUnitType(UnitType.Hero_Matriarch);
    public static final AUnitType Hero_Torrasque = new AUnitType(UnitType.Hero_Torrasque);
    public static final AUnitType Hero_Unclean_One = new AUnitType(UnitType.Hero_Unclean_One);
    public static final AUnitType Hero_Yggdrasill = new AUnitType(UnitType.Hero_Yggdrasill);
    public static final AUnitType Zerg_Creep_Colony = new AUnitType(UnitType.Zerg_Creep_Colony);
    public static final AUnitType Zerg_Defiler_Mound = new AUnitType(UnitType.Zerg_Defiler_Mound);
    public static final AUnitType Zerg_Evolution_Chamber = new AUnitType(UnitType.Zerg_Evolution_Chamber);
    public static final AUnitType Zerg_Extractor = new AUnitType(UnitType.Zerg_Extractor);
    public static final AUnitType Zerg_Greater_Spire = new AUnitType(UnitType.Zerg_Greater_Spire);
    public static final AUnitType Zerg_Hatchery = new AUnitType(UnitType.Zerg_Hatchery);
    public static final AUnitType Zerg_Hive = new AUnitType(UnitType.Zerg_Hive);
    public static final AUnitType Zerg_Hydralisk_Den = new AUnitType(UnitType.Zerg_Hydralisk_Den);
    public static final AUnitType Zerg_Infested_Command_Center = new AUnitType(UnitType.Zerg_Infested_Command_Center);
    public static final AUnitType Zerg_Lair = new AUnitType(UnitType.Zerg_Lair);
    public static final AUnitType Zerg_Nydus_Canal = new AUnitType(UnitType.Zerg_Nydus_Canal);
    public static final AUnitType Zerg_Queens_Nest = new AUnitType(UnitType.Zerg_Queens_Nest);
    public static final AUnitType Zerg_Spawning_Pool = new AUnitType(UnitType.Zerg_Spawning_Pool);
    public static final AUnitType Zerg_Spire = new AUnitType(UnitType.Zerg_Spire);
    public static final AUnitType Zerg_Spore_Colony = new AUnitType(UnitType.Zerg_Spore_Colony);
    public static final AUnitType Zerg_Sunken_Colony = new AUnitType(UnitType.Zerg_Sunken_Colony);
    public static final AUnitType Zerg_Ultralisk_Cavern = new AUnitType(UnitType.Zerg_Ultralisk_Cavern);
    public static final AUnitType Special_Cerebrate = new AUnitType(UnitType.Special_Cerebrate);
    public static final AUnitType Special_Cerebrate_Daggoth = new AUnitType(UnitType.Special_Cerebrate_Daggoth);
    public static final AUnitType Special_Mature_Chrysalis = new AUnitType(UnitType.Special_Mature_Chrysalis);
    public static final AUnitType Special_Overmind = new AUnitType(UnitType.Special_Overmind);
    public static final AUnitType Special_Overmind_Cocoon = new AUnitType(UnitType.Special_Overmind_Cocoon);
    public static final AUnitType Special_Overmind_With_Shell = new AUnitType(UnitType.Special_Overmind_With_Shell);
    public static final AUnitType Critter_Bengalaas = new AUnitType(UnitType.Critter_Bengalaas);
    public static final AUnitType Critter_Kakaru = new AUnitType(UnitType.Critter_Kakaru);
    public static final AUnitType Critter_Ragnasaur = new AUnitType(UnitType.Critter_Ragnasaur);
    public static final AUnitType Critter_Rhynadon = new AUnitType(UnitType.Critter_Rhynadon);
    public static final AUnitType Critter_Scantid = new AUnitType(UnitType.Critter_Scantid);
    public static final AUnitType Critter_Ursadon = new AUnitType(UnitType.Critter_Ursadon);
    public static final AUnitType Resource_Mineral_Field = new AUnitType(UnitType.Resource_Mineral_Field);
    public static final AUnitType Resource_Mineral_Field_Type_2 = new AUnitType(UnitType.Resource_Mineral_Field_Type_2);
    public static final AUnitType Resource_Mineral_Field_Type_3 = new AUnitType(UnitType.Resource_Mineral_Field_Type_3);
    public static final AUnitType Resource_Vespene_Geyser = new AUnitType(UnitType.Resource_Vespene_Geyser);
    public static final AUnitType Spell_Dark_Swarm = new AUnitType(UnitType.Spell_Dark_Swarm);
    public static final AUnitType Spell_Disruption_Web = new AUnitType(UnitType.Spell_Disruption_Web);
    public static final AUnitType Spell_Scanner_Sweep = new AUnitType(UnitType.Spell_Scanner_Sweep);
    public static final AUnitType Special_Protoss_Beacon = new AUnitType(UnitType.Special_Protoss_Beacon);
    public static final AUnitType Special_Protoss_Flag_Beacon = new AUnitType(UnitType.Special_Protoss_Flag_Beacon);
    public static final AUnitType Special_Terran_Beacon = new AUnitType(UnitType.Special_Terran_Beacon);
    public static final AUnitType Special_Terran_Flag_Beacon = new AUnitType(UnitType.Special_Terran_Flag_Beacon);
    public static final AUnitType Special_Zerg_Beacon = new AUnitType(UnitType.Special_Zerg_Beacon);
    public static final AUnitType Special_Zerg_Flag_Beacon = new AUnitType(UnitType.Special_Zerg_Flag_Beacon);
    public static final AUnitType Powerup_Data_Disk = new AUnitType(UnitType.Powerup_Data_Disk);
    public static final AUnitType Powerup_Flag = new AUnitType(UnitType.Powerup_Flag);
    public static final AUnitType Powerup_Khalis_Crystal = new AUnitType(UnitType.Powerup_Khalis_Crystal);
    public static final AUnitType Powerup_Khaydarin_Crystal = new AUnitType(UnitType.Powerup_Khaydarin_Crystal);
    public static final AUnitType Powerup_Mineral_Cluster_Type_1 = new AUnitType(UnitType.Powerup_Mineral_Cluster_Type_1);
    public static final AUnitType Powerup_Mineral_Cluster_Type_2 = new AUnitType(UnitType.Powerup_Mineral_Cluster_Type_2);
    public static final AUnitType Powerup_Protoss_Gas_Orb_Type_1 = new AUnitType(UnitType.Powerup_Protoss_Gas_Orb_Type_1);
    public static final AUnitType Powerup_Protoss_Gas_Orb_Type_2 = new AUnitType(UnitType.Powerup_Protoss_Gas_Orb_Type_2);
    public static final AUnitType Powerup_Psi_Emitter = new AUnitType(UnitType.Powerup_Psi_Emitter);
    public static final AUnitType Powerup_Terran_Gas_Tank_Type_1 = new AUnitType(UnitType.Powerup_Terran_Gas_Tank_Type_1);
    public static final AUnitType Powerup_Terran_Gas_Tank_Type_2 = new AUnitType(UnitType.Powerup_Terran_Gas_Tank_Type_2);
    public static final AUnitType Powerup_Uraj_Crystal = new AUnitType(UnitType.Powerup_Uraj_Crystal);
    public static final AUnitType Powerup_Young_Chrysalis = new AUnitType(UnitType.Powerup_Young_Chrysalis);
    public static final AUnitType Powerup_Zerg_Gas_Sac_Type_1 = new AUnitType(UnitType.Powerup_Zerg_Gas_Sac_Type_1);
    public static final AUnitType Powerup_Zerg_Gas_Sac_Type_2 = new AUnitType(UnitType.Powerup_Zerg_Gas_Sac_Type_2);
    public static final AUnitType Special_Floor_Gun_Trap = new AUnitType(UnitType.Special_Floor_Gun_Trap);
    public static final AUnitType Special_Floor_Missile_Trap = new AUnitType(UnitType.Special_Floor_Missile_Trap);
    public static final AUnitType Special_Right_Wall_Flame_Trap = new AUnitType(UnitType.Special_Right_Wall_Flame_Trap);
    public static final AUnitType Special_Right_Wall_Missile_Trap = new AUnitType(UnitType.Special_Right_Wall_Missile_Trap);
    public static final AUnitType Special_Wall_Flame_Trap = new AUnitType(UnitType.Special_Wall_Flame_Trap);
    public static final AUnitType Special_Wall_Missile_Trap = new AUnitType(UnitType.Special_Wall_Missile_Trap);
    public static final AUnitType Special_Pit_Door = new AUnitType(UnitType.Special_Pit_Door);
    public static final AUnitType Special_Right_Pit_Door = new AUnitType(UnitType.Special_Right_Pit_Door);
    public static final AUnitType Special_Right_Upper_Level_Door = new AUnitType(UnitType.Special_Right_Upper_Level_Door);
    public static final AUnitType Special_Upper_Level_Door = new AUnitType(UnitType.Special_Upper_Level_Door);
    public static final AUnitType Special_Cargo_Ship = new AUnitType(UnitType.Special_Cargo_Ship);
    public static final AUnitType Special_Floor_Hatch = new AUnitType(UnitType.Special_Floor_Hatch);
    public static final AUnitType Special_Independant_Starport = new AUnitType(UnitType.Special_Independant_Starport);
    public static final AUnitType Special_Map_Revealer = new AUnitType(UnitType.Special_Map_Revealer);
    public static final AUnitType Special_Mercenary_Gunship = new AUnitType(UnitType.Special_Mercenary_Gunship);
    public static final AUnitType Special_Start_Location = new AUnitType(UnitType.Special_Start_Location);
    public static final AUnitType None = new AUnitType(UnitType.None);
    public static final AUnitType AllUnits = new AUnitType(UnitType.AllUnits);
    public static final AUnitType Men = new AUnitType(UnitType.Men);
    public static final AUnitType Buildings = new AUnitType(UnitType.Buildings);
    //    public static final AUnitType Factories = new AUnitType(UnitType.Factories);
    public static final AUnitType Unknown = new AUnitType(UnitType.Unknown);

    // =========================================================
    // =========================================================
    // =========================================================

    /**
     * You can "Terran_Marine" or "Terran Marine" or even "Marine".
     */
    public static AUnitType getByName(String unitName) {
        unitName = unitName.replace(" ", "_").toLowerCase()
            .replace("terran_", "").replace("protoss_", "").replace("zerg_", "");

        for (Field field : UnitType.class.getFields()) {
            String otherTypeName = field.getName().toLowerCase()
                .replace("terran_", "").replace("protoss_", "").replace("zerg_", "");
            if (!otherTypeName.startsWith("Hero") && otherTypeName.equals(unitName)) {
                try {
                    AUnitType unitType = (AUnitType) AUnitType.class.getField(field.getName()).get(null);
                    return unitType;
                } catch (Exception e) {
                    if (!disableErrorReporting) {
                        System.err.println("error trying to find AUnitType for: '" + unitName + "'\n" + e.getMessage());
                    }
                }
            }
        }

        // If not found and ends with "s", try removing the "s"
        if (unitName.trim().endsWith("s")) {
            return getByName(unitName.substring(0, unitName.trim().length() - 1));
        }

        return null;
    }

    /**
     * Returns true if given type equals to one of types passed as parameter.
     */
    public boolean is(AUnitType... types) {
        for (AUnitType otherType : types) {
            if (this.equals(otherType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if given unit is considered to be "melee" unit (not ranged).
     */
    public boolean isMelee() {
        return (boolean) cache.get(
            "isMelee",
            -1,
            () -> isRealUnit() && !hasNoWeaponAtAll()
                && !isCarrier() && !isBunker() && !isMine() && !isScarab()
                && groundWeapon().maxRange() <= 64 && airWeapon().maxRange() <= 64
        );
    }

    /**
     * Returns true if given unit is considered to be "ranged" unit (not melee).
     */
    public boolean isRanged() {
        return (boolean) cache.get(
            "isRanged",
            -1,
            () -> !isReaver() && !isMelee() && !hasNoWeaponAtAll() && (isRealUnit() || isMine() || isScarab() || isCombatBuilding())
        );
    }

    /**
     * Returns total sum of minerals and gas this unit is worth. Accounts for the Zergling case (each Zergling
     * costs half the pair ;-)
     */
    public int getTotalResources() {
        int total = ut.gasPrice() + ut.mineralPrice();
        if (this.equals(AUnitType.Zerg_Zergling)) {
            total /= 2;
        }
        return total;
    }

    /**
     * Returns name for of unit type like e.g. "Zerg Zergling", "Terran Marine", "Protoss Gateway".
     */
    public String fullName() {
        return ut.toString();
//        return (String) cache.get(
//                "getName",
//                -1,
//                () -> ut.toString()
//        );
    }

    // =========================================================
    // Auxiliary methods

    /**
     * Returns short name for of unit type like e.g. "Zergling", "Marine", "Mutalisk", "Barracks".
     */
    public String name() {
        return (String) cache.get(
            "name",
            -1,
            () -> {
                String name = fullName()
                    .replace("Terran_Vulture_", "")
                    .replace("Terran_", "").replace("Protoss_", "")
                    .replace("Zerg_", "").replace("Hero_", "")
                    .replace("Special_", "").replace("Powerup_", "")
                    .replace("_", " ").replace("Terran ", "")
                    .replace("Protoss ", "").replace("Zerg ", "")
                    .replace("Hero ", "").replace("Special ", "")
                    .replace("Powerup ", "").replace("Resource ", "")
                    .replace("Resource_", "").replace("Siege Mode", "")
                    .replace("_Siege_Mode", "").replace("Tank Mode", "")
                    .replace("_Tank_Mode", "").replace("_", "")
                    .trim();

                if (name.contains(" ")) {
                    String[] split = name.split("\\s+");
                    name = split[0] + split[1].charAt(0);
                }

                return name;
            }
        );
    }

    // =========================================================
    // Override
    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof AUnitType) {
            AUnitType other = (AUnitType) obj;
            return ut.name().equals(other.ut.name());
        }
        else if (obj instanceof UnitType) {
            UnitType other = (UnitType) obj;
            return ut.name().equals(other.name());
        }

        return false;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof AUnitType) {
            return Integer.compare(ID, ((AUnitType) o).ID);
        }
        else if (o instanceof UnitType) {
            return Integer.compare(ut.ordinal(), ((UnitType) o).ordinal());
        }

        return -1;

//        return this.ut.toString().compareTo(o.toString());
//        return Integer.compare(ID, o.ID);
    }
//    public int compareTo(AUnitType o) {
////        return this.ut.toString().compareTo(o.toString());
//        return Integer.compare(ID, o.ID);
//    }

    @Override
    public String toString() {
        return name();
    }

    // =========================================================
    // Auxiliary

    /**
     * Converts collection of <b>UnitType</b> variables into collection of <b>AUnitType</b> variables.
     */
    protected static Object convertToAUnitTypesCollection(Object collection) {
        if (collection instanceof Map) {
//            Map<AUnitType, Integer> result = new HashMap<>();
            Counter<AUnitType> units = new Counter<>();
            for (Object key : ((Map) collection).keySet()) {
                UnitType ut = (UnitType) key;
                AUnitType unitType = from(ut);
//                result.put(unitType, (Integer) ((Map) collection).get(ut));
                units.setValueFor(unitType, (Integer) ((Map) collection).get(ut));
            }
            return units;
        }
        else if (collection instanceof List) {
            List<AUnitType> result = new ArrayList<>();
            for (Object key : (List) collection) {
                UnitType ut = (UnitType) key;
                AUnitType unitType = from(ut);
                result.add(unitType);
            }
            return result;
        }
        else {
            throw new RuntimeException("I don't know how to convert collection of type: "
                + collection.toString());
        }
    }

    // =========================================================
    // Type comparison methods

    public boolean isBase() {
        return (boolean) cache.get(
            "isBase",
            -1,
            () -> is(
                AUnitType.Terran_Command_Center, AUnitType.Protoss_Nexus, AUnitType.Zerg_Hatchery,
                AUnitType.Zerg_Lair, AUnitType.Zerg_Hive
            )
        );
    }

    public boolean isInitialBase() {
        return (boolean) cache.get(
            "isInitialBase",
            -1,
            () -> is(
                AUnitType.Terran_Command_Center, AUnitType.Protoss_Nexus, AUnitType.Zerg_Hatchery
            )
        );
    }

    public boolean isPrimaryBase() {
        return (boolean) cache.get(
            "isPrimaryBase",
            -1,
            () -> is(AUnitType.Terran_Command_Center, AUnitType.Protoss_Nexus, AUnitType.Zerg_Hatchery)
        );
    }

    public boolean isInvincible() {
        return (boolean) cache.get(
            "isInvincible",
            -1,
            () -> ut.isInvincible()
        );
    }

    public boolean isInfantry() {
        return ut.isOrganic();
    }

    public boolean isBunker() {
        return (boolean) cache.get(
            "isBunker",
            -1,
            () -> is(Terran_Bunker)
        );
    }

    public boolean isPylon() {
        return (boolean) cache.get(
            "isPylon",
            -1,
            () -> is(Protoss_Pylon)
        );
    }

    public boolean isReaver() {
        return is(Protoss_Reaver);
    }

    public boolean isHighTemplar() {
        return is(Protoss_High_Templar);
    }

    public boolean isDarkTemplar() {
        return is(Protoss_Dark_Templar);
    }

    public boolean isCannon() {
        return is(Protoss_Photon_Cannon);
    }

    public boolean isSupplyDepot() {
        return is(Terran_Supply_Depot);
    }

    public boolean isCarrier() {
        return is(Protoss_Carrier);
    }

    public boolean isVehicle() {
        return ut.isMechanical();
    }

    public boolean isTerranInfantry() {
        return (boolean) cache.get(
            "isTerranInfantry",
            -1,
            () -> is(AUnitType.Terran_Marine, AUnitType.Terran_Medic,
                AUnitType.Terran_Firebat, AUnitType.Terran_Ghost)
        );
    }

    public boolean isTerranInfantryWithoutMedics() {
        return (boolean) cache.get(
            "isTerranInfantryWithoutMedics",
            -1,
            () -> is(AUnitType.Terran_Marine, AUnitType.Terran_Firebat, AUnitType.Terran_Ghost)
        );
    }

    public boolean isTankSieged() {
        return (boolean) cache.get(
            "isTankSieged",
            -1,
            () -> is(AUnitType.Terran_Siege_Tank_Siege_Mode)
        );
    }

    public boolean isTankUnsieged() {
        return (boolean) cache.get(
            "isTankUnsieged",
            -1,
            () -> is(AUnitType.Terran_Siege_Tank_Tank_Mode)
        );
    }

    public boolean isTank() {
        return (boolean) cache.get(
            "isTank",
            -1,
            () -> is(Terran_Siege_Tank_Tank_Mode, Terran_Siege_Tank_Siege_Mode)
        );
    }

    public boolean isFactory() {
        return is(AUnitType.Terran_Factory);
    }

    public boolean isMedic() {
        return is(AUnitType.Terran_Medic);
    }

    public boolean isGasBuilding() {
        return (boolean) cache.get(
            "isGasBuilding",
            0,
            () -> is(AUnitType.Terran_Refinery, AUnitType.Protoss_Assimilator, AUnitType.Zerg_Extractor)
        );
    }

    public boolean isLarva() {
        return this.equals(AUnitType.Zerg_Larva);
    }

    public boolean isEgg() {
        return this.equals(AUnitType.Zerg_Egg);
    }

    public boolean isExtractor() {
        return this.equals(AUnitType.Zerg_Extractor);
    }

    public boolean isOverlord() {
        return this.equals(AUnitType.Zerg_Overlord);
    }

    public boolean isAddon() {
        return ut.isAddon();
    }

    public boolean isAir() {
        return ut.isFlyer();
    }

    public boolean isGroundUnit() {
        return !ut.isFlyer();
    }

    public WeaponType groundWeapon() {
        return ut.groundWeapon();
    }

    public WeaponType airWeapon() {
        return ut.airWeapon();
    }

    public boolean isBuilding() {
        return ut.isBuilding();
    }

    public int getGasPrice() {
        return ut.gasPrice();
    }

    public int getMineralPrice() {
        return ut.mineralPrice();
    }

    public boolean isOrganic() {
        return ut.isOrganic();
    }

    public boolean isMechanical() {
        return ut.isMechanical();
    }

    public TechType getRequiredTech() {
        return ut.requiredTech();
    }

    public int getTileWidth() {
        return ut.tileWidth();
    }

    public int getTileHeight() {
        return ut.tileHeight();
    }

    public int maxHp() {
        return ut.maxHitPoints();
    }

    public boolean isWorker() {
        return (boolean) cache.get(
            "isWorker",
            -1,
            () -> is(AUnitType.Terran_SCV, AUnitType.Protoss_Probe, AUnitType.Zerg_Drone)
        );
    }

    public boolean isMineralField() {
        return (boolean) cache.get(
            "isMineralField",
            -1,
            () -> is(Resource_Mineral_Field)
        );
    }

    public boolean isSupplyUnit() {
        return (boolean) cache.get(
            "isSupplyUnit",
            -1,
            () -> is(Protoss_Pylon, Terran_Supply_Depot, Zerg_Overlord)
        );
    }

    /**
     * Replaces variable _isMilitaryBuildingAntiGround of old AUnit class
     *
     * @return
     */
    public boolean isMilitaryBuildingAntiGround() {
        return (boolean) cache.get(
            "isMilitaryBuildingAntiGround",
            -1,
            () -> is(AUnitType.Terran_Bunker, AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Sunken_Colony)
        );

    }

    /**
     * Replaces variable _isMilitaryBuildingAntiAir of old AUnit class
     *
     * @return
     */
    public boolean isMilitaryBuildingAntiAir() {
        return (boolean) cache.get(
            "isMilitaryBuildingAntiAir",
            -1,
            () -> is(
                AUnitType.Terran_Bunker, AUnitType.Protoss_Photon_Cannon, AUnitType.Zerg_Spore_Colony
            )
        );
    }

    /**
     * Returns true if given unit type is one of buildings like Bunker, Photon Cannon etc. For more details,
     * you have to specify at least one <b>true</b> to the params.
     */
    public boolean isMilitaryBuilding() {
        return (boolean) cache.get(
            "isMilitaryBuilding",
            -1,
            () -> isMilitaryBuildingAntiGround() || isMilitaryBuildingAntiAir()
        );
    }

    /**
     * Returns true if given unit type is one of buildings like Bunker, Photon Cannon etc. For more details,
     * you have to specify at least one <b>true</b> to the params.
     */
    public boolean isMilitaryBuilding(boolean canShootGround, boolean canShootAir) {
        return (boolean) cache.get(
            "isMilitaryBuilding",
            -1,
            () -> {
                if (!isBuilding()) return false;
                if (canShootGround && isMilitaryBuildingAntiGround()) return true;
                else return canShootAir && isMilitaryBuildingAntiAir();
            }
        );
    }

    /**
     * Returns true if this is Bunker, Turret, Photon Cannon, Sunken/Spore Colony.
     */
    public boolean isCombatBuilding() {
        return (boolean) cache.get(
            "isCombatBuilding",
            -1,
            () -> is(
                AUnitType.Terran_Bunker,
                AUnitType.Terran_Missile_Turret,
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Zerg_Sunken_Colony,
                AUnitType.Zerg_Spore_Colony
            )
        );
    }

    public boolean isCombatBuildingOrCreepColony() {
        return (boolean) cache.get(
            "isCombatBuildingOrCreepColony",
            -1,
            () -> is(
                AUnitType.Terran_Bunker,
                AUnitType.Terran_Missile_Turret,
                AUnitType.Protoss_Photon_Cannon,
                AUnitType.Zerg_Creep_Colony,
                AUnitType.Zerg_Sunken_Colony,
                AUnitType.Zerg_Spore_Colony
            )
        );
    }

    public Counter<AUnitType> requiredUnits() {
        return (Counter<AUnitType>) cache.get(
            "requiredUnits",
            -1,
            () -> convertToAUnitTypesCollection(ut.requiredUnits())
        );
    }

    /**
     * Returns building type (or parent type for units like Archon, Lurker) that produces this unit type.
     */
    public AUnitType whatBuildsIt() {
        return (AUnitType) cache.get(
            "whatBuildsIt",
            -1,
            () -> from(ut.whatBuilds().getFirst())
//                () -> {
//                    return from(ut.whatBuilds().getFirst());
//                }
        );
    }

    /**
     * In pixels (1 tile = 32px)
     */
    public int dimensionLeftPx() {
        return ut.dimensionLeft();
    }

    /**
     * In pixels (1 tile = 32px)
     */
    public int dimensionRightPx() {
        return ut.dimensionRight();
    }

    /**
     * In pixels (1 tile = 32px)
     */
    public int dimensionUpPx() {
        return ut.dimensionUp();
    }

    /**
     * In pixels (1 tile = 32px)
     */
    public int dimensionDownPx() {
        return ut.dimensionDown();
    }

    /**
     * Returns true if given building is able to build add-on like Terran Machine Shop.
     */
    public boolean canHaveAddon() {
        return (boolean) cache.get(
            "canHaveAddon",
            -1,
            () -> is(AUnitType.Terran_Factory, AUnitType.Terran_Command_Center,
                AUnitType.Terran_Starport, AUnitType.Terran_Science_Facility)
        );
    }

    public int addonWidthInPx() {
        return (int) cache.get(
            "addonWidthInPx",
            -1,
            () -> {
                if (is(AUnitType.Terran_Factory)) return calculateAddonWidthInPx(Terran_Machine_Shop);
                if (is(AUnitType.Terran_Command_Center)) return calculateAddonWidthInPx(Terran_Comsat_Station);
                if (is(AUnitType.Terran_Starport)) return calculateAddonWidthInPx(Terran_Control_Tower);
                if (is(AUnitType.Terran_Science_Facility)) return calculateAddonWidthInPx(Terran_Physics_Lab);

                return 0;
            }
        );
    }

    private int calculateAddonWidthInPx(AUnitType addon) {
        return addon.dimensionLeftPx() + addon.dimensionRightPx();
    }

    /**
     * Returns most default addon for Terran building like Machine Shop for Factory, Comsat Station for
     * Command Center or null if building can't have addon.
     */
//    public AUnitType getRelatedAddon() {
//        return (AUnitType) cache.get(
//            "getRelatedAddon",
//            -1,
//            () -> {
//                if (this.equals(Terran_Factory)) {
//                    return Terran_Machine_Shop;
//                }
//                else if (this.equals(Terran_Command_Center)) {
//                    return Terran_Comsat_Station;
//                }
//                else if (this.equals(Terran_Starport)) {
//                    return Terran_Control_Tower;
//                }
//                else if (this.equals(Terran_Science_Facility)) {
//                    return Terran_Physics_Lab;
//                }
//                else {
//                    return null;
//                }
//            }
//        );
//    }
    public boolean isDangerousGroundUnit() {
        return (boolean) cache.get(
            "isDangerousGroundUnit",
            -1,
            () -> is(
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Protoss_Reaver,
                AUnitType.Protoss_High_Templar,
                AUnitType.Zerg_Lurker,
                AUnitType.Zerg_Ultralisk
            )
        );
    }

    public boolean isMine() {
        return (boolean) cache.get(
            "isMine",
            -1,
            () -> is(AUnitType.Terran_Vulture_Spider_Mine)
        );
    }

    public boolean isScarab() {
        return (boolean) cache.get(
            "isScarab",
            -1,
            () -> is(AUnitType.Protoss_Scarab)
        );
    }

    public boolean isVulture() {
        return (boolean) cache.get(
            "isVulture",
            -1,
            () -> is(AUnitType.Terran_Vulture)
        );
    }

    public boolean isDragoon() {
        return (boolean) cache.get(
            "isDragoon",
            -1,
            () -> is(AUnitType.Protoss_Dragoon)
        );
    }

    public boolean isDetectorNonBuilding() {
        return (boolean) cache.get(
            "isDetectorNonBuilding",
            -1,
            () -> is(AUnitType.Protoss_Observer, Terran_Science_Vessel, Zerg_Overlord)
        );
    }

    public boolean isNeutralType() {
        return (boolean) cache.get(
            "isNeutralType",
            -1,
            () -> fullName().charAt(0) != 'Z' && fullName().charAt(0) != 'T' && fullName().charAt(0) != 'P'
        );
    }

    /**
     * Returns true if given unit is powerup or special map revealer etc.
     */
    public boolean isSpecial() {
        return (boolean) cache.get(
            "isSpecial",
            -1,
            () -> {
                String name = fullName();
                return name.startsWith("Powerup") || name.startsWith("Special");
            }
        );
    }

    /**
     * For buildings it returns what type of building is required to build it (e.g. Engineering Bay for Turret).
     * <br /><br />
     * For units it returns what type of building produces them (e.g. Barracks for Marines).
     */
    public AUnitType whatIsRequired() {
        return (AUnitType) cache.get(
            "whatIsRequired",
            -1,
            () -> {
                if (isBuilding()) {
                    for (AUnitType requiredUnit : requiredUnits().map().keySet()) {
                        if (requiredUnit.isBuilding() && !requiredUnit.isInitialBase() && !requiredUnit.isLarva()) {
                            return requiredUnit;
                        }
                    }
//                        System.err.println("getWhatTypeIsRequired reached end for: " + this);
//                        for (AUnitType requiredUnit : getRequiredUnits().keySet()) {
//                            System.err.println(requiredUnit + " x" + getRequiredUnits().get(requiredUnit));
//                        }
                    return null;
                }
                else {
                    return whatBuildsIt();
                }
            }
        );
    }

    public int weaponRangeAgainst(AUnit anotherUnit) {
        return (int) cache.get(
            "weaponRangeAgainst:" + anotherUnit.type().name(),
            -1,
            () -> {
                if (isCannon() || isSunken() || isSporeColony()) {
                    return 7;
                }
                if (isBunker()) {
                    return 6;
                }

                if (isGroundUnit()) {
                    if (isSunken()) {
                        return 7;
                    }
                }
                else {
                    if (isSporeColony()) {
                        return 7;
                    }
                }

                return weaponAgainst(anotherUnit.type()).maxRange() / 32;
            }
        );
    }

    public boolean isSunken() {
        return (boolean) cache.get(
            "isSunken",
            -1,
            () -> is(Zerg_Sunken_Colony)
        );
    }

    public boolean isSunkenOrCreep() {
        return (boolean) cache.get(
            "isSunkenOrCreep",
            -1,
            () -> is(Zerg_Sunken_Colony, Zerg_Creep_Colony)
        );
    }

    public boolean isSporeColony() {
        return (boolean) cache.get(
            "isSporeColony",
            -1,
            () -> is(Zerg_Spore_Colony)
        );
    }

    public boolean isLair() {
        return (boolean) cache.get(
            "isLair",
            -1,
            () -> is(Zerg_Lair)
        );
    }

    public boolean isHive() {
        return (boolean) cache.get(
            "isHive",
            -1,
            () -> is(Zerg_Hive)
        );
    }

    public boolean isGreaterSpire() {
        return (boolean) cache.get(
            "isGreaterSpire",
            -1,
            () -> is(Zerg_Greater_Spire)
        );
    }

    public WeaponType weaponAgainst(AUnitType target) {
        if (target.isGroundUnit()) {
            return groundWeapon();
        }
        else {
            return airWeapon();
        }
    }

    public boolean isGeyser() {
        return (boolean) cache.get(
            "isGeyser",
            -1,
            () -> is(Resource_Vespene_Geyser)
        );
    }

    public int totalTrainTime() {
        return ut.buildTime();
    }

    public boolean isCloakable() {
        return ut.isCloakable();
    }

    public boolean isSpell() {
        return ut.isSpell();
    }

    public boolean isTransportExcludeOverlords() {
        return (boolean) cache.get(
            "isTransportExcludeOverlords",
            -1,
            () -> is(Protoss_Shuttle, Terran_Dropship)
        );
    }

    public boolean isTransport() {
        return (boolean) cache.get(
            "isTransport",
            -1,
            () -> is(Protoss_Shuttle, Terran_Dropship, Zerg_Overlord)
        );
    }

    public boolean hasNoWeaponAtAll() {
        return (boolean) cache.get(
            "hasNoWeaponAtAll",
            -1,
            () -> {
                if (isCarrier() || isReaver()) return false;

                return groundWeapon().damageAmount() == 0 && airWeapon().damageAmount() == 0;
            }
        );
    }

    public int getID() {
        return ID;
    }

    public int id() {
        return ID;
    }

    public boolean isRealUnit() {
        return (boolean) cache.get(
            "isRealUnit",
            -1,
            () -> !isNeutral() && !isNotRealUnit()
        );
    }

    public boolean isRealUnitOrBuilding() {
        return (boolean) cache.get(
            "isRealUnitOrBuilding",
            -1,
            () -> !isNeutral() && (isBuilding() || !isNotRealUnit())
        );
    }

    /**
     * Not that we're racist, but spider mines and larvas aren't really units...
     */
    private boolean isNotRealUnit() {
        return (boolean) cache.get(
            "isNotRealUnit",
            -1,
            () -> isLarvaOrEgg() || isMineralField() || isInvincible()
                || isGeyser() || isSpell() || isMine() || isFlagOrBeacon()
        );
    }

    private boolean isFlagOrBeacon() {
        return (boolean) cache.get(
            "isFlagOrBeacon",
            -1,
            () -> ut.isFlagBeacon() || is(
                AUnitType.Powerup_Flag,
                AUnitType.Special_Map_Revealer,
                AUnitType.Special_Terran_Beacon,
                AUnitType.Special_Terran_Flag_Beacon,
                AUnitType.Special_Protoss_Beacon,
                AUnitType.Special_Protoss_Flag_Beacon,
                AUnitType.Special_Zerg_Beacon,
                AUnitType.Special_Zerg_Flag_Beacon
            )
        );
    }

    private boolean isNeutral() {
        return (boolean) cache.get(
            "isNeutral",
            -1,
            ut::isNeutral
        );
    }

    private boolean isLarvaOrEgg() {
        return (boolean) cache.get(
            "isLarvaOrEgg",
            -1,
            () -> is(Zerg_Larva, Zerg_Egg, Zerg_Lurker_Egg, Zerg_Cocoon)
        );
    }

    public boolean isCombatUnit() {
        return (boolean) cache.get(
            "isCombatUnit",
            -1,
            () -> !isWorker()
                && isRealUnit()
                && !isInvincible()
                && !isMine()
                && !isObserver()
                && (!isBuilding() || isCombatBuilding() || isSunkenOrCreep())
                && !isOverlord()
        );
    }

    private boolean isObserver() {
        return (boolean) cache.get(
            "isObserver",
            -1,
            () -> is(Protoss_Observer)
        );
    }

    public boolean isUnknown() {
        return AUnitType.Unknown.equals(this);
    }

    public boolean isAirUnitAntiAir() {
        return (boolean) cache.get(
            "isAirUnitAntiAir",
            -1,
            () -> is(
                Protoss_Corsair,
                Terran_Valkyrie,
                Zerg_Devourer,
                Zerg_Scourge
            )
        );
    }

    public boolean hasRequiredUnit() {
        return (boolean) cache.get(
            "hasRequiredUnit",
            -1,
            () -> whatIsRequired() != null
        );
    }

    public static String arrayToIds(AUnitType[] types) {
        StringBuilder result = new StringBuilder();
        for (AUnitType type : types) {
            result.append((result.length() > 0) ? "," : "").append(type.id());
        }
        return result.toString();
    }

    public DamageType damageTypeAgainst(AUnitType target) {
        return (DamageType) cache.get(
            "damageTypeAgainst:" + target.name(),
            -1,
            () -> this.weaponAgainst(target).damageType()
        );
    }

    public boolean isSmall() {
        return (boolean) cache.get(
            "isSmall",
            -1,
            () -> ut.size() == UnitSizeType.Small
        );
    }

    public boolean isMedium() {
        return (boolean) cache.get(
            "isMedium",
            -1,
            () -> ut.size() == UnitSizeType.Medium
        );
    }

    public boolean isLarge() {
        return (boolean) cache.get(
            "isLarge",
            -1,
            () -> ut.size() == UnitSizeType.Large
        );
    }

    public int supplyNeeded() {
        return (int) cache.get(
            "supplyNeeded",
            -1,
            ut::supplyRequired
        );
    }

    public boolean isGasBuildingOrGeyser() {
        return (boolean) cache.get(
            "isGasBuildingOrGeyser",
            -1,
            () -> isGeyser() || isGasBuilding()
        );
    }

    public boolean isLurker() {
        return (boolean) cache.get(
            "isLurker",
            -1,
            () -> is(Zerg_Lurker)
        );
    }

    public boolean isDefiler() {
        return (boolean) cache.get(
            "isDefiler",
            -1,
            () -> is(Zerg_Defiler)
        );
    }

    public boolean isMarine() {
        return (boolean) cache.get(
            "isMarine",
            -1,
            () -> is(Terran_Marine)
        );
    }

    public boolean isGhost() {
        return (boolean) cache.get(
            "isGhost",
            -1,
            () -> is(Terran_Ghost)
        );
    }

    public boolean isFirebat() {
        return (boolean) cache.get(
            "isFirebat",
            -1,
            () -> is(Terran_Firebat)
        );
    }

    public int totalCost() {
        return (int) cache.get(
            "totalCost",
            -1,
            () -> {
                if (isWorker()) {
                    return 50;
                }
                else if (is(Zerg_Zergling)) {
                    return 25;
                }

                int baseCost = 0;

                if (!requiredUnits().isEmpty() && isValidUnitRequirement(requiredUnits().first(), this)) {
                    baseCost += requiredUnits().size() * requiredUnits().first().totalCost();
                }

                return baseCost + getMineralPrice() + getGasPrice();
            }
        );
    }

    private boolean isValidUnitRequirement(AUnitType requirement, AUnitType unit) {
        if (requirement.isPylon()) return false;

        if (!unit.isBuilding() && requirement.isBuilding()) return false;

        if (isBuilding() && (requirement.isBuilding() && requirement.isBase())) return false;

        if (isBuilding() && requirement.isWorker()) return false;

        return true;
    }

    public boolean isMutalisk() {
        return (boolean) cache.get(
            "isMutalisk",
            -1,
            () -> is(Zerg_Mutalisk)
        );
    }

    public boolean isZealot() {
        return (boolean) cache.get(
            "isZealot",
            -1,
            () -> is(Protoss_Zealot)
        );
    }

    public boolean isMissileTurret() {
        return (boolean) cache.get(
            "isMissileTurret",
            -1,
            () -> is(Terran_Missile_Turret)
        );
    }

    public boolean isScv() {
        return (boolean) cache.get(
            "isScv",
            -1,
            () -> is(Terran_SCV)
        );
    }

    public boolean isProtoss() {
        return (boolean) cache.get(
            "isProtoss",
            -1,
            () -> ut.getRace().equals(Race.Protoss)
        );
    }

    public boolean isZerg() {
        return (boolean) cache.get(
            "isZerg",
            -1,
            () -> ut.getRace().equals(Race.Zerg)
        );
    }

    public boolean isTerran() {
        return (boolean) cache.get(
            "isTerran",
            -1,
            () -> ut.getRace().equals(Race.Terran)
        );
    }

    public boolean canAttackGround() {
        return (boolean) cache.get(
            "canAttackGround",
            -1,
            () -> groundWeapon().damageAmount() > 0 || isReaver() || isBunker() || isCarrier()
        );
    }

    public boolean canAttackAir() {
        return (boolean) cache.get(
            "canAttackAir",
            -1,
            () -> airWeapon().damageAmount() > 0 || isBunker() || isCarrier()
        );
    }

    public boolean isZergling() {
        return (boolean) cache.get(
            "isZergling",
            -1,
            () -> is(Zerg_Zergling)
        );
    }

    public boolean isScienceVessel() {
        return (boolean) cache.get(
            "isScienceVessel",
            -1,
            () -> is(Terran_Science_Vessel)
        );
    }
}
