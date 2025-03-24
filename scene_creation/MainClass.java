package scene_creation;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.ColorCube;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

public class MainClass extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private final float MOVE_STEP = 0.15f;
	private float yaw = 0.0f;
	private float pitch = 0.0f;
	private int lastMouseX = -1, lastMouseY = -1;
	private boolean firstMouse = true;
	private int pointerX = 0, pointerY = 0;
	private float pointerSensitivity = 1.0f;
	private float rotationSensitivity = 0.005f;
	private TransformGroup viewTG;
	private CustomCanvas3D canvas;
	private BranchGroup sceneBG;
	private PickTool pickTool;
	private JFrame bookshelfPopup;

	// Map to track usage state for each bookshelf.
	private static Map<TransformGroup, Boolean> bookshelfUsage = new HashMap<>();

	// Custom Canvas3D to draw a red pointer on the screen
	private static class CustomCanvas3D extends Canvas3D {
		private int pointerX, pointerY;
		public CustomCanvas3D(GraphicsConfiguration config) {
			super(config);
		}
		public void setPointerPosition(int x, int y) {
			this.pointerX = x;
			this.pointerY = y;
		}
		@Override
		public void postRender() {
			Graphics2D g = (Graphics2D) getGraphics();
			if (g != null) {
				g.setColor(Color.RED);
				int size = 10;
				g.fillOval(pointerX - size / 2, pointerY - size / 2, size, size);
				g.dispose();
			}
		}
	}

	// Creates the complete scene: the library plus lighting
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();
		sceneTG.addChild(Library.create_Library());
		sceneBG.addChild(sceneTG);
		sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1));
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

		// Ensure Library.characterTG is not null before using it
		if (Library.characterTG == null) {
			Library.characterTG = new TransformGroup();
		}
		Library.characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		sceneBG = create_Scene();
		sceneBG.compile();
		universe.addBranchGraph(sceneBG);

		pickTool = new PickTool(sceneBG);
		pickTool.setMode(PickTool.GEOMETRY);

		Transform3D initialView = new Transform3D();
		initialView.setTranslation(new Vector3f(Library.position.x, Library.position.y, Library.position.z));
		viewTG.setTransform(initialView);

		// Set initial pointer position
		pointerX = 400;
		pointerY = 400;
		canvas.setPointerPosition(pointerX, pointerY);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		processMouseMovement(e);
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		processMouseMovement(e);
	}
	private void processMouseMovement(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (firstMouse) {
			lastMouseX = x;
			lastMouseY = y;
			pointerX = x;
			pointerY = y;
			canvas.setPointerPosition(pointerX, pointerY);
			firstMouse = false;
			return;
		}
		int deltaX = x - lastMouseX;
		int deltaY = y - lastMouseY;
		lastMouseX = x;
		lastMouseY = y;

		// Always update the pointer position.
		pointerX += (int)(deltaX * pointerSensitivity);
		pointerY += (int)(deltaY * pointerSensitivity);
		if (pointerX < 0) pointerX = 0;
		if (pointerX > canvas.getWidth()) pointerX = canvas.getWidth();
		if (pointerY < 0) pointerY = 0;
		if (pointerY > canvas.getHeight()) pointerY = canvas.getHeight();
		canvas.setPointerPosition(pointerX, pointerY);

		// Only update the view if Shift is NOT held down.
		if (!e.isShiftDown()) {
			yaw += deltaX * rotationSensitivity;
			pitch += deltaY * rotationSensitivity;
			float pitchLimit = (float)Math.toRadians(89);
			if (pitch > pitchLimit) pitch = pitchLimit;
			if (pitch < -pitchLimit) pitch = -pitchLimit;
			updateLook();
		}
	}

	private void updateLook() {
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
	@Override
	public void mouseClicked(MouseEvent event) {
		System.out.println("Mouse clicked at pointer (" + pointerX + ", " + pointerY + ")");
		int x = pointerX;
		int y = pointerY;
		Point3d point3d = new Point3d();
		Point3d center = new Point3d();
		canvas.getPixelLocationInImagePlate(x, y, point3d);
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
			System.out.println("Picked Shape3D: " + node);
			while (node != null) {
				if (node instanceof TransformGroup) {
					TransformGroup tg = (TransformGroup) node;
					System.out.println("Checking TG with user data: " + tg.getUserData());
					if ("book".equals(tg.getUserData())) {
						// Pass the picked bookshelf node to the popup method.
						showBookshelfPopup(tg);
						return;
					}
				}
				node = node.getParent();
			}
			System.out.println("No book TransformGroup found in the hierarchy.");
		} else {
			System.out.println("No pick result - nothing was clicked.");
		}
	}

	// Show a popup for the specified bookshelf. If it is already used, display a message.
	private void showBookshelfPopup(TransformGroup bookshelfTG) {
		SwingUtilities.invokeLater(() -> {
			// Check if this particular bookshelf has been used.
			if (bookshelfUsage.getOrDefault(bookshelfTG, false)) {
				JOptionPane.showMessageDialog(this,
						"I have used that one already",
						"Notice",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (bookshelfPopup != null && bookshelfPopup.isVisible()) {
				return;
			}
			bookshelfPopup = new JFrame("Bookshelf Options");
			bookshelfPopup.setSize(300, 150);
			bookshelfPopup.setLocationRelativeTo(frame);
			bookshelfPopup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

			JButton chooseButton = new JButton("Choose Book");
			chooseButton.addActionListener(e -> {
				bookshelfPopup.dispose();
				bookshelfPopup = null;
				// Mark this bookshelf as used.
				bookshelfUsage.put(bookshelfTG, true);
				startBookOrderingGame();
			});

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(e -> {
				bookshelfPopup.dispose();
				bookshelfPopup = null;
			});

			panel.add(chooseButton);
			panel.add(cancelButton);
			bookshelfPopup.add(panel);
			bookshelfPopup.setVisible(true);
			System.out.println("Bookshelf popup opened!");
		});
	}

	// Starts a simple "book ordering" game window (as a placeholder)
	private void startBookOrderingGame() {
		JFrame gameFrame = new JFrame("Organize Books");
		gameFrame.setSize(400, 300);
		gameFrame.setLocationRelativeTo(frame);
		gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel gamePanel = new JPanel(new BorderLayout());
		JLabel placeholderLabel = new JLabel("Book Ordering Game: Arrange books in the correct order", SwingConstants.CENTER);
		gamePanel.add(placeholderLabel, BorderLayout.CENTER);
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> gameFrame.dispose());
		gamePanel.add(closeButton, BorderLayout.SOUTH);
		gameFrame.add(gamePanel);
		gameFrame.setVisible(true);
		System.out.println("Book ordering game started!");
		new NumberSortGame();
	}

	// Process keyboard events to move the viewer's position
	@Override
	public void keyPressed(KeyEvent e) {
		Library.lastSafePosition.set(Library.position);
		float moveX = 0, moveZ = 0;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				moveX = -(float)(Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
				moveZ = -(float)(Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				moveX = (float)(Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
				moveZ = (float)(Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
				break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
				moveX = -(float)Math.cos(yaw) * MOVE_STEP;
				moveZ = (float)Math.sin(yaw) * MOVE_STEP;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				moveX = (float)Math.cos(yaw) * MOVE_STEP;
				moveZ = (float)Math.sin(yaw) * MOVE_STEP;
				break;
		}
		Library.position.x += moveX;
		Library.position.z += moveZ;
		Movement.updatePosition();
		updateLook();
	}

	@Override public void keyTyped(KeyEvent e) {}
	@Override public void keyReleased(KeyEvent e) {}
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}

	// Static method to start the background sound using Clip so it plays immediately
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
			System.out.println("Background sound started using Clip.");
		} catch (Exception ex) {
			System.err.println("Error loading background sound: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// Start background sound immediately as the program begins
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
}
