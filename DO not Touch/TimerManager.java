////package scene_creation;
//
//import javax.swing.*;
//import java.awt.event.ActionListener;
//
//public class TimerManager {
//    private int timeRemaining = 300; // 5 minutes in seconds
//    private final JLabel timerLabel;
//    private final Timer gameTimer;
//    private boolean gameOver = false;
//    private final AudioManager audioManager;
//    private final GameState gameState;
//    private final Runnable onGameOver;
//
//    public TimerManager(JLabel timerLabel, AudioManager audioManager, GameState gameState, Runnable onGameOver) {
//        if (timerLabel == null || audioManager == null || gameState == null || onGameOver == null) {
//            throw new IllegalArgumentException("TimerManager dependencies cannot be null");
//        }
//
//        this.timerLabel = timerLabel;
//        this.audioManager = audioManager;
//        this.gameState = gameState;
//        this.onGameOver = onGameOver;
//
//        this.gameTimer = new Timer(1000, createTimerListener());
//        updateTimerDisplay(); // Initialize display immediately
//    }
//
//    private ActionListener createTimerListener() {
//        return e -> {
//            if (gameOver || !gameTimer.isRunning()) return; // Extra safety check
//            timeRemaining--;
//            updateTimerDisplay();
//            if (timeRemaining <= 0) {
//                stopTimer(); // Use method for consistency
//                handleGameOver();
//            }
//        };
//    }
//
//    public void startTimer() {
//        if (!gameOver && !gameTimer.isRunning()) {
//            gameTimer.start();
//        }
//    }
//
//    public void stopTimer() {
//        gameTimer.stop();
//    }
//
//    private void updateTimerDisplay() {
//        int minutes = Math.max(0, timeRemaining / 60); // Prevent negative display
//        int seconds = Math.max(0, timeRemaining % 60);
//        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
//    }
//
//    private void handleGameOver() {
//        gameOver = true;
//        gameState.setGameOver(true);
//        audioManager.playYouLoseSound();
//        int option = JOptionPane.showConfirmDialog(
//                null,
//                "Game Over! Time's up.\nDo you want to restart the game?",
//                "Game Over",
//                JOptionPane.YES_NO_OPTION,
//                JOptionPane.WARNING_MESSAGE
//        );
//
//        if (option == JOptionPane.YES_OPTION) {
//            onGameOver.run();
//        } else {
//            System.exit(0);
//        }
//    }
//
//    public void resetTimer() {
//        stopTimer(); // Ensure timer is stopped before resetting
//        timeRemaining = 300;
//        gameOver = false;
//        updateTimerDisplay();
//        startTimer();
//    }
//
//    // Optional: Getter for time remaining (if needed elsewhere)
//    public int getTimeRemaining() {
//        return timeRemaining;
//    }
//}