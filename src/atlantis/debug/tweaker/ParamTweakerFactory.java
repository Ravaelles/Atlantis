package atlantis.debug.tweaker;

import atlantis.battle.kite.CombatTuning;

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
                () -> CombatTuning.current().getKiteEnemyTiles(),
                () -> { applyOrRandomize("KiteEnemyTiles", 40, 60, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteEnemyTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteGlueTiles",
                () -> CombatTuning.current().getKiteGlueTiles(),
                () -> { applyOrRandomize("KiteGlueTiles", 3, 8, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteGlueTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteStepTiles",
                () -> CombatTuning.current().getKiteStepTiles(),
                () -> { applyOrRandomize("KiteStepTiles", 15, 30, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteStepTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteHuntedTiles",
                () -> CombatTuning.current().getKiteHuntedTiles(),
                () -> { applyOrRandomize("KiteHuntedTiles", 0, 10, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteHuntedTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteFacedTiles",
                () -> CombatTuning.current().getKiteFacedTiles(),
                () -> { applyOrRandomize("KiteFacedTiles", 0, 10, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteFacedTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginBaseTiles",
                () -> CombatTuning.current().getKiteMarginBaseTiles(),
                () -> { applyOrRandomize("KiteMarginBaseTiles", 5, 15, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteMarginBaseTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginDyingTiles",
                () -> CombatTuning.current().getKiteMarginDyingTiles(),
                () -> { applyOrRandomize("KiteMarginDyingTiles", 30, 50, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteMarginDyingTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteMarginPerHitTiles",
                () -> CombatTuning.current().getKiteMarginPerHitTiles(),
                () -> { applyOrRandomize("KiteMarginPerHitTiles", 15, 35, 100.0, v -> CombatTuning.install(CombatTuning.current().withKiteMarginPerHitTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteZealotPassDamage",
                () -> CombatTuning.current().getKiteZealotPassDamage(),
                () -> { applyOrRandomize("KiteZealotPassDamage", 140, 180, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteZealotPassDamage(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteStandoffInsetTiles",
                () -> CombatTuning.current().getKiteStandoffInsetTiles(),
                () -> { applyOrRandomize("KiteStandoffInsetTiles", 0, 10, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteStandoffInsetTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteGlideDeadbandTiles",
                () -> CombatTuning.current().getKiteGlideDeadbandTiles(),
                () -> { applyOrRandomize("KiteGlideDeadbandTiles", 2, 8, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteGlideDeadbandTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteRideFrames",
                () -> CombatTuning.current().getKiteRideFrames(),
                () -> { applyOrRandomizeInt("KiteRideFrames", 20, 40, v -> CombatTuning.install(CombatTuning.current().withKiteRideFrames(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteWoundedHitsLeft",
                () -> CombatTuning.current().getKiteWoundedHitsLeft(),
                () -> { applyOrRandomize("KiteWoundedHitsLeft", 40, 80, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteWoundedHitsLeft(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteWoundedEdgeInsetTiles",
                () -> CombatTuning.current().getKiteWoundedEdgeInsetTiles(),
                () -> { applyOrRandomize("KiteWoundedEdgeInsetTiles", 2, 8, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteWoundedEdgeInsetTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "KiteStickyChaseTiles",
                () -> CombatTuning.current().getKiteStickyChaseTiles(),
                () -> { applyOrRandomize("KiteStickyChaseTiles", 5, 15, 10.0, v -> CombatTuning.install(CombatTuning.current().withKiteStickyChaseTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonLowHp",
                () -> CombatTuning.current().getDragoonLowHp(),
                () -> { applyOrRandomizeInt("DragoonLowHp", 30, 60, v -> CombatTuning.install(CombatTuning.current().withDragoonLowHp(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonLowHpHoldTiles",
                () -> CombatTuning.current().getDragoonLowHpHoldTiles(),
                () -> { applyOrRandomize("DragoonLowHpHoldTiles", 20, 40, 10.0, v -> CombatTuning.install(CombatTuning.current().withDragoonLowHpHoldTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonLowHpFireCooldown",
                () -> CombatTuning.current().getDragoonLowHpFireCooldown(),
                () -> { applyOrRandomizeInt("DragoonLowHpFireCooldown", 3, 8, v -> CombatTuning.install(CombatTuning.current().withDragoonLowHpFireCooldown(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonSurroundedMeleeCount",
                () -> CombatTuning.current().getDragoonSurroundedMeleeCount(),
                () -> { applyOrRandomizeInt("DragoonSurroundedMeleeCount", 2, 4, v -> CombatTuning.install(CombatTuning.current().withDragoonSurroundedMeleeCount(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonPinchHp",
                () -> CombatTuning.current().getDragoonPinchHp(),
                () -> { applyOrRandomizeInt("DragoonPinchHp", 60, 100, v -> CombatTuning.install(CombatTuning.current().withDragoonPinchHp(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonPinchMeleeCount",
                () -> CombatTuning.current().getDragoonPinchMeleeCount(),
                () -> { applyOrRandomizeInt("DragoonPinchMeleeCount", 1, 3, v -> CombatTuning.install(CombatTuning.current().withDragoonPinchMeleeCount(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonPinchMeleeTiles",
                () -> CombatTuning.current().getDragoonPinchMeleeTiles(),
                () -> { applyOrRandomize("DragoonPinchMeleeTiles", 15, 25, 10.0, v -> CombatTuning.install(CombatTuning.current().withDragoonPinchMeleeTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonSurroundedStandCooldown",
                () -> CombatTuning.current().getDragoonSurroundedStandCooldown(),
                () -> { applyOrRandomizeInt("DragoonSurroundedStandCooldown", 4, 8, v -> CombatTuning.install(CombatTuning.current().withDragoonSurroundedStandCooldown(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "DragoonSurroundedStandDistTiles",
                () -> CombatTuning.current().getDragoonSurroundedStandDistTiles(),
                () -> { applyOrRandomize("DragoonSurroundedStandDistTiles", 20, 40, 10.0, v -> CombatTuning.install(CombatTuning.current().withDragoonSurroundedStandDistTiles(v))); return null; }
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

    /**
     * The single source of truth for tuner param bounds (NEXT #160, step 6):
     * the same [min, max] the registration lambdas above randomize in
     * (values already divided - these are game units, not raw ints).
     * The gradient proposer and the batch loop read this table instead of
     * re-spelling ranges; adding a param here without registering it above
     * (or vice versa) is a bug, pinned by the registry test.
     */
    public static java.util.Map<String, double[]> bounds() {
        java.util.LinkedHashMap<String, double[]> bounds = new java.util.LinkedHashMap<>();
        bounds.put("SafetyMeleeInfantryBaseNoMedic", new double[] { 2.2, 2.5 });
        bounds.put("SafetyMeleeInfantryWoundNoMedic", new double[] { 70, 85 });
        bounds.put("KiteEnemyTiles", new double[] { 4.0, 6.0 });
        bounds.put("KiteGlueTiles", new double[] { 0.3, 0.8 });
        bounds.put("KiteStepTiles", new double[] { 1.5, 3.0 });
        bounds.put("KiteHuntedTiles", new double[] { 0.0, 1.0 });
        bounds.put("KiteFacedTiles", new double[] { 0.0, 1.0 });
        bounds.put("KiteMarginBaseTiles", new double[] { 0.5, 1.5 });
        bounds.put("KiteMarginDyingTiles", new double[] { 3.0, 5.0 });
        bounds.put("KiteMarginPerHitTiles", new double[] { 0.15, 0.35 });
        bounds.put("KiteZealotPassDamage", new double[] { 14.0, 18.0 });
        bounds.put("KiteStandoffInsetTiles", new double[] { 0.0, 1.0 });
        bounds.put("KiteGlideDeadbandTiles", new double[] { 0.2, 0.8 });
        bounds.put("KiteRideFrames", new double[] { 20, 40 });
        bounds.put("KiteWoundedHitsLeft", new double[] { 4.0, 8.0 });
        bounds.put("KiteWoundedEdgeInsetTiles", new double[] { 0.2, 0.8 });
        bounds.put("KiteStickyChaseTiles", new double[] { 0.5, 1.5 });
        bounds.put("DragoonLowHp", new double[] { 30, 60 });
        bounds.put("DragoonLowHpHoldTiles", new double[] { 2.0, 4.0 });
        bounds.put("DragoonLowHpFireCooldown", new double[] { 3, 8 });
        bounds.put("DragoonSurroundedMeleeCount", new double[] { 2, 4 });
        bounds.put("DragoonPinchHp", new double[] { 60, 100 });
        bounds.put("DragoonPinchMeleeCount", new double[] { 1, 3 });
        bounds.put("DragoonPinchMeleeTiles", new double[] { 1.5, 2.5 });
        bounds.put("DragoonSurroundedStandCooldown", new double[] { 4, 8 });
        bounds.put("DragoonSurroundedStandDistTiles", new double[] { 2.0, 4.0 });
        return bounds;
    }

    /**
     * Params rendered without decimals in env/tweaker rows (the factory
     * parses them with {@code Integer.parseInt}, which rejects "49.0" and
     * silently falls back to random - so the proposer must render integral
     * values for exactly these names).
     */
    public static java.util.Set<String> integerParams() {
        java.util.Set<String> ints = new java.util.HashSet<>();
        ints.add("SafetyMeleeInfantryWoundNoMedic");
        ints.add("KiteRideFrames");
        ints.add("DragoonLowHp");
        ints.add("DragoonLowHpFireCooldown");
        ints.add("DragoonSurroundedMeleeCount");
        ints.add("DragoonPinchHp");
        ints.add("DragoonPinchMeleeCount");
        ints.add("DragoonSurroundedStandCooldown");
        return ints;
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
