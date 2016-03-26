package atlantis.information;

import bwapi.Position;
import bwapi.PositionedObject;
import bwapi.Unit;
import bwapi.UnitType;

/**
 * Stores information about units in order to retrieve them when 
 * they are out of sight
 * @author Anderson
 *
 */
public class UnitData extends PositionedObject {

	private Position position;
	private Unit unit;
	private UnitType type, buildType;
	
	public UnitData(Unit u){
		unit = u;
		position = u.getPosition();
		type = u.getType();
		buildType = u.getBuildType();
	}
	
	public UnitType getType(){
		return type;
	}
	
	public UnitType getBuildType(){
		return buildType;
	}
	
	
	public Unit getUnit(){
		return unit;
	}
	
	public Position getPosition(){
		return position;
	}
	
	public UnitData update(Unit updated){
		if (updated.getID() != unit.getID()){
			throw new RuntimeException(
				String.format("Unexpected unit ID. Expected %d, received %d", unit.getID(), updated.getID())
			);
		}
		position = updated.getPosition();
		type = unit.getType();
		
		return this;
	}
}
