package game;

import game.achievements.Achievement;
import game.achievements.AchievementManager;
import game.achievements.PlayerStatsTracker;
import game.ui.UI;
import game.utility.Direction;

import java.util.List;

import static game.utility.Direction.*;
import static game.utility.Direction.RIGHT;

/**
 * The Controller handling the game flow and interactions.
 * Holds references to the UI and the Model, so it can pass information and references back and forth as necessary.
 * Manages changes to the game, which are stored in the Model, and displayed by the UI.
 */
public class GameController {

    private boolean paused = false;


    private final long startTime;
    private final UI ui;
    private final GameModel model;
    private final AchievementManager achievementManager;

    /**
     * An internal variable indicating whether certain methods should log their actions.
     * Not all methods respect isVerbose.
     */
    private boolean isVerbose = false;


    /**
     * Initializes the game controller with the given UI, GameModel and AchievementManager.
     * Stores the UI, GameModel, AchievementManager and start time.
     * The start time System.currentTimeMillis() should be stored as a long.
     * Starts the UI using UI.start().
     *
     * @param ui the UI used to draw the Game
     * @param model the model used to maintain game information
     * @param achievementManager the manager used to maintain achievement information
     *
     * @requires ui is not null
     * @requires model is not null
     * @requires achievementManager is not null
     * @provided
     */
    public GameController(UI ui, GameModel model, AchievementManager achievementManager) {
        this.ui = ui;
        ui.start();
        this.model = model;
        this.startTime = System.currentTimeMillis(); // Current time
        this.achievementManager = achievementManager;
    }


    /**
     * Initializes the game controller with the given UI and GameModel.
     * Stores the ui, model and start time.
     * The start time System.currentTimeMillis() should be stored as a long.
     * @param ui    the UI used to draw the Game
     * @param achievementManager the manager used to maintain achievement information
     * @requires ui is not null
     * @requires achievementManager is not null
     * @provided
     */
    public GameController(UI ui, AchievementManager achievementManager) {
        //this(ui, new GameModel(ui::log), achievementManager);
        this(ui, new GameModel(ui::log, new PlayerStatsTracker()), achievementManager);

    }

    /**
     * Returns the current GameModel.
     * */
    public GameModel getModel() {
        return this.model;
    }

    /**
     * Starts the main game loop.
     * Passes onTick and handlePlayerInput to ui.onStep and ui.onKey respectively.
     * @provided
     */
    public void startGame() {
        ui.onStep(this::onTick);
        ui.onKey(this::handlePlayerInput);
    }


    /**
     * Uses the provided tick to call and advance the following:
     * - A call to model.updateGame(tick) to advance the game by the given tick.
     * - A call to model.checkCollisions() to handle game interactions.
     * - A call to model.spawnObjects() to handle object creation.
     * - A call to model.levelUp() to check and handle leveling.
     * - A call to refreshAchievements(tick) to handle achievement updating.
     * - A call to renderGame() to draw the current state of the game.
     * @param tick the provided tick
     * @provided
     */
    public void onTick(int tick) {
        model.updateGame(tick); // Update GameObjects
        model.checkCollisions(); // Check for Collisions
        model.spawnObjects(); // Handles new spawns
        model.levelUp(); // Level up when score threshold is met
        refreshAchievements(tick); // Handle achievement updating.
        renderGame(); // Update Visual

        // Check game over
        if (model.checkGameOver()) {
            pauseGame();
            showGameOverWindow();
        }
    }

    private boolean added = false;

    /**
     * Displays a Game Over window containing the player's final statistics and achievement
     * progress.
     * This window includes:
     * - Number of shots fired and shots hit
     * - Number of Enemies destroyed
     * - Survival time in seconds
     * - Progress for each achievement, including name, description, completion percentage
     * and current tier
     * @provided
     */
    private void showGameOverWindow() {
        javax.swing.JFrame gameOverFrame = new javax.swing.JFrame("Game Over - Player Stats");
        gameOverFrame.setSize(400, 300);
        gameOverFrame.setLocationRelativeTo(null);
        gameOverFrame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);

