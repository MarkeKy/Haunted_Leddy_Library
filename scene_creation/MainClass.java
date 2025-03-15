package scene_creation;

/* Copyright material for students working on assignments */
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

public class MainClass extends JPanel implements KeyListener {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;

	private static final int OBJ_NUM = 6; // +4 objects (Shaft, Motor, Blade, Guard)
	private static Objects[] object3D = new Objects[OBJ_NUM];  // Number of objects
	private static TransformGroup characterTG;

	// Character position used for movement
	private Vector3f position = new Vector3f();
	private final float MOVE_STEP = 0.2f;

	// For controlling camera look (first-person orientation)
	private float yaw = 0.0f;   // rotation around Y axis (left/right)
	private float pitch = 0.0f; // rotation around X axis (up/down)

	// For mouse motion tracking
	private int lastMouseX = -1, lastMouseY = -1;
	private boolean firstMouse = true;

	// Store the view transform group to update camera transforms
	private TransformGroup viewTG;

	// --- Helper Methods ---

	// Positions a wall using a translation vector.
	private static TransformGroup define_wall(TransformGroup wall, Vector3f vector) {
		TransformGroup wallTG = new TransformGroup();
		Transform3D wallTrans = new Transform3D();
		wallTrans.setTranslation(vector);
		wallTG.setTransform(wallTrans);
		wallTG.addChild(wall);
		return wallTG;
	}

	// Creates a single shelf positioned with a translation.
	private static TransformGroup createShelf(String textureFile, Vector3f translation) {
		ShelfObject shelf = new ShelfObject(textureFile);
		TransformGroup shelfTG = shelf.position_Object();
		TransformGroup positionedShelfTG = new TransformGroup();
		Transform3D translationTransform = new Transform3D();
		translationTransform.setTranslation(translation);
		positionedShelfTG.setTransform(translationTransform);
		positionedShelfTG.addChild(shelfTG);
		return positionedShelfTG;
	}

	// Creates a group of shelves.
	private static TransformGroup createShelves(int numShelves, float spacing, String textureFile) {
		TransformGroup shelvesTG = new TransformGroup();
		float totalWidth = (numShelves - 1) * spacing;
		float startX = -totalWidth / 2;
		for (int i = 0; i < numShelves; i++) {
			float xPos = startX + i * spacing;
			Vector3f shelfPos = new Vector3f(xPos, 1f, 0f);
			TransformGroup shelfTG = createShelf(textureFile, shelfPos);
			shelvesTG.addChild(shelfTG);
		}
		return shelvesTG;
	}

	// Creates the library scene (floor, walls, shelves, and character).
	private static TransformGroup create_Library() {
		TransformGroup libraryTG = new TransformGroup();

		// Create character transform group.
		characterTG = new TransformGroup();
		characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Appearance sphereAppearance = new Appearance();
		sphereAppearance.setMaterial(new Material());
		Sphere character = new Sphere(0.2f, sphereAppearance);
		characterTG.addChild(character);

		// Floor and walls.
		object3D[0] = new SquareShape("ImageEmrald.jpg", 4f, 0.01f, 4f);             // Floor
		object3D[1] = new SquareShape("MarbleTexture.jpg", 4f, 1.5f, 0.05f);           // Front wall
		object3D[2] = new SquareShape("MarbleTexture.jpg", 4f, 1.5f, 0.05f);           // Back wall
		object3D[3] = new SquareShape("ImageEmrald.jpg", 0.05f, 1.5f, 4f);             // Left wall
		object3D[4] = new SquareShape("ImageEmrald.jpg", 0.05f, 1.5f, 4f);             // Right wall

		TransformGroup frontWallTG = define_wall(object3D[1].position_Object(), new Vector3f(0f, 1.5f, 4f));
		TransformGroup backWallTG  = define_wall(object3D[2].position_Object(), new Vector3f(0f, 1.5f, -4f));
		TransformGroup leftWallTG  = define_wall(object3D[3].position_Object(), new Vector3f(-4f, 1.5f, 0f));
		TransformGroup rightWallTG = define_wall(object3D[4].position_Object(), new Vector3f(4f, 1.5f, 0f));

		// Create shelf objects.
		TransformGroup shelvesTG1 = createShelves(5, 1.5f, "FloorTexture.jpg");
		TransformGroup shelvesTG2 = createShelves(5, 1.5f, "FloorTexture.jpg");
		Transform3D zOffset = new Transform3D();
		zOffset.setTranslation(new Vector3f(0.0f, 0.0f, 5.0f));
		shelvesTG2.setTransform(zOffset);

		object3D[0].add_Child(shelvesTG1);
		object3D[0].add_Child(shelvesTG2);
		libraryTG.addChild(object3D[0].position_Object());
		libraryTG.addChild(frontWallTG);
		libraryTG.addChild(backWallTG);
		libraryTG.addChild(leftWallTG);
		libraryTG.addChild(rightWallTG);
		libraryTG.addChild(characterTG);

		return libraryTG;
	}

