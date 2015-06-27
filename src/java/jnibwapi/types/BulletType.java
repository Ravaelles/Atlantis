package jnibwapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a StarCraft bullet type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/BulletType
 */
public class BulletType {
	
	private static Map<Integer, BulletType> idToBulletType = new HashMap<>();
	
	public static class BulletTypes {
		public static final BulletType Melee = new BulletType(0);
		public static final BulletType Fusion_Cutter_Hit = new BulletType(141);
		public static final BulletType Gauss_Rifle_Hit = new BulletType(142);
		public static final BulletType C_10_Canister_Rifle_Hit = new BulletType(143);
		public static final BulletType Gemini_Missiles = new BulletType(144);
		public static final BulletType Fragmentation_Grenade = new BulletType(145);
		public static final BulletType Longbolt_Missile = new BulletType(146);
		public static final BulletType Undefined147 = new BulletType(147);
		public static final BulletType ATS_ATA_Laser_Battery = new BulletType(148);
		public static final BulletType Burst_Lasers = new BulletType(149);
		public static final BulletType Arclite_Shock_Cannon_Hit = new BulletType(150);
		public static final BulletType EMP_Missile = new BulletType(151);
		public static final BulletType Dual_Photon_Blasters_Hit = new BulletType(152);
		public static final BulletType Particle_Beam_Hit = new BulletType(153);
		public static final BulletType Anti_Matter_Missile = new BulletType(154);
		public static final BulletType Pulse_Cannon = new BulletType(155);
		public static final BulletType Psionic_Shockwave_Hit = new BulletType(156);
		public static final BulletType Psionic_Storm = new BulletType(157);
		public static final BulletType Yamato_Gun = new BulletType(158);
		public static final BulletType Phase_Disruptor = new BulletType(159);
		public static final BulletType STA_STS_Cannon_Overlay = new BulletType(160);
		public static final BulletType Sunken_Colony_Tentacle = new BulletType(161);
		public static final BulletType Acid_Spore = new BulletType(163);
		public static final BulletType Glave_Wurm = new BulletType(165);
		public static final BulletType Seeker_Spores = new BulletType(166);
		public static final BulletType Queen_Spell_Carrier = new BulletType(167);
		public static final BulletType Plague_Cloud = new BulletType(168);
		public static final BulletType Consume = new BulletType(169);
		public static final BulletType Needle_Spine_Hit = new BulletType(171);
		public static final BulletType Invisible = new BulletType(172);
		public static final BulletType Optical_Flare_Grenade = new BulletType(201);
		public static final BulletType Halo_Rockets = new BulletType(202);
		public static final BulletType Subterranean_Spines = new BulletType(203);
		public static final BulletType Corrosive_Acid_Shot = new BulletType(204);
		public static final BulletType Neutron_Flare = new BulletType(206);
		public static final BulletType None = new BulletType(209);
		public static final BulletType Unknown = new BulletType(210);
		
		public static BulletType getBulletType(int id) {
			return idToBulletType.get(id);
		}
		
		public static Collection<BulletType> getAllBulletTypes() {
			return Collections.unmodifiableCollection(idToBulletType.values());
		}
	}
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	private BulletType(int ID) {
		this.ID = ID;
		idToBulletType.put(ID, this);
	}
	
	public void initialize(int[] data, int index, String name) {
		if (ID != data[index++])
			throw new IllegalArgumentException();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return ID;
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getID() + ")";
	}
	
}
