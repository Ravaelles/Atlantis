package atlantis.debug.tweaker;

import atlantis.battle.kite.CombatTuning;
import atlantis.battle.escape.EscapeTuning;

import atlantis.battle.avoid.margin.SafetyMarginAgainstMelee;
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
        // Escape scoring (NEXT #172 step 5): weights explore around the
        // hand-tuned scoring constants, never blind. Radii and the pocket
        // definition stay structural (candidate count, pocket meaning).
        tweaker.addParam(new Param(
                "EscapeStaticWeight",
                () -> EscapeTuning.current().getStaticWeight(),
                () -> { applyOrRandomize("EscapeStaticWeight", 50, 150, 10.0, v -> EscapeTuning.install(EscapeTuning.current().withStaticWeight(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "EscapeCornerWeight",
                () -> EscapeTuning.current().getCornerWeight(),
                () -> { applyOrRandomize("EscapeCornerWeight", 40, 120, 10.0, v -> EscapeTuning.install(EscapeTuning.current().withCornerWeight(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "EscapePocketExtra",
                () -> EscapeTuning.current().getPocketExtra(),
                () -> { applyOrRandomize("EscapePocketExtra", 100, 300, 10.0, v -> EscapeTuning.install(EscapeTuning.current().withPocketExtra(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "EscapeEnemyWeight",
                () -> EscapeTuning.current().getEnemyWeight(),
                () -> { applyOrRandomize("EscapeEnemyWeight", 20, 80, 10.0, v -> EscapeTuning.install(EscapeTuning.current().withEnemyWeight(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "EscapeStaticRadiusTiles",
                () -> EscapeTuning.current().getStaticRadiusTiles(),
                () -> { applyOrRandomize("EscapeStaticRadiusTiles", 20, 40, 10.0, v -> EscapeTuning.install(EscapeTuning.current().withStaticRadiusTiles(v))); return null; }
        ));
        tweaker.addParam(new Param(
                "EscapeEnemyRadiusTiles",
                () -> EscapeTuning.current().getEnemyRadiusTiles(),
                () -> { applyOrRandomize("EscapeEnemyRadiusTiles", 150, 250, 10.0, v -> EscapeTuning.install(EscapeTuning.current().withEnemyRadiusTiles(v))); return null; }
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
     * Deterministic by-value application of one registered param, in game
     * units (the same units bounds(), the env files and the tuner
     * fragments use - the factory's int ranges are raw and divided only
     * on the random path). The sim batch runner's only write path: the
     * wine batch goes through per-game env files instead. Unknown names
     * fail loudly - a silent random would tune a different vector than
     * the manifest records.
     */
    public static void apply(String name, double gameUnitsValue) {
        switch (name) {
            case "SafetyMeleeInfantryBaseNoMedic":
                SafetyMarginAgainstMelee.INFANTRY_BASE_IF_NO_MEDIC = gameUnitsValue;
                return;

            case "SafetyMeleeInfantryWoundNoMedic":
                SafetyMarginAgainstMelee.INFANTRY_WOUND_MODIFIER_WITHOUT_MEDIC = (int) Math.round(gameUnitsValue);
                return;

            case "KiteEnemyTiles":
                CombatTuning.install(CombatTuning.current().withKiteEnemyTiles(gameUnitsValue));
                return;

            case "KiteGlueTiles":
                CombatTuning.install(CombatTuning.current().withKiteGlueTiles(gameUnitsValue));
                return;

            case "KiteStepTiles":
                CombatTuning.install(CombatTuning.current().withKiteStepTiles(gameUnitsValue));
                return;

            case "KiteHuntedTiles":
                CombatTuning.install(CombatTuning.current().withKiteHuntedTiles(gameUnitsValue));
                return;

            case "KiteFacedTiles":
                CombatTuning.install(CombatTuning.current().withKiteFacedTiles(gameUnitsValue));
                return;

            case "KiteMarginBaseTiles":
                CombatTuning.install(CombatTuning.current().withKiteMarginBaseTiles(gameUnitsValue));
                return;

            case "KiteMarginDyingTiles":
                CombatTuning.install(CombatTuning.current().withKiteMarginDyingTiles(gameUnitsValue));
                return;

            case "KiteMarginPerHitTiles":
                CombatTuning.install(CombatTuning.current().withKiteMarginPerHitTiles(gameUnitsValue));
                return;

            case "KiteZealotPassDamage":
                CombatTuning.install(CombatTuning.current().withKiteZealotPassDamage(gameUnitsValue));
                return;

            case "KiteStandoffInsetTiles":
                CombatTuning.install(CombatTuning.current().withKiteStandoffInsetTiles(gameUnitsValue));
                return;

            case "KiteGlideDeadbandTiles":
                CombatTuning.install(CombatTuning.current().withKiteGlideDeadbandTiles(gameUnitsValue));
                return;

            case "KiteRideFrames":
                CombatTuning.install(CombatTuning.current().withKiteRideFrames((int) Math.round(gameUnitsValue)));
                return;

            case "KiteWoundedHitsLeft":
                CombatTuning.install(CombatTuning.current().withKiteWoundedHitsLeft(gameUnitsValue));
                return;

            case "KiteWoundedEdgeInsetTiles":
                CombatTuning.install(CombatTuning.current().withKiteWoundedEdgeInsetTiles(gameUnitsValue));
                return;

            case "KiteStickyChaseTiles":
                CombatTuning.install(CombatTuning.current().withKiteStickyChaseTiles(gameUnitsValue));
                return;

            case "DragoonLowHp":
                CombatTuning.install(CombatTuning.current().withDragoonLowHp((int) Math.round(gameUnitsValue)));
                return;

            case "DragoonLowHpHoldTiles":
                CombatTuning.install(CombatTuning.current().withDragoonLowHpHoldTiles(gameUnitsValue));
                return;

            case "DragoonLowHpFireCooldown":
                CombatTuning.install(CombatTuning.current().withDragoonLowHpFireCooldown((int) Math.round(gameUnitsValue)));
                return;

            case "DragoonSurroundedMeleeCount":
                CombatTuning.install(CombatTuning.current().withDragoonSurroundedMeleeCount((int) Math.round(gameUnitsValue)));
                return;

            case "DragoonPinchHp":
                CombatTuning.install(CombatTuning.current().withDragoonPinchHp((int) Math.round(gameUnitsValue)));
                return;

            case "DragoonPinchMeleeCount":
                CombatTuning.install(CombatTuning.current().withDragoonPinchMeleeCount((int) Math.round(gameUnitsValue)));
                return;

            case "DragoonPinchMeleeTiles":
                CombatTuning.install(CombatTuning.current().withDragoonPinchMeleeTiles(gameUnitsValue));
                return;

            case "DragoonSurroundedStandCooldown":
                CombatTuning.install(CombatTuning.current().withDragoonSurroundedStandCooldown((int) Math.round(gameUnitsValue)));
                return;

            case "DragoonSurroundedStandDistTiles":
                CombatTuning.install(CombatTuning.current().withDragoonSurroundedStandDistTiles(gameUnitsValue));
                return;

            case "EscapeStaticWeight":
                EscapeTuning.install(EscapeTuning.current().withStaticWeight(gameUnitsValue));
                return;

            case "EscapeCornerWeight":
                EscapeTuning.install(EscapeTuning.current().withCornerWeight(gameUnitsValue));
                return;

            case "EscapePocketExtra":
                EscapeTuning.install(EscapeTuning.current().withPocketExtra(gameUnitsValue));
                return;

            case "EscapeEnemyWeight":
                EscapeTuning.install(EscapeTuning.current().withEnemyWeight(gameUnitsValue));
                return;

            case "EscapeStaticRadiusTiles":
                EscapeTuning.install(EscapeTuning.current().withStaticRadiusTiles(gameUnitsValue));
                return;

            case "EscapeEnemyRadiusTiles":
                EscapeTuning.install(EscapeTuning.current().withEnemyRadiusTiles(gameUnitsValue));
                return;

            default:
                throw new IllegalArgumentException("unregistered tuner param: " + name);
        }
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
        bounds.put("EscapeStaticWeight", new double[] { 5.0, 15.0 });
        bounds.put("EscapeCornerWeight", new double[] { 4.0, 12.0 });
        bounds.put("EscapePocketExtra", new double[] { 10.0, 30.0 });
        bounds.put("EscapeEnemyWeight", new double[] { 2.0, 8.0 });
        bounds.put("EscapeStaticRadiusTiles", new double[] { 2.0, 4.0 });
        bounds.put("EscapeEnemyRadiusTiles", new double[] { 15.0, 25.0 });
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
