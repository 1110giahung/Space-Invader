package game.achievements;

/**
 * A concrete implementation of the Achievement interface.
 * Sample logic:
 *
 * Progress is tracked as a value between 0.0 and 1.0.
 * Tiers are determined as:
 * "Novice" if progress < 0.5,
 * "Expert" if progress is between 0.5 (inclusive) and 1.0,
 * "Master" if progress equals 1.0.
 */
public class GameAchievement implements Achievement {

    private final String name;
    private final String description;
    private double progress;

    /**
     * Constructs a GameAchievement with the specified name and description.
     * Initial progress is 0.0.
     *
     * @param name        the unique name of the achievement
     * @param description the description of the achievement
     * @throws IllegalArgumentException if name or description is null or empty
     */
    public GameAchievement(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Achievement name cannot be null or empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Achievement description cannot be null or empty.");
        }
        this.name = name;
        this.description = description;
        this.progress = 0.0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public void setProgress(double newProgress) {
        if (newProgress < 0.0 || newProgress > 1.0) {
            throw new IllegalArgumentException("Progress must be between 0.0 and 1.0.");
        }
        this.progress = Math.min(newProgress, 1.0);
    }

    @Override
    public String getCurrentTier() {
        if (progress >= 0.999) {
            return "Master";
        } else if (progress >= 0.5) {
            return "Expert";
        } else {
            return "Novice";
        }
    }
}