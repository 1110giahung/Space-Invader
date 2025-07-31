package game.achievements;

/**
 * Tracks player statistics: shots fired, shots hit, accuracy, and elapsed time.
 */
public class PlayerStatsTracker {

    private final long startTime;
    private int shotsFired;
    private int shotsHit;

    /**
     * Constructs a tracker with the current system time as start time.
     */
    public PlayerStatsTracker() {
        this(System.currentTimeMillis());
    }

    /**
     * Constructs a tracker with a custom start time.
     * @param startTime the start time in milliseconds.
     */
    public PlayerStatsTracker(long startTime) {
        this.startTime = startTime;
        this.shotsFired = 0;
        this.shotsHit = 0;
    }

    /**
     * Records the player firing one shot.
     */
    public void recordShotFired() {
        shotsFired++;
    }

    /**
     * Records the player hitting a target.
     */
    public void recordShotHit() {
        shotsHit++;
    }

    /**
     * Returns the number of shots the player has fired.
     * @return total shots fired
     */
    public int getShotsFired() {
        return shotsFired;
    }

    /**
     * Returns the number of shots the player has hit.
     * @return total shots hit
     */
    public int getShotsHit() {
        return shotsHit;
    }

    /**
     * Returns the time elapsed since the tracker started, in seconds.
     * @return elapsed time in seconds
     */
    public long getElapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    /**
     * Returns the player's shooting accuracy.
     * @return accuracy as a decimal (e.g., 0.75 for 75%)
     */
    public double getAccuracy() {
        if (shotsFired == 0) {
            return 0.0;
        }
        return (double) shotsHit / shotsFired;
    }
}