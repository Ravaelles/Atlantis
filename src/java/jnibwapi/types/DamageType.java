package jnibwapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a StarCraft damage type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/DamageType
 */
public class DamageType {
	
	private static Map<Integer, DamageType> idToDamageType = new HashMap<>();
	
	public static class DamageTypes {
		public static final DamageType Independent = new DamageType(0);
		public static final DamageType Explosive = new DamageType(1);
		public static final DamageType Concussive = new DamageType(2);
		public static final DamageType Normal = new DamageType(3);
		public static final DamageType Ignore_Armor = new DamageType(4);
		public static final DamageType None = new DamageType(5);
		public static final DamageType Unknown = new DamageType(6);
		
		public static DamageType getDamageType(int id) {
			return idToDamageType.get(id);
		}
		
		public static Collection<DamageType> getAllDamageTypes() {
			return Collections.unmodifiableCollection(idToDamageType.values());
		}
	}
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	private DamageType(int ID) {
		this.ID = ID;
		idToDamageType.put(ID, this);
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
