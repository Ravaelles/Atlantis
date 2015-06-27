package jnibwapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a StarCraft unit command type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/UnitCommandType
 */
public class UnitCommandType {
	
	private static Map<Integer, UnitCommandType> idToUnitCommandType = new HashMap<>();
	
	public static class UnitCommandTypes {
		public static final UnitCommandType Attack_Move = new UnitCommandType(0);
		public static final UnitCommandType Attack_Unit = new UnitCommandType(1);
		public static final UnitCommandType Build = new UnitCommandType(2);
		public static final UnitCommandType Build_Addon = new UnitCommandType(3);
		public static final UnitCommandType Train = new UnitCommandType(4);
		public static final UnitCommandType Morph = new UnitCommandType(5);
		public static final UnitCommandType Research = new UnitCommandType(6);
		public static final UnitCommandType Upgrade = new UnitCommandType(7);
		public static final UnitCommandType Set_Rally_Position = new UnitCommandType(8);
		public static final UnitCommandType Set_Rally_Unit = new UnitCommandType(9);
		public static final UnitCommandType Move = new UnitCommandType(10);
		public static final UnitCommandType Patrol = new UnitCommandType(11);
		public static final UnitCommandType Hold_Position = new UnitCommandType(12);
		public static final UnitCommandType Stop = new UnitCommandType(13);
		public static final UnitCommandType Follow = new UnitCommandType(14);
		public static final UnitCommandType Gather = new UnitCommandType(15);
		public static final UnitCommandType Return_Cargo = new UnitCommandType(16);
		public static final UnitCommandType Repair = new UnitCommandType(17);
		public static final UnitCommandType Burrow = new UnitCommandType(18);
		public static final UnitCommandType Unburrow = new UnitCommandType(19);
		public static final UnitCommandType Cloak = new UnitCommandType(20);
		public static final UnitCommandType Decloak = new UnitCommandType(21);
		public static final UnitCommandType Siege = new UnitCommandType(22);
		public static final UnitCommandType Unsiege = new UnitCommandType(23);
		public static final UnitCommandType Lift = new UnitCommandType(24);
		public static final UnitCommandType Land = new UnitCommandType(25);
		public static final UnitCommandType Load = new UnitCommandType(26);
		public static final UnitCommandType Unload = new UnitCommandType(27);
		public static final UnitCommandType Unload_All = new UnitCommandType(28);
		public static final UnitCommandType Unload_All_Position = new UnitCommandType(29);
		public static final UnitCommandType Right_Click_Position = new UnitCommandType(30);
		public static final UnitCommandType Right_Click_Unit = new UnitCommandType(31);
		public static final UnitCommandType Halt_Construction = new UnitCommandType(32);
		public static final UnitCommandType Cancel_Construction = new UnitCommandType(33);
		public static final UnitCommandType Cancel_Addon = new UnitCommandType(34);
		public static final UnitCommandType Cancel_Train = new UnitCommandType(35);
		public static final UnitCommandType Cancel_Train_Slot = new UnitCommandType(36);
		public static final UnitCommandType Cancel_Morph = new UnitCommandType(37);
		public static final UnitCommandType Cancel_Research = new UnitCommandType(38);
		public static final UnitCommandType Cancel_Upgrade = new UnitCommandType(39);
		public static final UnitCommandType Use_Tech = new UnitCommandType(40);
		public static final UnitCommandType Use_Tech_Position = new UnitCommandType(41);
		public static final UnitCommandType Use_Tech_Unit = new UnitCommandType(42);
		public static final UnitCommandType Place_COP = new UnitCommandType(43);
		public static final UnitCommandType None = new UnitCommandType(44);
		public static final UnitCommandType Unknown = new UnitCommandType(45);
		
		public static UnitCommandType getUnitCommandType(int id) {
			return idToUnitCommandType.get(id);
		}
		
		public static Collection<UnitCommandType> getAllUnitCommandTypes() {
			return Collections.unmodifiableCollection(idToUnitCommandType.values());
		}
	}
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	private UnitCommandType(int ID) {
		this.ID = ID;
		idToUnitCommandType.put(ID, this);
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
