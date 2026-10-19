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
                () -> { applyOrRandomize("SafetyMeleeInfantryBaseNoMedic", 220, 250, 100.0, v -> SafetyMarginAgainstMelee.INFANTRY_BASE_IF_NO_MEDIC = v); return null; }
        ));
        tweaker.addParam(new Param(
                "SafetyMeleeInfantryWoundNoMedic",
                () -> SafetyMarginAgainstMelee.INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC,
                () -> { applyOrRandomizeInt("SafetyMeleeInfantryWoundNoMedic", 70, 85, v -> SafetyMarginAgainstMelee.INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = v); return null; }
        ));
        // Kiting duel (owner: 1Drag_v_1Zeal on Wine): ranges explore
        // around the engine-faithful defaults, never blind.
        tweaker.addParam(new Param(
                "KiteEnemyTiles",
                () -> BattleCommander.tuning().getKiteEnemyTiles(),
                () -> { applyOrRandomize("KiteEnemyTiles", 40, 60, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteEnemyTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteGlueTiles",
                () -> BattleCommander.tuning().getKiteGlueTiles(),
                () -> { applyOrRandomize("KiteGlueTiles", 3, 8, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteGlueTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteStepTiles",
                () -> BattleCommander.tuning().getKiteStepTiles(),
                () -> { applyOrRandomize("KiteStepTiles", 15, 30, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteStepTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteHuntedTiles",
                () -> BattleCommander.tuning().getKiteHuntedTiles(),
                () -> { applyOrRandomize("KiteHuntedTiles", 0, 10, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteHuntedTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteFacedTiles",
                () -> BattleCommander.tuning().getKiteFacedTiles(),
                () -> { applyOrRandomize("KiteFacedTiles", 0, 10, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteFacedTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginBaseTiles",
                () -> BattleCommander.tuning().getKiteMarginBaseTiles(),
                () -> { applyOrRandomize("KiteMarginBaseTiles", 5, 15, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteMarginBaseTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginDyingTiles",
                () -> BattleCommander.tuning().getKiteMarginDyingTiles(),
                () -> { applyOrRandomize("KiteMarginDyingTiles", 30, 50, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteMarginDyingTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginPerHitTiles",
                () -> BattleCommander.tuning().getKiteMarginPerHitTiles(),
                () -> { applyOrRandomize("KiteMarginPerHitTiles", 15, 35, 100.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteMarginPerHitTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteZealotPassDamage",
                () -> BattleCommander.tuning().getKiteZealotPassDamage(),
                () -> { applyOrRandomize("KiteZealotPassDamage", 140, 180, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteZealotPassDamage(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteStandoffInsetTiles",
                () -> BattleCommander.tuning().getKiteStandoffInsetTiles(),
                () -> { applyOrRandomize("KiteStandoffInsetTiles", 0, 10, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteStandoffInsetTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteGlideDeadbandTiles",
                () -> BattleCommander.tuning().getKiteGlideDeadbandTiles(),
                () -> { applyOrRandomize("KiteGlideDeadbandTiles", 2, 8, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteGlideDeadbandTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteRideFrames",
                () -> BattleCommander.tuning().getKiteRideFrames(),
                () -> { applyOrRandomizeInt("KiteRideFrames", 20, 40, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteRideFrames(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteWoundedHitsLeft",
                () -> BattleCommander.tuning().getKiteWoundedHitsLeft(),
                () -> { applyOrRandomize("KiteWoundedHitsLeft", 40, 80, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteWoundedHitsLeft(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteWoundedEdgeInsetTiles",
                () -> BattleCommander.tuning().getKiteWoundedEdgeInsetTiles(),
                () -> { applyOrRandomize("KiteWoundedEdgeInsetTiles", 2, 8, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteWoundedEdgeInsetTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteStickyChaseTiles",
                () -> BattleCommander.tuning().getKiteStickyChaseTiles(),
                () -> { applyOrRandomize("KiteStickyChaseTiles", 5, 15, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withKiteStickyChaseTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonLowHp",
                () -> BattleCommander.tuning().getDragoonLowHp(),
                () -> { applyOrRandomizeInt("DragoonLowHp", 30, 60, v -> BattleCommander.installTuning(BattleCommander.tuning().withDragoonLowHp(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonLowHpHoldTiles",
                () -> BattleCommander.tuning().getDragoonLowHpHoldTiles(),
                () -> { applyOrRandomize("DragoonLowHpHoldTiles", 20, 40, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withDragoonLowHpHoldTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonLowHpFireCooldown",
                () -> BattleCommander.tuning().getDragoonLowHpFireCooldown(),
                () -> { applyOrRandomizeInt("DragoonLowHpFireCooldown", 3, 8, v -> BattleCommander.installTuning(BattleCommander.tuning().withDragoonLowHpFireCooldown(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonSurroundedMeleeCount",
                () -> BattleCommander.tuning().getDragoonSurroundedMeleeCount(),
                () -> { applyOrRandomizeInt("DragoonSurroundedMeleeCount", 2, 4, v -> BattleCommander.installTuning(BattleCommander.tuning().withDragoonSurroundedMeleeCount(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonSurroundedStandCooldown",
                () -> BattleCommander.tuning().getDragoonSurroundedStandCooldown(),
                () -> { applyOrRandomizeInt("DragoonSurroundedStandCooldown", 4, 8, v -> BattleCommander.installTuning(BattleCommander.tuning().withDragoonSurroundedStandCooldown(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonSurroundedStandDistTiles",
                () -> BattleCommander.tuning().getDragoonSurroundedStandDistTiles(),
                () -> { applyOrRandomize("DragoonSurroundedStandDistTiles", 20, 40, 10.0, v -> BattleCommander.installTuning(BattleCommander.tuning().withDragoonSurroundedStandDistTiles(v))); return null; }
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
