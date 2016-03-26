package bwapi;


public class UnitCommand {

    public static native UnitCommand attack(Unit unit, PositionOrUnit target);

    public static native UnitCommand attack(Unit unit, PositionOrUnit target, boolean shiftQueueCommand);

    public static native UnitCommand build(Unit unit, TilePosition target, UnitType type);

    public static native UnitCommand buildAddon(Unit unit, UnitType type);

    public static native UnitCommand train(Unit unit, UnitType type);

    public static native UnitCommand morph(Unit unit, UnitType type);

    public static native UnitCommand research(Unit unit, TechType tech);

    public static native UnitCommand upgrade(Unit unit, UpgradeType upgrade);

    public static native UnitCommand setRallyPoint(Unit unit, PositionOrUnit target);

    public static native UnitCommand move(Unit unit, Position target);

    public static native UnitCommand move(Unit unit, Position target, boolean shiftQueueCommand);

    public static native UnitCommand patrol(Unit unit, Position target);

    public static native UnitCommand patrol(Unit unit, Position target, boolean shiftQueueCommand);

    public static native UnitCommand holdPosition(Unit unit);

    public static native UnitCommand holdPosition(Unit unit, boolean shiftQueueCommand);

    public static native UnitCommand stop(Unit unit);

    public static native UnitCommand stop(Unit unit, boolean shiftQueueCommand);

    public static native UnitCommand follow(Unit unit, Unit target);

    public static native UnitCommand follow(Unit unit, Unit target, boolean shiftQueueCommand);

    public static native UnitCommand gather(Unit unit, Unit target);

    public static native UnitCommand gather(Unit unit, Unit target, boolean shiftQueueCommand);

    public static native UnitCommand returnCargo(Unit unit);

    public static native UnitCommand returnCargo(Unit unit, boolean shiftQueueCommand);

    public static native UnitCommand repair(Unit unit, Unit target);

    public static native UnitCommand repair(Unit unit, Unit target, boolean shiftQueueCommand);

    public static native UnitCommand burrow(Unit unit);

    public static native UnitCommand unburrow(Unit unit);

    public static native UnitCommand cloak(Unit unit);

    public static native UnitCommand decloak(Unit unit);

    public static native UnitCommand siege(Unit unit);

    public static native UnitCommand unsiege(Unit unit);

    public static native UnitCommand lift(Unit unit);

    public static native UnitCommand land(Unit unit, TilePosition target);

    public static native UnitCommand load(Unit unit, Unit target);

    public static native UnitCommand load(Unit unit, Unit target, boolean shiftQueueCommand);

    public static native UnitCommand unload(Unit unit, Unit target);

    public static native UnitCommand unloadAll(Unit unit);

    public static native UnitCommand unloadAll(Unit unit, boolean shiftQueueCommand);

    public static native UnitCommand unloadAll(Unit unit, Position target);

    public static native UnitCommand unloadAll(Unit unit, Position target, boolean shiftQueueCommand);

    public static native UnitCommand rightClick(Unit unit, PositionOrUnit target);

    public static native UnitCommand rightClick(Unit unit, PositionOrUnit target, boolean shiftQueueCommand);

    public static native UnitCommand haltConstruction(Unit unit);

    public static native UnitCommand cancelConstruction(Unit unit);

    public static native UnitCommand cancelAddon(Unit unit);

    public static native UnitCommand cancelTrain(Unit unit);

    public static native UnitCommand cancelTrain(Unit unit, int slot);

    public static native UnitCommand cancelMorph(Unit unit);

    public static native UnitCommand cancelResearch(Unit unit);

    public static native UnitCommand cancelUpgrade(Unit unit);

    public static native UnitCommand useTech(Unit unit, TechType tech);

    public static native UnitCommand useTech(Unit unit, TechType tech, PositionOrUnit target);

    public static native UnitCommand placeCOP(Unit unit, TilePosition target);

    private Unit unit;

    private UnitCommandType unitCommandType;

    private Unit target;

    private int x, y;

    private int extra;

    private UnitCommand(Unit unit, UnitCommandType unitCommandType, Unit target, int x, int y, int extra) {
        this.unit = unit;
        this.unitCommandType = unitCommandType;
        this.target = target;
        this.x = x;
        this.y = y;
        this.extra = extra;
    }

    public Unit getUnit() {
        return unit;
    }

    public UnitCommandType getUnitCommandType() {
        return unitCommandType;
    }

    public Unit getTarget() {
        return target;
    }



    public int getSlot() {
        if (unitCommandType == UnitCommandType.None) {
            return extra;
        }
        return -1;
    }

    public Position getTargetPosition() {
        if (unitCommandType == UnitCommandType.Build ||
                unitCommandType == UnitCommandType.Land ||
                unitCommandType == UnitCommandType.Place_COP) {
            return new Position(x * 32, y * 32);
        }
        return new Position(x, y);
    }

    public TilePosition getTargetTilePosition() {
        if (unitCommandType == UnitCommandType.Build ||
                unitCommandType == UnitCommandType.Land ||
                unitCommandType == UnitCommandType.Place_COP) {
            return new TilePosition(x, y);
        }
        return new TilePosition(x / 32, y / 32);
    }

    public boolean isQueued() {
        if (unitCommandType == UnitCommandType.Attack_Move ||
                unitCommandType == UnitCommandType.Attack_Unit ||
                unitCommandType == UnitCommandType.Move ||
                unitCommandType == UnitCommandType.Patrol ||
                unitCommandType == UnitCommandType.Hold_Position ||
                unitCommandType == UnitCommandType.Stop ||
                unitCommandType == UnitCommandType.Follow ||
                unitCommandType == UnitCommandType.Gather ||
                unitCommandType == UnitCommandType.Return_Cargo ||
                unitCommandType == UnitCommandType.Repair ||
                unitCommandType == UnitCommandType.Load ||
                unitCommandType == UnitCommandType.Unload_All ||
                unitCommandType == UnitCommandType.Unload_All_Position ||
                unitCommandType == UnitCommandType.Right_Click_Position ||
                unitCommandType == UnitCommandType.Right_Click_Unit) {
            return extra != 0;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitCommand)) return false;

        UnitCommand that = (UnitCommand) o;

        if (extra != that.extra) return false;
        if (x != that.x) return false;
        if (y != that.y) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) return false;
        if (unitCommandType != null ? !unitCommandType.equals(that.unitCommandType) : that.unitCommandType != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = unit != null ? unit.hashCode() : 0;
        result = 31 * result + (unitCommandType != null ? unitCommandType.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + extra;
        return result;
    }
}