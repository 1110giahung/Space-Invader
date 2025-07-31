package game.achievements;

import java.util.*;

/**
 * GameAchievementManager coordinates achievement updates, file persistence management.
 * Responsibilities:
 *
 * Register new achievements.
 * Update achievement progress.
 * Check for Mastered achievements and log them using AchievementFile.
 * Provide access to the current list of achievements.
 * */

public class AchievementManager {

    private final AchievementFile achievementFile;
    private final Map<String, Achievement> achievementMap;
    private final Set<String> loggedAchievements;

    /**
     * Constructs a GameAchievementManager with the specified AchievementFile.
     *
     * @param achievementFile - the AchievementFile instance to use (non-null)
     * @throws IllegalArgumentException - if achievementFile is null.
     */
    public AchievementManager(AchievementFile achievementFile) {
        if (achievementFile == null) {
            throw new IllegalArgumentException("AchievementFile cannot be null.");
        }
        this.achievementFile = achievementFile;
        this.achievementMap = new HashMap<>();
        this.loggedAchievements = new HashSet<>();
    }


    /**
     * Registers a new achievement.
     *
     * @param achievement - the Achievement to register.
     * @throws IllegalArgumentException - if achievement is already registered.
     * @requires achievement != null
     */
    public void addAchievement(Achievement achievement) {
        if (achievement == null) {
            throw new IllegalArgumentException("Achievement cannot be null.");
        }
        String name = achievement.getName();
        if (achievementMap.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Achievement with name '" + name + "' is already registered.");
        }
        achievementMap.put(name, achievement);
    }

    /**
     * Sets the progress of the specified achievement to a given amount.
     * @param achievementName - the name of the achievement.
     * @param absoluteProgressValue - the value the achievement's progress will be set to.
     * @throws IllegalArgumentException - if no achievement is registered under the provided name.
     * @requites achievementName must be a non-null, non-empty string identifying a registered achievement.
     */
    public void updateAchievement(String achievementName, double absoluteProgressValue) {
        if (achievementName == null || achievementName.isEmpty()) {
            throw new IllegalArgumentException("Achievement name cannot be null or empty.");
        }
        Achievement achievement = achievementMap.get(achievementName);
        if (achievement == null) {
            throw new IllegalArgumentException(
                    "No achievement registered with name: " + achievementName);
        }
        achievement.setProgress(absoluteProgressValue);
    }

    /**
     * Checks all registered achievements.
     * For any achievement that is mastered and has not yet been logged,
     *          this method logs the event via AchievementFile, and marks the achievement as logged.
     */
    public void logAchievementMastered() {
        for (Achievement achievement : achievementMap.values()) {
            if (achievement.getProgress() >= 0.999
                    && !loggedAchievements.contains(achievement.getName())) {
                achievementFile.save("Mastered: " + achievement.getName());
                loggedAchievements.add(achievement.getName());
            }
        }
    }

    /**
     * Returns a list of all registered achievements.
     * @return a List of Achievement objects.
     */
    public List<Achievement> getAchievements() {
        return new ArrayList<>(achievementMap.values());
    }
}
