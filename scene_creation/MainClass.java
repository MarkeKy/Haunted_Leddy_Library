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

public class MainClass extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private int lastMouseX = -1, lastMouseY = -1;
	private boolean firstMouse = true;
	private TransformGroup viewTG;
	private CustomCanvas3D canvas;
	private BranchGroup sceneBG;
	private PickTool pickTool;
	private static BookGame bookgame;
	protected static Map<String, Boolean> bookshelfUsage = new HashMap<>();
	private boolean bookGameActive = false;
	private static int points = 0;
	private JLabel pointsLabel;
	private JLabel timerLabel;
	private Timer gameTimer;
	private int timeRemaining = 300;
	private boolean gameOver = false;
	private Movement movement;
	private SoundManager soundManager;
	private GhostObject ghostObject; // Reference to the GhostObject
	private Library library; // Persistent Library instance

	private static class CustomCanvas3D extends Canvas3D {
		public CustomCanvas3D(GraphicsConfiguration config) {
			super(config);
			setDoubleBufferEnable(true);
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
		}
	}

	public BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();
		Library library = new Library();
		sceneTG.addChild(library.create_Library());

		// Find the GhostObject in the library's objects
		for (Objects obj : library.getObjects()) {
			if (obj instanceof GhostObject) {
				ghostObject = (GhostObject) obj;
				break;
			}
		}

		sceneBG.addChild(sceneTG);
		sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1));
		return sceneBG;
	}

	public MainClass() {
		soundManager = new SoundManager();

		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new CustomCanvas3D(config);
		canvas.setFocusable(true);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);

		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);

		JPanel topPanel = new JPanel();
		topPanel.setOpaque(true);
		topPanel.setBackground(Color.WHITE);
		topPanel.setLayout(new BorderLayout());

		timerLabel = new JLabel("Time: 05:00");
		timerLabel.setForeground(Color.BLACK);
		timerLabel.setFont(new Font("Serif", Font.BOLD, 16));
		timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		topPanel.add(timerLabel, BorderLayout.WEST);

		pointsLabel = new JLabel("Puzzles Solved: " + points);
		pointsLabel.setForeground(Color.BLACK);
		pointsLabel.setFont(new Font("Serif", Font.BOLD, 16));
		pointsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		topPanel.add(pointsLabel, BorderLayout.EAST);

		add(topPanel, BorderLayout.NORTH);

		SimpleUniverse universe = new SimpleUniverse(canvas);
		viewTG = universe.getViewingPlatform().getViewPlatformTransform();
		movement = new Movement(viewTG);

		if (Library.characterTG == null) {
			Library.characterTG = new TransformGroup();
		}
		Library.characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		sceneBG = create_Scene();
		pickTool = new PickTool(sceneBG);
		pickTool.setMode(PickTool.GEOMETRY);

		if (Library.characterTG.numChildren() > 0) {
			Sphere characterSphere = (Sphere) Library.characterTG.getChild(0);
			Shape3D characterShape = (Shape3D) characterSphere.getChild(0);
			CollisionDetectCharacter collisionBehavior = new CollisionDetectCharacter(characterShape);
			collisionBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
			Library.characterTG.addChild(collisionBehavior);
		}

		bookgame = new BookGame();

		Transform3D initialView = new Transform3D();
		initialView.setTranslation(new Vector3f(0f, 2f, 20f));
		viewTG.setTransform(initialView);

		// Ensure the ghost is hidden initially
		if (ghostObject != null) {
			ghostObject.hideGhost();
		}

		sceneBG.compile();
		universe.addBranchGraph(sceneBG);

		startTimer();
	}

	private void startTimer() {
		gameTimer = new Timer(1000, e -> {
			if (gameOver) return;
			timeRemaining--;
			updateTimerDisplay();
			if (timeRemaining <= 0) {
				gameTimer.stop();
				handleGameOver();
			}
		});
		gameTimer.start();
	}

	private void updateTimerDisplay() {
		int minutes = timeRemaining / 60;
		int seconds = timeRemaining % 60;
		timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
	}

	private void handleGameOver() {
		gameOver = true;
		soundManager.playSound("lose.wav", false);

		// Show the ghost in front of the character
		if (ghostObject != null) {
			// Get the character's position and orientation
			Transform3D viewTransform = new Transform3D();
			viewTG.getTransform(viewTransform);
			Vector3f characterPos = new Vector3f();
			viewTransform.get(characterPos);

			// Get the character's forward direction (Z-axis in view space)
			Vector3f forward = new Vector3f(0, 0, -1); // Forward in view space (negative Z)
			viewTransform.transform(forward);
			forward.normalize();

			// Position the ghost 2 units in front of the character
			float distanceInFront = 2.0f;
			Vector3f ghostPos = new Vector3f(forward);
			ghostPos.scale(distanceInFront);
			ghostPos.add(characterPos);
			ghostPos.y = characterPos.y - 0.5f; // Lower the ghost by 1 unit relative to the character

			// Update the ghost's position and show it
			ghostObject.setPosition(ghostPos);
			ghostObject.showGhost();
		}

		int option = JOptionPane.showConfirmDialog(
				this,
				"Game Over! Time's up.\nDo you want to restart the game?",
				"Game Over",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
		);

		if (option == JOptionPane.YES_OPTION) {
			restartGame();
		} else {
			System.exit(0);
		}
	}

	private void restartGame() {
		points = 0;
		bookshelfUsage.clear();
		timeRemaining = 300;
		gameOver = false;

		Transform3D initialView = new Transform3D();
		initialView.setTranslation(new Vector3f(0f, 2f, 0f));
		viewTG.setTransform(initialView);
		Library.position.set(new Vector3f(0f, 2f, 20f));
		firstMouse = true;

		pointsLabel.setText("Puzzles Solved: " + points);
		timerLabel.setText("Time: 05:00");

		// Hide the ghost again on restart
		if (ghostObject != null) {
			ghostObject.hideGhost();
		}

		startTimer();
		canvas.requestFocusInWindow();
	}

	public void incrementPoints() {
		if (gameOver) return;
		points++;
		pointsLabel.setText("Puzzles Solved: " + points);
		pointsLabel.revalidate();
		pointsLabel.repaint();
	}

	public void markShelfAsUsed(String shelfId) {
		bookshelfUsage.put(shelfId, true);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!bookGameActive && !gameOver) {
			processMouseMovement(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!bookGameActive && !gameOver) {
			processMouseMovement(e);
		}
	}

	private void processMouseMovement(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (firstMouse) {
			lastMouseX = x;
			lastMouseY = y;
			firstMouse = false;
			System.out.println("Mouse reset to: (" + x + ", " + y + ")");
			return;
		}
		int deltaX = x - lastMouseX;
		int deltaY = y - lastMouseY;
		lastMouseX = x;
		lastMouseY = y;
		movement.processMouseMovement(deltaX, deltaY, e.isShiftDown());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (bookGameActive || gameOver) return;

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

		System.out.println("Click at (" + e.getX() + ", " + e.getY() + ") - Pick result: " + (pickResult != null ? "Hit" : "Miss"));

		if (pickResult != null) {
			Node node = pickResult.getNode(PickResult.SHAPE3D);
			System.out.println("Picked node: " + (node != null ? node.getClass().getSimpleName() : "null"));

			while (node != null) {
				System.out.println("Node: " + node.getClass().getSimpleName());
				if (node instanceof TransformGroup) {
					TransformGroup tg = (TransformGroup) node;
					Object userData = tg.getUserData();
					System.out.println("  UserData: " + userData);

					if ("book".equals(userData)) {
						Node parent = tg.getParent();
						while (parent != null) {
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
									startBookOrderingGame(shelfNumber, shelfId);
									return;
								}
							}
							parent = parent.getParent();
						}
						System.out.println("No shelf found in hierarchy above book!");
						return;
					} else if ("door".equals(userData) || "door_open".equals(userData)) {
						movement.toggleNearbyDoors();
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

	private void startBookOrderingGame(int shelfNumber, String shelfId) {
		SwingUtilities.invokeLater(() -> {
			movement.saveViewState();
			bookGameActive = true;
			BookOrderingGame game = new BookOrderingGame(shelfNumber, shelfId, this);
			game.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					bookGameActive = false;
					canvas.requestFocusInWindow();
					movement.restoreViewState();
					firstMouse = true;
					Point mousePos = canvas.getMousePosition();
					if (mousePos != null) {
						lastMouseX = mousePos.x;
						lastMouseY = mousePos.y;
						System.out.println("Mouse position reset to: (" + lastMouseX + ", " + lastMouseY + ") on restore");
					}
					// Check if the game was won and trigger book swapping
					if (game.isGameWon()) {
						System.out.println("Book ordering game won for " + shelfId + "! Triggering book swap...");
						//library.checkWinAndSwap();

						// Check if all puzzles are solved (win condition)
						boolean allPuzzlesSolved = true;
						for (int i = 1; i <= 8; i++) {
							String currentShelfId = "shelf_" + i;
							if (!bookshelfUsage.getOrDefault(currentShelfId, false)) {
								allPuzzlesSolved = false;
								break;
							}
						}

						// If all puzzles are solved, automatically open the doors
						if (allPuzzlesSolved) {
							System.out.println("All puzzles solved! Player has won the game. Opening doors...");
							// Ensure doors are opened (if not already open)
							for (TransformGroup doorTG : Library.doors) {
								Object userData = doorTG.getUserData();
								boolean isOpen = userData instanceof String && ((String) userData).startsWith("door_open");
								if (!isOpen) {
									movement.toggleNearbyDoors();
									break; // toggleNearbyDoors will handle both doors
								}
							}
							// Optionally, show a win message
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

	@Override
	public void keyPressed(KeyEvent e) {
		if (bookGameActive || gameOver) return;
		movement.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (bookGameActive || gameOver) return;
		movement.keyReleased(e);
	}

	public static void main(String[] args) {
		SoundManager soundManager = new SoundManager();
		soundManager.playSound("Horror.wav", true);
		SwingUtilities.invokeLater(() -> {
			frame = new JFrame("Haunted Leddy");
			MainClass mainPanel = new MainClass();
			frame.getContentPane().add(mainPanel);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(800, 800);
			frame.setVisible(true);
			mainPanel.canvas.requestFocusInWindow();
		});
	}

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