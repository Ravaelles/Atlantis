package jnibwapi;

import jnibwapi.Position.PosType;
import jnibwapi.Position.Positions;
import jnibwapi.types.UnitCommandType;
import jnibwapi.types.UnitCommandType.UnitCommandTypes;

/**
 * Represents a BWAPI UnitCommand. Used internally in JNIBWAPI to simplify the interface. Should not
 * be needed by client code.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/UnitCommand
 */
public class UnitCommand {
	private Unit unit = null;
	private UnitCommandType type = null;
	private Unit target = null;
	private Position targetPosition = Positions.None;
	/**
	 * UnitType/TechType/UpgradeType ID or slot number or queued depending on the UnitCommandType
	 */
	private int extra = 0;
	
	public UnitCommand(Unit u, UnitCommandType t) {
		unit = u;
		type = t;
	}
	
	public UnitCommand(Unit u, UnitCommandType t, int extra) {
		this(u, t);
		this.extra = extra;
	}
	
	public UnitCommand(Unit u, UnitCommandType t, boolean queued) {
		this(u, t, queued ? 1 : 0);
	}
	
	public UnitCommand(Unit u, UnitCommandType t, Unit target) {
		this(u, t);
		this.target = target;
	}
	
	public UnitCommand(Unit u, UnitCommandType t, Unit target, boolean queued) {
		this(u, t, queued);
		this.target = target;
	}
	
	public UnitCommand(Unit u, UnitCommandType t, Unit target, int extra) {
		this(u, t, extra);
		this.target = target;
	}
	
	public UnitCommand(Unit u, UnitCommandType t, Position targetPos) {
		this(u, t);
		targetPosition = targetPos.makeValid();
	}
	
	public UnitCommand(Unit u, UnitCommandType t, Position targetPos, boolean queued) {
		this(u, t, queued);
		targetPosition = targetPos.makeValid();
	}
	
	public UnitCommand(Unit u, UnitCommandType t, Position targetPos, int extra) {
		this(u, t, extra);
		targetPosition = targetPos.makeValid();
	}
	
	public Unit getUnit() {
		return unit;
	}
	
	
	public UnitCommandType getType() {
		return type;
	}
	
	public Unit getTarget() {
		return target;
	}
	
	public int getTargetUnitID() {
		if (target != null)
			return target.getID();
		return -1;
	}
	
	public Position getTargetPosition() {
		return targetPosition;
	}
	
	public int getX() {
		return targetPosition.getX(getPosType());
	}
	
	public int getY() {
		return targetPosition.getY(getPosType());
	}
	
	/** Necessary to differentiate commands which take tilePositions */
	public PosType getPosType() {
		if (type == UnitCommandTypes.Build ||
				type == UnitCommandTypes.Land ||
				type == UnitCommandTypes.Place_COP)
			return PosType.BUILD;
		return PosType.PIXEL;
	}
	
	public int getExtra() {
		return extra;
	}
	
}
