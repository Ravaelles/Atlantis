package jnibwapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a StarCraft upgrade type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/UpgradeType
 */
public class UpgradeType {
	private static Map<Integer, UpgradeType> idToUpgradeType = new HashMap<>();
	
	public static class UpgradeTypes {
		public static final UpgradeType Terran_Infantry_Armor = new UpgradeType(0);
		public static final UpgradeType Terran_Vehicle_Plating = new UpgradeType(1);
		public static final UpgradeType Terran_Ship_Plating = new UpgradeType(2);
		public static final UpgradeType Zerg_Carapace = new UpgradeType(3);
		public static final UpgradeType Zerg_Flyer_Carapace = new UpgradeType(4);
		public static final UpgradeType Protoss_Ground_Armor = new UpgradeType(5);
		public static final UpgradeType Protoss_Air_Armor = new UpgradeType(6);
		public static final UpgradeType Terran_Infantry_Weapons = new UpgradeType(7);
		public static final UpgradeType Terran_Vehicle_Weapons = new UpgradeType(8);
		public static final UpgradeType Terran_Ship_Weapons = new UpgradeType(9);
		public static final UpgradeType Zerg_Melee_Attacks = new UpgradeType(10);
		public static final UpgradeType Zerg_Missile_Attacks = new UpgradeType(11);
		public static final UpgradeType Zerg_Flyer_Attacks = new UpgradeType(12);
		public static final UpgradeType Protoss_Ground_Weapons = new UpgradeType(13);
		public static final UpgradeType Protoss_Air_Weapons = new UpgradeType(14);
		public static final UpgradeType Protoss_Plasma_Shields = new UpgradeType(15);
		/** Marine Range */
		public static final UpgradeType U_238_Shells = new UpgradeType(16);
		/** Vulture Speed */
		public static final UpgradeType Ion_Thrusters = new UpgradeType(17);
		// Undefined18
		/** Science Vessel Energy */
		public static final UpgradeType Titan_Reactor = new UpgradeType(19);
		/** Ghost Sight */
		public static final UpgradeType Ocular_Implants = new UpgradeType(20);
		/** Ghost Energy */
		public static final UpgradeType Moebius_Reactor = new UpgradeType(21);
		/** Wraith Energy */
		public static final UpgradeType Apollo_Reactor = new UpgradeType(22);
		/** Battle Cruiser Energy */
		public static final UpgradeType Colossus_Reactor = new UpgradeType(23);
		/** Overlord Transport */
		public static final UpgradeType Ventral_Sacs = new UpgradeType(24);
		/** Overlord Sight */
		public static final UpgradeType Antennae = new UpgradeType(25);
		/** Overlord Speed */
		public static final UpgradeType Pneumatized_Carapace = new UpgradeType(26);
		/** Zergling Speed */
		public static final UpgradeType Metabolic_Boost = new UpgradeType(27);
		/** Zergling Attack */
		public static final UpgradeType Adrenal_Glands = new UpgradeType(28);
		/** Hydralisk Speed */
		public static final UpgradeType Muscular_Augments = new UpgradeType(29);
		/** Hydralisk Range */
		public static final UpgradeType Grooved_Spines = new UpgradeType(30);
		/** Queen Energy */
		public static final UpgradeType Gamete_Meiosis = new UpgradeType(31);
		/** Defiler Energy */
		public static final UpgradeType Metasynaptic_Node = new UpgradeType(32);
		/** Dragoon Range */
		public static final UpgradeType Singularity_Charge = new UpgradeType(33);
		/** Zealot Speed */
		public static final UpgradeType Leg_Enhancements = new UpgradeType(34);
		public static final UpgradeType Scarab_Damage = new UpgradeType(35);
		public static final UpgradeType Reaver_Capacity = new UpgradeType(36);
		/** Shuttle Speed */
		public static final UpgradeType Gravitic_Drive = new UpgradeType(37);
		/** Observer Sight */
		public static final UpgradeType Sensor_Array = new UpgradeType(38);
		/** Observer Speed */
		public static final UpgradeType Gravitic_Boosters = new UpgradeType(39);
		/** Templar Energy */
		public static final UpgradeType Khaydarin_Amulet = new UpgradeType(40);
		/** Scout Sight */
		public static final UpgradeType Apial_Sensors = new UpgradeType(41);
		/** Scout Speed */
		public static final UpgradeType Gravitic_Thrusters = new UpgradeType(42);
		public static final UpgradeType Carrier_Capacity = new UpgradeType(43);
		/** Arbiter Energy */
		public static final UpgradeType Khaydarin_Core = new UpgradeType(44);
		// Undefined45
		// Undefined46
		/** Corsair Energy */
		public static final UpgradeType Argus_Jewel = new UpgradeType(47);
		// Undefined48
		/** Dark Archon Energy */
		public static final UpgradeType Argus_Talisman = new UpgradeType(49);
		// Undefined50
		/** Medic Energy */
		public static final UpgradeType Caduceus_Reactor = new UpgradeType(51);
		/** Ultralisk Armor */
		public static final UpgradeType Chitinous_Plating = new UpgradeType(52);
		/** Ultralisk Speed */
		public static final UpgradeType Anabolic_Synthesis = new UpgradeType(53);
		/** Goliath Range */
		public static final UpgradeType Charon_Boosters = new UpgradeType(54);
		// Undefined55-60
		public static final UpgradeType None = new UpgradeType(61);
		public static final UpgradeType Unknown = new UpgradeType(62);
		
		public static UpgradeType getUpgradeType(int id) {
			return idToUpgradeType.get(id);
		}
		
		public static Collection<UpgradeType> getAllUpgradeTypes() {
			return Collections.unmodifiableCollection(idToUpgradeType.values());
		}
	}
	
	public static final int numAttributes = 10;
	
	private String name;
	private int ID;
	private int raceID;
	private int mineralPriceBase;
	private int mineralPriceFactor;
	private int gasPriceBase;
	private int gasPriceFactor;
	private int upgradeTimeBase;
	private int upgradeTimeFactor;
	private int maxRepeats;
	private int whatUpgradesTypeID;
	
	private UpgradeType(int ID) {
		this.ID = ID;
		idToUpgradeType.put(ID, this);
	}
	
	public void initialize(int[] data, int index, String name) {
		if (ID != data[index++])
			throw new IllegalArgumentException();
		raceID = data[index++];
		mineralPriceBase = data[index++];
		mineralPriceFactor = data[index++];
		gasPriceBase = data[index++];
		gasPriceFactor = data[index++];
		upgradeTimeBase = data[index++];
		upgradeTimeFactor = data[index++];
		maxRepeats = data[index++];
		whatUpgradesTypeID = data[index++];
		
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return ID;
	}
	
	public int getRaceID() {
		return raceID;
	}
	
	public int getMineralPriceBase() {
		return mineralPriceBase;
	}
	
	public int getMineralPriceFactor() {
		return mineralPriceFactor;
	}
	
	public int getGasPriceBase() {
		return gasPriceBase;
	}
	
	public int getGasPriceFactor() {
		return gasPriceFactor;
	}
	
	public int getUpgradeTimeBase() {
		return upgradeTimeBase;
	}
	
	public int getUpgradeTimeFactor() {
		return upgradeTimeFactor;
	}
	
	public int getMaxRepeats() {
		return maxRepeats;
	}
	
	public int getWhatUpgradesTypeID() {
		return whatUpgradesTypeID;
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getID() + ")";
	}
	
}
