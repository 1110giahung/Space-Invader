package game.achievements;

/**
 * Represents a single achievement with progress tracking and tier information.
 * The progress value of an achievement is always maintained between 0.0 (0%) and 1.0 (100%). When updating progress, the increment must be non-negative and the cumulative progress is capped at 1.0.
 * */
public interface Achievement {

    /**
     * Returns the unique name of the achievement.
     *
     * @return the name of the achievement.
     */
    String getName();

    /**
     * Returns a description of the achievement.
     *
     * @return the description of the achievement.
     */
    String getDescription();

    /**
     * Returns the current progress as a value between 0.0 and 1.0.
     *
     * @return current progress.
     */
    double getProgress();

    /**
     * Sets the progress to a specified value between 0.0 and 1.0.
     * Progress is capped at 1.0 and must not be negative.
     *
     * @param newProgress the updated progress value.
     */
    void setProgress(double newProgress);

    /**
     * Returns the current tier based on progress:
     * - "Novice" if progress < 0.5
     * - "Expert" if 0.5 <= progress < 0.999
     * - "Master" if progress >= 0.999
     *
     * @return the current tier as a string.
     */
    String getCurrentTier();
}