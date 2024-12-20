package atlantis.units.actions;

/**
 * All possible unit action labels. Very useful for debugging.
 */
public class Actions {

    public static final Action ATTACK_POSITION = new Action(true).setName("ATTACK_POSITION");
    public static final Action ATTACK_UNIT = new Action(true).setName("ATTACK_UNIT");
    public static final Action BUILD = new Action().setName("BUILD");
    public static final Action BURROW = new Action().setName("BURROW");
    public static final Action CANCEL = new Action().setName("CANCEL");
    public static final Action CLOAK = new Action().setName("CLOAK");
    public static final Action DECLOAK = new Action().setName("DECLOAK");
    public static final Action GATHER_MINERALS = new Action().setName("GATHER_MINERALS");
    public static final Action GATHER_GAS = new Action().setName("GATHER_GAS");
    public static final Action INIT = new Action().setName("INIT");
    public static final Action INVALID = new Action().setName("INVALID");
    public static final Action HEAL = new Action().setName("HEAL");
    public static final Action HOLD_POSITION = new Action().setName("HOLD_POSITION");
    public static final Action LAND = new Action().setName("LAND");
    public static final Action LIFT = new Action().setName("LIFT");
    public static final Action LOAD = new Action().setName("LOAD");
    public static final Action MORPH = new Action().setName("MORPH");
    public static final Action MOVE_ADVANCE = new Action().setName("MOVE_ADVANCE");
    public static final Action MOVE_ATTACK = new Action().setName("MOVE_ATTACK");
    public static final Action MOVE_AVOID = new Action().setName("MOVE_AVOID");
    public static final Action MOVE_BUILD = new Action().setName("MOVE_BUILD");
    public static final Action MOVE_DANCE_AWAY = new Action().setName("MOVE_DANCE_AWAY");
    public static final Action MOVE_DANCE_TO = new Action().setName("MOVE_DANCE_TO");
    public static final Action MOVE_ENGAGE = new Action().setName("MOVE_ENGAGE");
    public static final Action MOVE_ERROR = new Action().setName("MOVE_ERROR");
    public static final Action MOVE_EXPLORE = new Action().setName("MOVE_EXPLORE");
    public static final Action MOVE_FOCUS = new Action().setName("MOVE_FOCUS");
    public static final Action MOVE_FOLLOW = new Action().setName("MOVE_FOLLOW");
    public static final Action MOVE_FORMATION = new Action().setName("MOVE_FORMATION");
    public static final Action MOVE_HEAL = new Action().setName("MOVE_HEAL");
    public static final Action MOVE_MACRO = new Action().setName("MOVE_MACRO");
    public static final Action MOVE_REPAIR = new Action().setName("MOVE_REPAIR");
    public static final Action MOVE_SAFETY = new Action().setName("MOVE_SAFETY");
    public static final Action MOVE_SCOUT = new Action().setName("MOVE_SCOUT");
    public static final Action MOVE_TRANSFER = new Action().setName("MOVE_TRANSFER");
    public static final Action MOVE_SPACE = new Action().setName("MOVE_SPACE");
    public static final Action SPECIAL = new Action().setName("MOVE_SPECIAL");
    public static final Action MOVE_UNFREEZE = new Action().setName("MOVE_UNFREEZE");
    public static final Action PATROL = new Action().setName("PATROL");
    public static final Action REPAIR = new Action().setName("REPAIR");
    //    public static final Action RETREAT = new Action(false, true).setName("RETREAT");
    public static final Action RETURN_CARGO = new Action().setName("RETURN_CARGO");

    // For the love of Aiur - dont use it unless there's no direct tech for it e.g. Protoss Recharge Shields
    public static final Action RIGHT_CLICK = new Action().setName("RIGHT_CLICK");

    //    public static final Action RUN_ = new Action(false, true).setName("RUN_");
    public static final Action RUN_ENEMY = new Action(false, true).setName("RUN_ENEMY");
    public static final Action RUN_ENEMIES = new Action(false, true).setName("RUN_ENEMIES");
    public static final Action RUN_IN_ANY_DIRECTION = new Action(false, true).setName("RUN_IN_ANY_DIR");
    public static final Action RUN_RETREAT = new Action(false, true).setName("RUN_RETREAT");
    public static final Action RESEARCH_OR_UPGRADE = new Action().setName("RESEARCH_OR_UPGRADE");
    public static final Action SIEGE = new Action().setName("SIEGE");
    public static final Action STOP = new Action().setName("STOP");
    public static final Action TRAIN = new Action().setName("TRAIN");
    public static final Action TRANSFER = new Action().setName("TRANSFER");
    public static final Action UNLOAD = new Action().setName("UNLOAD");
    public static final Action UNBURROW = new Action().setName("UNBURROW");
    public static final Action UNSIEGE = new Action().setName("UNSIEGE");
    public static final Action USING_TECH = new Action().setName("USING_TECH");

}