        String statsText = buildGameOverStatsText();

        javax.swing.JTextArea statsArea = new javax.swing.JTextArea(statsText);
        statsArea.setEditable(false);
        statsArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(statsArea);
        gameOverFrame.add(scrollPane);
        gameOverFrame.setVisible(true);
    }

    private String buildGameOverStatsText() {
        StringBuilder sb = new StringBuilder();

        appendBasicStats(sb);
        appendAchievementStats(sb);

        return sb.toString();
    }

    private void appendBasicStats(StringBuilder sb) {
        sb.append("Shots Fired: ").append(getStatsTracker().getShotsFired()).append("\n");
        sb.append("Shots Hit: ").append(getStatsTracker().getShotsHit()).append("\n");
        sb.append("Enemies Destroyed: ")
                .append(getStatsTracker().getShotsHit()).append("\n");
        sb.append("Survival Time: ")
                .append(getStatsTracker().getElapsedSeconds()).append(" seconds\n");
    }

    private void appendAchievementStats(StringBuilder sb) {
        for (Achievement ach : achievementManager.getAchievements()) {
            double progressPercent = ach.getProgress() * 100;
            sb.append(ach.getName())
                    .append(" - ")
                    .append(ach.getDescription())
                    .append(" (")
                    .append(String.format("%.0f%%", progressPercent))
                    .append(" complete, Tier: ")
                    .append(ach.getCurrentTier())
                    .append(")\n");
        }
    }

    /**
     * Returns the current PlayerStatsTracker.
     * @return the current PlayerStatsTracker
     */
    public PlayerStatsTracker getStatsTracker() {
        return model.getStatsTracker();
    }

    /**
     * Sets verbose state to the provided input. Also sets the models verbose state to the provided input.
     * @param verbose - whether to set verbose state to true or false.
     */
    public void setVerbose(boolean verbose) {
        this.isVerbose = verbose;
        model.setVerbose(verbose);
    }

    /**
     * Updates the player's progress towards achievements on every game tick, and uses the achievementManager to track and update the player's achievements.
     * Progress is a double representing completion percentage, and must be >= 0.0, and <= 1.0.
     * Achievement Progress Calculations:
     * - Survivor achievement: survival time since game start in seconds, mastered at 120 seconds.
     * - Enemy Exterminator achievement: shots hit since game start, mastered at 20 shots.
     * - Sharp Shooter achievement: if shots fired > 10, then result is accuracy / 0.99, with the maximum result possible being 1; otherwise if shots fired <= 10, result is 0.
     * (This is so that mastery is achieved at accuracy >= 0.99)
     * The AchievementManager stores all new achievements mastered, and then updates the UI statistics with each new achievement's name and progress value.
     * Once every 100 ticks, and only if verbose is true, the achievement progress is logged to the UI.
     * @param tick - the provided tick
     */
    public void refreshAchievements(int tick) {
        calculateAndUpdateAchievements();
        updateAchievementStatsUi();
        optionallyLogAchievements(tick);
    }

    private void calculateAndUpdateAchievements() {
        long elapsedSeconds = getStatsTracker().getElapsedSeconds();

        double survivorProgress = Math.min(elapsedSeconds / 120.0, 1.0);
        double exterminatorProgress = Math.min(getStatsTracker().getShotsHit() / 20.0, 1.0);
        double sharpshooterProgress = getStatsTracker().getShotsFired() > 10
                ? Math.min(getStatsTracker().getAccuracy() / 0.99, 1.0)
                : 0.0;

        achievementManager.updateAchievement("Survivor", survivorProgress);
        achievementManager.updateAchievement("Enemy Exterminator", exterminatorProgress);
        achievementManager.updateAchievement("Sharp Shooter", sharpshooterProgress);
        achievementManager.logAchievementMastered();
    }

    private void updateAchievementStatsUi() {

        for (Achievement achievement : achievementManager.getAchievements()) {
            this.ui.setAchievementProgressStat(achievement.getName(), achievement.getProgress());
        }
    }

    private void optionallyLogAchievements(int tick) {
        if (isVerbose && tick % 100 == 0) {
            ui.logAchievements(achievementManager.getAchievements());
        }
    }

    /**
     * Renders the current game state, including score, health, level, and survival time.
     * - Uses ui.setStat() to update the "Score", "Health" and "Level" appropriately with information from the model.
     * - Uses ui.setStat() to update "Time Survived" with (System.currentTimeMillis() - startTime) / 1000 + " seconds"
     * - Renders all spaceObjects using one call to ui.render().
     */
    public void renderGame() {
        if (!added) {
            model.addObject(model.getShip());
            added = true;
        }
        updateStatsUi();
        ui.render(model.getSpaceObjects());
    }

    private void updateStatsUi() {
        ui.setStat("Health", String.valueOf(model.getShip().getHealth()));
        ui.setStat("Score", String.valueOf(model.getShip().getScore()));
        ui.setStat("Level", String.valueOf(model.getLevel()));
        ui.setStat("Time Survived", getSurvivalTime());
    }

    private String getSurvivalTime() {
        return (System.currentTimeMillis() - startTime) / 1000 + " seconds";
    }

    /**
     * Handles player input and performs actions such as moving the ship or firing Bullets.
     * Uppercase and lowercase inputs should be treated identically:
     * - For movement keys "W", "A", "S" and "D" the ship should be moved up, left, down, or right respectively, unless the game is paused. The movement should also be logged, provided verbose is true, as follows:
     * "Ship moved to ({model.getShip().getX()}, {model.getShip().getY()})"
     * - For input "F" the fireBullet() method of the Model instance should be called, and the recordShotFired() method of the PlayerStatsTracker instance should be called.
     * - For input "P" the pauseGame() method should be called.
     * - For all other inputs, the following should be logged, irrespective of the verbose state:
     * "Invalid input. Use W, A, S, D, F, or P."
     * When the game is paused, only un-pausing should be possible. No other action of printing should occur.
     * @param input - the player's input command.
     */
    public void handlePlayerInput(String input) {
        if (model.getShip() == null) {
            return;
        }
        if (isPaused()) {
            if (input.equalsIgnoreCase("P")) {
                pauseGame();
            }
            return;
        }
        switch (input.toUpperCase()) {
            case "W" -> handleMovementInput(UP);
            case "S" -> handleMovementInput(DOWN);
            case "A" -> handleMovementInput(LEFT);
            case "D" -> handleMovementInput(RIGHT);

            case "P" -> handlePauseInput();
            case "F" -> handleFireInput();
            default -> ui.log("Invalid input. Use W, A, S, D, F, or P.");
        }
    }

    private void logMovement() {

        if (isVerbose) {
            ui.log("Ship moved to ("
                    + model.getShip().getX() + ", "
                    + model.getShip().getY() + ")");
        }
    }

    private void handleMovementInput(Direction direction) {
        model.getShip().move(direction);
        logMovement();
    }

    private void handlePauseInput() {
        pauseGame();
    }

    private void handleFireInput() {
        model.fireBullet();
        getStatsTracker().recordShotFired();
    }

    /**
     * Calls ui.pause() to pause the game until the method is called again.
     * Calls ui.pause(). Logs "Game paused." or "Game unpaused." as appropriate, after calling ui.pause(), irrespective of verbose state.
     */
    public void pauseGame() {
        togglePause();
    }

    private void togglePause() {
        ui.pause();
        if (paused) {
            onUnpause();
        } else {
            onPause();
        }
        paused = !paused;
    }

    private boolean isPaused() {
        return paused;
    }

    private void onPause() {
        ui.log("Game paused.");
    }

    private void onUnpause() {
        ui.log("Game unpaused.");
    }

}