	// Builds the content branch.
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();
		sceneTG.addChild(create_Library());
		sceneBG.addChild(sceneTG);
		sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1));
		return sceneBG;
	}

	// --- Constructor ---
	public MainClass(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas = new Canvas3D(config);
		canvas.setFocusable(true);
		canvas.addKeyListener(this);
		add(canvas);

		SimpleUniverse su = new SimpleUniverse(canvas);
		viewTG = su.getViewingPlatform().getViewPlatformTransform();
		characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

		// Set initial camera at the character's eye-level.
		Transform3D initialView = new Transform3D();
		Vector3f eyeLevelPos = new Vector3f(position.x, position.y + 0.2f, position.z);
		initialView.setTranslation(eyeLevelPos);
		viewTG.setTransform(initialView);

		// Add a mouse motion listener to update the look direction.
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
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
					firstMouse = false;
					return;
				}
				int deltaX = x - lastMouseX;
				int deltaY = y - lastMouseY;
				lastMouseX = x;
				lastMouseY = y;

				// Sensitivity factor for smoother control.
				float sensitivity = 0.005f;
				yaw   += deltaX * sensitivity;
				pitch += deltaY * sensitivity;

				// Clamp pitch to avoid extreme up/down angles.
				float pitchLimit = (float)Math.toRadians(89);
				if (pitch > pitchLimit) {
					pitch = pitchLimit;
				}
				if (pitch < -pitchLimit) {
					pitch = -pitchLimit;
				}
				updateLook();
			}
		});

		sceneBG.compile();
		su.addBranchGraph(sceneBG);

		setLayout(new BorderLayout());
		add("Center", canvas);
		frame.setSize(800, 800);
		frame.setVisible(true);
	}

	// --- KeyListener Methods for Movement ---
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()){
			case KeyEvent.VK_UP:    position.z -= MOVE_STEP; break;
			case KeyEvent.VK_DOWN:  position.z += MOVE_STEP; break;
			case KeyEvent.VK_LEFT:  position.x -= MOVE_STEP; break;
			case KeyEvent.VK_RIGHT: position.x += MOVE_STEP; break;
		}
		updatePosition();
		updateLook();
	}

	// Updates the character's transform.
	private void updatePosition() {
		Transform3D transform = new Transform3D();
		transform.setTranslation(position);
		characterTG.setTransform(transform);
	}

	// --- Updates the view transform based on current yaw, pitch, and position ---
	private void updateLook() {
		Transform3D pitchRot = new Transform3D();
		pitchRot.rotX(pitch);
		Transform3D yawRot = new Transform3D();
		yawRot.rotY(yaw);
		yawRot.mul(pitchRot);

		// Extract the rotation as a Matrix3f.
		Matrix3f rotMatrix = new Matrix3f();
		yawRot.get(rotMatrix);

		// Eye-level position (adding an offset on Y).
		Vector3f eyePos = new Vector3f(position.x, position.y + 0.2f, position.z);
		Transform3D viewTransform = new Transform3D(rotMatrix, eyePos, 1.0f);
		viewTG.setTransform(viewTransform);
	}

	@Override
	public void keyTyped(KeyEvent e) { }
	@Override
	public void keyReleased(KeyEvent e) { }

	// --- Main Method ---
	public static void main(String[] args) {
		frame = new JFrame("Haunted Leddy");
		frame.getContentPane().add(new MainClass(create_Scene()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
