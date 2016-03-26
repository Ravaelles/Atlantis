package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
The abstract Game class is implemented by BWAPI and is the primary means of obtaining all game state information from Starcraft Broodwar. Game state information includes all units, resources, players, forces, bullets, terrain, fog of war, regions, etc.
*/
public class Game {

/**
Retrieves the set of all teams/forces. Forces are commonly seen in Use Map Settings game types and some others such as Top vs Bottom and the team versions of game types. Returns Forceset containing all forces in the game.
*/
    public List<Force> getForces() {
        return getForces_native(pointer);
    }

/**
Retrieves the set of all players in the match. This includes the neutral player, which owns all the resources and critters by default. Returns Playerset containing all players in the game.
*/
    public List<Player> getPlayers() {
        return getPlayers_native(pointer);
    }

/**
Retrieves the set of all accessible units. If Flag::CompleteMapInformation is enabled, then the set also includes units that are not visible to the player. Note Units that are inside refineries are not included in this set. Returns Unitset containing all known units in the game.
*/
    public List<Unit> getAllUnits() {
        return getAllUnits_native(pointer);
    }

/**
Retrieves the set of all accessible Mineral Fields in the game. Returns Unitset containing Mineral Fields
*/
    public List<Unit> getMinerals() {
        return getMinerals_native(pointer);
    }

/**
Retrieves the set of all accessible Vespene Geysers in the game. Returns Unitset containing Vespene Geysers
*/
    public List<Unit> getGeysers() {
        return getGeysers_native(pointer);
    }

/**
Retrieves the set of all accessible neutral units in the game. This includes Mineral Fields, Vespene Geysers, and Critters. Returns Unitset containing all neutral units.
*/
    public List<Unit> getNeutralUnits() {
        return getNeutralUnits_native(pointer);
    }

/**
Retrieves the set of all Mineral Fields that were available at the beginning of the game. Note This set includes resources that have been mined out or are inaccessible. Returns Unitset containing static Mineral Fields
*/
    public List<Unit> getStaticMinerals() {
        return getStaticMinerals_native(pointer);
    }

/**
Retrieves the set of all Vespene Geysers that were available at the beginning of the game. Note This set includes resources that are inaccessible. Returns Unitset containing static Vespene Geysers
*/
    public List<Unit> getStaticGeysers() {
        return getStaticGeysers_native(pointer);
    }

/**
Retrieves the set of all units owned by the neutral player (resources, critters, etc.) that were available at the beginning of the game. Note This set includes units that are inaccessible. Returns Unitset containing static neutral units
*/
    public List<Unit> getStaticNeutralUnits() {
        return getStaticNeutralUnits_native(pointer);
    }

/**
Retrieves the set of all accessible bullets. Returns Bulletset containing all accessible Bullet objects.
*/
    public List<Bullet> getBullets() {
        return getBullets_native(pointer);
    }

/**
Retrieves the set of all accessible Nuke dots. Note Nuke dots are the red dots painted by a Ghost when using the nuclear strike ability. Returns Set of Positions giving the coordinates of nuke locations.
*/
    public List<Position> getNukeDots() {
        return getNukeDots_native(pointer);
    }

/**
Retrieves the Force interface object associated with a given identifier. Parameters forceID The identifier for the Force object. Returns Force interface object mapped to the given forceID. Return values nullptr if the given identifier is invalid.
*/
    public Force getForce(int forceID) {
        return getForce_native(pointer, forceID);
    }

/**
Retrieves the Player interface object associated with a given identifier. Parameters playerID The identifier for the Player object. Returns Player interface object mapped to the given playerID. Return values nullptr if the given identifier is invalid.
*/
    public Player getPlayer(int playerID) {
        return getPlayer_native(pointer, playerID);
    }

/**
Retrieves the Unit interface object associated with a given identifier. Parameters unitID The identifier for the Unit object. Returns Unit interface object mapped to the given unitID. Return values nullptr if the given identifier is invalid.
*/
    public Unit getUnit(int unitID) {
        return getUnit_native(pointer, unitID);
    }

/**
Retrieves a Unit interface object from a given unit index. The value given as an index maps directly to Broodwar's unit array index and matches the index found in replay files. In order to use this function, CompleteMapInformation must be enabled. Parameters unitIndex The unitIndex to identify the Unit with. A valid index is 0 <= unitIndex & 0x7FF < 1700. Returns Unit interface object that matches the given unitIndex. Return values nullptr if the given index is invalid.
*/
    public Unit indexToUnit(int unitIndex) {
        return indexToUnit_native(pointer, unitIndex);
    }

/**
Retrieves the Region interface object associated with a given identifier. Parameters regionID The identifier for the Region object. Returns Region interface object mapped to the given regionID. Return values nullptr if the given ID is invalid.
*/
    public Region getRegion(int regionID) {
        return getRegion_native(pointer, regionID);
    }

/**
Retrieves the GameType of the current game. Returns GameType indicating the rules of the match. See also GameType
*/
    public GameType getGameType() {
        return getGameType_native(pointer);
    }

/**
Retrieves the current latency setting that the game is set to. Latency indicates the delay between issuing a command and having it processed. Returns The latency setting of the game, which is of Latency::Enum. See also Latency::Enum
*/
    public int getLatency() {
        return getLatency_native(pointer);
    }

/**
Retrieves the number of logical frames since the beginning of the match. If the game is paused, then getFrameCount will not increase. Returns Number of logical frames that have elapsed since the game started as an integer.
*/
    public int getFrameCount() {
        return getFrameCount_native(pointer);
    }

/**
Retrieves the maximum number of logical frames that have been recorded in a replay. If the game is not a replay, then the value returned is undefined. Returns The number of logical frames that the replay contains.
*/
    public int getReplayFrameCount() {
        return getReplayFrameCount_native(pointer);
    }

/**
Retrieves the logical frame rate of the game in frames per second (FPS). Example: BWAPI::Broodwar->setLocalSpeed(0); // Log and display the best logical FPS seen in the game static int bestFPS = 0; bestFPS = std::max(bestFPS, BWAPI::Broodwar->getFPS()); BWAPI::Broodwar->drawTextScreen(BWAPI::Positions::Origin, "%cBest: %d GFPS\nCurrent: %d GFPS", BWAPI::Text::White, bestFPS, BWAPI::Broodwar->getFPS()); Returns Logical frames per second that the game is currently running at as an integer. See also getAverageFPS
*/
    public int getFPS() {
        return getFPS_native(pointer);
    }

/**
Retrieves the average logical frame rate of the game in frames per second (FPS). Returns Average logical frames per second that the game is currently running at as a double. See also getFPS
*/
    public double getAverageFPS() {
        return getAverageFPS_native(pointer);
    }

/**
Retrieves the position of the user's mouse on the screen, in Position coordinates. Returns Position indicating the location of the mouse. Return values Positions::Unknown if Flag::UserInput is disabled.
*/
    public Position getMousePosition() {
        return getMousePosition_native(pointer);
    }

/**
Retrieves the state of the given mouse button. Parameters button A MouseButton enum member indicating which button on the mouse to check. Returns A bool indicating the state of the given button. true if the button was pressed and false if it was not. Return values false always if Flag::UserInput is disabled. See also MouseButton
*/
    public boolean getMouseState(MouseButton button) {
        return getMouseState_native(pointer, button);
    }

/**
Retrieves the state of the given keyboard key. Parameters key A Key enum member indicating which key on the keyboard to check. Returns A bool indicating the state of the given key. true if the key was pressed and false if it was not. Return values false always if Flag::UserInput is disabled. See also Key
*/
    public boolean getKeyState(Key key) {
        return getKeyState_native(pointer, key);
    }

/**
Retrieves the top left position of the viewport from the top left corner of the map, in pixels. Returns Position containing the coordinates of the top left corner of the game's viewport. Return values Positions::Unknown always if Flag::UserInput is disabled. See also setScreenPosition
*/
    public Position getScreenPosition() {
        return getScreenPosition_native(pointer);
    }

/**
Moves the top left corner of the viewport to the provided position relative to the map's origin (top left (0,0)). Parameters x The x coordinate to move the screen to, in pixels. y The y coordinate to move the screen to, in pixels. See also getScreenPosition
*/
    public void setScreenPosition(int x, int y) {
        setScreenPosition_native(pointer, x, y);
    }

/**
Moves the top left corner of the viewport to the provided position relative to the map's origin (top left (0,0)). Parameters x The x coordinate to move the screen to, in pixels. y The y coordinate to move the screen to, in pixels. See also getScreenPosition
*/
    public void setScreenPosition(Position p) {
        setScreenPosition_native(pointer, p);
    }

/**
Pings the minimap at the given position. Minimap pings are visible to allied players. Parameters x The x coordinate to ping at, in pixels, from the map's origin (left). y The y coordinate to ping at, in pixels, from the map's origin (top).
*/
    public void pingMinimap(int x, int y) {
        pingMinimap_native(pointer, x, y);
    }

/**
Pings the minimap at the given position. Minimap pings are visible to allied players. Parameters x The x coordinate to ping at, in pixels, from the map's origin (left). y The y coordinate to ping at, in pixels, from the map's origin (top).
*/
    public void pingMinimap(Position p) {
        pingMinimap_native(pointer, p);
    }

/**
Checks if the state of the given flag is enabled or not. Note Flags may only be enabled at the start of the match during the AIModule::onStart callback. Parameters flag The Flag::Enum entry describing the flag's effects on BWAPI. Returns true if the given flag is enabled, false if the flag is disabled. See also Flag::Enum
*/
    public boolean isFlagEnabled(int flag) {
        return isFlagEnabled_native(pointer, flag);
    }

/**
Enables the state of a given flag. Note Flags may only be enabled at the start of the match during the AIModule::onStart callback. Parameters flag The Flag::Enum entry describing the flag's effects on BWAPI. See also Flag::Enum
*/
    public void enableFlag(int flag) {
        enableFlag_native(pointer, flag);
    }

/**
Retrieves the set of accessible units that are on a given build tile. Parameters tileX The X position, in tiles. tileY The Y position, in tiles. pred (optional) A function predicate that indicates which units are included in the returned set. Returns A Unitset object consisting of all the units that have any part of them on the given build tile.
*/
    public List<Unit> getUnitsOnTile(int tileX, int tileY) {
        return getUnitsOnTile_native(pointer, tileX, tileY);
    }

/**
Retrieves the set of accessible units that are on a given build tile. Parameters tileX The X position, in tiles. tileY The Y position, in tiles. pred (optional) A function predicate that indicates which units are included in the returned set. Returns A Unitset object consisting of all the units that have any part of them on the given build tile.
*/
    public List<Unit> getUnitsOnTile(TilePosition tile) {
        return getUnitsOnTile_native(pointer, tile);
    }

/**
Retrieves the set of accessible units that are in a given rectangle. Parameters left The X coordinate of the left position of the bounding box, in pixels. top The Y coordinate of the top position of the bounding box, in pixels. right The X coordinate of the right position of the bounding box, in pixels. bottom The Y coordinate of the bottom position of the bounding box, in pixels. pred (optional) A function predicate that indicates which units are included in the returned set. Returns A Unitset object consisting of all the units that have any part of them within the given rectangle bounds.
*/
    public List<Unit> getUnitsInRectangle(int left, int top, int right, int bottom) {
        return getUnitsInRectangle_native(pointer, left, top, right, bottom);
    }

/**
Retrieves the set of accessible units that are in a given rectangle. Parameters left The X coordinate of the left position of the bounding box, in pixels. top The Y coordinate of the top position of the bounding box, in pixels. right The X coordinate of the right position of the bounding box, in pixels. bottom The Y coordinate of the bottom position of the bounding box, in pixels. pred (optional) A function predicate that indicates which units are included in the returned set. Returns A Unitset object consisting of all the units that have any part of them within the given rectangle bounds.
*/
    public List<Unit> getUnitsInRectangle(Position topLeft, Position bottomRight) {
        return getUnitsInRectangle_native(pointer, topLeft, bottomRight);
    }

/**
Retrieves the set of accessible units that are within a given radius of a position. Parameters x The x coordinate of the center, in pixels. y The y coordinate of the center, in pixels. radius The radius from the center, in pixels, to include units. pred (optional) A function predicate that indicates which units are included in the returned set. Returns A Unitset object consisting of all the units that have any part of them within the given radius from the center position.
*/
    public List<Unit> getUnitsInRadius(int x, int y, int radius) {
        return getUnitsInRadius_native(pointer, x, y, radius);
    }

/**
Retrieves the set of accessible units that are within a given radius of a position. Parameters x The x coordinate of the center, in pixels. y The y coordinate of the center, in pixels. radius The radius from the center, in pixels, to include units. pred (optional) A function predicate that indicates which units are included in the returned set. Returns A Unitset object consisting of all the units that have any part of them within the given radius from the center position.
*/
    public List<Unit> getUnitsInRadius(Position center, int radius) {
        return getUnitsInRadius_native(pointer, center, radius);
    }

/**
Returns the last error that was set using setLastError. If a function call in BWAPI has failed, you can use this function to retrieve the reason it failed. Returns Error type containing the reason for failure. See also setLastError, Errors
*/
    public Error getLastError() {
        return getLastError_native(pointer);
    }

/**
Sets the last error so that future calls to getLastError will return the value that was set. Parameters e (optional) The error code to set. If omitted, then the last error will be cleared. Return values true If the type passed was Errors::None, clearing the last error. false If any other error type was passed. See also getLastError, Errors
*/
    public boolean setLastError() {
        return setLastError_native(pointer);
    }

