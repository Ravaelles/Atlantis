package jfap;

import atlantis.units.AUnitType;
import bwapi.*;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class JFAP extends AJFAP {
    static Game game;
    private Set<JFAPUnit> player1 = new TreeSet<>();
    private Set<JFAPUnit> player2 = new TreeSet<>();
    private boolean didSomething = false;
    private int nFrames = 96;

    public JFAP(Game game) {
        JFAP.game = game;
    }

    @Override
    public void addUnitPlayer1(JFAPUnit fu) {
        player1.add(fu);
    }

    @Override
    public void addIfCombatUnitPlayer1(JFAPUnit fu) {
        if (fu.unitType == AUnitType.Protoss_Interceptor) return;
        if (fu.groundDamage > 0 || fu.airDamage > 0 || fu.unitType == AUnitType.Terran_Medic) addUnitPlayer1(fu);
    }

    @Override
    public void addUnitPlayer2(JFAPUnit fu) {
        player2.add(fu);
    }

    @Override
    public void addIfCombatUnitPlayer2(JFAPUnit fu) {
        if (fu.groundDamage > 0 || fu.airDamage > 0 || fu.unitType == AUnitType.Terran_Medic) addUnitPlayer2(fu);
    }

    @Override
    public void simulate(int nFrames) {
        this.nFrames = nFrames;
        simulate();
        this.nFrames = 96;
    }

    @Override
    public void simulate() {
        try {
            int nFrames = this.nFrames;
            while (nFrames > 0) {
                if (player1.isEmpty() || player2.isEmpty()) break;
                didSomething = false;
                iSimulate();
                if (!didSomething) break;
                nFrames--;
            }
        } catch (Exception e) {
            System.err.print("JFAP Exception");
            e.printStackTrace();
        }
    }

    private int score(final JFAPUnit fu) {
        int score = 0;
        if (fu.health > 0 && fu.maxHealth > 0) {
            score = ((fu.score * (fu.health * 3 + fu.shields) + fu.score) / (fu.maxHealth * 3 + fu.maxShields));
            if (fu.unitType == AUnitType.Terran_Bunker) score += AUnitType.Terran_Marine.ut().destroyScore() * 4;
        }
        return score;
    }

    @Override
    public MutablePair<Integer, Integer> playerScores() {
        MutablePair<Integer, Integer> res = new MutablePair<>(0, 0);
        for (final JFAPUnit u : player1) res.first += score(u);
        for (final JFAPUnit u : player2) res.second += score(u);
        return res;
    }

    @Override
    public MutablePair<Integer, Integer> playerScoresUnits() {
        MutablePair<Integer, Integer> res = new MutablePair<>(0, 0);
        for (final JFAPUnit u : player1) {
            if (!u.unitType.isABuilding()) res.first += score(u);
        }
        for (final JFAPUnit u : player2) {
            if (!u.unitType.isABuilding()) res.second += score(u);
        }
        return res;
    }

    @Override
    public MutablePair<Integer, Integer> playerScoresBuildings() {
        MutablePair<Integer, Integer> res = new MutablePair<>(0, 0);
        for (final JFAPUnit u : player1) {
            if (u.unitType.isABuilding()) res.first += score(u);
        }
        for (final JFAPUnit u : player2) {
            if (u.unitType.isABuilding()) res.second += score(u);
        }
        return res;
    }

    @Override
    public MutablePair<Set<JFAPUnit>, Set<JFAPUnit>> getState() {
        return new MutablePair<>(player1, player2);
    }

    @Override
    public void clear() {
        player1.clear();
        player2.clear();
    }

    private void dealDamage(JFAPUnit fu, int damage, DamageType damageType) {
        damage <<= 8;
        final int remainingShields = fu.shields - damage + (fu.shieldArmor << 8);
        if (remainingShields > 0) {
            fu.shields = remainingShields;
            return;
        }
        else if (fu.shields > 0) {
            damage -= fu.shields + (fu.shieldArmor << 8);
            fu.shields = 0;
        }
        if (damage == 0) return;
        damage -= fu.armor << 8;
        if (damageType == DamageType.Concussive) {
            if (fu.unitSize == UnitSizeType.Large) damage = damage / 4;
            else if (fu.unitSize == UnitSizeType.Medium) damage = damage / 2;
        }
        else if (damageType == DamageType.Explosive) {
            if (fu.unitSize == UnitSizeType.Small) damage = damage / 2;
            else if (fu.unitSize == UnitSizeType.Medium) damage = (damage * 3) / 4;
        }
        fu.health -= Math.max(128, damage);
    }

    private int distButNotReally(JFAPUnit u1, JFAPUnit u2) {
        return (u1.x - u2.x) * (u1.x - u2.x) + (u1.y - u2.y) * (u1.y - u2.y);
    }

    private boolean isSuicideUnit(AUnitType ut) {
        return (ut == AUnitType.Zerg_Scourge ||
            ut == AUnitType.Terran_Vulture_Spider_Mine ||
            ut == AUnitType.Zerg_Infested_Terran ||
            ut == AUnitType.Protoss_Scarab);
    }

    // Thanks and credits to @bmnielsen for kiting code sim
    private void unitSim(JFAPUnit fu, Set<JFAPUnit> enemyUnits) {
        boolean kite = false;
        if (fu.attackCooldownRemaining > 0) {
            if (fu.unitType == AUnitType.Terran_Marine && fu.attackCooldownRemaining <= AUnitType.Terran_Marine.groundWeapon().damageCooldown() - 9)
                kite = true;
            if (!kite) {
                didSomething = true;
                return;
            }
        }
        JFAPUnit closestEnemy = null;
        int closestDist = 0;
        for (JFAPUnit enemy : enemyUnits) {
            if (enemy.flying) {
                if (fu.airDamage > 0) {
                    final int d = distButNotReally(fu, enemy);
                    if ((closestEnemy == null || d < closestDist) && d >= fu.airMinRange) {
                        closestDist = d;
                        closestEnemy = enemy;
                    }
                }
            }
            else if (fu.groundDamage > 0) {
                final int d = distButNotReally(fu, enemy);
                if ((closestEnemy == null || d < closestDist) && d >= fu.groundMinRange) {
                    closestDist = d;
                    closestEnemy = enemy;
                }
            }
        }
        if (kite) {
            if (closestEnemy != null && closestEnemy.groundMaxRange < fu.groundMaxRange && closestDist <= (fu.groundMaxRange + fu.speed) && closestEnemy.groundMaxRange <= 32) {
                int dx = closestEnemy.x - fu.x;
                int dy = closestEnemy.y - fu.y;
                fu.x -= (int) (dx * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
                fu.y -= (int) (dy * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
                didSomething = true;
                return;
            }
        }
        if (closestEnemy != null && Math.sqrt(closestDist) <= fu.speed && !(fu.x == closestEnemy.x && fu.y == closestEnemy.y)) {
            fu.x = closestEnemy.x;
            fu.y = closestEnemy.y;
            closestDist = 0;
            didSomething = true;
        }
        if (closestEnemy != null && closestDist <= (closestEnemy.flying ? fu.groundMaxRange : fu.airMinRange)) {
            if (closestEnemy.flying) {
                dealDamage(closestEnemy, fu.airDamage, fu.airDamageType);
                fu.attackCooldownRemaining = fu.airCooldown;
            }
            else {
                dealDamage(closestEnemy, fu.groundDamage, fu.groundDamageType);
                fu.attackCooldownRemaining = fu.groundCooldown;
                if (fu.elevation != -1 && closestEnemy.elevation != -1 && closestEnemy.elevation > fu.elevation) {
                    fu.attackCooldownRemaining += fu.groundCooldown;
                }
            }
            if (closestEnemy.health < 1) {
                enemyUnits.remove(closestEnemy);
                unitDeath(closestEnemy, enemyUnits);
            }
            didSomething = true;
        }
        else if (closestEnemy != null && Math.sqrt(closestDist) > fu.speed) {
            final int dx = closestEnemy.x - fu.x;
            final int dy = closestEnemy.y - fu.y;
            fu.x += (int) (dx * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
            fu.y += (int) (dy * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
            didSomething = true;
        }
    }

    private void medicsim(JFAPUnit fu, Set<JFAPUnit> player12) {
        JFAPUnit closestHealable = null;
        int closestDist = 0;
        for (JFAPUnit friendlyUnit : player12) {
            if (friendlyUnit.isOrganic && friendlyUnit.health < friendlyUnit.maxHealth && !friendlyUnit.didHealThisFrame) {
                final int d = distButNotReally(fu, friendlyUnit);
                if (closestHealable == null || d < closestDist) {
                    closestHealable = friendlyUnit;
                    closestDist = d;
                }
            }
        }
        if (closestHealable != null) {
            fu.x = closestHealable.x;
            fu.y = closestHealable.y;
            closestHealable.health += 150;
            if (closestHealable.health > closestHealable.maxHealth) closestHealable.health = closestHealable.maxHealth;
            closestHealable.didHealThisFrame = true;
        }
    }

    private boolean suicideSim(JFAPUnit fu, Set<JFAPUnit> player) {
        JFAPUnit closestEnemy = null;
        int closestDist = 0;
        for (JFAPUnit enemy : player) {
            if (enemy.flying) {
                if (fu.airDamage > 0) {
                    final int d = distButNotReally(fu, enemy);
                    if ((closestEnemy == null || d < closestDist) && d >= fu.airMinRange) {
                        closestDist = d;
                        closestEnemy = enemy;
                    }
                }
            }
            else if (fu.groundDamage > 0) {
                int d = distButNotReally(fu, enemy);
                if ((closestEnemy == null || d < closestDist) && d >= fu.groundMinRange) {
                    closestDist = d;
                    closestEnemy = enemy;
                }
            }
        }
        if (closestEnemy != null && Math.sqrt(closestDist) <= fu.speed) {
            if (closestEnemy.flying) dealDamage(closestEnemy, fu.airDamage, fu.airDamageType);
            else dealDamage(closestEnemy, fu.groundDamage, fu.groundDamageType);
            if (closestEnemy.health < 1) {
                player.remove(closestEnemy);
                unitDeath(closestEnemy, player);
            }
            didSomething = true;
            return true;
        }
        else if (closestEnemy != null && Math.sqrt(closestDist) > fu.speed) {
            final int dx = closestEnemy.x - fu.x;
            final int dy = closestEnemy.y - fu.y;
            fu.x += (int) (dx * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
            fu.y += (int) (dy * (fu.speed / Math.sqrt(dx * dx + dy * dy)));
            didSomething = true;
        }
        return false;
    }

    private void simUnit(Iterator<JFAPUnit> unit, Set<JFAPUnit> player1, Set<JFAPUnit> player2) {
        JFAPUnit ju = unit.next();
        if (isSuicideUnit(ju.unitType)) {
            final boolean unitDied = suicideSim(ju, player2);
            if (unitDied) unit.remove();
        }
        else if (ju.unitType == AUnitType.Terran_Medic) medicsim(ju, player1);
        else unitSim(ju, player2);
    }

    private void updateUnit(JFAPUnit fu) {
        if (fu.attackCooldownRemaining > 0) --fu.attackCooldownRemaining;
        if (fu.didHealThisFrame) fu.didHealThisFrame = false;
        if (fu.race == Race.Zerg) {
            if (fu.health < fu.maxHealth) fu.health += 4;
            if (fu.health > fu.maxHealth) fu.health = fu.maxHealth;
        }
        else if (fu.race == Race.Protoss) {
            if (fu.shields < fu.maxShields) fu.shields += 7;
            if (fu.shields > fu.maxShields) fu.shields = fu.maxShields;
        }
    }

    private void iSimulate() {
        Iterator<JFAPUnit> it1 = player1.iterator();
        while (it1.hasNext()) simUnit(it1, player1, player2);
        Iterator<JFAPUnit> it2 = player2.iterator();
        while (it2.hasNext()) simUnit(it2, player2, player1);
        for (JFAPUnit fu : player1) updateUnit(fu);
        for (JFAPUnit fu : player2) updateUnit(fu);
    }

    private void unitDeath(JFAPUnit fu, Set<JFAPUnit> player) {
        if (fu.unitType == AUnitType.Terran_Bunker) {
            JFAPUnit m = convertToUnitType(fu, AUnitType.Terran_Marine);
            m.unitType = AUnitType.Terran_Marine;
            for (int i = 0; i < 4; ++i) player.add(m);
        }
    }

    private JFAPUnit convertToUnitType(JFAPUnit fu, AUnitType ut) {
        JFAPUnit aux = new JFAPUnit();
        aux.id = fu.id;
        aux.x = fu.x;
        aux.y = fu.y;
        aux.player = fu.player;
        aux.unitType = ut;
        aux.attackCooldownRemaining = fu.attackCooldownRemaining;
        aux.elevation = fu.elevation;
        return aux;
    }
}
