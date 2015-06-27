package jnibwapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a StarCraft race type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/Race
 */
public class RaceType {
	
	private static Map<Integer, RaceType> idToRaceType = new HashMap<>();
	
	public static class RaceTypes {
		public static final RaceType Zerg = new RaceType(0);
		public static final RaceType Terran = new RaceType(1);
		public static final RaceType Protoss = new RaceType(2);
		// NOTE: Changes in BWAPI4 to:
		// Unused = 3,4,5, Random = 6, None = 7, Unknown = 8
		public static final RaceType Random = new RaceType(3);
		public static final RaceType Other = new RaceType(4);
		public static final RaceType None = new RaceType(5);
		public static final RaceType Unknown = new RaceType(6);
		
		public static RaceType getRaceType(int id) {
			return idToRaceType.get(id);
		}
		
		public static Collection<RaceType> getAllRaceTypes() {
			return Collections.unmodifiableCollection(idToRaceType.values());
		}
	}
	
	public static final int numAttributes = 6;
	
	private String name;
	private int ID;
	private int workerID;
	private int centerID;
	private int refineryID;
	private int transportID;
	private int supplyProviderID;
	
	private RaceType(int ID) {
		this.ID = ID;
		idToRaceType.put(ID, this);
	}
	
	public void initialize(int[] data, int index, String name) {
		if (ID != data[index++])
			throw new IllegalArgumentException();
		workerID = data[index++];
		centerID = data[index++];
		refineryID = data[index++];
		transportID = data[index++];
		supplyProviderID = data[index++];
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return ID;
	}
	
	public int getWorkerID() {
		return workerID;
	}
	
	public int getCenterID() {
		return centerID;
	}
	
	public int getRefineryID() {
		return refineryID;
	}
	
	public int getTransportID() {
		return transportID;
	}
	
	public int getSupplyProviderID() {
		return supplyProviderID;
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getID() + ")";
	}
	
}
