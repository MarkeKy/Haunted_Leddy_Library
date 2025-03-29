//package scene_creation;

import java.util.HashMap;
import java.util.Map;

public class GameState {
    private int points = 0;
    private final Map<String, Boolean> bookshelfUsage = new HashMap<>();
    private boolean gameOver = false;

    public int getPoints() {
        return points;
    }

    public void incrementPoints() {
        if (!gameOver) {
            points++;
        }
    }

    public void resetPoints() {
        points = 0;
    }

    public Map<String, Boolean> getBookshelfUsage() {
        return bookshelfUsage;
    }

    public void markShelfAsUsed(String shelfId) {
        bookshelfUsage.put(shelfId, true);
    }

    public void resetBookshelfUsage() {
        bookshelfUsage.clear();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}