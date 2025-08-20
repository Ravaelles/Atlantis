//package atlantis.combat.micro.generic.managers;
//
//import atlantis.architecture.Manager;
//import atlantis.combat.advance.focus.AFocusPoint;
//import atlantis.combat.squad.squads.alpha.Alpha;
//import atlantis.game.A;
//import atlantis.information.enemy.EnemyUnits;
//import atlantis.map.position.HasPosition;
//import atlantis.units.AUnit;
//import atlantis.units.actions.Actions;
//import atlantis.units.select.Selection;
//import atlantis.util.log.ErrorLog;
//
//public class IdleFollowAlphaLeader extends Manager {
//    private HasPosition followPoint;
//
//    public IdleFollowAlphaLeader(AUnit unit) {
//        super(unit);
//    }
//
//    @Override
//    public boolean applies() {
//        if (true) return false;
//
////        if (unit.isLeader()) return false;
//        if (unit.isIdle() || unit.lastPositionChangedAgo() >= 60) return true;
//
//        followPoint = followPoint();
//        if (followPoint != null && followPoint.hasPosition() && followPoint.isWalkable()) {
//            return true;
//        }
//
//        return false;
//    }
//
//    private HasPosition followPoint() {
//        AUnit leader = Alpha.get().leader();
//        if (leader == null || leader.isDead() || unit.isLeader()) return null;
//
//        HasPosition basePoint = leader;
//        AFocusPoint focus = leader.mission().focusPoint();
////        if (focus == null) return leader;
////
////        HasPosition towards = unit.squadCenter();
////        if (towards == null) return leader;
//
//        if (focus != null) {
////            basePoint = basePoint.translatePercentTowards(50, focus);
//            basePoint = basePoint.translateTilesTowards(10, focus);
//        }
//
//        Selection enemies = EnemyUnits.discovered().inRadius(25, unit);
//
//        AUnit nearestEnemy = enemies.nearestTo(basePoint);
//        if (nearestEnemy != null) basePoint = basePoint.translateTilesTowards(9, focus);
//
////        AAdvancedPainter.paintCircleFilled(towards, 16, Color.Orange);
//
//        AUnit undetected = enemies.havingWeapon().effUndetected().first();
//        if (undetected != null && undetected.hasPosition()) {
//            basePoint = basePoint.translatePercentTowards(undetected, 85);
//        }
//        else {
//            AUnit cloakable = enemies.havingWeapon().cloakable().first();
//            if (cloakable != null && cloakable.hasPosition()) {
//                basePoint = basePoint.translatePercentTowards(cloakable, 85);
//            }
//        }
//
//        if (leader.distTo(basePoint) > 12) {
//            basePoint = leader.translateTilesTowards(12, basePoint);
//        }
//
//        return basePoint;
//    }
//
//    public Manager handle() {
//        if (followPoint == null || !followPoint.hasPosition()) {
//            return null;
//        }
//        if (unit.distTo(followPoint) < 0.5) return null;
//
//        if (canFollowAhead() && unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowLeaderA", true)) {
//            return usedManager(this);
//        }
//
//        AUnit leader = Alpha.alphaLeader();
//        if (leader != null && unit.move(followPoint, Actions.MOVE_FOLLOW, "FollowLeaderB")) {
//            return usedManager(this);
//        }
//
//        return null;
//    }
//
//    private boolean canFollowAhead() {
//        return (!unit.isStopped() && unit.lastPositionChangedAgo() <= 100) || (A.s % 6 <= 1);
//    }
//}