    public boolean setLastError(Error e) {
        return setLastError_native(pointer, e);
    }

/**
Retrieves the width of the map in build tile units. Returns Width of the map in tiles. See also mapHeight
*/
    public int mapWidth() {
        return mapWidth_native(pointer);
    }

/**
Retrieves the height of the map in build tile units. Returns Height of the map in tiles. See also mapHeight
*/
    public int mapHeight() {
        return mapHeight_native(pointer);
    }

/**
Retrieves the file name of the currently loaded map. Returns Map file name as std::string object. See also mapPathName, mapName
*/
    public String mapFileName() {
        return mapFileName_native(pointer);
    }

/**
Retrieves the full path name of the currently loaded map. Returns Map file name as std::string object. See also mapFileName, mapName
*/
    public String mapPathName() {
        return mapPathName_native(pointer);
    }

/**
Retrieves the title of the currently loaded map. Returns Map title as std::string object. See also mapFileName, mapPathName
*/
    public String mapName() {
        return mapName_native(pointer);
    }

/**
Calculates the SHA-1 hash of the currently loaded map file. Returns std::string object containing SHA-1 hash. Note Campaign maps will return a hash of their internal map chunk components(.chk), while standard maps will return a hash of their entire map archive (.scm,.scx).
*/
    public String mapHash() {
        return mapHash_native(pointer);
    }

/**
Checks if the given mini-tile position is walkable. Note This function only checks if the static terrain is walkable. Its current occupied state is excluded from this check. To see if the space is currently occupied or not, then see getUnitsInRectangle . Parameters walkX The x coordinate of the mini-tile, in mini-tile units (8 pixels). walkY The y coordinate of the mini-tile, in mini-tile units (8 pixels). Returns true if the mini-tile is walkable and false if it is impassable for ground units.
*/
    public boolean isWalkable(int walkX, int walkY) {
        return isWalkable_native(pointer, walkX, walkY);
    }

/**
Checks if the given mini-tile position is walkable. Note This function only checks if the static terrain is walkable. Its current occupied state is excluded from this check. To see if the space is currently occupied or not, then see getUnitsInRectangle . Parameters walkX The x coordinate of the mini-tile, in mini-tile units (8 pixels). walkY The y coordinate of the mini-tile, in mini-tile units (8 pixels). Returns true if the mini-tile is walkable and false if it is impassable for ground units.
*/
    public boolean isWalkable(WalkPosition position) {
        return isWalkable_native(pointer, position);
    }

/**
Returns the ground height at the given tile position. Parameters tileX X position to query, in tiles tileY Y position to query, in tiles Returns The tile height as an integer. Possible values are: 0: Low ground 1: Low ground doodad 2: High ground 3: High ground doodad 4: Very high ground 5: Very high ground doodad
*/
    public int getGroundHeight(int tileX, int tileY) {
        return getGroundHeight_native(pointer, tileX, tileY);
    }

/**
Returns the ground height at the given tile position. Parameters tileX X position to query, in tiles tileY Y position to query, in tiles Returns The tile height as an integer. Possible values are: 0: Low ground 1: Low ground doodad 2: High ground 3: High ground doodad 4: Very high ground 5: Very high ground doodad
*/
    public int getGroundHeight(TilePosition position) {
        return getGroundHeight_native(pointer, position);
    }

/**
Checks if a given tile position is buildable. This means that, if all other requirements are met, a structure can be placed on this tile. This function uses static map data. Parameters tileX The x value of the tile to check. tileY The y value of the tile to check. includeBuildings (optional) If this is true, then this function will also check if any visible structures are occupying the space. If this value is false, then it only checks the static map data for tile buildability. This value is false by default. Returns boolean identifying if the given tile position is buildable (true) or not (false). If includeBuildings was provided, then it will return false if a structure is currently occupying the tile.
*/
    public boolean isBuildable(int tileX, int tileY) {
        return isBuildable_native(pointer, tileX, tileY);
    }

    public boolean isBuildable(int tileX, int tileY, boolean includeBuildings) {
        return isBuildable_native(pointer, tileX, tileY, includeBuildings);
    }

/**
Checks if a given tile position is buildable. This means that, if all other requirements are met, a structure can be placed on this tile. This function uses static map data. Parameters tileX The x value of the tile to check. tileY The y value of the tile to check. includeBuildings (optional) If this is true, then this function will also check if any visible structures are occupying the space. If this value is false, then it only checks the static map data for tile buildability. This value is false by default. Returns boolean identifying if the given tile position is buildable (true) or not (false). If includeBuildings was provided, then it will return false if a structure is currently occupying the tile.
*/
    public boolean isBuildable(TilePosition position) {
        return isBuildable_native(pointer, position);
    }

    public boolean isBuildable(TilePosition position, boolean includeBuildings) {
        return isBuildable_native(pointer, position, includeBuildings);
    }

/**
Checks if a given tile position is visible to the current player. Parameters tileX The x value of the tile to check. tileY The y value of the tile to check. Returns boolean identifying the visibility of the tile. If the given tile is visible, then the value is true. If the given tile is concealed by the fog of war, then this value will be false.
*/
    public boolean isVisible(int tileX, int tileY) {
        return isVisible_native(pointer, tileX, tileY);
    }

/**
Checks if a given tile position is visible to the current player. Parameters tileX The x value of the tile to check. tileY The y value of the tile to check. Returns boolean identifying the visibility of the tile. If the given tile is visible, then the value is true. If the given tile is concealed by the fog of war, then this value will be false.
*/
    public boolean isVisible(TilePosition position) {
        return isVisible_native(pointer, position);
    }

/**
Checks if a given tile position has been explored by the player. An explored tile position indicates that the player has seen the location at some point in the match, partially revealing the fog of war for the remainder of the match. Parameters tileX The x tile coordinate to check. tileY The y tile coordinate to check. Return values true If the player has explored the given tile position (partially revealed fog). false If the tile position was never explored (completely black fog). See also isVisible
*/
    public boolean isExplored(int tileX, int tileY) {
        return isExplored_native(pointer, tileX, tileY);
    }

/**
Checks if a given tile position has been explored by the player. An explored tile position indicates that the player has seen the location at some point in the match, partially revealing the fog of war for the remainder of the match. Parameters tileX The x tile coordinate to check. tileY The y tile coordinate to check. Return values true If the player has explored the given tile position (partially revealed fog). false If the tile position was never explored (completely black fog). See also isVisible
*/
    public boolean isExplored(TilePosition position) {
        return isExplored_native(pointer, position);
    }

/**
Checks if the given tile position has Zerg creep on it. Parameters tileX The x tile coordinate to check. tileY The y tile coordinate to check. Return values true If the given tile has creep on it. false If the given tile does not have creep, or if it is concealed by the fog of war.
*/
    public boolean hasCreep(int tileX, int tileY) {
        return hasCreep_native(pointer, tileX, tileY);
    }

/**
Checks if the given tile position has Zerg creep on it. Parameters tileX The x tile coordinate to check. tileY The y tile coordinate to check. Return values true If the given tile has creep on it. false If the given tile does not have creep, or if it is concealed by the fog of war.
*/
    public boolean hasCreep(TilePosition position) {
        return hasCreep_native(pointer, position);
    }

/**
Checks if the given pixel position is powered by an owned Protoss Pylon for an optional unit type. Parameters x The x pixel coordinate to check. y The y pixel coordinate to check. unitType (optional) Checks if the given UnitType requires power or not. If ommitted, then it will assume that the position requires power for any unit type. Return values true if the type at the given position will have power. false if the type at the given position will be unpowered.
*/
    public boolean hasPowerPrecise(int x, int y) {
        return hasPowerPrecise_native(pointer, x, y);
    }

