package scene_creation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import javax.sound.sampled.*; // New import for Clip and AudioInputStream
import java.net.URL;

public class NumberSortGame extends JPanel implements ActionListener, MouseListener {
    private final int BLOCKS = 15;
    private final int MAX_MOVES = 30;
    private final JButton[] gridButtons = new JButton[BLOCKS];
    private final int[] numbers = new int[BLOCKS];
    private int score = 0;
    private int movesLeft = MAX_MOVES;
    private JLabel scoreLabel;
    private JLabel timerLabel;
    private JLabel movesLabel;
    private JLabel correctLabel;
    private Timer gameTimer;
    private int timeLeft = 60;
    private JButton firstSelected = null;
    private int firstIndex = -1;

    public NumberSortGame() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // Top panel for labels
        JPanel topPanel = new JPanel(new GridLayout(1, 4));
        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        scoreLabel.setForeground(Color.WHITE);
        timerLabel = new JLabel("Time: " + timeLeft, SwingConstants.CENTER);
        timerLabel.setForeground(Color.WHITE);
        movesLabel = new JLabel("Moves: " + movesLeft, SwingConstants.CENTER);
        movesLabel.setForeground(Color.WHITE);
        correctLabel = new JLabel("Correct: 0", SwingConstants.CENTER);
        correctLabel.setForeground(Color.WHITE);
        topPanel.setBackground(Color.DARK_GRAY);
        topPanel.add(scoreLabel);
        topPanel.add(timerLabel);
        topPanel.add(movesLabel);
        topPanel.add(correctLabel);
        add(topPanel, BorderLayout.NORTH);

        // Grid panel for numbers
        JPanel gridPanel = new JPanel(new GridLayout(1, BLOCKS, 2, 2));
        gridPanel.setBackground(Color.DARK_GRAY);
        initializeGrid(gridPanel);
        add(gridPanel, BorderLayout.CENTER);

        // Bottom panel for reset button
        JPanel bottomPanel = new JPanel();
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetGame());
        bottomPanel.add(resetButton);
        bottomPanel.setBackground(Color.DARK_GRAY);
        add(bottomPanel, BorderLayout.SOUTH); // Fixed typo from 'customPanel' to 'bottomPanel'

        // Start timer
        gameTimer = new Timer(1000, this);
        gameTimer.start();

        // Frame setup
        JFrame frame = new JFrame("Number Sort Game");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(this);
        frame.setSize(900, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initializeGrid(JPanel gridPanel) {
        for (int i = 0; i < BLOCKS; i++) {
            numbers[i] = i + 1;
        }
        shuffleArray(numbers);

        for (int i = 0; i < BLOCKS; i++) {
            JButton btn = new JButton(String.valueOf(numbers[i]));
            btn.setOpaque(true);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            btn.setFont(new Font("Arial", Font.PLAIN, 16));
            btn.addMouseListener(this);
            gridButtons[i] = btn;
            gridPanel.add(btn);
        }
        updateCorrectCount();
    }

    private void shuffleArray(int[] arr) {
        Random random = new Random();
        for (int i = arr.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = arr[i];
            arr[i] = arr[index];
            arr[index] = temp;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == gameTimer) {
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            if (timeLeft <= 0) {
                gameTimer.stop();
                endGame("Time's up! Game Over.");
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        for (int i = 0; i < BLOCKS; i++) {
            if (e.getSource() == gridButtons[i]) {
                handleClick(i);
                break;
            }
        }
    }

    private void handleClick(int index) {
        JButton clicked = gridButtons[index];
        if (firstSelected == null) {
            firstSelected = clicked;
            firstIndex = index;
            firstSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
            playSound("select.wav");
        } else if (firstSelected == clicked) {
            firstSelected.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            firstSelected = null;
            firstIndex = -1;
            playSound("deselect.wav");
        } else {
            if (movesLeft > 0) {
                swapNumbers(firstIndex, index);
                animateSwap(firstIndex, index);
                firstSelected.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                firstSelected = null;
                firstIndex = -1;
                movesLeft--;
                movesLabel.setText("Moves: " + movesLeft);
                checkWinCondition();
                updateCorrectCount();
                if (movesLeft <= 0) {
                    gameTimer.stop();
                    endGame("No moves left! Game Over.");
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
                animateError(index);
            }
        }
    }

    private void swapNumbers(int fromIndex, int toIndex) {
        int temp = numbers[fromIndex];
        numbers[fromIndex] = numbers[toIndex];
        numbers[toIndex] = temp;
        gridButtons[fromIndex].setText(String.valueOf(numbers[fromIndex]));
        gridButtons[toIndex].setText(String.valueOf(numbers[toIndex]));
        score += 10;
        scoreLabel.setText("Score: " + score);
        playSound("click.wav");
    }

    private void animateSwap(int fromIndex, int toIndex) {
        gridButtons[fromIndex].setBackground(Color.YELLOW);
        gridButtons[toIndex].setBackground(Color.YELLOW);
        Timer timer = new Timer(100, e -> {
            updateCorrectCount();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void animateError(int index) {
        gridButtons[index].setBackground(Color.RED);
        Timer timer = new Timer(100, e -> {
            gridButtons[index].setBackground(numbers[index] == index + 1 ? Color.GREEN : Color.WHITE);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private boolean isSorted() {
        for (int i = 0; i < BLOCKS - 1; i++) {
            if (numbers[i] > numbers[i + 1]) {
                return false;
            }
        }
        return true;
    }

    private void checkWinCondition() {
        if (isSorted()) {
            gameTimer.stop();
            playSound("win.wav");
            endGame("You Win! Numbers are sorted!");
        }
    }

    private void endGame(String message) {
        if (message.contains("Game Over")) {
            playSound("lose.wav");
        }
        int result = JOptionPane.showConfirmDialog(this, message + " Score: " + score + "\nPlay again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void updateCorrectCount() {
        int correctCount = 0;
        for (int i = 0; i < BLOCKS; i++) {
            if (numbers[i] == i + 1) {
                correctCount++;
                gridButtons[i].setBackground(Color.GREEN);
                gridButtons[i].setFont(new Font("Arial", Font.BOLD, 16));
            } else {
                gridButtons[i].setBackground(Color.WHITE);
                gridButtons[i].setFont(new Font("Arial", Font.PLAIN, 16));
            }
        }
        correctLabel.setText("Correct: " + correctCount);
    }

    private void resetGame() {
        score = 0;
        movesLeft = MAX_MOVES;
        scoreLabel.setText("Score: " + score);
        timerLabel.setText("Time: " + timeLeft);
        movesLabel.setText("Moves: " + movesLeft);
        timeLeft = 60;
        shuffleArray(numbers);
        for (int i = 0; i < BLOCKS; i++) {
            gridButtons[i].setText(String.valueOf(numbers[i]));
            gridButtons[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            gridButtons[i].setEnabled(true);
        }
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
        firstSelected = null;
        firstIndex = -1;
        updateCorrectCount();
    }

    private void playSound(String fileName) {
        try {
            URL soundURL = NumberSortGame.class.getResource("/" + fileName); // Use current class
            if (soundURL == null) {
                System.err.println(fileName + " not found in classpath!");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start(); // Play once (not looping)
            System.out.println("Playing sound: " + fileName);
        } catch (Exception ex) {
            System.err.println("Error playing sound " + fileName + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NumberSortGame::new);
    }
}
