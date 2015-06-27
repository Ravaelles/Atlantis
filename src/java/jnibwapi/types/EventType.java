package jnibwapi.types;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Represents a StarCraft event type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/Event
 */
public enum EventType {
	MatchStart,
	MatchEnd,
	MatchFrame,
	MenuFrame,
	SendText,
	ReceiveText,
	PlayerLeft,
	NukeDetect,
	UnitDiscover,
	UnitEvade,
	UnitShow,
	UnitHide,
	UnitCreate,
	UnitDestroy,
	UnitMorph,
	UnitRenegade,
	SaveGame,
	UnitComplete,
	// TriggerAction,
	PlayerDropped, // Will be removed in later versions of BWAPI
	None;
	public int getID() {
		return ordinal();
	}
	
	public String getName() {
		return name();
	}
	
	public static EventType getEventType(int id) {
		return EventType.values()[id];
	}
	
	public static Collection<EventType> getAllEventTypes() {
		return Collections.unmodifiableCollection(Arrays.asList(EventType.values()));
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getID() + ")";
	}
}