    public boolean hasPowerPrecise(int x, int y, UnitType unitType) {
        return hasPowerPrecise_native(pointer, x, y, unitType);
    }

/**
Checks if the given pixel position is powered by an owned Protoss Pylon for an optional unit type. Parameters x The x pixel coordinate to check. y The y pixel coordinate to check. unitType (optional) Checks if the given UnitType requires power or not. If ommitted, then it will assume that the position requires power for any unit type. Return values true if the type at the given position will have power. false if the type at the given position will be unpowered.
*/
    public boolean hasPowerPrecise(Position position) {
        return hasPowerPrecise_native(pointer, position);
    }

    public boolean hasPowerPrecise(Position position, UnitType unitType) {
        return hasPowerPrecise_native(pointer, position, unitType);
    }

/**
Checks if the given tile position if powered by an owned Protoss Pylon for an optional unit type. Parameters tileX The x tile coordinate to check. tileY The y tile coordinate to check. unitType (optional) Checks if the given UnitType will be powered if placed at the given tile position. If omitted, then only the immediate tile position is checked for power, and the function will assume that the location requires power for any unit type. Return values true if the type at the given tile position will receive power. false if the type will be unpowered at the given tile position.
*/
    public boolean hasPower(int tileX, int tileY) {
        return hasPower_native(pointer, tileX, tileY);
    }

    public boolean hasPower(int tileX, int tileY, UnitType unitType) {
        return hasPower_native(pointer, tileX, tileY, unitType);
    }

/**
Checks if the given tile position if powered by an owned Protoss Pylon for an optional unit type. Parameters tileX The x tile coordinate to check. tileY The y tile coordinate to check. unitType (optional) Checks if the given UnitType will be powered if placed at the given tile position. If omitted, then only the immediate tile position is checked for power, and the function will assume that the location requires power for any unit type. Return values true if the type at the given tile position will receive power. false if the type will be unpowered at the given tile position.
*/
    public boolean hasPower(TilePosition position) {
        return hasPower_native(pointer, position);
    }

    public boolean hasPower(TilePosition position, UnitType unitType) {
        return hasPower_native(pointer, position, unitType);
    }

/**
Checks if the given tile position if powered by an owned Protoss Pylon for an optional unit type. Parameters tileX The x tile coordinate to check. tileY The y tile coordinate to check. unitType (optional) Checks if the given UnitType will be powered if placed at the given tile position. If omitted, then only the immediate tile position is checked for power, and the function will assume that the location requires power for any unit type. Return values true if the type at the given tile position will receive power. false if the type will be unpowered at the given tile position.
*/
    public boolean hasPower(int tileX, int tileY, int tileWidth, int tileHeight) {
        return hasPower_native(pointer, tileX, tileY, tileWidth, tileHeight);
    }

    public boolean hasPower(int tileX, int tileY, int tileWidth, int tileHeight, UnitType unitType) {
        return hasPower_native(pointer, tileX, tileY, tileWidth, tileHeight, unitType);
    }

/**
Checks if the given tile position if powered by an owned Protoss Pylon for an optional unit type. Parameters tileX The x tile coordinate to check. tileY The y tile coordinate to check. unitType (optional) Checks if the given UnitType will be powered if placed at the given tile position. If omitted, then only the immediate tile position is checked for power, and the function will assume that the location requires power for any unit type. Return values true if the type at the given tile position will receive power. false if the type will be unpowered at the given tile position.
*/
    public boolean hasPower(TilePosition position, int tileWidth, int tileHeight) {
        return hasPower_native(pointer, position, tileWidth, tileHeight);
    }

    public boolean hasPower(TilePosition position, int tileWidth, int tileHeight, UnitType unitType) {
        return hasPower_native(pointer, position, tileWidth, tileHeight, unitType);
    }

/**
Checks if the given unit type can be built at the given build tile position. This function checks for creep, power, and resource distance requirements in addition to the tiles' buildability and possible units obstructing the build location. Note If the type is an addon and a builer is provided, then the location of the addon will be placed 4 tiles to the right and 1 tile down from the given position. If the builder is not given, then the check for the addon will be conducted at position. Parameters position Indicates the tile position that the top left corner of the structure is intended to go. type The UnitType to check for. builder (optional) The intended unit that will build the structure. If specified, then this function will also check if there is a path to the build site and exclude the builder from the set of units that may be blocking the build site. checkExplored (optional) If this parameter is true, it will also check if the target position has been explored by the current player. This value is false by default, ignoring the explored state of the build site. Returns true indicating that the structure can be placed at the given tile position, and false if something may be obstructing the build location.
*/
    public boolean canBuildHere(TilePosition position, UnitType type, Unit builder) {
        return canBuildHere_native(pointer, position, type, builder);
    }

    public boolean canBuildHere(TilePosition position, UnitType type) {
        return canBuildHere_native(pointer, position, type);
    }

    public boolean canBuildHere(TilePosition position, UnitType type, Unit builder, boolean checkExplored) {
        return canBuildHere_native(pointer, position, type, builder, checkExplored);
    }

/**
Checks all the requirements in order to make a given unit type for the current player. These include resources, supply, technology tree, availability, and required units. Parameters type The UnitType to check. builder (optional) The Unit that will be used to build/train the provided unit type. If this value is nullptr or excluded, then the builder will be excluded in the check. Returns true indicating that the type can be made. If builder is provided, then it is only true if builder can make the type. Otherwise it will return false, indicating that the unit type can not be made.
*/
    public boolean canMake(UnitType type) {
        return canMake_native(pointer, type);
    }

    public boolean canMake(UnitType type, Unit builder) {
        return canMake_native(pointer, type, builder);
    }

/**
Checks all the requirements in order to research a given technology type for the current player. These include resources, technology tree, availability, and required units. Parameters type The TechType to check. unit (optional) The Unit that will be used to research the provided technology type. If this value is nullptr or excluded, then the unit will be excluded in the check. checkCanIssueCommandType (optional) TODO fill this in Returns true indicating that the type can be researched. If unit is provided, then it is only true if unit can research the type. Otherwise it will return false, indicating that the technology can not be researched.
*/
    public boolean canResearch(TechType type, Unit unit) {
        return canResearch_native(pointer, type, unit);
    }

    public boolean canResearch(TechType type) {
        return canResearch_native(pointer, type);
    }

    public boolean canResearch(TechType type, Unit unit, boolean checkCanIssueCommandType) {
        return canResearch_native(pointer, type, unit, checkCanIssueCommandType);
    }

/**
Checks all the requirements in order to upgrade a given upgrade type for the current player. These include resources, technology tree, availability, and required units. Parameters type The UpgradeType to check. unit (optional) The Unit that will be used to upgrade the provided upgrade type. If this value is nullptr or excluded, then the unit will be excluded in the check. checkCanIssueCommandType (optional) TODO fill this in Returns true indicating that the type can be upgraded. If unit is provided, then it is only true if unit can upgrade the type. Otherwise it will return false, indicating that the upgrade can not be upgraded.
*/
    public boolean canUpgrade(UpgradeType type, Unit unit) {
        return canUpgrade_native(pointer, type, unit);
    }

    public boolean canUpgrade(UpgradeType type) {
        return canUpgrade_native(pointer, type);
    }

