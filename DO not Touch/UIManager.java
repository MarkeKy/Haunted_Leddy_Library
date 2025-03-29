//package scene_creation;

import javax.swing.*;
import java.awt.*;

public class UIManager {
    private final JPanel topPanel;
    private final JLabel pointsLabel;
    private final JLabel timerLabel;

    public UIManager() {
        topPanel = new JPanel();
        topPanel.setOpaque(true);
        topPanel.setBackground(Color.WHITE);
        topPanel.setLayout(new BorderLayout());

        timerLabel = new JLabel("Time: 05:00");
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setFont(new Font("Serif", Font.BOLD, 16));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        topPanel.add(timerLabel, BorderLayout.WEST);

        pointsLabel = new JLabel("Puzzles Solved: 0");
        pointsLabel.setForeground(Color.BLACK);
        pointsLabel.setFont(new Font("Serif", Font.BOLD, 16));
        pointsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        topPanel.add(pointsLabel, BorderLayout.EAST);
    }

    public JPanel getTopPanel() {
        return topPanel;
    }

    public JLabel getPointsLabel() {
        return pointsLabel;
    }

    public JLabel getTimerLabel() {
        return timerLabel;
    }

    public void updatePoints(int points) {
        pointsLabel.setText("Puzzles Solved: " + points);
        pointsLabel.revalidate();
        pointsLabel.repaint();
    }
}