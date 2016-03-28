package atlantis.units;

import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for BWMirror UnitType class that makes it much easier to use.
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
//public class AUnitType implements Comparable<Object> {
public class AUnitType implements Comparable<AUnitType> {

    private static final HashMap<UnitType, AUnitType> instances = new HashMap<>();
//    private static final List<AUnitType> instances = new ArrayList<>();

    private UnitType ut;

    // =========================================================
    
    private AUnitType(UnitType ut) {
        if (ut == null) {
            throw new RuntimeException("AUnitType constructor: type is null");
        }
        this.ut = ut;
        this.ID = firstFreeID++;
    }

    public static AUnitType createFrom(UnitType ut) {
        if (ut == null) {
            throw new RuntimeException("AUnitType constructor: type is null");
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
//        if ((unitType = getBWMirrorUnitType(ut)) != null) {
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

    private static AUnitType getBWMirrorUnitType(UnitType ut) {
        for (AUnitType unitType : instances.values()) {
            if (unitType.ut.equals(ut)) {
                return unitType;
            }
        }
        return null;
    }
    
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
    public static final AUnitType Factories = new AUnitType(UnitType.Factories);
    public static final AUnitType Unknown = new AUnitType(UnitType.Unknown);

    // =========================================================
    // =========================================================
    // =========================================================
    
    private static int firstFreeID = 1;

    private int ID;
    private String _name = null;
    private String _shortName = null;
    public static boolean disableErrorReporting = false;

    // =========================================================
    public static Collection<AUnitType> getAllUnitTypes() {
        return instances.values();
    }

    /**
     * You can "Terran_Marine" or "Terran Marine" or even "Marine".
     */
    public static AUnitType getByName(String string) {
        string = string.replace(" ", "_").toLowerCase()
                .replace("terran_", "").replace("protoss_", "").replace("zerg_", "");

        for (Field field : UnitType.class.getFields()) {
            String otherTypeName = field.getName().toLowerCase()
                    .replace("terran_", "").replace("protoss_", "").replace("zerg_", "");
            if (!otherTypeName.startsWith("Hero") && otherTypeName.equals(string)) {
                try {
                    AUnitType unitType = (AUnitType) AUnitType.class.getField(field.getName()).get(null);
//                    return instances.get(unitType);
                    return unitType;
                } catch (Exception e) {
                    if (!disableErrorReporting) {
                        System.err.println("error trying to find AUnitType for: '" + string + "'\n" + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns true if given type equals to one of types passed as parameter.
     */
    public boolean isType(AUnitType... types) {
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
    private boolean _checkedIfIsMelee = false;
    private boolean _isMelee = false;

    public boolean isMeleeUnit() {
        if (!_checkedIfIsMelee) {
            _checkedIfIsMelee = true;
            _isMelee = isType(
                    // Terran
                    AUnitType.Terran_SCV,
                    AUnitType.Terran_SCV,
                    AUnitType.Terran_Firebat,
                    // Protoss
                    AUnitType.Protoss_Probe,
                    AUnitType.Protoss_Zealot,
                    AUnitType.Protoss_Dark_Templar,
                    // Zerg
                    AUnitType.Zerg_Drone,
                    AUnitType.Zerg_Zergling,
                    AUnitType.Zerg_Broodling
            );
        }
        return _isMelee;
    }

    /**
     * Returns true if given unit is considered to be "ranged" unit (not melee).
     */
    public boolean isRangedUnit() {
        return !isMeleeUnit();
    }

    /**
     * Returns total sum of minerals and gas this unit is worth.
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
    public String getName() {
        if (_name == null) {
            _name = ut.toString();
        }
        return _name;
//        if (_name == null) {
//            try {
//                for (AUnitType type : instances) {
//                    if (type.equals(this)) {
//                        System.out.println(type + " / " + this);
//                        _name = type.getName().replace("_", " ");
//                        break;
//                    }
//                }
//            } catch (Exception ex) {
//                System.err.println(ex.getMessage());
//                System.err.println("Can't define name for unit type: " + this);
//                return "error";
//            }
//        }
//        return _name;
    }

    // =========================================================
    // Auxiliary methods
    /**
     * Returns short name for of unit type like e.g. "Zergling", "Marine", "Mutalisk", "Barracks".
     */
    public String getShortName() {
        String name = getName();
        if (_shortName == null) {
            _shortName = name.replace("Terran_", "").replace("Protoss_", "").replace("Zerg_", "")
                    .replace("Hero_", "").replace("Special_", "").replace("Powerup_", "").replace("_", " ")
                    .replace("Terran ", "").replace("Protoss ", "").replace("Zerg ", "")
                    .replace("Hero ", "").replace("Special ", "").replace("Powerup ", "");
        }

        return _shortName;
    }

    // =========================================================
    // Override
    
    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        
        if (obj instanceof AUnitType) {
            AUnitType other = (AUnitType) obj;
//            return (ut == other.ut);
//            System.out.println("@@@@@@@@@@@@@@@@@@@@@" + ut.toString() + " / " + other.ut.toString());
            return (ut.toString().equals(other.ut.toString()));
//            boolean condition = ID == other.ID;
//            boolean condition = (ut == other.ut);
//            if (condition) {
//                System.out.println(this + " #EQUALS# " + obj);
//            }
//            return condition;
        }
        
        return false;
    }

    @Override
    public int compareTo(AUnitType o) {
//        return this.ut.toString().compareTo(o.toString());
        return Integer.compare(ID, o.ID);
    }
    
//    @Override
//    public int hashCode() {
//        return ID;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        
//        if (obj instanceof AUnitType) {
//            AUnitType other = (AUnitType) obj;
//            return ID == other.ID;
//        }
//        else if (obj instanceof UnitType) {
//            UnitType other = (UnitType) obj;
//            return ut == obj;
//        }
//        
//        return false;
//    }
//
//    @Override
//    public int compareTo(Object o) {
//        return this.ut.toString().compareTo(o.toString());
//    }

    @Override
    public String toString() {
        return ut.toString();
    }
    
    // =========================================================
    // Type comparison methods
    public boolean isBase() {
        return isType(AUnitType.Terran_Command_Center, AUnitType.Protoss_Nexus, AUnitType.Zerg_Hatchery,
                AUnitType.Zerg_Lair, AUnitType.Zerg_Hive);
    }

    public boolean isInfantry() {
        return ut.isOrganic();
    }

    public boolean isVehicle() {
        return ut.isMechanical();
    }

    public boolean isTerranInfantry() {
        return isType(AUnitType.Terran_Marine, AUnitType.Terran_Medic, 
                AUnitType.Terran_Firebat, AUnitType.Terran_Ghost);
    }

    public boolean isMedic() {
        return isType(AUnitType.Terran_Medic);
    }

    public boolean isGasBuilding() {
        return isType(AUnitType.Terran_Refinery, AUnitType.Protoss_Assimilator, AUnitType.Zerg_Extractor);
    }

    public boolean isLarva() {
        return this.equals(AUnitType.Zerg_Larva);
    }

    public boolean isAirUnit() {
        return ut.isFlyer();
    }

    public boolean isGroundUnit() {
        return !ut.isFlyer();
    }

    public WeaponType getGroundWeapon() {
        return ut.groundWeapon();
    }

    public WeaponType getAirWeapon() {
        return ut.airWeapon();
    }

    public boolean isBuilding() {
        return ut.isBuilding();
    }

    public int gasPrice() {
        return ut.gasPrice();
    }

    public int mineralPrice() {
        return ut.mineralPrice();
    }

    public boolean isOrganic() {
        return ut.isOrganic();
    }

    public boolean isMechanical() {
        return ut.isMechanical();
    }

    public Map<UnitType, Integer> requiredUnits() {
        return ut.requiredUnits();
    }

    public TechType requiredTech() {
        return ut.requiredTech();
    }

    public int tileWidth() {
        return ut.tileWidth();
    }

    public int tileHeight() {
        return ut.tileHeight();
    }

    public int getMaxHitPoints() {
        return ut.maxHitPoints();
    }

    public boolean isWorker() {
        return isType(AUnitType.Terran_SCV, AUnitType.Protoss_Probe, AUnitType.Zerg_Drone);
    }

    public boolean isMineralField() {
        return isType(Resource_Mineral_Field);
    }

    public boolean isSupplyUnit() {
        return isType(Protoss_Pylon, Terran_Supply_Depot, Zerg_Overlord);
    }

}
