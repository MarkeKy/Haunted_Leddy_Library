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

	private static final int OBJ_NUM = 6;
	private static Objects[] object3D = new Objects[OBJ_NUM];
	private static TransformGroup characterTG;

	// Character position used for movement
	private Vector3f position = new Vector3f();
	private final float MOVE_STEP = 0.2f;

	// For controlling camera look (first-person orientation)
	private float yaw = 0.0f;   // Rotation around Y axis (left/right)
	private float pitch = 0.0f; // Rotation around X axis (up/down)

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
		// The sphere represents the character.
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

		// Set the initial view transform so the camera is exactly at the sphere's center.
		Transform3D initialView = new Transform3D();
		initialView.setTranslation(new Vector3f(position.x, position.y, position.z));
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

				float sensitivity = 0.005f;
				yaw   += deltaX * sensitivity;
				pitch += deltaY * sensitivity;

				// Clamp pitch so you can't flip over.
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

	// --- KeyListener Methods for FPP Movement ---
	@Override
	public void keyPressed(KeyEvent e) {
		float moveX = 0, moveY = 0, moveZ = 0;

		// For a typical Java 3D FPP setup, the default view direction is along -Z.
		// So we define the forward vector accordingly.
		switch(e.getKeyCode()){
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				// Forward vector: note the negative Z component.
				moveX = -(float)(Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
				moveY = (float)(Math.sin(pitch)) * MOVE_STEP;
				moveZ = -(float)(Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				// Backward: the inverse of forward.
				moveX = (float)(Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
				moveY = -(float)(Math.sin(pitch)) * MOVE_STEP;
				moveZ = (float)(Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
				break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
				// Strafe left (perpendicular to forward).
				moveX = -(float)Math.cos(yaw) * MOVE_STEP;
				moveZ = -(float)Math.sin(yaw) * MOVE_STEP;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				// Strafe right.
				moveX = (float)Math.cos(yaw) * MOVE_STEP;
				moveZ = (float)Math.sin(yaw) * MOVE_STEP;
				break;
		}

		// Update the sphere's (character's) position.
		position.x += moveX;
		position.y += moveY;
		position.z += moveZ;

		updatePosition();
		updateLook();
	}

	@Override
	public void keyTyped(KeyEvent e) { }
	@Override
	public void keyReleased(KeyEvent e) { }

	// Updates the sphere's transform.
	private void updatePosition() {
		Transform3D transform = new Transform3D();
		transform.setTranslation(position);
		characterTG.setTransform(transform);
	}

	// Updates the view transform so the camera stays inside the sphere.
	// Here we combine the translation (to the sphere’s center) with the rotation (from yaw and pitch).
	private void updateLook() {
		// Build the rotation transform.
		Transform3D rotation = new Transform3D();
		rotation.rotY(yaw);
		Transform3D pitchRot = new Transform3D();
		pitchRot.rotX(pitch);
		rotation.mul(pitchRot); // rotation = R_yaw * R_pitch

		// Build the translation transform.
		Transform3D translation = new Transform3D();
		translation.setTranslation(new Vector3f(position.x, position.y+1.0f, position.z));

		// Combine: viewTransform = translation * rotation.
		// This ensures that the camera is first placed at the sphere's center, then rotated.
		Transform3D viewTransform = new Transform3D();
		viewTransform.mul(translation, rotation);
		viewTG.setTransform(viewTransform);
	}

	// --- Main Method ---
	public static void main(String[] args) {
		frame = new JFrame("Haunted Leddy");
		frame.getContentPane().add(new MainClass(create_Scene()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
