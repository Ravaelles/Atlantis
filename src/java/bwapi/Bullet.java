package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
An interface object representing a bullet or missile spawned from an attack. The Bullet interface allows you to detect bullets, missiles, and other types of non-melee attacks or special abilities that would normally be visible through human eyes (A lurker spike or a Queen's flying parasite), allowing quicker reaction to unavoidable consequences. For example, ordering medics to restore units that are about to receive a lockdown to compensate for latency and minimize its effects. You can't know entirely which unit will be receiving a lockdown unless you can detect the lockdown missile using the Bullet class. Bullet objects are re-used after they are destroyed, however their ID is updated when it represents a new Bullet. If Flag::CompleteMapInformation is disabled, then a Bullet is accessible if and only if it is visible. Otherwise if Flag::CompleteMapInformation is enabled, then all Bullets in the game are accessible. See also Game::getBullets, BulletInterface::exists
*/
public class Bullet {

/**
Retrieves a unique identifier for the current Bullet. Returns An integer value containing the identifier.
*/
    public int getID() {
        return getID_native(pointer);
    }

/**
Checks if the Bullet exists in the view of the BWAPI player. Return values true If the bullet exists or is visible. false If the bullet was destroyed or has gone out of scope. If Flag::CompleteMapInformation is disabled, and a Bullet is not visible, then the return value will be false regardless of the Bullet's true existence. This is because absolutely no state information on invisible enemy bullets is made available to the AI. If Flag::CompleteMapInformation is enabled, then this function is accurate for all Bullet information. See also isVisible, UnitInterface::exists
*/
    public boolean exists() {
        return exists_native(pointer);
    }

/**
Retrieves the Player interface that owns the Bullet. Return values nullptr If the Player object for this Bullet is inaccessible. Returns The owning Player interface object.
*/
    public Player getPlayer() {
        return getPlayer_native(pointer);
    }

/**
Retrieves the type of this Bullet. Return values BulletTypes::Unknown if the Bullet is inaccessible. Returns A BulletType representing the Bullet's type.
*/
    public BulletType getType() {
        return getType_native(pointer);
    }

/**
Retrieves the Unit interface that the Bullet spawned from. Return values nullptr If the source can not be identified or is inaccessible. Returns The owning Unit interface object. See also getTarget
*/
    public Unit getSource() {
        return getSource_native(pointer);
    }

/**
Retrieves the Bullet's current position. Return values Positions::Unknown If the Bullet is inaccessible. Returns A Position containing the Bullet's current coordinates. See also getTargetPosition
*/
    public Position getPosition() {
        return getPosition_native(pointer);
    }

/**
Retrieve's the direction the Bullet is facing. If the angle is 0, then the Bullet is facing right. Return values 0.0 If the bullet is inaccessible. Returns A double representing the direction the Bullet is facing.
*/
    public double getAngle() {
        return getAngle_native(pointer);
    }

/**
Retrieves the X component of the Bullet's velocity, measured in pixels per frame. Return values 0.0 if the Bullet is inaccessible. Returns A double representing the number of pixels moved on the X axis per frame. See also getVelocityY, getAngle
*/
    public double getVelocityX() {
        return getVelocityX_native(pointer);
    }

/**
Retrieves the Y component of the Bullet's velocity, measured in pixels per frame. Return values 0.0 if the Bullet is inaccessible. Returns A double representing the number of pixels moved on the Y axis per frame. See also getVelocityX, getAngle
*/
    public double getVelocityY() {
        return getVelocityY_native(pointer);
    }

/**
Retrieves the Unit interface that the Bullet is heading to. Return values nullptr If the Bullet's target Unit is inaccessible, the Bullet is targetting the ground, or if the Bullet itself is inaccessible. Returns The target Unit interface object, if one exists. See also getTargetPosition, getSource
*/
    public Unit getTarget() {
        return getTarget_native(pointer);
    }

/**
Retrieves the target position that the Bullet is heading to. Return values Positions::Unknown If the bullet is inaccessible. Returns A Position indicating where the Bullet is headed. See also getTarget, getPosition
*/
    public Position getTargetPosition() {
        return getTargetPosition_native(pointer);
    }

/**
Retrieves the timer that indicates the Bullet's life span. Bullets are not permanent objects, so they will often have a limited life span. This life span is measured in frames. Normally a Bullet will reach its target before being removed. Return values 0 If the Bullet is inaccessible. Returns An integer representing the remaining number of frames until the Bullet self-destructs.
*/
    public int getRemoveTimer() {
        return getRemoveTimer_native(pointer);
    }

/**
Retrieves the visibility state of the Bullet. Parameters player (optional) If this parameter is specified, then the Bullet's visibility to the given player is checked. If this parameter is omitted, then a default value of nullptr is used, which will check if the BWAPI player has vision of the Bullet. Note If player is nullptr and Broodwar->self() is also nullptr, then the visibility of the Bullet is determined by checking if at least one other player has vision of the Bullet. Return values true If the Bullet is visible to the specified player. false If the Bullet is not visible to the specified player.
*/
    public boolean isVisible() {
        return isVisible_native(pointer);
    }

    public boolean isVisible(Player player) {
        return isVisible_native(pointer, player);
    }


    private static Map<Long, Bullet> instances = new HashMap<Long, Bullet>();

    private Bullet(long pointer) {
        this.pointer = pointer;
    }

    private static Bullet get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Bullet instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Bullet(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native int getID_native(long pointer);

    private native boolean exists_native(long pointer);

    private native Player getPlayer_native(long pointer);

    private native BulletType getType_native(long pointer);

    private native Unit getSource_native(long pointer);

    private native Position getPosition_native(long pointer);

    private native double getAngle_native(long pointer);

    private native double getVelocityX_native(long pointer);

    private native double getVelocityY_native(long pointer);

    private native Unit getTarget_native(long pointer);

    private native Position getTargetPosition_native(long pointer);

    private native int getRemoveTimer_native(long pointer);

    private native boolean isVisible_native(long pointer);

    private native boolean isVisible_native(long pointer, Player player);


}
