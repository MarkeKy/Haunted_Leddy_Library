package scene_creation;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

// MainClass is the primary class for the Haunted Leddy game, handling the 3D scene, user input, game logic, and UI.
public class MainClass extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L; // Serialization ID for the JPanel.
	private static JFrame frame; // The main window frame for the game.
	private int lastMouseX = -1, lastMouseY = -1; // Last recorded mouse position for mouse movement calculations.
	private boolean firstMouse = true; // Flag to initialize mouse position on first movement.
	private TransformGroup viewTG; // TransformGroup for the camera/view platform.
	private CustomCanvas3D canvas; // Custom Canvas3D for rendering the 3D scene.
	private BranchGroup sceneBG; // Root BranchGroup for the 3D scene.
	private PickTool pickTool; // Tool for picking objects in the 3D scene (e.g., clicking on books or doors).
	protected static Map<String, Boolean> bookshelfUsage = new HashMap<>(); // Tracks which shelves have been solved.
	private boolean bookGameActive = false; // Flag to indicate if the BookOrderingGame is active.
	private static int points = 0; // Player's score (number of puzzles solved).
	private JLabel pointsLabel; // UI label to display the player's points.
	private JLabel timerLabel; // UI label to display the remaining time.
	private Timer gameTimer; // Timer for the game countdown.
	private int timeRemaining = 300; // Initial time in seconds (5 minutes).
	private boolean gameOver = false; // Flag to indicate if the game is over.
	private Movement movement; // Handles camera movement and rotation.
	private SoundManager soundManager; // Manages sound effects and background music.
	private GhostObject ghostObject; // Reference to the GhostObject in the scene.
	private Library library; // Persistent Library instance (though not used in this version).

	// Custom Canvas3D subclass for rendering the 3D scene with double buffering enabled.
	private static class CustomCanvas3D extends Canvas3D {
		public CustomCanvas3D(GraphicsConfiguration config) {
			super(config);
			setDoubleBufferEnable(true); // Enable double buffering for smoother rendering.
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g); // Call the parent class's paint method to render the 3D scene.
		}
	}

	// Method to create the 3D scene graph.
	public BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup(); // Create the root BranchGroup for the scene.
		TransformGroup sceneTG = new TransformGroup(); // Create a TransformGroup to hold the library scene.
		Library library = new Library(); // Create a new Library instance to build the scene.
		sceneTG.addChild(library.create_Library()); // Add the library scene to the TransformGroup.

		// Find the GhostObject in the library's objects array.
		for (Objects obj : library.getObjects()) {
			if (obj instanceof GhostObject) {
				ghostObject = (GhostObject) obj;
				break;
			}
		}

		sceneBG.addChild(sceneTG); // Add the TransformGroup to the BranchGroup.
		sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1)); // Add lighting to the scene.
		return sceneBG;
	}

	// Constructor for MainClass, initializes the game UI and 3D scene.
	public MainClass() {
		soundManager = new SoundManager(); // Initialize the sound manager.

		// Set up the 3D canvas for rendering.
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new CustomCanvas3D(config);
		canvas.setFocusable(true); // Ensure the canvas can receive input events.
		canvas.addKeyListener(this); // Add key listener for keyboard input.
		canvas.addMouseListener(this); // Add mouse listener for clicking.
		canvas.addMouseMotionListener(this); // Add mouse motion listener for camera movement.

		// Set the layout of the panel and add the canvas.
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);

		// Create a top panel for the UI (timer and points).
		JPanel topPanel = new JPanel();
		topPanel.setOpaque(true);
		topPanel.setBackground(Color.WHITE);
		topPanel.setLayout(new BorderLayout());

		// Initialize the timer label.
		timerLabel = new JLabel("Time: 05:00");
		timerLabel.setForeground(Color.BLACK);
		timerLabel.setFont(new Font("Serif", Font.BOLD, 16));
		timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		topPanel.add(timerLabel, BorderLayout.WEST);

		// Initialize the points label.
		pointsLabel = new JLabel("Puzzles Solved: " + points);
		pointsLabel.setForeground(Color.BLACK);
		pointsLabel.setFont(new Font("Serif", Font.BOLD, 16));
		pointsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		topPanel.add(pointsLabel, BorderLayout.EAST);

		// Add the top panel to the main panel.
		add(topPanel, BorderLayout.NORTH);

		// Set up the 3D universe and viewing platform.
		SimpleUniverse universe = new SimpleUniverse(canvas);
		viewTG = universe.getViewingPlatform().getViewPlatformTransform();
		movement = new Movement(viewTG); // Initialize the movement handler.

		// Ensure the character's TransformGroup is initialized.
		if (Library.characterTG == null) {
			Library.characterTG = new TransformGroup();
		}
		Library.characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// Create the scene and set up the picking tool.
		sceneBG = create_Scene();
		pickTool = new PickTool(sceneBG);
		pickTool.setMode(PickTool.GEOMETRY);

		// Add collision detection to the character.
		if (Library.characterTG.numChildren() > 0) {
			Sphere characterSphere = (Sphere) Library.characterTG.getChild(0);
			Shape3D characterShape = (Shape3D) characterSphere.getChild(0);
			CollisionDetectCharacter collisionBehavior = new CollisionDetectCharacter(characterShape);
			collisionBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
			Library.characterTG.addChild(collisionBehavior);
		}

		// Set the initial camera position.
		Transform3D initialView = new Transform3D();
		initialView.setTranslation(new Vector3f(0f, 1f, 20f));
		viewTG.setTransform(initialView);

		// Ensure the ghost is hidden initially.
		if (ghostObject != null) {
			ghostObject.hideGhost();
		}

		// Compile the scene and add it to the universe.
		sceneBG.compile();
		universe.addBranchGraph(sceneBG);

		// Start the game timer.
		startTimer();
	}

	// Method to start the game timer.
	private void startTimer() {
		gameTimer = new Timer(1000, e -> {
			if (gameOver) return; // Stop if the game is over.
			timeRemaining--; // Decrease the remaining time by 1 second.
			updateTimerDisplay(); // Update the timer display.
			if (timeRemaining <= 0) { // Check if time has run out.
				gameTimer.stop();
				handleGameOver(); // Handle game over condition.
			}
		});
		gameTimer.start();
	}

	// Method to update the timer display in the UI.
	private void updateTimerDisplay() {
		int minutes = timeRemaining / 60;
		int seconds = timeRemaining % 60;
		timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
	}

	// Method to handle the game over condition when time runs out.
	private void handleGameOver() {
		gameOver = true;
		soundManager.playSound("lose.wav", false); // Play the lose sound.

		// Show the ghost in front of the character.
		if (ghostObject != null) {
			// Get the character's position and orientation.
			Transform3D viewTransform = new Transform3D();
			viewTG.getTransform(viewTransform);
			Vector3f characterPos = new Vector3f();
			viewTransform.get(characterPos);

			// Get the character's forward direction (Z-axis in view space).
			Vector3f forward = new Vector3f(0, 0, -1); // Forward in view space (negative Z).
			viewTransform.transform(forward);
			forward.normalize();

			// Position the ghost 2 units in front of the character.
			float distanceInFront = 2.0f;
			Vector3f ghostPos = new Vector3f(forward);
			ghostPos.scale(distanceInFront);
			ghostPos.add(characterPos);
			ghostPos.y = characterPos.y - 0.5f; // Lower the ghost relative to the character.

			// Update the ghost's position and show it.
			ghostObject.setPosition(ghostPos);
			ghostObject.showGhost();
		}

		// Show a game over dialog with an option to restart.
		int option = JOptionPane.showConfirmDialog(
				this,
				"Game Over! Time's up.\nDo you want to restart the game?",
				"Game Over",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
		);

		if (option == JOptionPane.YES_OPTION) {
			restartGame(); // Restart the game if the player chooses to.
		} else {
			System.exit(0); // Exit the application if the player chooses not to restart.
		}
	}

	// Method to restart the game, resetting all game state.
	private void restartGame() {
		points = 0; // Reset points.
		bookshelfUsage.clear(); // Clear the bookshelf usage map.
		timeRemaining = 300; // Reset the timer to 5 minutes.
		gameOver = false; // Reset the game over flag.

		// Reset the camera position.
		Transform3D initialView = new Transform3D();
		initialView.setTranslation(new Vector3f(0f, 2f, 0f));
		viewTG.setTransform(initialView);
		Library.position.set(new Vector3f(0f, 2f, 20f));
		firstMouse = true; // Reset mouse movement tracking.

		// Update the UI labels.
		pointsLabel.setText("Puzzles Solved: " + points);
		timerLabel.setText("Time: 05:00");

		// Hide the ghost again on restart.
		if (ghostObject != null) {
			ghostObject.hideGhost();
		}

		// Restart the timer and refocus the canvas.
		startTimer();
		canvas.requestFocusInWindow();
	}

	// Method to increment the player's points when a puzzle is solved.
	public void incrementPoints() {
		if (gameOver) return; // Do nothing if the game is over.
		points++;
		pointsLabel.setText("Puzzles Solved: " + points);
		pointsLabel.revalidate();
		pointsLabel.repaint();
	}

	// Method to mark a shelf as used (solved).
	public void markShelfAsUsed(String shelfId) {
		bookshelfUsage.put(shelfId, true);
	}

	// Mouse movement handler for camera rotation.
	@Override
	public void mouseMoved(MouseEvent e) {
		if (!bookGameActive && !gameOver) {
			processMouseMovement(e);
		}
	}

	// Mouse drag handler for camera rotation (same as mouseMoved).
	@Override
	public void mouseDragged(MouseEvent e) {
		if (!bookGameActive && !gameOver) {
			processMouseMovement(e);
		}
	}

	// Process mouse movement to rotate the camera.
	private void processMouseMovement(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (firstMouse) { // Initialize mouse position on first movement.
			lastMouseX = x;
			lastMouseY = y;
			firstMouse = false;
			System.out.println("Mouse reset to: (" + x + ", " + y + ")");
			return;
		}
		int deltaX = x - lastMouseX; // Calculate mouse movement delta.
		int deltaY = y - lastMouseY;
		lastMouseX = x;
		lastMouseY = y;
		movement.processMouseMovement(deltaX, deltaY, e.isShiftDown()); // Update camera rotation.
	}

	// Mouse click handler for interacting with objects (books, doors).
	@Override
	public void mouseClicked(MouseEvent e) {
		if (bookGameActive || gameOver) return; // Ignore clicks if the book game is active or the game is over.

		// Convert the mouse click position to a 3D ray for picking.
		Point3d point3d = new Point3d();
		Point3d center = new Point3d();
		canvas.getPixelLocationInImagePlate(e.getX(), e.getY(), point3d);
		canvas.getCenterEyeInImagePlate(center);
		Transform3D transform3D = new Transform3D();
		canvas.getImagePlateToVworld(transform3D);
		transform3D.transform(point3d);
		transform3D.transform(center);
		Vector3d mouseVec = new Vector3d();
		mouseVec.sub(point3d, center);
		mouseVec.normalize();
		pickTool.setShapeRay(point3d, mouseVec);
		PickResult pickResult = pickTool.pickClosest();

		// Log the click and pick result.
		System.out.println("Click at (" + e.getX() + ", " + e.getY() + ") - Pick result: " + (pickResult != null ? "Hit" : "Miss"));

		if (pickResult != null) {
			Node node = pickResult.getNode(PickResult.SHAPE3D); // Get the picked Shape3D node.
			System.out.println("Picked node: " + (node != null ? node.getClass().getSimpleName() : "null"));

			// Traverse up the scene graph to find a TransformGroup with user data.
			while (node != null) {
				System.out.println("Node: " + node.getClass().getSimpleName());
				if (node instanceof TransformGroup) {
					TransformGroup tg = (TransformGroup) node;
					Object userData = tg.getUserData();
					System.out.println("  UserData: " + userData);

					if ("book".equals(userData)) { // Check if the picked object is a book.
						Node parent = tg.getParent();
						while (parent != null) { // Traverse up to find the shelf.
							if (parent instanceof TransformGroup) {
								TransformGroup parentTG = (TransformGroup) parent;
								Object parentUserData = parentTG.getUserData();
								System.out.println("  Parent UserData: " + parentUserData);
								if (parentUserData != null && parentUserData.toString().startsWith("shelf_")) {
									String shelfId = (String) parentUserData;
									System.out.println("Found book on shelf: " + shelfId);
									if (bookshelfUsage.getOrDefault(shelfId, false)) {
										JOptionPane.showMessageDialog(this, shelfId + " has already been solved!");
										return;
									}
									int shelfNumber = Integer.parseInt(shelfId.split("_")[1]);
									startBookOrderingGame(shelfNumber, shelfId); // Start the book ordering game.
									return;
								}
							}
							parent = parent.getParent();
						}
						System.out.println("No shelf found in hierarchy above book!");
						return;
					} else if ("door".equals(userData) || "door_open".equals(userData)) { // Check if the picked object is a door.
						movement.toggleNearbyDoors(); // Toggle the door state (open/close).
						return;
					}
				}
				node = node.getParent();
			}
			System.out.println("Clicked object is not a book or door or lacks appropriate userData");
		} else {
			System.out.println("Nothing picked at (" + e.getX() + ", " + e.getY() + ")");
		}
	}

	// Method to start the BookOrderingGame for a specific shelf.
	private void startBookOrderingGame(int shelfNumber, String shelfId) {
		SwingUtilities.invokeLater(() -> {
			movement.saveViewState(); // Save the current camera state.
			bookGameActive = true; // Set the book game active flag.
			BookOrderingGame game = new BookOrderingGame(shelfNumber, shelfId, this); // Launch the book ordering game.
			game.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					bookGameActive = false; // Reset the book game active flag.
					canvas.requestFocusInWindow(); // Refocus the canvas.
					movement.restoreViewState(); // Restore the camera state.
					firstMouse = true; // Reset mouse movement tracking.
					Point mousePos = canvas.getMousePosition();
					if (mousePos != null) {
						lastMouseX = mousePos.x;
						lastMouseY = mousePos.y;
						System.out.println("Mouse position reset to: (" + lastMouseX + ", " + lastMouseY + ") on restore");
					}
					// Check if the game was won and trigger book swapping.
					if (game.isGameWon()) {
						System.out.println("Book ordering game won for " + shelfId + "! Triggering book swap...");
						// library.checkWinAndSwap(); // Commented out; intended to swap books after winning.

						// Check if all puzzles are solved (win condition).
						boolean allPuzzlesSolved = true;
						for (int i = 1; i <= 8; i++) {
							String currentShelfId = "shelf_" + i;
							if (!bookshelfUsage.getOrDefault(currentShelfId, false)) {
								allPuzzlesSolved = false;
								break;
							}
						}

						// If all puzzles are solved, automatically open the doors.
						if (allPuzzlesSolved) {
							System.out.println("All puzzles solved! Player has won the game. Opening doors...");
							for (TransformGroup doorTG : Library.doors) {
								Object userData = doorTG.getUserData();
								boolean isOpen = userData instanceof String && ((String) userData).startsWith("door_open");
								if (!isOpen) {
									movement.toggleNearbyDoors();
									break; // toggleNearbyDoors will handle both doors.
								}
							}
							// Show a win message.
							JOptionPane.showMessageDialog(MainClass.this,
									"Congratulations! You’ve solved all puzzles and won the game!\nThe doors are now open.",
									"Victory",
									JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
			});
			System.out.println("BookOrderingGame launched for Shelf " + shelfNumber);
		});
	}

	// Key press handler for character movement.
	@Override
	public void keyPressed(KeyEvent e) {
		if (bookGameActive || gameOver) return; // Ignore input if the book game is active or the game is over.
		movement.keyPressed(e);
	}

	// Key release handler for character movement.
	@Override
	public void keyReleased(KeyEvent e) {
		if (bookGameActive || gameOver) return; // Ignore input if the book game is active or the game is over.
		movement.keyReleased(e);
	}

	// Main method to launch the game.
	public static void main(String[] args) {
		SoundManager soundManager = new SoundManager();
		soundManager.playSound("Horror.wav", true); // Play background music.
		SwingUtilities.invokeLater(() -> {
			frame = new JFrame("Haunted Leddy"); // Create the main window.
			MainClass mainPanel = new MainClass(); // Create the main game panel.
			frame.getContentPane().add(mainPanel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 800);
			frame.setVisible(true);
			mainPanel.canvas.requestFocusInWindow(); // Focus the canvas for input.
		});
	}

	// Empty implementations for unused MouseListener and KeyListener methods.
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
}