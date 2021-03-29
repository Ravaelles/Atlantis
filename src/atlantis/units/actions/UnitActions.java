package atlantis.units.actions;

public class UnitActions {
    
    public static final UnitAction ATTACK_POSITION = new UnitAction(true).setName("ATTACK_POSITION");
    public static final UnitAction ATTACK_UNIT = new UnitAction(true).setName("ATTACK_UNIT");
    public static final UnitAction BUILD = new UnitAction().setName("BUILD");
    public static final UnitAction BURROW = new UnitAction().setName("BURROW");
    public static final UnitAction CLOAK = new UnitAction().setName("CLOAK");
    public static final UnitAction DECLOAK = new UnitAction().setName("DECLOAK");
//    public static final UnitAction ENGAGE = new UnitAction().setName("ENGAGE");
    public static final UnitAction EXPLORE = new UnitAction().setName("EXPLORE");
    public static final UnitAction FOLLOW = new UnitAction().setName("FOLLOW");
    public static final UnitAction GATHER_MINERALS = new UnitAction().setName("GATHER_MINERALS");
    public static final UnitAction GATHER_GAS = new UnitAction().setName("GATHER_GAS");
    public static final UnitAction HEAL = new UnitAction().setName("HEAL");
    public static final UnitAction HOLD_POSITION = new UnitAction().setName("HOLD_POSITION");
    public static final UnitAction LAND = new UnitAction().setName("LAND");
    public static final UnitAction LIFT = new UnitAction().setName("LIFT");
    public static final UnitAction LOAD = new UnitAction().setName("LOAD");
    public static final UnitAction MORPH = new UnitAction().setName("MORPH");
    public static final UnitAction MOVE = new UnitAction().setName("MOVE");
    public static final UnitAction MOVE_TO_BUILD = new UnitAction().setName("MOVE_TO_BUILD");
    public static final UnitAction MOVE_TO_ENGAGE = new UnitAction().setName("MOVE_TO_ENGAGE");
    public static final UnitAction MOVE_TO_REPAIR = new UnitAction().setName("MOVE_TO_REPAIR");
    public static final UnitAction PATROL = new UnitAction().setName("PATROL");
    public static final UnitAction REPAIR = new UnitAction().setName("REPAIR");
    public static final UnitAction RETREAT = new UnitAction(false, true).setName("RETREAT");
    public static final UnitAction RUN = new UnitAction(false, true).setName("RUN");
    public static final UnitAction RESEARCH_OR_UPGRADE = new UnitAction().setName("RESEARCH_OR_UPGRADE");
    public static final UnitAction SIEGE = new UnitAction().setName("SIEGE");
//    public static final UnitAction STICK_CLOSER = new UnitAction().setName("");
    public static final UnitAction STOP = new UnitAction().setName("STOP");
    public static final UnitAction TOGETHER = new UnitAction().setName("TOGETHER");
    public static final UnitAction TRAIN = new UnitAction().setName("TRAIN");
    public static final UnitAction UNLOAD = new UnitAction().setName("UNLOAD");
    public static final UnitAction UNBURROW = new UnitAction().setName("UNBURROW");
    public static final UnitAction UNSIEGE = new UnitAction().setName("UNSIEGE");
    public static final UnitAction USING_TECH = new UnitAction().setName("USING_TECH");
    
}
