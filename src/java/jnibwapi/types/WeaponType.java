package jnibwapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a StarCraft weapon type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/WeaponType
 */
public class WeaponType {
	
	private static Map<Integer, WeaponType> idToWeaponType = new HashMap<>();
	
	public static class WeaponTypes {
		public static final WeaponType Gauss_Rifle = new WeaponType(0);
		public static final WeaponType Gauss_Rifle_Jim_Raynor = new WeaponType(1);
		public static final WeaponType C_10_Canister_Rifle = new WeaponType(2);
		public static final WeaponType C_10_Canister_Rifle_Sarah_Kerrigan = new WeaponType(3);
		public static final WeaponType Fragmentation_Grenade = new WeaponType(4);
		public static final WeaponType Fragmentation_Grenade_Jim_Raynor = new WeaponType(5);
		public static final WeaponType Spider_Mines = new WeaponType(6);
		public static final WeaponType Twin_Autocannons = new WeaponType(7);
		public static final WeaponType Hellfire_Missile_Pack = new WeaponType(8);
		public static final WeaponType Twin_Autocannons_Alan_Schezar = new WeaponType(9);
		public static final WeaponType Hellfire_Missile_Pack_Alan_Schezar = new WeaponType(10);
		public static final WeaponType Arclite_Cannon = new WeaponType(11);
		public static final WeaponType Arclite_Cannon_Edmund_Duke = new WeaponType(12);
		public static final WeaponType Fusion_Cutter = new WeaponType(13);
		// 14 is undefined
		public static final WeaponType Gemini_Missiles = new WeaponType(15);
		public static final WeaponType Burst_Lasers = new WeaponType(16);
		public static final WeaponType Gemini_Missiles_Tom_Kazansky = new WeaponType(17);
		public static final WeaponType Burst_Lasers_Tom_Kazansky = new WeaponType(18);
		public static final WeaponType ATS_Laser_Battery = new WeaponType(19);
		public static final WeaponType ATA_Laser_Battery = new WeaponType(20);
		public static final WeaponType ATS_Laser_Battery_Hero = new WeaponType(21);
		public static final WeaponType ATA_Laser_Battery_Hero = new WeaponType(22);
		public static final WeaponType ATS_Laser_Battery_Hyperion = new WeaponType(23);
		public static final WeaponType ATA_Laser_Battery_Hyperion = new WeaponType(24);
		public static final WeaponType Flame_Thrower = new WeaponType(25);
		public static final WeaponType Flame_Thrower_Gui_Montag = new WeaponType(26);
		public static final WeaponType Arclite_Shock_Cannon = new WeaponType(27);
		public static final WeaponType Arclite_Shock_Cannon_Edmund_Duke = new WeaponType(28);
		public static final WeaponType Longbolt_Missile = new WeaponType(29);
		public static final WeaponType Yamato_Gun = new WeaponType(30);
		public static final WeaponType Nuclear_Strike = new WeaponType(31);
		public static final WeaponType Lockdown = new WeaponType(32);
		public static final WeaponType EMP_Shockwave = new WeaponType(33);
		public static final WeaponType Irradiate = new WeaponType(34);
		public static final WeaponType Claws = new WeaponType(35);
		public static final WeaponType Claws_Devouring_One = new WeaponType(36);
		public static final WeaponType Claws_Infested_Kerrigan = new WeaponType(37);
		public static final WeaponType Needle_Spines = new WeaponType(38);
		public static final WeaponType Needle_Spines_Hunter_Killer = new WeaponType(39);
		public static final WeaponType Kaiser_Blades = new WeaponType(40);
		public static final WeaponType Kaiser_Blades_Torrasque = new WeaponType(41);
		public static final WeaponType Toxic_Spores = new WeaponType(42);
		public static final WeaponType Spines = new WeaponType(43);
		// 44-45 are undefined
		public static final WeaponType Acid_Spore = new WeaponType(46);
		public static final WeaponType Acid_Spore_Kukulza = new WeaponType(47);
		public static final WeaponType Glave_Wurm = new WeaponType(48);
		public static final WeaponType Glave_Wurm_Kukulza = new WeaponType(49);
		// 50-51 are undefined
		public static final WeaponType Seeker_Spores = new WeaponType(52);
		public static final WeaponType Subterranean_Tentacle = new WeaponType(53);
		public static final WeaponType Suicide_Infested_Terran = new WeaponType(54);
		public static final WeaponType Suicide_Scourge = new WeaponType(55);
		public static final WeaponType Parasite = new WeaponType(56);
		public static final WeaponType Spawn_Broodlings = new WeaponType(57);
		public static final WeaponType Ensnare = new WeaponType(58);
		public static final WeaponType Dark_Swarm = new WeaponType(59);
		public static final WeaponType Plague = new WeaponType(60);
		public static final WeaponType Consume = new WeaponType(61);
		public static final WeaponType Particle_Beam = new WeaponType(62);
		// 63 is undefined
		public static final WeaponType Psi_Blades = new WeaponType(64);
		public static final WeaponType Psi_Blades_Fenix = new WeaponType(65);
		public static final WeaponType Phase_Disruptor = new WeaponType(66);
		public static final WeaponType Phase_Disruptor_Fenix = new WeaponType(67);
		// 68 is undefined
		public static final WeaponType Psi_Assault = new WeaponType(69);
		public static final WeaponType Psionic_Shockwave = new WeaponType(70);
		public static final WeaponType Psionic_Shockwave_Tassadar_Zeratul_Archon =
				new WeaponType(71);
		// 72 is undefined
		public static final WeaponType Dual_Photon_Blasters = new WeaponType(73);
		public static final WeaponType Anti_Matter_Missiles = new WeaponType(74);
		public static final WeaponType Dual_Photon_Blasters_Mojo = new WeaponType(75);
		public static final WeaponType Anti_Matter_Missiles_Mojo = new WeaponType(76);
		public static final WeaponType Phase_Disruptor_Cannon = new WeaponType(77);
		public static final WeaponType Phase_Disruptor_Cannon_Danimoth = new WeaponType(78);
		public static final WeaponType Pulse_Cannon = new WeaponType(79);
		public static final WeaponType STS_Photon_Cannon = new WeaponType(80);
		public static final WeaponType STA_Photon_Cannon = new WeaponType(81);
		public static final WeaponType Scarab = new WeaponType(82);
		public static final WeaponType Stasis_Field = new WeaponType(83);
		public static final WeaponType Psionic_Storm = new WeaponType(84);
		public static final WeaponType Warp_Blades_Zeratul = new WeaponType(85);
		public static final WeaponType Warp_Blades_Hero = new WeaponType(86);
		// 87-92 are undefined
	    public static final WeaponType Independant_Laser_Battery = new WeaponType(93);
	    // 94-95 are undefined
	    public static final WeaponType Twin_Autocannons_Floor_Trap = new WeaponType(96);
	    public static final WeaponType Hellfire_Missile_Pack_Wall_Trap = new WeaponType(97);
	    public static final WeaponType Flame_Thrower_Wall_Trap = new WeaponType(98);
	    public static final WeaponType Hellfire_Missile_Pack_Floor_Trap = new WeaponType(99);
		public static final WeaponType Neutron_Flare = new WeaponType(100);
		public static final WeaponType Disruption_Web = new WeaponType(101);
		public static final WeaponType Restoration = new WeaponType(102);
		public static final WeaponType Halo_Rockets = new WeaponType(103);
		public static final WeaponType Corrosive_Acid = new WeaponType(104);
		public static final WeaponType Mind_Control = new WeaponType(105);
		public static final WeaponType Feedback = new WeaponType(106);
		public static final WeaponType Optical_Flare = new WeaponType(107);
		public static final WeaponType Maelstrom = new WeaponType(108);
		public static final WeaponType Subterranean_Spines = new WeaponType(109);
		// 110 is undefined
		public static final WeaponType Warp_Blades = new WeaponType(111);
		public static final WeaponType C_10_Canister_Rifle_Samir_Duran = new WeaponType(112);
		public static final WeaponType C_10_Canister_Rifle_Infested_Duran = new WeaponType(113);
		public static final WeaponType Dual_Photon_Blasters_Artanis = new WeaponType(114);
		public static final WeaponType Anti_Matter_Missiles_Artanis = new WeaponType(115);
		public static final WeaponType C_10_Canister_Rifle_Alexei_Stukov = new WeaponType(116);
		// 117-129 are undefined
		public static final WeaponType None = new WeaponType(130);
		public static final WeaponType Unknown = new WeaponType(131);
		
