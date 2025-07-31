package game;


import game.achievements.PlayerStatsTracker;
import game.core.*;
import game.utility.Logger;
import game.core.SpaceObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the game information and state. Stores and manipulates the game state.
 */
public class GameModel {

    public static final int GAME_HEIGHT = 20;
    public static final int GAME_WIDTH = 10;
    public static final int START_SPAWN_RATE = 2; // spawn rate (percentage chance per tick)
    public static final int SPAWN_RATE_INCREASE = 5; // Increase spawn rate by 5% per level
    public static final int START_LEVEL = 1; // Starting level value
    public static final int SCORE_THRESHOLD = 100; // Score threshold for leveling
    public static final int ASTEROID_DAMAGE = 10; // The amount of damage an asteroid deals
    public static final int ENEMY_DAMAGE = 20; // The amount of damage an enemy deals
    public static final double ENEMY_SPAWN_RATE = 0.5; // Percentage of asteroid spawn chance
    public static final double POWER_UP_SPAWN_RATE = 0.25; // Percentage of asteroid spawn chance

    private final Random random = new Random(); // ONLY USED IN this.spawnObjects()
    private final List<SpaceObject> spaceObjects; // List of all objects
    private Ship boat; // Core.Ship starts at (5, 10) with 100 health
    private int lvl; // The current game level
    private int spawnRate; // The current game spawn rate
    private Logger wrter; // The Logger reference used for logging.

    private PlayerStatsTracker statsTracker = new PlayerStatsTracker();
    private boolean verbose;
    /**
     * Models a game, storing and modifying data relevant to the game.
     *
     * Logger argument should be a method reference to a .log method such as the UI.log method.
     * Example: Model gameModel = new GameModel(ui::log)
     *
     * - Instantiates an empty list for storing all SpaceObjects (except the ship) that the model needs to track.
     * - Instantiates the game level with the starting level value.
     * - Instantiates the game spawn rate with the starting spawn rate.
     * - Instantiates a new ship. (The ship should not be stored in the SpaceObjects list)
     * - Stores reference to the given logger.
     *
     * @param wrter a functional interface for passing information between classes.
     */

    public GameModel(Logger wrter) {
        spaceObjects = new ArrayList<>();
        lvl = START_LEVEL;
        spawnRate = START_SPAWN_RATE;
        boat = new Ship();
        this.wrter = wrter;
        this.verbose = false;
    }


    /**
     * Models a game, storing and modifying data relevant to the game.
     * Logger argument should be a method reference to a .log method such as the UI.log method.
     * - Instantiates an empty list for storing all SpaceObjects the model needs to track.
     * - Instantiates the game level with the starting level value.
     * - Instantiates the game spawn rate with the starting spawn rate.
     * - Instantiates a new ship.
     * - Stores reference to the given Logger.
     * - Stores reference to the given PlayerStatsTracker.
     *
     * @param logger - a functional interface for passing information between classes.
     * @param statsTracker - a PlayerStatsTracker instance to record stats.
     */
    public GameModel(Logger logger,
                     PlayerStatsTracker statsTracker) {
        spaceObjects = new ArrayList<>();
        lvl = START_LEVEL;
        spawnRate = START_SPAWN_RATE;
        boat = new Ship();
        this.wrter = logger;
        this.statsTracker = statsTracker;
    }


    /**
     * Returns the ship instance in the game.
     *
     * @return the current ship instance.
     */
    public Ship getShip() {
        return boat;
    }

    /**
     * Returns a list of all SpaceObjects in the game.
     *
     * @return a list of all spaceObjects.
     */
    public List<SpaceObject> getSpaceObjects() {
        return spaceObjects;
    }

    /**
     * Returns the current level.
     *
     * @return the current level.
     */
    public int getLevel() {
        return lvl;
    }

    /**
     * Adds a SpaceObject to the game.
     *
     * Objects are considered part of the game only when they are tracked by the model.
     *
     * @param object the SpaceObject to be added to the game.
     * @requires object != null.
     */
    public void addObject(SpaceObject object) {
        if (object != null) {
            this.spaceObjects.add(object);
        }
    }

    /**
     * Updates the game state by moving all objects and then removing off-screen objects.
     *
     * Objects should be moved by calling .tick(tick) on each object.
     * Objects are considered off-screen if they are at y-coordinate &gt; GAME_HEIGHT.
     *
     * @param tick the tick value passed through to the objects tick() method.
     */
    public void updateGame(int tick) {
        List<SpaceObject> toRemove = new ArrayList<>();
        for (SpaceObject obj : spaceObjects) {
            obj.tick(tick); // Move objects downward
            if (obj instanceof Ship) {
                continue;
            }
            if (!isInBounds(obj)) { // Remove objects that move off-screen
                toRemove.add(obj);
            }
        }
        spaceObjects.removeAll(toRemove);
    }

