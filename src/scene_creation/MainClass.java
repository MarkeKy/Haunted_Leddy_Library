package scene_creation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

public class MainClass extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private final float MOVE_STEP = 0.15f;
	private final float JUMP_HEIGHT = 1.0f;  // Height to jump
	private final float CROUCH_HEIGHT = 1.0f; // Height to lower when crouching
	private float yaw = 0.0f;
	private float pitch = 0.0f;
	private int lastMouseX = -1, lastMouseY = -1;
	private boolean firstMouse = true;
	private float rotationSensitivity = 0.005f;
	private TransformGroup viewTG;
	private CustomCanvas3D canvas;
	private BranchGroup sceneBG;
	private PickTool pickTool;
	private static BookGame bookgame;
	private static Map<String, Boolean> bookshelfUsage = new HashMap<>();
	private boolean bookGameActive = false;
	private boolean isCrouching = false; // Track crouch state
	private boolean isJumping = false;  // Track jump state
	private float defaultHeight;        // Store default y position

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
	private static BranchGroup createAxes() {
	    BranchGroup axisGroup = new BranchGroup();
	    
	    // Create a LineArray with 12 vertices for 6 lines (2 per axis)
	    LineArray axisLines = new LineArray(12, LineArray.COORDINATES | LineArray.COLOR_3);
	    
	    // X-axis: Negative part (Yellow)
	    axisLines.setCoordinate(0, new Point3f(-100.0f, 0.0f, 0.0f)); // Start of negative X
	    axisLines.setCoordinate(1, new Point3f(0.0f, 0.0f, 0.0f));    // End at origin
	    axisLines.setColor(0, new Color3f(1.0f, 0.0f, 0.0f));         // Yellow
	    axisLines.setColor(1, new Color3f(1.0f, 0.0f, 0.0f));         // Yellow
	    
	    // X-axis: Positive part (Blue)
	    axisLines.setCoordinate(2, new Point3f(0.0f, 0.0f, 0.0f));    // Start at origin
	    axisLines.setCoordinate(3, new Point3f(100.0f, 0.0f, 0.0f));  // End of positive X
	    axisLines.setColor(2, new Color3f(1.0f, 0.5f, 0.0f));         // Blue
	    axisLines.setColor(3, new Color3f(1.0f, 0.5f, 0.0f));         // Blue
	    
	    // Y-axis: Negative part (Green)
	    axisLines.setCoordinate(4, new Point3f(0.0f, -100.0f, 0.0f)); // Start of negative Y
	    axisLines.setCoordinate(5, new Point3f(0.0f, 0.0f, 0.0f));    // End at origin
	    axisLines.setColor(4, new Color3f(0.0f, 1.0f, 0.0f));         // Green
	    axisLines.setColor(5, new Color3f(0.0f, 1.0f, 0.0f));         // Green
	    
	    // Y-axis: Positive part (Green)
	    axisLines.setCoordinate(6, new Point3f(0.0f, 0.0f, 0.0f));    // Start at origin
	    axisLines.setCoordinate(7, new Point3f(0.0f, 100.0f, 0.0f));  // End of positive Y
	    axisLines.setColor(6, new Color3f(0.0f, 1.0f, 0.0f));         // Green
	    axisLines.setColor(7, new Color3f(0.0f, 1.0f, 0.0f));         // Green
	    
	    // Z-axis: Negative part (Red)
	    axisLines.setCoordinate(8, new Point3f(0.0f, 0.0f, -100.0f)); // Start of negative Z
	    axisLines.setCoordinate(9, new Point3f(0.0f, 0.0f, 0.0f));    // End at origin
	    axisLines.setColor(8, new Color3f(0.0f, 0.0f, 1.0f));         // Red
	    axisLines.setColor(9, new Color3f(0.0f, 0.0f, 1.0f));         // Red
	    
	    // Z-axis: Positive part (Orange)
	    axisLines.setCoordinate(10, new Point3f(0.0f, 0.0f, 0.0f));   // Start at origin
	    axisLines.setCoordinate(11, new Point3f(0.0f, 0.0f, 100.0f)); // End of positive Z
	    axisLines.setColor(10, CommonsSK.Yellow);        // Orange
	    axisLines.setColor(11, CommonsSK.Yellow);        // Orange
	    
	    // Create a Shape3D object for the axes
	    Shape3D axisShape = new Shape3D(axisLines);
	    
	    // Set appearance to ensure per-vertex colors are used
	    Appearance axisAppearance = new Appearance();
	    ColoringAttributes ca = new ColoringAttributes();
	    ca.setShadeModel(ColoringAttributes.NICEST);  // Best color accuracy
	    axisAppearance.setColoringAttributes(ca);
	    
	    axisShape.setAppearance(axisAppearance);
	    axisGroup.addChild(axisShape);
	    
	    return axisGroup;
	}


	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();
		sceneTG.addChild(Library.create_Library());
		sceneBG.addChild(sceneTG);
		sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1));
		// Add reference axes
	    sceneBG.addChild(createAxes());
		return sceneBG;
	}

	public MainClass() {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new CustomCanvas3D(config);
		canvas.setFocusable(true);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);

		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);

		SimpleUniverse universe = new SimpleUniverse(canvas);
		viewTG = universe.getViewingPlatform().getViewPlatformTransform();

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
		initialView.setTranslation(new Vector3f(0f, 3f, 20f));
		viewTG.setTransform(initialView);

		defaultHeight = Library.position.y; // Store initial height (e.g., 2.0f)

		sceneBG.compile();
		universe.addBranchGraph(sceneBG);
	}

	private boolean tryMove(Vector3f proposedPosition) {
		Transform3D tempTransform = new Transform3D();
		tempTransform.setTranslation(proposedPosition);
		Library.characterTG.setTransform(tempTransform);

		// Reset colliding to false before checking to ensure fresh state
		CollisionDetectCharacter.colliding = false;

		// Brief delay to allow collision behavior to process (if needed)
		try {
			Thread.sleep(10);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}

		if (CollisionDetectCharacter.colliding) {
			System.out.println("Collision detected at " + proposedPosition + ", reverting to " + Library.position);
			tempTransform.setTranslation(Library.position);
			Library.characterTG.setTransform(tempTransform);
			return false;
		}
		return true;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!bookGameActive) {
			processMouseMovement(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!bookGameActive) {
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
			return;
		}
		int deltaX = x - lastMouseX;
		int deltaY = y - lastMouseY;
		lastMouseX = x;
		lastMouseY = y;

		if (!e.isShiftDown()) {
			yaw += deltaX * rotationSensitivity;
			pitch += deltaY * rotationSensitivity;
			float pitchLimit = (float)Math.toRadians(89);
			pitch = Math.max(-pitchLimit, Math.min(pitchLimit, pitch));
			updateLook();
		}
	}

	private void updateLook() {
		if (!bookGameActive) {
			Transform3D rotation = new Transform3D();
			rotation.rotY(yaw);
			Transform3D pitchRot = new Transform3D();
			pitchRot.rotX(pitch);
			rotation.mul(pitchRot);
			Transform3D translation = new Transform3D();
			translation.setTranslation(new Vector3f(Library.position.x, Library.position.y, Library.position.z));
			Transform3D viewTransform = new Transform3D();
			viewTransform.mul(translation, rotation);
			viewTG.setTransform(viewTransform);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (bookGameActive) return;

		Point3d point3d = new Point3d();
		Point3d center = new Point3d();
		canvas.getPixelLocationInImagePlate(e.getX(), e.getY(), point3d); // Use mouse position directly
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

		if (pickResult != null) {
			Node node = pickResult.getNode(PickResult.SHAPE3D);
			System.out.println("Picked node: " + (node != null ? node.getClass().getSimpleName() : "null"));

			while (node != null) {
				if (node instanceof TransformGroup) {
					TransformGroup tg = (TransformGroup) node;
					Object userData = tg.getUserData();
					System.out.println("Checking node with userData: " + userData);

					if ("book".equals(userData)) {
						TransformGroup bookRow = (TransformGroup) tg.getParent();
						if (bookRow == null) {
							System.out.println("Book has no row parent!");
							return;
						}
						TransformGroup shelfTG = (TransformGroup) bookRow.getParent();
						if (shelfTG == null) {
							System.out.println("Row has no shelf parent!");
							return;
						}
						TransformGroup positionedShelfTG = (TransformGroup) shelfTG.getParent();
						if (positionedShelfTG == null || positionedShelfTG.getUserData() == null) {
							System.out.println("Shelf has no parent or no user data!");
							return;
						}
						String shelfId = (String) positionedShelfTG.getUserData();
						System.out.println("Found book on shelf: " + shelfId);
						if (bookshelfUsage.getOrDefault(shelfId, false)) {
							JOptionPane.showMessageDialog(this, shelfId + " has already been used!");
							return;
						}
						bookshelfUsage.put(shelfId, true);
						int shelfNumber = Integer.parseInt(shelfId.split("_")[1]);
						startBookOrderingGame(shelfNumber);
						return;
					}
				}
				node = node.getParent();
			}
			System.out.println("Clicked object is not a book or lacks 'book' userData");
		} else {
			System.out.println("Nothing picked at (" + e.getX() + ", " + e.getY() + ")");
		}
	}

	private void startBookOrderingGame(int shelfNumber) {
		SwingUtilities.invokeLater(() -> {
			bookGameActive = true;
			BookOrderingGame game = new BookOrderingGame(shelfNumber);
			game.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					bookGameActive = false;
					canvas.requestFocusInWindow();
				}
			});
			System.out.println("BookOrderingGame launched for Shelf " + shelfNumber);
		});
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (bookGameActive) return;

		float moveX = 0, moveZ = 0, moveY = 0;
		boolean updatePosition = false;

		switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				moveX = -(float)(Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
				moveZ = -(float)(Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
				updatePosition = true;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				moveX = (float)(Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
				moveZ = (float)(Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
				updatePosition = true;
				break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
				moveX = -(float)Math.cos(yaw) * MOVE_STEP;
				moveZ = (float)Math.sin(yaw) * MOVE_STEP;
				updatePosition = true;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				moveX = (float)Math.cos(yaw) * MOVE_STEP;
				moveZ = -(float)Math.sin(yaw) * MOVE_STEP;
				updatePosition = true;
				break;
			case KeyEvent.VK_SPACE: // Jump
				if (!isJumping && !isCrouching) {
					isJumping = true;
					Vector3f jumpPosition = new Vector3f(Library.position);
					jumpPosition.y += JUMP_HEIGHT;
					System.out.println("Attempting jump to: " + jumpPosition);
					if (tryMove(jumpPosition)) {
						Library.lastSafePosition.set(Library.position);
						Library.position.set(jumpPosition);
						Movement.updatePosition();
						System.out.println("Jumped to: " + Library.position);
					} else {
						System.out.println("Jump blocked by collision");
					}
					// Immediately try to return to default height
					Vector3f returnPosition = new Vector3f(Library.position);
					returnPosition.y = defaultHeight;
					if (tryMove(returnPosition)) {
						Library.lastSafePosition.set(Library.position);
						Library.position.set(returnPosition);
						Movement.updatePosition();
						System.out.println("Returned to: " + Library.position);
					} else {
						System.out.println("Return blocked by collision");
					}
					isJumping = false;
					updatePosition = false; // Handled manually
				}
				break;
			case KeyEvent.VK_CONTROL: // Crouch
				if (!isCrouching && !isJumping) {
					moveY = -CROUCH_HEIGHT;
					isCrouching = true;
					updatePosition = true;
					System.out.println("Crouching to height: " + (Library.position.y + moveY));
				}
				break;
			default:
				return;
		}

		if (updatePosition) {
			Vector3f proposedPosition = new Vector3f(Library.position);
			proposedPosition.x += moveX;
			proposedPosition.z += moveZ;
			proposedPosition.y += moveY;

			if (tryMove(proposedPosition)) {
				Library.lastSafePosition.set(Library.position);
				Library.position.set(proposedPosition);
				Movement.updatePosition();
				System.out.println("Moved to: " + Library.position);
			} else {
				System.out.println("Blocked by collision at: " + proposedPosition);
			}

			updateLook();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (bookGameActive) return;

		if (e.getKeyCode() == KeyEvent.VK_CONTROL && isCrouching) {
			Vector3f proposedPosition = new Vector3f(Library.position);
			proposedPosition.y = defaultHeight;

			if (tryMove(proposedPosition)) {
				Library.lastSafePosition.set(Library.position);
				Library.position.set(proposedPosition);
				Movement.updatePosition();
				System.out.println("Standing up to: " + Library.position);
			} else {
				System.out.println("Can’t stand up due to collision at: " + proposedPosition);
			}

			isCrouching = false;
			updateLook();
		}
	}

	private static void startBackgroundSound() {
		try {
			URL soundURL = MainClass.class.getResource("Horrorsound.wav");
			if (soundURL == null) {
				System.err.println("Horrorsound.wav not found!");
				return;
			}
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
			javax.sound.sampled.Clip clip = AudioSystem.getClip();
			clip.open(audioIn);
			clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
		} catch (Exception ex) {
			System.err.println("Error loading background sound: " + ex.getMessage());
		}
	}

	public static void main(String[] args) {
		startBackgroundSound();
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

	@Override public void keyTyped(KeyEvent e) {}
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
}