		public static WeaponType getWeaponType(int id) {
			return idToWeaponType.get(id);
		}
		
		public static Collection<WeaponType> getAllWeaponTypes() {
			return Collections.unmodifiableCollection(idToWeaponType.values());
		}
	}
	
	public static final int numAttributes = 24;
	
	private String name;
	private int ID;
	private int techID;
	private int whatUsesTypeID;
	private int damageAmount;
	private int damageBonus;
	private int damageCooldown;
	private int damageFactor;
	private int upgradeTypeID;
	private int damageTypeID;
	private int explosionType;
	private int minRange;
	private int maxRange;
	private int innerSplashRadius;
	private int medianSplashRadius;
	private int outerSplashRadius;
	private boolean targetsAir;
	private boolean targetsGround;
	private boolean targetsMechanical;
	private boolean targetsOrganic;
	private boolean targetsNonBuilding;
	private boolean targetsNonRobotic;
	private boolean targetsTerrain;
	private boolean targetsOrgOrMech;
	private boolean targetsOwn;
	
	private WeaponType(int ID) {
		this.ID = ID;
		idToWeaponType.put(ID, this);
	}
	
	public void initialize(int[] data, int index, String name) {
		if (ID != data[index++])
			throw new IllegalArgumentException();
		techID = data[index++];
		whatUsesTypeID = data[index++];
		damageAmount = data[index++];
		damageBonus = data[index++];
		damageCooldown = data[index++];
		damageFactor = data[index++];
		upgradeTypeID = data[index++];
		damageTypeID = data[index++];
		explosionType = data[index++];
		minRange = data[index++];
		maxRange = data[index++];
		innerSplashRadius = data[index++];
		medianSplashRadius = data[index++];
		outerSplashRadius = data[index++];
		targetsAir = data[index++] == 1;
		targetsGround = data[index++] == 1;
		targetsMechanical = data[index++] == 1;
		targetsOrganic = data[index++] == 1;
		targetsNonBuilding = data[index++] == 1;
		targetsNonRobotic = data[index++] == 1;
		targetsTerrain = data[index++] == 1;
		targetsOrgOrMech = data[index++] == 1;
		targetsOwn = data[index++] == 1;
		
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return ID;
	}
	