    /**
     * Spawns new objects (asteroids, enemies, and power-ups) at random positions.
     * Uses this.random to make EXACTLY 6 calls to random.nextInt() and 1 random.nextBoolean.
     * Random calls should be in the following order:
     * 1. Check if an asteroid should spawn (random.nextInt(100) &lt; spawnRate)
     * 2. If spawning an asteroid, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     * 3. Check if an enemy should spawn (random.nextInt(100) &lt; spawnRate * ENEMY_SPAWN_RATE)
     * 4. If spawning an enemy, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     * 5. Check if a power-up should spawn (random.nextInt(100) &lt; spawnRate * POWER_UP_SPAWN_RATE)
     * 6. If spawning a power-up, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     * 7. If spawning a power-up, spawn a ShieldPowerUp if random.nextBoolean(), else a HealthPowerUp.
     * Failure to match random calls correctly will result in failed tests.
     * Objects spawn at y = 0 (top of the screen).
     * Objects may not spawn if there is a ship at the intended spawn location.
     * This should NOT impact calls to random.
     */
    public void spawnObjects() {
        // Spawn asteroids with a chance determined by spawnRate
        spawnAsteroid();
        spawnEnemy();
        spawnPowerUp();
    }

    private void spawnAsteroid() {
        if (random.nextInt(100) < spawnRate) {
            int x = random.nextInt(GAME_WIDTH); // Random x-coordinate
            int y = 0; // Spawn at the top of the screen
            if (isSpaceOccupied(x, y)) {
                spaceObjects.add(new Asteroid(x, y));
            }
        }
    }

    private void spawnEnemy() {
        if (random.nextInt(100) < spawnRate * ENEMY_SPAWN_RATE) {
            int x = random.nextInt(GAME_WIDTH);
            int y = 0;
            if (isSpaceOccupied(x, y)) {
                spaceObjects.add(new Enemy(x, y));
            }
        }
    }

    private void spawnPowerUp() {
        if (random.nextInt(100) < spawnRate * POWER_UP_SPAWN_RATE) {
            int x = random.nextInt(GAME_WIDTH);
            int y = 0;
            PowerUp powerUp = random.nextBoolean() ? new ShieldPowerUp(x, y) :
                    new HealthPowerUp(x, y);
            if (isSpaceOccupied(x, y)) {
                spaceObjects.add(powerUp);
            }
        }
    }



    private boolean isSpaceOccupied(int x, int y) {
        return !isCollidingWithExistingSpaceObjects(x, y) && !isCollidingWithShip(x, y);
    }
    /**
     * Checks if a given position would collide with the ship.
     *
     * @param x the x-coordinate to check.
     * @param y the y-coordinate to check.
     * @return true if the position collides with the ship, false otherwise.
     */

    private boolean isCollidingWithShip(int x, int y) {
        return (boat.getX() == x) && (boat.getY() == y);
    }