    public boolean canUpgrade(UpgradeType type, Unit unit, boolean checkCanIssueCommandType) {
        return canUpgrade_native(pointer, type, unit, checkCanIssueCommandType);
    }

/**
Retrieves the set of all starting locations for the current map. A starting location is essentially a candidate for a player's spawn point. Returns A TilePosition::list containing all the TilePosition objects that indicate a start location. See also PlayerInterface::getStartLocation
*/
    public List<TilePosition> getStartLocations() {
        return getStartLocations_native(pointer);
    }

/**
Prints text to the screen as a notification. This function allows text formatting using Text::Enum members. The behaviour of this function is the same as printf, located in header cstdio. Note That text printed through this function is not seen by other players or in replays. Parameters format Text formatting. See std::printf for more information. Refrain from passing non-constant strings directly in this parameter. ... The arguments that will be formatted using the given text formatting. See also Text::Enum, std::printf
*/
    public void printf(String cstr_format) {
        printf_native(pointer, cstr_format);
    }

/**
Sends a text message to all other players in the game. The behaviour of this function is the same as std::printf, located in header cstdio. Note In a single player game this function can be used to execute cheat codes. Parameters format Text formatting. See std::printf for more information. Refrain from passing non-constant strings directly in this parameter. See also sendTextEx, std::printf
*/
    public void sendText(String cstr_format) {
        sendText_native(pointer, cstr_format);
    }

/**
An extended version of Game::sendText which allows messages to be forwarded to allies. The behaviour of this function is the same as std::printf, located in header cstdio. Parameters toAllies If this parameter is set to true, then the message is only sent to allied players, otherwise it will be sent to all players. format Text formatting. See std::printf for more information. Refrain from passing non-constant strings directly in this parameter. See also sendText, std::printf
*/
    public void sendTextEx(boolean toAllies, String cstr_format) {
        sendTextEx_native(pointer, toAllies, cstr_format);
    }

/**
Checks if the current client is inside a game. Returns true if the client is in a game, and false if it is not.
*/
    public boolean isInGame() {
        return isInGame_native(pointer);
    }

/**
Checks if the current client is inside a multiplayer game. Returns true if the client is in a multiplayer game, and false if it is a single player game, a replay, or some other state.
*/
    public boolean isMultiplayer() {
        return isMultiplayer_native(pointer);
    }

/**
Checks if the client is in a game that was created through the Battle.net multiplayer gaming service. Returns true if the client is in a multiplayer Battle.net game and false if it is not.
*/
    public boolean isBattleNet() {
        return isBattleNet_native(pointer);
    }

/**
Checks if the current game is paused. While paused, AIModule::onFrame will still be called. Returns true if the game is paused and false otherwise See also pauseGame, resumeGame
*/
    public boolean isPaused() {
        return isPaused_native(pointer);
    }

/**
Checks if the client is watching a replay. Returns true if the client is watching a replay and false otherwise
*/
    public boolean isReplay() {
        return isReplay_native(pointer);
    }

/**
Pauses the game. While paused, AIModule::onFrame will still be called. See also resumeGame
*/
    public void pauseGame() {
        pauseGame_native(pointer);
    }

/**
Resumes the game from a paused state. See also pauseGame
*/
    public void resumeGame() {
        resumeGame_native(pointer);
    }

/**
Leaves the current game by surrendering and enters the post-game statistics/score screen.
*/
    public void leaveGame() {
        leaveGame_native(pointer);
    }

/**
Restarts the match. Works the same as if the match was restarted from the in-game menu (F10). This option is only available in single player games.
*/
    public void restartGame() {
        restartGame_native(pointer);
    }

/**
Sets the number of milliseconds Broodwar spends in each frame. The default values are as follows: Fastest: 42ms/frame Faster: 48ms/frame Fast: 56ms/frame Normal: 67ms/frame Slow: 83ms/frame Slower: 111ms/frame Slowest: 167ms/frame Note Specifying a value of 0 will not guarantee that logical frames are executed as fast as possible. If that is the intention, use this in combination with setFrameSkip. Parameters speed The time spent per frame, in milliseconds. A value of 0 indicates that frames are executed immediately with no delay. Negative values will restore the default value as listed above. See also setFrameSkip, getFPS
*/
    public void setLocalSpeed(int speed) {
        setLocalSpeed_native(pointer, speed);
    }

/**
Issues a given command to a set of units. This function automatically splits the set into groups of 12 and issues the same command to each of them. If a unit is not capable of executing the command, then it is simply ignored. Parameters units A Unitset containing all the units to issue the command for. command A UnitCommand object containing relevant information about the command to be issued. The Unit interface object associated with the command will be ignored. Returns true if any one of the units in the Unitset were capable of executing the command, and false if none of the units were capable of executing the command.
*/
    public boolean issueCommand(List<Unit> units, UnitCommand command) {
        return issueCommand_native(pointer, units, command);
    }

/**
Retrieves the set of units that are currently selected by the user outside of BWAPI. This function requires that Flag::UserInput be enabled. Returns A Unitset containing the user's selected units. If Flag::UserInput is disabled, then this set is always empty. See also enableFlag
*/
    public List<Unit> getSelectedUnits() {
        return getSelectedUnits_native(pointer);
    }

/**
Retrieves the player object that BWAPI is controlling. Returns Pointer to Player interface object representing the current player. Return values nullptr if the current game is a replay. Example usage void ExampleAIModule::onStart() { if ( BWAPI::Broodwar->self() ) BWAPI::Broodwar->sendText("Hello, my name is %s.", BWAPI::Broodwar->self()->getName().c_str()); }
*/
    public Player self() {
        return self_native(pointer);
    }

/**
Retrieves the Player interface that represents the enemy player. If there is more than one enemy, and that enemy is destroyed, then this function will still retrieve the same, defeated enemy. If you wish to handle multiple opponents, see the Game::enemies function. Returns Player interface representing an enemy player. Return values nullptr If there is no enemy or the current game is a replay. See also enemies
*/
    public Player enemy() {
        return enemy_native(pointer);
    }

/**
Retrieves the Player interface object representing the neutral player. The neutral player owns all the resources and critters on the map by default. Returns Player interface indicating the neutral player.
*/
    public Player neutral() {
        return neutral_native(pointer);
    }

/**
Retrieves a set of all the current player's remaining allies. Returns Playerset containing all allied players.
*/
    public List<Player> allies() {
        return allies_native(pointer);
    }

/**
Retrieves a set of all the current player's remaining enemies. Returns Playerset containing all enemy players.
*/
    public List<Player> enemies() {
        return enemies_native(pointer);
    }

/**
Retrieves a set of all players currently observing the game. An observer is defined typically in a Use Map Settings game type as not having any impact on the game. This means an observer cannot start with any units, and cannot have any active trigger actions that create units for it. Returns Playerset containing all currently active observer players
*/
    public List<Player> observers() {
        return observers_native(pointer);
    }

/**
Sets the size of the text for all calls to drawText following this one. Parameters size (optional) The size of the text. This value is one of Text::Size::Enum. If this value is omitted, then a default value of Text::Size::Default is used. Example usage void ExampleAIModule::onFrame() { // Centers the name of the player in the upper middle of the screen BWAPI::Broodwar->setTextSize(BWAPI::Text::Size::Large); BWAPI::Broodwar->drawTextScreen(BWAPI::Positions::Origin, "%c%c%s", BWAPI::Text::Align_Center, BWAPI::Text::Green, BWAPI::Broodwar->self()->getName().c_str() ); BWAPI::Broodwar->setTextSize(); // Set text size back to default } See also Text::Size::Enum
*/
    public void setTextSize() {
        setTextSize_native(pointer);
    }