	public int getTechID() {
		return techID;
	}
	
	public int getWhatUsesTypeID() {
		return whatUsesTypeID;
	}
	
	public int getDamageAmount() {
		return damageAmount;
	}
	
	public int getDamageBonus() {
		return damageBonus;
	}
	
	public int getDamageCooldown() {
		return damageCooldown;
	}
	
	public int getDamageFactor() {
		return damageFactor;
	}
	
	public int getUpgradeTypeID() {
		return upgradeTypeID;
	}
	
	public int getDamageTypeID() {
		return damageTypeID;
	}
	
	public int getExplosionType() {
		return explosionType;
	}
	
	public int getMinRange() {
		return minRange;
	}
	
	public int getMaxRange() {
		return maxRange;
	}
	
	public int getInnerSplashRadius() {
		return innerSplashRadius;
	}
	
	public int getMedianSplashRadius() {
		return medianSplashRadius;
	}
	
	public int getOuterSplashRadius() {
		return outerSplashRadius;
	}
	
	public boolean isTargetsAir() {
		return targetsAir;
	}
	
	public boolean isTargetsGround() {
		return targetsGround;
	}
	
	public boolean isTargetsMechanical() {
		return targetsMechanical;
	}
	
	public boolean isTargetsOrganic() {
		return targetsOrganic;
	}
	
	public boolean isTargetsNonBuilding() {
		return targetsNonBuilding;
	}
	
	public boolean isTargetsNonRobotic() {
		return targetsNonRobotic;
	}
	
	public boolean isTargetsTerrain() {
		return targetsTerrain;
	}
	
	public boolean isTargetsOrgOrMech() {
		return targetsOrgOrMech;
	}
	
	public boolean isTargetsOwn() {
		return targetsOwn;
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getID() + ")";
	}
}