    private boolean isCollidingWithExistingSpaceObjects(int x, int y) {
        for (SpaceObject object : spaceObjects) {
            if ((object.getX() == x) && (object.getY() == y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If level progression requirements are satisfied, levels up the game by
     * increasing the spawn rate and level number.
     *
     * To level up, the score must not be less than the current level multiplied by the score threshold.
     * To increase the level the spawn rate should increase by SPAWN_RATE_INCREASE, and the level number should increase by 1.
     * If the level is increased, log the following:
     * "Level Up! Welcome to Level {new level}. Spawn rate increased to {new spawn rate}%."
     * @hint score is not stored in the GameModel.
     */
    public void levelUp() {
        if (boat.getScore() < lvl * SCORE_THRESHOLD) {
            return;
        }
        lvl++;
        spawnRate += SPAWN_RATE_INCREASE;

        if (this.verbose) {
            wrter.log("Level Up! Welcome to Level " + lvl + ". Spawn rate increased to "
                    + spawnRate + "%.");
        }

    }

    /**
     * Fires a bullet from the ship's current position.
     *
     * Creates a new bullet at the coordinates the ship occupies.
     * Logs "Core.Bullet fired!"
     */
    public void fireBullet() {
        int bulletX = boat.getX();
        int bulletY = boat.getY(); // Core.Bullet starts just above the ship
        spaceObjects.add(new Bullet(bulletX, bulletY));
        //wrter.log("Core.Bullet fired!");
    }

    /**
     * Detects and handles collisions between spaceObjects (Ship and Bullet collisions).
     * Objects are considered to be colliding if they share x and y coordinates.
     *
     * First checks ship collision:
     * - If the ship is colliding with a powerup, apply the effect, and
     * .log("Power-up collected: " + obj.render())
     * - If the ship is colliding with an asteroid, take the appropriate damage, and
     * .log("Hit by asteroid! Health reduced by " + ASTEROID_DAMAGE + ".")
     * - If the ship is colliding with an enemy, take the appropriate damage, and
     * .log("Hit by enemy! Health reduced by " + ENEMY_DAMAGE + ".")
     * For any collisions with the ship, the colliding object should be removed.
     *
     * Then check bullet collision:
     * If a bullet collides with an enemy, remove both the enemy and the bullet. No logging required.
     */
    public void checkCollisions() {
        List<SpaceObject> toRemove = new ArrayList<>();

        handleShipCollisions(toRemove);
        handleBulletCollisions(toRemove);

        spaceObjects.removeAll(toRemove);
    }

    private void handleShipCollisions(List<SpaceObject> toRemove) {
        for (SpaceObject obj : spaceObjects) {
            if (obj instanceof Ship || obj instanceof Bullet) {
                continue;
            }

            if (isCollidingWithShip(obj.getX(), obj.getY())) {
                if (obj instanceof PowerUp powerUp) {
                    powerUp.applyEffect(boat);
                    if (verbose) {
                        wrter.log("PowerUp collected: " + obj.render());
                    }
                } else if (obj instanceof Asteroid) {
                    boat.takeDamage(ASTEROID_DAMAGE);
                    if (verbose) {
                        wrter.log("Hit by " + obj.render()
                                + "! Health reduced by " + ASTEROID_DAMAGE + ".");
                    }
                } else if (obj instanceof Enemy) {
                    boat.takeDamage(ENEMY_DAMAGE);
                    if (verbose) {
                        wrter.log("Hit by " + obj.render()
                                + "! Health reduced by " + ENEMY_DAMAGE + ".");
                    }
                }
                toRemove.add(obj);
            }
        }
    }

    private void handleBulletCollisions(List<SpaceObject> toRemove) {
        for (SpaceObject obj : spaceObjects) {
            if (!(obj instanceof Bullet)) {
                continue;
            }

            for (SpaceObject other : spaceObjects) {
                if (other instanceof Enemy
                        && obj.getX() == other.getX() && obj.getY() == other.getY()) {
                    statsTracker.recordShotHit();
                    toRemove.add(obj);
                    toRemove.add(other);
                    break;
                }
            }

            for (SpaceObject other : spaceObjects) {
                if (other instanceof Asteroid
                        && obj.getX() == other.getX() && obj.getY() == other.getY()) {
                    toRemove.add(obj);
                    break;
                }
            }
        }
    }

    /**
     * Sets the seed of the Random instance created in the constructor using .setSeed().
     *
     * This method should NEVER be called.
     *
     * @param seed to be set for the Random instance
     * @provided
     */
    public void setRandomSeed(int seed) {
        this.random.setSeed(seed);
    }


    /**
     * Returns the current player stats tracker.
     * @return the current player stats tracker.
     */
    public PlayerStatsTracker getStatsTracker() {
        return statsTracker;
    }

    /**
     * Sets verbose state to the provided input.
     * @param verbose - whether to set verbose state to true or false.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Checks if the game is over.
     * The game is considered over if the Ship heath is <= 0.
     * @return true if the Ship health is <= 0, false otherwise
     */
    public boolean checkGameOver() {
        return this.boat.getHealth() <= 0;
    }

    /**
     * Checks if the given SpaceObject is inside the game bounds.
     * The SpaceObject is considered outside the game boundaries if they are at:
     * x-coordinate >= GAME_WIDTH,
     * y-coordinate >= GAME_HEIGHT,
     * x-coordinate < 0, or
     * y-coordinate < 0
     * @param spaceObject - the SpaceObject to check
     * @return true if the SpaceObject is in bounds, false otherwise
     * @requires spaceObject is not Null
     */
    public static boolean isInBounds(SpaceObject spaceObject) {
        int x = spaceObject.getX();
        int y = spaceObject.getY();

        if (x >= GAME_WIDTH || x < 0) {
            return false;
        }

        if (y >= GAME_HEIGHT || y < 0) {
            return false;
        }

        return true;
    }


}