    public void setTextSize(bwapi.Text.Size.Enum size) {
        setTextSize_native(pointer, size);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawText(bwapi.CoordinateType.Enum ctype, int x, int y, String cstr_format) {
        drawText_native(pointer, ctype, x, y, cstr_format);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTextMap(int x, int y, String cstr_format) {
        drawTextMap_native(pointer, x, y, cstr_format);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTextMap(Position p, String cstr_format) {
        drawTextMap_native(pointer, p, cstr_format);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTextMouse(int x, int y, String cstr_format) {
        drawTextMouse_native(pointer, x, y, cstr_format);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTextMouse(Position p, String cstr_format) {
        drawTextMouse_native(pointer, p, cstr_format);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTextScreen(int x, int y, String cstr_format) {
        drawTextScreen_native(pointer, x, y, cstr_format);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTextScreen(Position p, String cstr_format) {
        drawTextScreen_native(pointer, p, cstr_format);
    }

/**
Draws a rectangle on the screen with the given color. Parameters ctype The coordinate type. Indicates the relative position to draw the shape. left The x coordinate, in pixels, relative to ctype, of the left edge of the rectangle. top The y coordinate, in pixels, relative to ctype, of the top edge of the rectangle. right The x coordinate, in pixels, relative to ctype, of the right edge of the rectangle. bottom The y coordinate, in pixels, relative to ctype, of the bottom edge of the rectangle. color The color of the rectangle. isSolid (optional) If true, then the shape will be filled and drawn as a solid, otherwise it will be drawn as an outline. If omitted, this value will default to false.
*/
    public void drawBox(bwapi.CoordinateType.Enum ctype, int left, int top, int right, int bottom, Color color) {
        drawBox_native(pointer, ctype, left, top, right, bottom, color);
    }

    public void drawBox(bwapi.CoordinateType.Enum ctype, int left, int top, int right, int bottom, Color color, boolean isSolid) {
        drawBox_native(pointer, ctype, left, top, right, bottom, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawBoxMap(int left, int top, int right, int bottom, Color color) {
        drawBoxMap_native(pointer, left, top, right, bottom, color);
    }

    public void drawBoxMap(int left, int top, int right, int bottom, Color color, boolean isSolid) {
        drawBoxMap_native(pointer, left, top, right, bottom, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawBoxMap(Position leftTop, Position rightBottom, Color color) {
        drawBoxMap_native(pointer, leftTop, rightBottom, color);
    }

    public void drawBoxMap(Position leftTop, Position rightBottom, Color color, boolean isSolid) {
        drawBoxMap_native(pointer, leftTop, rightBottom, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawBoxMouse(int left, int top, int right, int bottom, Color color) {
        drawBoxMouse_native(pointer, left, top, right, bottom, color);
    }

    public void drawBoxMouse(int left, int top, int right, int bottom, Color color, boolean isSolid) {
        drawBoxMouse_native(pointer, left, top, right, bottom, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawBoxMouse(Position leftTop, Position rightBottom, Color color) {
        drawBoxMouse_native(pointer, leftTop, rightBottom, color);
    }

    public void drawBoxMouse(Position leftTop, Position rightBottom, Color color, boolean isSolid) {
        drawBoxMouse_native(pointer, leftTop, rightBottom, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawBoxScreen(int left, int top, int right, int bottom, Color color) {
        drawBoxScreen_native(pointer, left, top, right, bottom, color);
    }

    public void drawBoxScreen(int left, int top, int right, int bottom, Color color, boolean isSolid) {
        drawBoxScreen_native(pointer, left, top, right, bottom, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawBoxScreen(Position leftTop, Position rightBottom, Color color) {
        drawBoxScreen_native(pointer, leftTop, rightBottom, color);
    }

    public void drawBoxScreen(Position leftTop, Position rightBottom, Color color, boolean isSolid) {
        drawBoxScreen_native(pointer, leftTop, rightBottom, color, isSolid);
    }

/**
Draws a triangle on the screen with the given color. Parameters ctype The coordinate type. Indicates the relative position to draw the shape. ax The x coordinate, in pixels, relative to ctype, of the first point. ay The y coordinate, in pixels, relative to ctype, of the first point. bx The x coordinate, in pixels, relative to ctype, of the second point. by The y coordinate, in pixels, relative to ctype, of the second point. cx The x coordinate, in pixels, relative to ctype, of the third point. cy The y coordinate, in pixels, relative to ctype, of the third point. color The color of the triangle. isSolid (optional) If true, then the shape will be filled and drawn as a solid, otherwise it will be drawn as an outline. If omitted, this value will default to false.
*/
    public void drawTriangle(bwapi.CoordinateType.Enum ctype, int ax, int ay, int bx, int by, int cx, int cy, Color color) {
        drawTriangle_native(pointer, ctype, ax, ay, bx, by, cx, cy, color);
    }

    public void drawTriangle(bwapi.CoordinateType.Enum ctype, int ax, int ay, int bx, int by, int cx, int cy, Color color, boolean isSolid) {
        drawTriangle_native(pointer, ctype, ax, ay, bx, by, cx, cy, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTriangleMap(int ax, int ay, int bx, int by, int cx, int cy, Color color) {
        drawTriangleMap_native(pointer, ax, ay, bx, by, cx, cy, color);
    }

    public void drawTriangleMap(int ax, int ay, int bx, int by, int cx, int cy, Color color, boolean isSolid) {
        drawTriangleMap_native(pointer, ax, ay, bx, by, cx, cy, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTriangleMap(Position a, Position b, Position c, Color color) {
        drawTriangleMap_native(pointer, a, b, c, color);
    }

    public void drawTriangleMap(Position a, Position b, Position c, Color color, boolean isSolid) {
        drawTriangleMap_native(pointer, a, b, c, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTriangleMouse(int ax, int ay, int bx, int by, int cx, int cy, Color color) {
        drawTriangleMouse_native(pointer, ax, ay, bx, by, cx, cy, color);
    }

    public void drawTriangleMouse(int ax, int ay, int bx, int by, int cx, int cy, Color color, boolean isSolid) {
        drawTriangleMouse_native(pointer, ax, ay, bx, by, cx, cy, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTriangleMouse(Position a, Position b, Position c, Color color) {
        drawTriangleMouse_native(pointer, a, b, c, color);
    }

    public void drawTriangleMouse(Position a, Position b, Position c, Color color, boolean isSolid) {
        drawTriangleMouse_native(pointer, a, b, c, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTriangleScreen(int ax, int ay, int bx, int by, int cx, int cy, Color color) {
        drawTriangleScreen_native(pointer, ax, ay, bx, by, cx, cy, color);
    }

    public void drawTriangleScreen(int ax, int ay, int bx, int by, int cx, int cy, Color color, boolean isSolid) {
        drawTriangleScreen_native(pointer, ax, ay, bx, by, cx, cy, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawTriangleScreen(Position a, Position b, Position c, Color color) {
        drawTriangleScreen_native(pointer, a, b, c, color);
    }

    public void drawTriangleScreen(Position a, Position b, Position c, Color color, boolean isSolid) {
        drawTriangleScreen_native(pointer, a, b, c, color, isSolid);
    }

/**
Draws a circle on the screen with the given color. Parameters ctype The coordinate type. Indicates the relative position to draw the shape. x The x coordinate, in pixels, relative to ctype. y The y coordinate, in pixels, relative to ctype. radius The radius of the circle, in pixels. color The color of the circle. isSolid (optional) If true, then the shape will be filled and drawn as a solid, otherwise it will be drawn as an outline. If omitted, this value will default to false.
*/
    public void drawCircle(bwapi.CoordinateType.Enum ctype, int x, int y, int radius, Color color) {
        drawCircle_native(pointer, ctype, x, y, radius, color);
    }

    public void drawCircle(bwapi.CoordinateType.Enum ctype, int x, int y, int radius, Color color, boolean isSolid) {
        drawCircle_native(pointer, ctype, x, y, radius, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawCircleMap(int x, int y, int radius, Color color) {
        drawCircleMap_native(pointer, x, y, radius, color);
    }

    public void drawCircleMap(int x, int y, int radius, Color color, boolean isSolid) {
        drawCircleMap_native(pointer, x, y, radius, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawCircleMap(Position p, int radius, Color color) {
        drawCircleMap_native(pointer, p, radius, color);
    }

    public void drawCircleMap(Position p, int radius, Color color, boolean isSolid) {
        drawCircleMap_native(pointer, p, radius, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawCircleMouse(int x, int y, int radius, Color color) {
        drawCircleMouse_native(pointer, x, y, radius, color);
    }

    public void drawCircleMouse(int x, int y, int radius, Color color, boolean isSolid) {
        drawCircleMouse_native(pointer, x, y, radius, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawCircleMouse(Position p, int radius, Color color) {
        drawCircleMouse_native(pointer, p, radius, color);
    }

    public void drawCircleMouse(Position p, int radius, Color color, boolean isSolid) {
        drawCircleMouse_native(pointer, p, radius, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawCircleScreen(int x, int y, int radius, Color color) {
        drawCircleScreen_native(pointer, x, y, radius, color);
    }

    public void drawCircleScreen(int x, int y, int radius, Color color, boolean isSolid) {
        drawCircleScreen_native(pointer, x, y, radius, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawCircleScreen(Position p, int radius, Color color) {
        drawCircleScreen_native(pointer, p, radius, color);
    }

    public void drawCircleScreen(Position p, int radius, Color color, boolean isSolid) {
        drawCircleScreen_native(pointer, p, radius, color, isSolid);
    }

/**
Draws an ellipse on the screen with the given color. Parameters ctype The coordinate type. Indicates the relative position to draw the shape. x The x coordinate, in pixels, relative to ctype. y The y coordinate, in pixels, relative to ctype. xrad The x radius of the ellipse, in pixels. yrad The y radius of the ellipse, in pixels. color The color of the ellipse. isSolid (optional) If true, then the shape will be filled and drawn as a solid, otherwise it will be drawn as an outline. If omitted, this value will default to false.
*/
    public void drawEllipse(bwapi.CoordinateType.Enum ctype, int x, int y, int xrad, int yrad, Color color) {
        drawEllipse_native(pointer, ctype, x, y, xrad, yrad, color);
    }

    public void drawEllipse(bwapi.CoordinateType.Enum ctype, int x, int y, int xrad, int yrad, Color color, boolean isSolid) {
        drawEllipse_native(pointer, ctype, x, y, xrad, yrad, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawEllipseMap(int x, int y, int xrad, int yrad, Color color) {
        drawEllipseMap_native(pointer, x, y, xrad, yrad, color);
    }

    public void drawEllipseMap(int x, int y, int xrad, int yrad, Color color, boolean isSolid) {
        drawEllipseMap_native(pointer, x, y, xrad, yrad, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawEllipseMap(Position p, int xrad, int yrad, Color color) {
        drawEllipseMap_native(pointer, p, xrad, yrad, color);
    }

    public void drawEllipseMap(Position p, int xrad, int yrad, Color color, boolean isSolid) {
        drawEllipseMap_native(pointer, p, xrad, yrad, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawEllipseMouse(int x, int y, int xrad, int yrad, Color color) {
        drawEllipseMouse_native(pointer, x, y, xrad, yrad, color);
    }

    public void drawEllipseMouse(int x, int y, int xrad, int yrad, Color color, boolean isSolid) {
        drawEllipseMouse_native(pointer, x, y, xrad, yrad, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawEllipseMouse(Position p, int xrad, int yrad, Color color) {
        drawEllipseMouse_native(pointer, p, xrad, yrad, color);
    }

    public void drawEllipseMouse(Position p, int xrad, int yrad, Color color, boolean isSolid) {
        drawEllipseMouse_native(pointer, p, xrad, yrad, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawEllipseScreen(int x, int y, int xrad, int yrad, Color color) {
        drawEllipseScreen_native(pointer, x, y, xrad, yrad, color);
    }

    public void drawEllipseScreen(int x, int y, int xrad, int yrad, Color color, boolean isSolid) {
        drawEllipseScreen_native(pointer, x, y, xrad, yrad, color, isSolid);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawEllipseScreen(Position p, int xrad, int yrad, Color color) {
        drawEllipseScreen_native(pointer, p, xrad, yrad, color);
    }

    public void drawEllipseScreen(Position p, int xrad, int yrad, Color color, boolean isSolid) {
        drawEllipseScreen_native(pointer, p, xrad, yrad, color, isSolid);
    }

/**
Draws a dot on the map or screen with a given color. Parameters ctype The coordinate type. Indicates the relative position to draw the shape. x The x coordinate, in pixels, relative to ctype. y The y coordinate, in pixels, relative to ctype. color The color of the dot.
*/
    public void drawDot(bwapi.CoordinateType.Enum ctype, int x, int y, Color color) {
        drawDot_native(pointer, ctype, x, y, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawDotMap(int x, int y, Color color) {
        drawDotMap_native(pointer, x, y, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawDotMap(Position p, Color color) {
        drawDotMap_native(pointer, p, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawDotMouse(int x, int y, Color color) {
        drawDotMouse_native(pointer, x, y, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawDotMouse(Position p, Color color) {
        drawDotMouse_native(pointer, p, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawDotScreen(int x, int y, Color color) {
        drawDotScreen_native(pointer, x, y, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawDotScreen(Position p, Color color) {
        drawDotScreen_native(pointer, p, color);
    }

/**
Draws a line on the map or screen with a given color. Parameters ctype The coordinate type. Indicates the relative position to draw the shape. x1 The starting x coordinate, in pixels, relative to ctype. y1 The starting y coordinate, in pixels, relative to ctype. x2 The ending x coordinate, in pixels, relative to ctype. y2 The ending y coordinate, in pixels, relative to ctype. color The color of the line.
*/
    public void drawLine(bwapi.CoordinateType.Enum ctype, int x1, int y1, int x2, int y2, Color color) {
        drawLine_native(pointer, ctype, x1, y1, x2, y2, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawLineMap(int x1, int y1, int x2, int y2, Color color) {
        drawLineMap_native(pointer, x1, y1, x2, y2, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawLineMap(Position a, Position b, Color color) {
        drawLineMap_native(pointer, a, b, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawLineMouse(int x1, int y1, int x2, int y2, Color color) {
        drawLineMouse_native(pointer, x1, y1, x2, y2, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawLineMouse(Position a, Position b, Color color) {
        drawLineMouse_native(pointer, a, b, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawLineScreen(int x1, int y1, int x2, int y2, Color color) {
        drawLineScreen_native(pointer, x1, y1, x2, y2, color);
    }

/**
This is an overloaded member function, provided for convenience. It differs from the above function only in what argument(s) it accepts.
*/
    public void drawLineScreen(Position a, Position b, Color color) {
        drawLineScreen_native(pointer, a, b, color);
    }

/**
Retrieves the maximum delay, in number of frames, between a command being issued and the command being executed by Broodwar. Note In Broodwar, latency is used to keep the game synchronized between players without introducing lag. Returns Difference in frames between commands being sent and executed. See also getLatencyTime, getRemainingLatencyFrames
*/
    public int getLatencyFrames() {
        return getLatencyFrames_native(pointer);
    }

/**
Retrieves the maximum delay, in milliseconds, between a command being issued and the command being executed by Broodwar. Returns Difference in milliseconds between commands being sent and executed. See also getLatencyFrames, getRemainingLatencyTime
*/
    public int getLatencyTime() {
        return getLatencyTime_native(pointer);
    }

/**
Retrieves the number of frames it will take before a command sent in the current frame will be executed by the game. Returns Number of frames until a command is executed if it were sent in the current frame. See also getRemainingLatencyTime, getLatencyFrames
*/
    public int getRemainingLatencyFrames() {
        return getRemainingLatencyFrames_native(pointer);
    }

/**
Retrieves the number of milliseconds it will take before a command sent in the current frame will be executed by Broodwar. Returns Amount of time, in milliseconds, until a command is executed if it were sent in the current frame. See also getRemainingLatencyFrames, getLatencyTime
*/
    public int getRemainingLatencyTime() {
        return getRemainingLatencyTime_native(pointer);
    }

/**
Retrieves the current revision of BWAPI. Returns The revision number of the current BWAPI interface. Note This function is considered thread-safe.
*/
    public int getRevision() {
        return getRevision_native(pointer);
    }

/**
Retrieves the debug state of the BWAPI build. Returns true if the BWAPI module is a DEBUG build, and false if it is a RELEASE build. Note This function is considered thread-safe.
*/
    public boolean isDebug() {
        return isDebug_native(pointer);
    }

/**
Checks the state of latency compensation. Returns true if latency compensation is enabled, false if it is disabled. See also setLatCom
*/
    public boolean isLatComEnabled() {
        return isLatComEnabled_native(pointer);
    }

/**
Changes the state of latency compensation. Latency compensation modifies the state of BWAPI's representation of units to reflect the implications of issuing a command immediately after the command was performed, instead of waiting consecutive frames for the results. Latency compensation is enabled by default. Parameters isEnabled Set whether the latency compensation feature will be enabled (true) or disabled (false). See also isLatComEnabled.
*/
    public void setLatCom(boolean isEnabled) {
        setLatCom_native(pointer, isEnabled);
    }

/**
Checks if the GUI is enabled. The GUI includes all drawing functions of BWAPI, as well as screen updates from Broodwar. Return values true If the GUI is enabled, and everything is visible false If the GUI is disabled and drawing functions are rejected See also setGUI
*/
    public boolean isGUIEnabled() {
        return isGUIEnabled_native(pointer);
    }

/**
Sets the rendering state of the Starcraft GUI. This typically gives Starcraft a very low graphical frame rate and disables all drawing functionality in BWAPI. Parameters enabled A boolean value that determines the state of the GUI. Passing false to this function will disable the GUI, and true will enable it. Example Usage: void ExampleAIModule::onStart() { // Make our bot run thousands of games as fast as possible! Broodwar->setLocalSpeed(0); Broodwar->setGUI(false); } See also isGUIEnabled
*/
    public void setGUI(boolean enabled) {
        setGUI_native(pointer, enabled);
    }

/**
Retrieves the Starcraft instance number recorded by BWAPI to identify which Starcraft instance an AI module belongs to. The very first instance should return 0. Returns An integer value representing the instance number. Note This function is considered thread-safe.
*/
    public int getInstanceNumber() {
        return getInstanceNumber_native(pointer);
    }

/**
Retrieves the Actions Per Minute (APM) that the bot is producing. Parameters includeSelects (optional) If true, the return value will include selections as individual commands, otherwise it will exclude selections. This value is false by default. Returns The number of actions that the bot has executed per minute, on average.
*/
    public int getAPM() {
        return getAPM_native(pointer);
    }

    public int getAPM(boolean includeSelects) {
        return getAPM_native(pointer, includeSelects);
    }

/**
Changes the map to the one specified. Once restarted, the game will load the map that was provided. Changes do not take effect unless the game is restarted. Parameters mapFileName A string containing the path and file name to the desired map. Return values true if the function succeeded and has changed the map. false if the function failed, does not have permission from the tournament module, failed to find the map specified, or received an invalid parameter.
*/
    public boolean setMap(String cstr_mapFileName) {
        return setMap_native(pointer, cstr_mapFileName);
    }

/**
Sets the number of graphical frames for every logical frame. This allows the game to run more logical frames per graphical frame, increasing the speed at which the game runs. Parameters frameSkip Number of graphical frames per logical frame. If this value is 0 or less, then it will default to 1. See also setLocalSpeed
*/
    public void setFrameSkip(int frameSkip) {
        setFrameSkip_native(pointer, frameSkip);
    }

/**
Checks if there is a path from source to destination. This only checks if the source position is connected to the destination position. This function does not check if all units can actually travel from source to destination. Because of this limitation, it has an O(1) complexity, and cases where this limitation hinders gameplay is uncommon at best. Parameters source The source position. destination The destination position. Return values true if there is a path between the two positions false if there is no path
*/
    public boolean hasPath(Position source, Position destination) {
        return hasPath_native(pointer, source, destination);
    }

/**
Sets the alliance state of the current player with the target player. Parameters player The target player to set alliance with. allied (optional) If true, the current player will ally the target player. If false, the current player will make the target player an enemy. This value is true by default. alliedVictory (optional) Sets the state of "allied victory". If true, the game will end in a victory if all allied players have eliminated their opponents. Otherwise, the game will only end if no other players are remaining in the game. This value is true by default.
*/
    public boolean setAlliance(Player player, boolean allied) {
        return setAlliance_native(pointer, player, allied);
    }

    public boolean setAlliance(Player player) {
        return setAlliance_native(pointer, player);
    }

    public boolean setAlliance(Player player, boolean allied, boolean alliedVictory) {
        return setAlliance_native(pointer, player, allied, alliedVictory);
    }

/**
In a game, this function sets the vision of the current BWAPI player with the target player. In a replay, this function toggles the visibility of the target player. Parameters player The target player to toggle vision. enabled (optional) The vision state. If true, and in a game, the current player will enable shared vision with the target player, otherwise it will unshare vision. If in a replay, the vision of the target player will be shown, otherwise the target player will be hidden. This value is true by default.
*/
    public boolean setVision(Player player) {
        return setVision_native(pointer, player);
    }

    public boolean setVision(Player player, boolean enabled) {
        return setVision_native(pointer, player, enabled);
    }

/**
Retrieves current amount of time in seconds that the game has elapsed. Returns Time, in seconds, that the game has elapsed as an integer.
*/
    public int elapsedTime() {
        return elapsedTime_native(pointer);
    }

/**
Sets the command optimization level. Command optimization is a feature in BWAPI that tries to reduce the APM of the bot by grouping or eliminating unnecessary game actions. For example, suppose the bot told 24 Zerglings to Burrow. At command optimization level 0, BWAPI is designed to select each Zergling to burrow individually, which costs 48 actions. With command optimization level 1, it can perform the same behaviour using only 4 actions. The command optimizer also reduces the amount of bytes used for each action if it can express the same action using a different command. For example, Right_Click uses less bytes than Move. Parameters level An integer representation of the aggressiveness for which commands are optimized. A lower level means less optimization, and a higher level means more optimization. The values for level are as follows: 0: No optimization. 1: Some optimization. Is not detected as a hack. Does not alter behaviour. Units performing the following actions are grouped and ordered 12 at a time: Attack_Unit Morph (Larva only) Hold_Position Stop Follow Gather Return_Cargo Repair Burrow Unburrow Cloak Decloak Siege Unsiege Right_Click_Unit Halt_Construction Cancel_Train (Carrier and Reaver only) Cancel_Train_Slot (Carrier and Reaver only) Cancel_Morph (for non-buildings only) Use_Tech Use_Tech_Unit The following order transformations are applied to allow better grouping: Attack_Unit becomes Right_Click_Unit if the target is an enemy Move becomes Right_Click_Position Gather becomes Right_Click_Unit if the target contains resources Set_Rally_Position becomes Right_Click_Position for buildings Set_Rally_Unit becomes Right_Click_Unit for buildings Use_Tech_Unit with Infestation becomes Right_Click_Unit if the target is valid 2: More optimization by grouping structures. Includes the optimizations made by all previous levels. May be detected as a hack by some replay utilities. Does not alter behaviour. Units performing the following actions are grouped and ordered 12 at a time: Attack_Unit (Turrets, Photon Cannons, Sunkens, Spores) Train Morph Set_Rally_Unit Lift Cancel_Construction Cancel_Addon Cancel_Train Cancel_Train_Slot Cancel_Morph Cancel_Research Cancel_Upgrade 3: Extensive optimization Includes the optimizations made by all previous levels. Units may behave or move differently than expected. Units performing the following actions are grouped and ordered 12 at a time: Attack_Move Set_Rally_Position Move Patrol Unload_All Unload_All_Position Right_Click_Position Use_Tech_Position 4: Aggressive optimization Includes the optimizations made by all previous levels. Positions used in commands will be rounded to multiples of 32. High Templar and Dark Templar that merge into Archons will be grouped and may choose a different target to merge with. It will not merge with a target that wasn't included.
*/
    public void setCommandOptimizationLevel(int level) {
        setCommandOptimizationLevel_native(pointer, level);
    }

/**
Returns the remaining countdown time. The countdown timer is used in Capture The Flag and Use Map Settings game types. Example usage: void ExampleAIModule::onStart() { // Register a callback that only occurs once when the countdown timer reaches 0 if ( BWAPI::Broodwar->getGameType() == BWAPI::GameTypes::Capture_The_Flag || BWAPI::Broodwar->getGameType() == BWAPI::GameTypes::Team_Capture_The_Flag ) { BWAPI::Broodwar->registerEvent([](BWAPI::Game*){ BWAPI::Broodwar->sendText("Try to find my flag!"); }, // action [](BWAPI::Game*){ return BWAPI::Broodwar->countdownTimer() == 0; }, // condition 1); // times to run (once) } } Returns Integer containing the time (in game seconds) on the countdown timer.
*/
    public int countdownTimer() {
        return countdownTimer_native(pointer);
    }

/**
Retrieves the set of all regions on the map. Returns Regionset containing all map regions.
*/
    public List<Region> getAllRegions() {
        return getAllRegions_native(pointer);
    }

/**
Retrieves the region at a given position. Parameters x The x coordinate, in pixels. y The y coordinate, in pixels. Returns Pointer to the Region interface at the given position. Return values nullptr if the provided position is not valid (i.e. not within the map bounds). Note If the provided position is invalid, the error Errors::Invalid_Parameter is set. See also getAllRegions, getRegion
*/
    public Region getRegionAt(int x, int y) {
        return getRegionAt_native(pointer, x, y);
    }

/**
Retrieves the region at a given position. Parameters x The x coordinate, in pixels. y The y coordinate, in pixels. Returns Pointer to the Region interface at the given position. Return values nullptr if the provided position is not valid (i.e. not within the map bounds). Note If the provided position is invalid, the error Errors::Invalid_Parameter is set. See also getAllRegions, getRegion
*/
    public Region getRegionAt(Position position) {
        return getRegionAt_native(pointer, position);
    }

/**
Retrieves the amount of time (in milliseconds) that has elapsed when running the last AI module callback. This is used by tournament modules to penalize AI modules that use too much processing time. Return values 0 When called from an AI module. Returns Time in milliseconds spent in last AI module call.
*/
    public int getLastEventTime() {
        return getLastEventTime_native(pointer);
    }

/**
Sets the state of the fog of war when watching a replay. Parameters reveal (optional) The state of the reveal all flag. If false, all fog of war will be enabled. If true, then the fog of war will be revealed. It is true by default.
*/
    public boolean setRevealAll() {
        return setRevealAll_native(pointer);
    }

    public boolean setRevealAll(boolean reveal) {
        return setRevealAll_native(pointer, reveal);
    }

/**
Retrieves a basic build position just as the default Computer AI would. This allows users to find simple build locations without relying on external libraries. Parameters type A valid UnitType representing the unit type to accomodate space for. desiredPosition A valid TilePosition containing the desired placement position. maxRange (optional) The maximum distance (in tiles) to build from desiredPosition. creep (optional) A special boolean value that changes the behaviour of Creep Colony placement. Return values TilePositions::Invalid If a build location could not be found within maxRange. Returns A TilePosition containing the location that the structure should be constructed at.
*/
    public TilePosition getBuildLocation(UnitType type, TilePosition desiredPosition, int maxRange) {
        return getBuildLocation_native(pointer, type, desiredPosition, maxRange);
    }

    public TilePosition getBuildLocation(UnitType type, TilePosition desiredPosition) {
        return getBuildLocation_native(pointer, type, desiredPosition);
    }

    public TilePosition getBuildLocation(UnitType type, TilePosition desiredPosition, int maxRange, boolean creep) {
        return getBuildLocation_native(pointer, type, desiredPosition, maxRange, creep);
    }

/**
Calculates the damage received for a given player. It can be understood as the damage from fromType to toType. Does not include shields in calculation. Includes upgrades if players are provided. Parameters fromType The unit type that will be dealing the damage. toType The unit type that will be receiving the damage. fromPlayer (optional) The player owner of the given type that will be dealing the damage. If omitted, then no player will be used to calculate the upgrades for fromType. toPlayer (optional) The player owner of the type that will be receiving the damage. If omitted, then this parameter will default to Broodwar->self(). Returns The amount of damage that fromType would deal to toType. See also getDamageTo
*/
    public int getDamageFrom(UnitType fromType, UnitType toType, Player fromPlayer) {
        return getDamageFrom_native(pointer, fromType, toType, fromPlayer);
    }

    public int getDamageFrom(UnitType fromType, UnitType toType) {
        return getDamageFrom_native(pointer, fromType, toType);
    }

    public int getDamageFrom(UnitType fromType, UnitType toType, Player fromPlayer, Player toPlayer) {
        return getDamageFrom_native(pointer, fromType, toType, fromPlayer, toPlayer);
    }

/**
Calculates the damage dealt for a given player. It can be understood as the damage to toType from fromType. Does not include shields in calculation. Includes upgrades if players are provided. Note This function is nearly the same as getDamageFrom. The only difference is that the last parameter is intended to default to Broodwar->self(). Parameters toType The unit type that will be receiving the damage. fromType The unit type that will be dealing the damage. toPlayer (optional) The player owner of the type that will be receiving the damage. If omitted, then no player will be used to calculate the upgrades for toType. fromPlayer (optional) The player owner of the given type that will be dealing the damage. If omitted, then this parameter will default to Broodwar->self(). Returns The amount of damage that fromType would deal to toType. See also getDamageFrom
*/
    public int getDamageTo(UnitType toType, UnitType fromType, Player toPlayer) {
        return getDamageTo_native(pointer, toType, fromType, toPlayer);
    }

    public int getDamageTo(UnitType toType, UnitType fromType) {
        return getDamageTo_native(pointer, toType, fromType);
    }

    public int getDamageTo(UnitType toType, UnitType fromType, Player toPlayer, Player fromPlayer) {
        return getDamageTo_native(pointer, toType, fromType, toPlayer, fromPlayer);
    }


    private static Map<Long, Game> instances = new HashMap<Long, Game>();

    private Game(long pointer) {
        this.pointer = pointer;
    }

    private static Game get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Game instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Game(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native List<Force> getForces_native(long pointer);

    private native List<Player> getPlayers_native(long pointer);

    private native List<Unit> getAllUnits_native(long pointer);

    private native List<Unit> getMinerals_native(long pointer);

    private native List<Unit> getGeysers_native(long pointer);

    private native List<Unit> getNeutralUnits_native(long pointer);

    private native List<Unit> getStaticMinerals_native(long pointer);

    private native List<Unit> getStaticGeysers_native(long pointer);

    private native List<Unit> getStaticNeutralUnits_native(long pointer);

    private native List<Bullet> getBullets_native(long pointer);

    private native List<Position> getNukeDots_native(long pointer);

    private native Force getForce_native(long pointer, int forceID);

    private native Player getPlayer_native(long pointer, int playerID);

    private native Unit getUnit_native(long pointer, int unitID);

    private native Unit indexToUnit_native(long pointer, int unitIndex);

    private native Region getRegion_native(long pointer, int regionID);

    private native GameType getGameType_native(long pointer);

    private native int getLatency_native(long pointer);

    private native int getFrameCount_native(long pointer);

    private native int getReplayFrameCount_native(long pointer);

    private native int getFPS_native(long pointer);

    private native double getAverageFPS_native(long pointer);

    private native Position getMousePosition_native(long pointer);

    private native boolean getMouseState_native(long pointer, MouseButton button);

    private native boolean getKeyState_native(long pointer, Key key);

    private native Position getScreenPosition_native(long pointer);

    private native void setScreenPosition_native(long pointer, int x, int y);

    private native void setScreenPosition_native(long pointer, Position p);

    private native void pingMinimap_native(long pointer, int x, int y);

    private native void pingMinimap_native(long pointer, Position p);

    private native boolean isFlagEnabled_native(long pointer, int flag);

    private native void enableFlag_native(long pointer, int flag);

    private native List<Unit> getUnitsOnTile_native(long pointer, int tileX, int tileY);

    private native List<Unit> getUnitsOnTile_native(long pointer, TilePosition tile);

    private native List<Unit> getUnitsInRectangle_native(long pointer, int left, int top, int right, int bottom);

    private native List<Unit> getUnitsInRectangle_native(long pointer, Position topLeft, Position bottomRight);

    private native List<Unit> getUnitsInRadius_native(long pointer, int x, int y, int radius);

    private native List<Unit> getUnitsInRadius_native(long pointer, Position center, int radius);

    private native Error getLastError_native(long pointer);

    private native boolean setLastError_native(long pointer);

    private native boolean setLastError_native(long pointer, Error e);

    private native int mapWidth_native(long pointer);

    private native int mapHeight_native(long pointer);

    private native String mapFileName_native(long pointer);

    private native String mapPathName_native(long pointer);

    private native String mapName_native(long pointer);

    private native String mapHash_native(long pointer);

    private native boolean isWalkable_native(long pointer, int walkX, int walkY);

    private native boolean isWalkable_native(long pointer, WalkPosition position);

    private native int getGroundHeight_native(long pointer, int tileX, int tileY);

    private native int getGroundHeight_native(long pointer, TilePosition position);

    private native boolean isBuildable_native(long pointer, int tileX, int tileY);

    private native boolean isBuildable_native(long pointer, int tileX, int tileY, boolean includeBuildings);

    private native boolean isBuildable_native(long pointer, TilePosition position);

    private native boolean isBuildable_native(long pointer, TilePosition position, boolean includeBuildings);

    private native boolean isVisible_native(long pointer, int tileX, int tileY);

    private native boolean isVisible_native(long pointer, TilePosition position);

    private native boolean isExplored_native(long pointer, int tileX, int tileY);

    private native boolean isExplored_native(long pointer, TilePosition position);

    private native boolean hasCreep_native(long pointer, int tileX, int tileY);

    private native boolean hasCreep_native(long pointer, TilePosition position);

    private native boolean hasPowerPrecise_native(long pointer, int x, int y);

    private native boolean hasPowerPrecise_native(long pointer, int x, int y, UnitType unitType);

    private native boolean hasPowerPrecise_native(long pointer, Position position);

    private native boolean hasPowerPrecise_native(long pointer, Position position, UnitType unitType);

    private native boolean hasPower_native(long pointer, int tileX, int tileY);

    private native boolean hasPower_native(long pointer, int tileX, int tileY, UnitType unitType);

    private native boolean hasPower_native(long pointer, TilePosition position);

    private native boolean hasPower_native(long pointer, TilePosition position, UnitType unitType);

    private native boolean hasPower_native(long pointer, int tileX, int tileY, int tileWidth, int tileHeight);

    private native boolean hasPower_native(long pointer, int tileX, int tileY, int tileWidth, int tileHeight, UnitType unitType);

    private native boolean hasPower_native(long pointer, TilePosition position, int tileWidth, int tileHeight);

    private native boolean hasPower_native(long pointer, TilePosition position, int tileWidth, int tileHeight, UnitType unitType);

    private native boolean canBuildHere_native(long pointer, TilePosition position, UnitType type, Unit builder);

    private native boolean canBuildHere_native(long pointer, TilePosition position, UnitType type);

    private native boolean canBuildHere_native(long pointer, TilePosition position, UnitType type, Unit builder, boolean checkExplored);

    private native boolean canMake_native(long pointer, UnitType type);

    private native boolean canMake_native(long pointer, UnitType type, Unit builder);

    private native boolean canResearch_native(long pointer, TechType type, Unit unit);

    private native boolean canResearch_native(long pointer, TechType type);

    private native boolean canResearch_native(long pointer, TechType type, Unit unit, boolean checkCanIssueCommandType);

    private native boolean canUpgrade_native(long pointer, UpgradeType type, Unit unit);

    private native boolean canUpgrade_native(long pointer, UpgradeType type);

    private native boolean canUpgrade_native(long pointer, UpgradeType type, Unit unit, boolean checkCanIssueCommandType);

    private native List<TilePosition> getStartLocations_native(long pointer);

    private native void printf_native(long pointer, String cstr_format);

    private native void sendText_native(long pointer, String cstr_format);

    private native void sendTextEx_native(long pointer, boolean toAllies, String cstr_format);

    private native boolean isInGame_native(long pointer);

    private native boolean isMultiplayer_native(long pointer);

    private native boolean isBattleNet_native(long pointer);

    private native boolean isPaused_native(long pointer);

    private native boolean isReplay_native(long pointer);

    private native void pauseGame_native(long pointer);

    private native void resumeGame_native(long pointer);

    private native void leaveGame_native(long pointer);

    private native void restartGame_native(long pointer);

    private native void setLocalSpeed_native(long pointer, int speed);

    private native boolean issueCommand_native(long pointer, List<Unit> units, UnitCommand command);

    private native List<Unit> getSelectedUnits_native(long pointer);

    private native Player self_native(long pointer);

    private native Player enemy_native(long pointer);

    private native Player neutral_native(long pointer);

    private native List<Player> allies_native(long pointer);

    private native List<Player> enemies_native(long pointer);

    private native List<Player> observers_native(long pointer);

    private native void setTextSize_native(long pointer);

    private native void setTextSize_native(long pointer, bwapi.Text.Size.Enum size);

    private native void drawText_native(long pointer, bwapi.CoordinateType.Enum ctype, int x, int y, String cstr_format);

    private native void drawTextMap_native(long pointer, int x, int y, String cstr_format);

    private native void drawTextMap_native(long pointer, Position p, String cstr_format);

    private native void drawTextMouse_native(long pointer, int x, int y, String cstr_format);

    private native void drawTextMouse_native(long pointer, Position p, String cstr_format);

    private native void drawTextScreen_native(long pointer, int x, int y, String cstr_format);

    private native void drawTextScreen_native(long pointer, Position p, String cstr_format);

    private native void drawBox_native(long pointer, bwapi.CoordinateType.Enum ctype, int left, int top, int right, int bottom, Color color);

    private native void drawBox_native(long pointer, bwapi.CoordinateType.Enum ctype, int left, int top, int right, int bottom, Color color, boolean isSolid);

    private native void drawBoxMap_native(long pointer, int left, int top, int right, int bottom, Color color);

    private native void drawBoxMap_native(long pointer, int left, int top, int right, int bottom, Color color, boolean isSolid);

    private native void drawBoxMap_native(long pointer, Position leftTop, Position rightBottom, Color color);

    private native void drawBoxMap_native(long pointer, Position leftTop, Position rightBottom, Color color, boolean isSolid);

    private native void drawBoxMouse_native(long pointer, int left, int top, int right, int bottom, Color color);

    private native void drawBoxMouse_native(long pointer, int left, int top, int right, int bottom, Color color, boolean isSolid);

    private native void drawBoxMouse_native(long pointer, Position leftTop, Position rightBottom, Color color);

    private native void drawBoxMouse_native(long pointer, Position leftTop, Position rightBottom, Color color, boolean isSolid);

    private native void drawBoxScreen_native(long pointer, int left, int top, int right, int bottom, Color color);

    private native void drawBoxScreen_native(long pointer, int left, int top, int right, int bottom, Color color, boolean isSolid);

    private native void drawBoxScreen_native(long pointer, Position leftTop, Position rightBottom, Color color);

    private native void drawBoxScreen_native(long pointer, Position leftTop, Position rightBottom, Color color, boolean isSolid);

    private native void drawTriangle_native(long pointer, bwapi.CoordinateType.Enum ctype, int ax, int ay, int bx, int by, int cx, int cy, Color color);

    private native void drawTriangle_native(long pointer, bwapi.CoordinateType.Enum ctype, int ax, int ay, int bx, int by, int cx, int cy, Color color, boolean isSolid);

    private native void drawTriangleMap_native(long pointer, int ax, int ay, int bx, int by, int cx, int cy, Color color);

    private native void drawTriangleMap_native(long pointer, int ax, int ay, int bx, int by, int cx, int cy, Color color, boolean isSolid);

    private native void drawTriangleMap_native(long pointer, Position a, Position b, Position c, Color color);

    private native void drawTriangleMap_native(long pointer, Position a, Position b, Position c, Color color, boolean isSolid);

    private native void drawTriangleMouse_native(long pointer, int ax, int ay, int bx, int by, int cx, int cy, Color color);

    private native void drawTriangleMouse_native(long pointer, int ax, int ay, int bx, int by, int cx, int cy, Color color, boolean isSolid);

    private native void drawTriangleMouse_native(long pointer, Position a, Position b, Position c, Color color);

    private native void drawTriangleMouse_native(long pointer, Position a, Position b, Position c, Color color, boolean isSolid);

    private native void drawTriangleScreen_native(long pointer, int ax, int ay, int bx, int by, int cx, int cy, Color color);

    private native void drawTriangleScreen_native(long pointer, int ax, int ay, int bx, int by, int cx, int cy, Color color, boolean isSolid);

    private native void drawTriangleScreen_native(long pointer, Position a, Position b, Position c, Color color);

    private native void drawTriangleScreen_native(long pointer, Position a, Position b, Position c, Color color, boolean isSolid);

    private native void drawCircle_native(long pointer, bwapi.CoordinateType.Enum ctype, int x, int y, int radius, Color color);

    private native void drawCircle_native(long pointer, bwapi.CoordinateType.Enum ctype, int x, int y, int radius, Color color, boolean isSolid);

    private native void drawCircleMap_native(long pointer, int x, int y, int radius, Color color);

    private native void drawCircleMap_native(long pointer, int x, int y, int radius, Color color, boolean isSolid);

    private native void drawCircleMap_native(long pointer, Position p, int radius, Color color);

    private native void drawCircleMap_native(long pointer, Position p, int radius, Color color, boolean isSolid);

    private native void drawCircleMouse_native(long pointer, int x, int y, int radius, Color color);

    private native void drawCircleMouse_native(long pointer, int x, int y, int radius, Color color, boolean isSolid);

    private native void drawCircleMouse_native(long pointer, Position p, int radius, Color color);

    private native void drawCircleMouse_native(long pointer, Position p, int radius, Color color, boolean isSolid);

    private native void drawCircleScreen_native(long pointer, int x, int y, int radius, Color color);

    private native void drawCircleScreen_native(long pointer, int x, int y, int radius, Color color, boolean isSolid);

    private native void drawCircleScreen_native(long pointer, Position p, int radius, Color color);

    private native void drawCircleScreen_native(long pointer, Position p, int radius, Color color, boolean isSolid);

    private native void drawEllipse_native(long pointer, bwapi.CoordinateType.Enum ctype, int x, int y, int xrad, int yrad, Color color);

    private native void drawEllipse_native(long pointer, bwapi.CoordinateType.Enum ctype, int x, int y, int xrad, int yrad, Color color, boolean isSolid);

    private native void drawEllipseMap_native(long pointer, int x, int y, int xrad, int yrad, Color color);

    private native void drawEllipseMap_native(long pointer, int x, int y, int xrad, int yrad, Color color, boolean isSolid);

    private native void drawEllipseMap_native(long pointer, Position p, int xrad, int yrad, Color color);

    private native void drawEllipseMap_native(long pointer, Position p, int xrad, int yrad, Color color, boolean isSolid);

    private native void drawEllipseMouse_native(long pointer, int x, int y, int xrad, int yrad, Color color);

    private native void drawEllipseMouse_native(long pointer, int x, int y, int xrad, int yrad, Color color, boolean isSolid);

    private native void drawEllipseMouse_native(long pointer, Position p, int xrad, int yrad, Color color);

    private native void drawEllipseMouse_native(long pointer, Position p, int xrad, int yrad, Color color, boolean isSolid);

    private native void drawEllipseScreen_native(long pointer, int x, int y, int xrad, int yrad, Color color);

    private native void drawEllipseScreen_native(long pointer, int x, int y, int xrad, int yrad, Color color, boolean isSolid);

    private native void drawEllipseScreen_native(long pointer, Position p, int xrad, int yrad, Color color);

    private native void drawEllipseScreen_native(long pointer, Position p, int xrad, int yrad, Color color, boolean isSolid);

    private native void drawDot_native(long pointer, bwapi.CoordinateType.Enum ctype, int x, int y, Color color);

    private native void drawDotMap_native(long pointer, int x, int y, Color color);

    private native void drawDotMap_native(long pointer, Position p, Color color);

    private native void drawDotMouse_native(long pointer, int x, int y, Color color);

    private native void drawDotMouse_native(long pointer, Position p, Color color);

    private native void drawDotScreen_native(long pointer, int x, int y, Color color);

    private native void drawDotScreen_native(long pointer, Position p, Color color);

    private native void drawLine_native(long pointer, bwapi.CoordinateType.Enum ctype, int x1, int y1, int x2, int y2, Color color);

    private native void drawLineMap_native(long pointer, int x1, int y1, int x2, int y2, Color color);

    private native void drawLineMap_native(long pointer, Position a, Position b, Color color);

    private native void drawLineMouse_native(long pointer, int x1, int y1, int x2, int y2, Color color);

    private native void drawLineMouse_native(long pointer, Position a, Position b, Color color);

    private native void drawLineScreen_native(long pointer, int x1, int y1, int x2, int y2, Color color);

    private native void drawLineScreen_native(long pointer, Position a, Position b, Color color);

    private native int getLatencyFrames_native(long pointer);

    private native int getLatencyTime_native(long pointer);

    private native int getRemainingLatencyFrames_native(long pointer);

    private native int getRemainingLatencyTime_native(long pointer);

    private native int getRevision_native(long pointer);

    private native boolean isDebug_native(long pointer);

    private native boolean isLatComEnabled_native(long pointer);

    private native void setLatCom_native(long pointer, boolean isEnabled);

    private native boolean isGUIEnabled_native(long pointer);

    private native void setGUI_native(long pointer, boolean enabled);

    private native int getInstanceNumber_native(long pointer);

    private native int getAPM_native(long pointer);

    private native int getAPM_native(long pointer, boolean includeSelects);

    private native boolean setMap_native(long pointer, String cstr_mapFileName);

    private native void setFrameSkip_native(long pointer, int frameSkip);

    private native boolean hasPath_native(long pointer, Position source, Position destination);

    private native boolean setAlliance_native(long pointer, Player player, boolean allied);

    private native boolean setAlliance_native(long pointer, Player player);

    private native boolean setAlliance_native(long pointer, Player player, boolean allied, boolean alliedVictory);

    private native boolean setVision_native(long pointer, Player player);

    private native boolean setVision_native(long pointer, Player player, boolean enabled);

    private native int elapsedTime_native(long pointer);

    private native void setCommandOptimizationLevel_native(long pointer, int level);

    private native int countdownTimer_native(long pointer);

    private native List<Region> getAllRegions_native(long pointer);

    private native Region getRegionAt_native(long pointer, int x, int y);

    private native Region getRegionAt_native(long pointer, Position position);

    private native int getLastEventTime_native(long pointer);

    private native boolean setRevealAll_native(long pointer);

    private native boolean setRevealAll_native(long pointer, boolean reveal);

    private native TilePosition getBuildLocation_native(long pointer, UnitType type, TilePosition desiredPosition, int maxRange);

    private native TilePosition getBuildLocation_native(long pointer, UnitType type, TilePosition desiredPosition);

    private native TilePosition getBuildLocation_native(long pointer, UnitType type, TilePosition desiredPosition, int maxRange, boolean creep);

    private native int getDamageFrom_native(long pointer, UnitType fromType, UnitType toType, Player fromPlayer);

    private native int getDamageFrom_native(long pointer, UnitType fromType, UnitType toType);

    private native int getDamageFrom_native(long pointer, UnitType fromType, UnitType toType, Player fromPlayer, Player toPlayer);

    private native int getDamageTo_native(long pointer, UnitType toType, UnitType fromType, Player toPlayer);

    private native int getDamageTo_native(long pointer, UnitType toType, UnitType fromType);

    private native int getDamageTo_native(long pointer, UnitType toType, UnitType fromType, Player toPlayer, Player fromPlayer);


}
