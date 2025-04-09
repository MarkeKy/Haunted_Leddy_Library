package scene_creation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookOrderingGame extends JFrame {
    private JPanel bookPanel;
    private List<BookButton> bookButtons;
    private JButton checkButton;
    private BookButton selectedBook = null;
    private int moves = 0;
    private JLabel movesLabel;
    private int shelfNumber; // Added to store shelf number

    private class BookButton extends JButton {
        private String correctTitle;

        public BookButton(String title) {
            super(title);
            this.correctTitle = title;
            setFont(new Font("Serif", Font.PLAIN, 16));
            setBackground(new Color(139, 69, 19));
            setForeground(Color.BLACK);
            setPreferredSize(new Dimension(150, 40));
        }

        public String getCorrectTitle() {
            return correctTitle;
        }
    }

    public BookOrderingGame(int shelfNumber) {
        super("Organize Books - Shelf " + shelfNumber); // Include shelf number in title
        this.shelfNumber = shelfNumber;
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        bookButtons = new ArrayList<>();
        initializeBooks();

        bookPanel = new JPanel();
        bookPanel.setLayout(new GridLayout(3, 3, 5, 5));
        bookPanel.setBackground(new Color(50, 30, 20));

        for (BookButton button : bookButtons) {
            bookPanel.add(button);
            button.addActionListener(new BookClickListener());
        }

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(50, 30, 20));

        movesLabel = new JLabel("Moves: 0");
        movesLabel.setForeground(Color.WHITE);
        checkButton = new JButton("Check Order");
        checkButton.addActionListener(e -> checkOrder());

        controlPanel.add(movesLabel);
        controlPanel.add(checkButton);

        // Updated instruction with shelf number
        add(new JLabel("Arrange books in alphabetical order (Shelf " + shelfNumber + ")",
                SwingConstants.CENTER), BorderLayout.NORTH);
        add(bookPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeBooks() {
        List<String> bookTitles = new ArrayList<>();
        bookTitles.add("Dracula");
        bookTitles.add("Frankenstein");
        bookTitles.add("The Haunting");
        bookTitles.add("Necronomicon");
        bookTitles.add("Witchcraft");
        bookTitles.add("Ghost Stories");
        bookTitles.add("The Raven");
        bookTitles.add("Cthulhu");
        bookTitles.add("Vampire Tales");

        Collections.shuffle(bookTitles);

        for (String title : bookTitles) {
            bookButtons.add(new BookButton(title));
        }
    }

    private class BookClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            playClickSound();
            BookButton clicked = (BookButton) e.getSource();

            if (selectedBook == null) {
                selectedBook = clicked;
                clicked.setBackground(new Color(184, 134, 11));
            } else if (selectedBook != clicked) {
                int index1 = bookButtons.indexOf(selectedBook);
                int index2 = bookButtons.indexOf(clicked);

                bookButtons.set(index1, clicked);
                bookButtons.set(index2, selectedBook);

                selectedBook.setBackground(new Color(139, 69, 19));
                selectedBook = null;

                moves++;
                movesLabel.setText("Moves: " + moves);

                bookPanel.removeAll();
                for (BookButton button : bookButtons) {
                    bookPanel.add(button);
                }
                bookPanel.revalidate();
                bookPanel.repaint();
            }
        }
    }

    private void playClickSound() {
        try {
            URL soundURL = BookOrderingGame.class.getResource("click.wav");
            if (soundURL == null) {
                System.err.println("click.wav not found!");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception ex) {
            System.err.println("Error playing click sound: " + ex.getMessage());
        }
    }

    private void checkOrder() {
        boolean correct = true;
        List<String> currentOrder = new ArrayList<>();
        List<String> correctOrder = new ArrayList<>();

        for (BookButton button : bookButtons) {
            currentOrder.add(button.getText());
            correctOrder.add(button.getCorrectTitle());
        }

        Collections.sort(correctOrder);

        for (int i = 0; i < currentOrder.size(); i++) {
            if (!currentOrder.get(i).equals(correctOrder.get(i))) {
                correct = false;
                break;
            }
        }

        if (correct) {
            JOptionPane.showMessageDialog(this,
                    "Congratulations! Books on Shelf " + shelfNumber + " are in correct order!\nMoves: " + moves,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Books on Shelf " + shelfNumber + " are not in alphabetical order yet.",
                    "Try Again",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}