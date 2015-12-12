package jnibwapi.types;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents a StarCraft player type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/PlayerType
 */
public enum PlayerType {
	None(0),
	Computer(1),
	Player(2),
	RescuePassive(3),
	Unused_RescueActive(4),
	EitherPreferComputer(5),
	EitherPreferHuman(6),
	Neutral(7),
	Closed(8),
	Unused_Observer(9),
	PlayerLeft(10),
	ComputerLeft(11),
	Unknown(12);
	
	private int ID;
	
	private PlayerType(int ID) {
		this.ID = ID;
	}
	
	public int getID() {
		return ID;
	}
	
	public String getName() {
		return name();
	}
	
	public static PlayerType getPlayerType(int id) {
		return PlayerType.values()[id];
	}
	
	public static Collection<PlayerType> getAllPlayerTypes() {
		return Collections.unmodifiableCollection(Arrays.asList(PlayerType.values()));
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getID() + ")";
	}
}
