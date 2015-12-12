package jnibwapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a StarCraft explosion type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/ExplosionType
 */
public class ExplosionType {
	
	private static Map<Integer, ExplosionType> idToExplosionType = new HashMap<>();
	
	public static class ExplosionTypes {
		public static final ExplosionType None = new ExplosionType(0);
		public static final ExplosionType Normal = new ExplosionType(1);
		public static final ExplosionType Radial_Splash = new ExplosionType(2);
		public static final ExplosionType Enemy_Splash = new ExplosionType(3);
		public static final ExplosionType Lockdown = new ExplosionType(4);
		public static final ExplosionType Nuclear_Missile = new ExplosionType(5);
		public static final ExplosionType Parasite = new ExplosionType(6);
		public static final ExplosionType Broodlings = new ExplosionType(7);
		public static final ExplosionType EMP_Shockwave = new ExplosionType(8);
		public static final ExplosionType Irradiate = new ExplosionType(9);
		public static final ExplosionType Ensnare = new ExplosionType(10);
		public static final ExplosionType Plague = new ExplosionType(11);
		public static final ExplosionType Stasis_Field = new ExplosionType(12);
		public static final ExplosionType Dark_Swarm = new ExplosionType(13);
		public static final ExplosionType Consume = new ExplosionType(14);
		public static final ExplosionType Yamato_Gun = new ExplosionType(15);
		public static final ExplosionType Restoration = new ExplosionType(16);
		public static final ExplosionType Disruption_Web = new ExplosionType(17);
		public static final ExplosionType Corrosive_Acid = new ExplosionType(18);
		public static final ExplosionType Mind_Control = new ExplosionType(19);
		public static final ExplosionType Feedback = new ExplosionType(20);
		public static final ExplosionType Optical_Flare = new ExplosionType(21);
		public static final ExplosionType Maelstrom = new ExplosionType(22);
		public static final ExplosionType Undefined23 = new ExplosionType(23);
		public static final ExplosionType Air_Splash = new ExplosionType(24);
		public static final ExplosionType Unknown = new ExplosionType(25);
		
		public static ExplosionType getExplosionType(int id) {
			return idToExplosionType.get(id);
		}
		
		public static Collection<ExplosionType> getAllExplosionTypes() {
			return Collections.unmodifiableCollection(idToExplosionType.values());
		}
	}
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	private ExplosionType(int ID) {
		this.ID = ID;
		idToExplosionType.put(ID, this);
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
