package scene_creation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/* Copyright material for students working on assignments */
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.Iterator;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

public class MainClass extends JPanel implements KeyListener, MouseListener, ActionListener{
	
	private static final long serialVersionUID = 1L;
	private static JFrame frame;
    private final float MOVE_STEP = 0.15f;
 // For controlling camera look (first-person orientation)
 	private float yaw = 0.0f;   // Rotation around Y axis (left/right)
 	private float pitch = 0.0f; // Rotation around X axis (up/down)

 	// For mouse motion tracking
 	private int lastMouseX = -1, lastMouseY = -1;
 	private boolean firstMouse = true;

 	// Store the view transform group to update camera transforms
 	private TransformGroup viewTG;
 	private static Canvas3D canvas;
 	private static PickTool pickTool;
 	
 	//Books that can be swapped
	private TransformGroup selectedTG = null; // Track first selection
	
	// Removed instance variables for Movement, Library, CollisionDetectCharacter
	// and instead refer to Library's static fields and use Movement's static method.
	private static BookGame bookgame; //Instance of BookGame class

	/* a function to build the content branch, including the fan and other environmental settings */
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();	   // make 'sceneTG' continuously rotating
		sceneTG.addChild(Library.create_Library());                    // add the library to the rotating 'sceneTG'

		sceneBG.addChild(sceneTG);                         // keep the following stationary
		sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1));

		return sceneBG;
	}

	// Movement via key presses.
	@Override
	public void keyPressed(KeyEvent e) {
		// If a collision is active, ignore further key input.
		if (CollisionDetectCharacter.colliding) { // ADDED: Using static colliding flag from CollisionDetectCharacter
			System.out.println("Movement ignored while colliding.");
			return;
		}

		// Save the current (safe) position.
		Library.lastSafePosition.set(Library.position); // ADDED: Use Library static fields

		float moveX = 0, moveY = 0, moveZ = 0;

		// For a typical Java 3D FPP setup, the default view direction is along -Z.
		switch(e.getKeyCode()){
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
				// Use perpendicular left vector: (-cos(yaw), sin(yaw))
				moveX = -(float)Math.cos(yaw) * MOVE_STEP;
				moveZ =  (float)Math.sin(yaw) * MOVE_STEP;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				// Use perpendicular right vector: (cos(yaw), -sin(yaw))
				moveX = (float)Math.cos(yaw) * MOVE_STEP;
				moveZ = -(float)Math.sin(yaw) * MOVE_STEP;
				break;
		}

		// Update the sphere's (character's) position.
		Library.position.x += moveX; // ADDED: Use Library static fields
		Library.position.z += moveZ;

		Movement.updatePosition(); // ADDED: Call static update method from Movement
		updateLook();
	}
    
	//Birds EYE view
//	    public MainClass(BranchGroup sceneBG) {
//	        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
//	        Canvas3D canvas = new Canvas3D(config);
//	        canvas.setFocusable(true);             // ensure canvas receives key events
//	        canvas.addKeyListener(this);           // add listener to this program
//	        add(canvas);
//	        
//	        SimpleUniverse su = new SimpleUniverse(canvas);
//	        CommonsSK.define_Viewer(su, new Point3d(0f, 13f, 0f)); //Change the eye to new Point3d(0f, 6f, 0f) to get birds eye view
//	        sceneBG.compile();
//	        su.addBranchGraph(sceneBG);
//	        setLayout(new BorderLayout());
//	        add("Center", canvas);
//	        frame.setSize(800, 800);
//	        frame.setVisible(true);
//	    }
	 
	//First Person Perspective
	public MainClass(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new Canvas3D(config);
		canvas.setFocusable(true);
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		add(canvas);

		SimpleUniverse su = new SimpleUniverse(canvas);
		viewTG = su.getViewingPlatform().getViewPlatformTransform();
		Library.characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		
		// Initialize PickTool
	    pickTool = new PickTool(sceneBG);
	    pickTool.setMode(PickTool.GEOMETRY);

		// Set the initial view transform so the camera is at the sphere's center.
		Transform3D initialView = new Transform3D();
		initialView.setTranslation(new Vector3f(Library.position.x, Library.position.y, Library.position.z));
		viewTG.setTransform(initialView);

		// Add collision detection behavior to the character.
		// Retrieve the character sphere from characterTG.
		if (Library.characterTG.numChildren() > 0) {
			Sphere characterSphere = (Sphere) Library.characterTG.getChild(0);
			Shape3D characterShape = (Shape3D) characterSphere.getChild(0);
			CollisionDetectCharacter collisionBehavior = new CollisionDetectCharacter(characterShape);
			collisionBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
			Library.characterTG.addChild(collisionBehavior);
		}

		// ADDED: Initialize book game instance.
		bookgame = new BookGame();

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
		canvas.requestFocusInWindow(); // Add this
	}
    
	// Updates the view transform so the camera stays inside the sphere.
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

	// Book Game
	@Override
	public void mouseClicked(MouseEvent event) {
		System.out.println("Mouse clicked at (" + event.getX() + ", " + event.getY() + ")");
		int x = event.getX();
		int y = event.getY();
		Point3d point3d = new Point3d(), center = new Point3d();
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
						if (selectedTG == null) {
							selectedTG = tg;
							bookgame.zoomObject(selectedTG, 1.2);  // Scale up
							System.out.println("First book selected at position: " + bookgame.getTransformPosition(tg));
						} else if (selectedTG != tg) {
							if (selectedTG.getParent() == tg.getParent()) {
								bookgame.zoomObject(selectedTG, 1);  // Scale down to original size
								bookgame.swapPositions(selectedTG, tg);
								System.out.println("Books swapped successfully.");
								selectedTG = null;
							} else {
								System.out.println("Cannot swap books from different rows.");
								selectedTG = null;
							}
						}
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

	public static void main(String[] args) {
		frame = new JFrame("Haunted Leddy");                   // NOTE: change XY to student's initials
		frame.getContentPane().add(new MainClass(create_Scene()));  // start the program
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
