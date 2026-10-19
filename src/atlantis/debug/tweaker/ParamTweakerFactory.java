package atlantis.debug.tweaker;

import atlantis.battle.BattleCommander;
import atlantis.battle.avoid.margin.SafetyMarginAgainstMelee;
import atlantis.game.A;
import atlantis.game.ARandom;
import atlantis.util.AConsole;

public class ParamTweakerFactory extends ParamTweaker {

    public static void init() {
        AConsole.println("###### INIT ParamTweaker ######");
        tweaker = new ParamTweaker();

//        tweaker.addParam(new Param(
//                "ENEMIES_Near_FACTOR",
//                () -> SafetyMarginAgainstMelee.ENEMIES_Near_FACTOR,
//                () -> SafetyMarginAgainstMelee.ENEMIES_Near_FACTOR = ARandom.rand(0, 200) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "ENEMIES_Near_MAX_DIST",
//                () -> SafetyMarginAgainstMelee.ENEMIES_Near_MAX_DIST,
//                () -> SafetyMarginAgainstMelee.ENEMIES_Near_MAX_DIST = ARandom.rand(120, 260) / 100.0
//        ));
        tweaker.addParam(new Param(
                "SafetyMeleeInfantryBaseNoMedic",
                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_NO_MEDIC,
                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_NO_MEDIC = ARandom.rand(220, 250) / 100.0
        ));
        tweaker.addParam(new Param(
                "SafetyMeleeInfantryWoundNoMedic",
                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC,
                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = ARandom.rand(70, 85)
        ));
        // Kiting duel (owner: 1Drag_v_1Zeal on Wine): ranges explore
        // around the engine-faithful defaults, never blind.
        tweaker.addParam(new Param(
                "KiteEnemyTiles",
                () -> BattleCommander.KITE_ENEMY_TILES,
                () -> { applyOrRandomize("KiteEnemyTiles", 40, 60, 10.0, v -> BattleCommander.KITE_ENEMY_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteGlueTiles",
                () -> BattleCommander.KITE_GLUE_TILES,
                () -> { applyOrRandomize("KiteGlueTiles", 3, 8, 10.0, v -> BattleCommander.KITE_GLUE_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteStepTiles",
                () -> BattleCommander.KITE_STEP_TILES,
                () -> { applyOrRandomize("KiteStepTiles", 15, 30, 10.0, v -> BattleCommander.KITE_STEP_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteHuntedTiles",
                () -> BattleCommander.KITE_HUNTED_TILES,
                () -> { applyOrRandomize("KiteHuntedTiles", 0, 10, 10.0, v -> BattleCommander.KITE_HUNTED_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteFacedTiles",
                () -> BattleCommander.KITE_FACED_TILES,
                () -> { applyOrRandomize("KiteFacedTiles", 0, 10, 10.0, v -> BattleCommander.KITE_FACED_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginBaseTiles",
                () -> BattleCommander.KITE_MARGIN_BASE_TILES,
                () -> { applyOrRandomize("KiteMarginBaseTiles", 5, 15, 10.0, v -> BattleCommander.KITE_MARGIN_BASE_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginDyingTiles",
                () -> BattleCommander.KITE_MARGIN_DYING_TILES,
                () -> { applyOrRandomize("KiteMarginDyingTiles", 30, 50, 10.0, v -> BattleCommander.KITE_MARGIN_DYING_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginPerHitTiles",
                () -> BattleCommander.KITE_MARGIN_PER_HIT_TILES,
                () -> { applyOrRandomize("KiteMarginPerHitTiles", 15, 35, 100.0, v -> BattleCommander.KITE_MARGIN_PER_HIT_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteZealotPassDamage",
                () -> BattleCommander.KITE_ZEALOT_PASS_DAMAGE,
                () -> { applyOrRandomize("KiteZealotPassDamage", 140, 180, 10.0, v -> BattleCommander.KITE_ZEALOT_PASS_DAMAGE = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteStandoffInsetTiles",
                () -> BattleCommander.KITE_STANDOFF_INSET_TILES,
                () -> { applyOrRandomize("KiteStandoffInsetTiles", 0, 10, 10.0, v -> BattleCommander.KITE_STANDOFF_INSET_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteGlideDeadbandTiles",
                () -> BattleCommander.KITE_GLIDE_DEADBAND_TILES,
                () -> { applyOrRandomize("KiteGlideDeadbandTiles", 2, 8, 10.0, v -> BattleCommander.KITE_GLIDE_DEADBAND_TILES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteRideFrames",
                () -> BattleCommander.KITE_RIDE_FRAMES,
                () -> { applyOrRandomizeInt("KiteRideFrames", 20, 40, v -> BattleCommander.KITE_RIDE_FRAMES = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteWoundedHitsLeft",
                () -> BattleCommander.KITE_WOUNDED_HITS_LEFT,
                () -> { applyOrRandomize("KiteWoundedHitsLeft", 40, 80, 10.0, v -> BattleCommander.KITE_WOUNDED_HITS_LEFT = v); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteWoundedEdgeInsetTiles",
                () -> BattleCommander.KITE_WOUNDED_EDGE_INSET_TILES,
                () -> { applyOrRandomize("KiteWoundedEdgeInsetTiles", 2, 8, 10.0, v -> BattleCommander.KITE_WOUNDED_EDGE_INSET_TILES = v); return null; }
        ));
//        tweaker.addParam(new Param(
//                "SafetyMeleeInfantryBaseIfMedic",
//                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_MEDIC,
//                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_MEDIC = ARandom.rand(60, 164) / 100.0
////                () -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_MEDIC = ARandom.rand(175, 255) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "SafetyMeleeInfantryWoundIfMedic",
//                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_IF_MEDIC,
//                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_IF_MEDIC = ARandom.rand(19, 23)
////                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_IF_MEDIC = ARandom.rand(30, 99)
//        ));
//        tweaker.addParam(new Param(
//                "RunAnyDirectionRadiusInfantry",
//                () -> ARunningManager.ANY_DIRECTION_INIT_RADIUS_INFANTRY,
//                () -> ARunningManager.ANY_DIRECTION_INIT_RADIUS_INFANTRY = ARandom.rand(10, 60) / 10
//        ));
//        tweaker.addParam(new Param(
//                "RunNotifyUnitsInRadiusBase",
//                () -> ARunningManager.NOTIFY_UNITS_IN_RADIUS_BASE,
//                () -> ARunningManager.NOTIFY_UNITS_IN_RADIUS_BASE = ARandom.rand(70, 95) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "NearUnitMakeSpace",
//                () -> ARunningManager.Near_UNIT_MAKE_SPACE,
//                () -> ARunningManager.Near_UNIT_MAKE_SPACE = ARandom.rand(60, 90) / 100.0
//        ));
//        tweaker.addParam(new Param(
//                "STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO",
//                () -> ARunningManager.STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO,
//                () -> ARunningManager.STOP_RUNNING_IF_STARTED_RUNNING_MORE_THAN_AGO = ARandom.rand(0, 10)
//        ));
//        tweaker.addParam(new Param(
//                "MIN_DIST_TO_ASSIGNMENT",
//                () -> TerranMedic.MIN_DIST_TO_ASSIGNMENT,
//                () -> TerranMedic.MIN_DIST_TO_ASSIGNMENT = ARandom.rand(50, 200) / 100.0
//        ));

        tweaker.initParamValues();
    }

    private static void applyOrRandomize(String name, int min, int max, double div, java.util.function.DoubleConsumer setter) {
        String raw = atlantis.config.env.Env.rawEnv(name);
        double value;
        if (raw != null) {
            double parsed;
            try { parsed = Double.parseDouble(raw.trim()); }
            catch (NumberFormatException e) { parsed = ARandom.rand(min, max) / div; }
            value = parsed;
        }
        else {
            value = ARandom.rand(min, max) / div;
        }
        setter.accept(value);
        AConsole.println("ParamTweaker " + name + " = " + value + (raw != null ? " (ENV)" : " (random)"));
    }

    private static void applyOrRandomizeInt(String name, int min, int max, java.util.function.IntConsumer setter) {
        String raw = atlantis.config.env.Env.rawEnv(name);
        int value;
        if (raw != null) {
            int parsed;
            try { parsed = Integer.parseInt(raw.trim()); }
            catch (NumberFormatException e) { parsed = ARandom.rand(min, max); }
            value = parsed;
        }
        else {
            value = ARandom.rand(min, max);
        }
        setter.accept(value);
        AConsole.println("ParamTweaker " + name + " = " + value + (raw != null ? " (ENV)" : " (random)"));
    }
}
