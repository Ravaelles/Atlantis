package atlantis.combat.targeting;

import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class WeakestOfType {
    protected static AUnit selectWeakestEnemyOfType(AUnitType type, AUnit ourUnit, double extraRange) {
        Selection targets = ourUnit
            .enemiesNear()
            .ofType(type)
            .canBeAttackedBy(ourUnit, extraRange);
//                .hasPathFrom(ourUnit);

//        System.err.println("ourUnit = " + ourUnit + " / " + ourUnit.groundWeaponRange());
//        System.err.println("targets in range (" + extraRange + ") = " + targets.size() + " / " + Select.enemies(type).size());

//        // It makes sense to focus fire on units that have lot of HP
//        boolean shouldFocusFire = ourUnit.friendsNearCount() <= 7
//            || (type.maxHp() > 35 && !type.isWorker());
//
//        if (shouldFocusFire) {
////            .inShootRangeOf(extraRange, ourUnit)
//            AUnit mostWounded = targets.mostWounded();
//            if (mostWounded != null && mostWounded.isWounded() && mostWounded.hp() >= 21) {
//                return mostWounded;
//            }
//        }

//        targets.print("targets with extraRange=" + extraRange + " / mostWounded=" + targets.mostWounded());

        /**
         * For units with low HP (Zerglings, workers), it makes sense to spread the fire across multiple units,
         * otherwise enemy that dies consumes unit's cooldown and effectively - it stops shooting at all.
         */
        SpreadFire spreadFire = new SpreadFire(ourUnit, targets);
        if (spreadFire.shouldSpreadFire(ourUnit, targets)) {
            AUnit randomPeasant = spreadFire.spreadFire(ourUnit, targets);
            if (randomPeasant != null) return randomPeasant;
        }

        if (extraRange <= 0.1) return targets.mostWounded();

        return targets.first();

//        HasPosition relativeTo = ourUnit.squadCenter() != null ? ourUnit.squadCenter() : ourUnit;
//        return targets.nearestTo(relativeTo);
    }

    protected static AUnit selectWeakestEnemyOfType(AUnitType enemyType, AUnit unit) {
        // Most wounded enemy IN RANGE
        AUnit enemy = selectWeakestEnemyOfType(enemyType, unit, 0);
//        A.errPrintln("@ " + A.now() + " enemy A = " + enemy);

        if (enemy != null) {
//            unit.addLog("AttackClose");
//            System.err.println("AttackClose");
            return enemy;
        }

        // Most wounded enemy some distance from away
        enemy = selectWeakestEnemyOfType(enemyType, unit, 1.6);
//        A.errPrintln("enemy B = " + enemy);
        if (enemy != null) {
//            System.err.println("Attack 1 range");
            return enemy;
        }

        return null;
    }
}
