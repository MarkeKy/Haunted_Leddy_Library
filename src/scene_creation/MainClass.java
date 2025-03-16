package scene_creation;

/* Copyright material for students working on assignments */
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;




public class MainClass extends JPanel implements KeyListener{
	
	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private static final int OBJ_NUM = 6; //+4 objects (Shaft, Motor, Blade, Guard)
	private static Objects[] object3D = new Objects[OBJ_NUM];  //Number of objects
	private static TransformGroup characterTG;
    private static Vector3f position = new Vector3f();         //Update position of the character
    private static Vector3f lastSafePosition = new Vector3f();   // Last non-colliding position
    private final float MOVE_STEP = 0.2f;
    private static boolean colliding = false;                  //Flag to stop movement if collision has occurred
    
    //Wall method
    private static TransformGroup define_wall(TransformGroup wall,  Vector3f vector) {
    	TransformGroup WallTG = new TransformGroup();
 	    Transform3D WallTrans = new Transform3D();
 	    
 	    WallTrans.setTranslation(vector);
 	    WallTG.setTransform(WallTrans);
 	    WallTG.addChild(wall);
 	    
 	    return WallTG;
    }
    
 // Helper method to create a single shelf positioned with a translation
    private static TransformGroup createShelf(String textureFile, Vector3f translation) {
        ShelfObject shelf = new ShelfObject(textureFile);
        TransformGroup shelfTG = shelf.position_Object();
        
        // Create a TransformGroup for positioning the shelf
        TransformGroup positionedShelfTG = new TransformGroup();
        Transform3D translationTransform = new Transform3D();
        translationTransform.setTranslation(translation);
        positionedShelfTG.setTransform(translationTransform);
        
        // Add the shelf geometry as a child to the positioned transform group
        positionedShelfTG.addChild(shelfTG);
        
        return positionedShelfTG;
    }

    // Method to create a group of shelves; numShelves and spacing can be adjusted
    private static TransformGroup createShelves(int numShelves, float spacing, String textureFile) {
        TransformGroup shelvesTG = new TransformGroup();
        
        // Calculate initial offset so that shelves are centered
        float totalWidth = (numShelves - 1) * spacing;
        float startX = -totalWidth / 2;
        
        for (int i = 0; i < numShelves; i++) {
            // Calculate the x-position for the current shelf
            float xPos = startX + i * spacing;
            // Adjust y and z positions as needed (for example, y = shelf height above the floor)
            Vector3f shelfPos = new Vector3f(xPos, 2f, 0f);
            
            // Create the shelf transform group and add it to the parent group
            TransformGroup shelfTG = createShelf(textureFile, shelfPos);
            shelvesTG.addChild(shelfTG);
        }
        return shelvesTG;
    }

	
	/* a function to create the library */
	private static TransformGroup create_Library() {
	    TransformGroup libraryTG = new TransformGroup();
	    
	    //Defining the transformation groups and objects of the scene graph
	    characterTG = new TransformGroup();
        characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Appearance sphereAppearance = new Appearance();
        sphereAppearance.setMaterial(new Material());
        Sphere character = new Sphere(0.2f, sphereAppearance);
        
        characterTG.addChild(character);
        Transform3D Offset1 = new Transform3D();
        position.set(0.0f, 2f, 0.0f);                                                  
        Offset1.setTranslation(new Vector3f(0f, 2f, 0.0f));                              //Set the starting position of the character
        characterTG.setTransform(Offset1);                  
        
        lastSafePosition.set(position);
        
        // Attach the collision behavior to the character.
        // (Assuming the sphere's geometry is its first child.)
        Shape3D characterShape = (Shape3D) character.getChild(0);
        CollisionDetectCharacter cds = new CollisionDetectCharacter(characterShape);  //Collision Detection
        cds.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0), 100));
        characterTG.addChild(cds);
        
	    object3D[0] = new SquareShape("ImageEmrald.jpg",4f,0.01f,4f);                   // create "FloorObject"
	    object3D[1] = new SquareShape("MarbleTexture.jpg",4f,1.5f,0.05f);                   // create Front and Back wall dimensions
	    object3D[2] = new SquareShape("MarbleTexture.jpg",4f,1.5f,0.05f);                   // create Front and Back wall dimensions
	    object3D[3] = new WallObject("ImageEmrald.jpg");                   // create Left and right wall dimensions
	    
	    Transform3D zAxis = new Transform3D();
	    zAxis.rotY(Math.PI/2); //Rotate along the z axis
	    //object3D[4] = new WallObject("FloorTexture.jpg");
	    object3D[4] = new SquareShape("ImageEmrald.jpg",0.05f, 1.5f, 4f);                   // create Left and right wall dimensions
	    
	    // Front Wall translation group
	    TransformGroup frontWallTG = define_wall(object3D[1].position_Object(), new Vector3f(0f, 1.5f, 4f));
	    
	    // Back Wall translation group
	    TransformGroup backWallTG = define_wall(object3D[2].position_Object(), new Vector3f(0f, 1.5f, -4f));
	    
	    // Left Wall translation group
	    TransformGroup leftWallTG = define_wall(object3D[3].position_Object(), new Vector3f(-4f, 1.5f, 0));
	    leftWallTG.setTransform(zAxis);
	    // Left Wall translation group
	    TransformGroup rightWallTG = define_wall(object3D[4].position_Object(), new Vector3f(4f, 1.5f, 0));
	    
	    
        //object3D[5] = new ShelfObject("FloorTexture.jpg");
        
	    //Create shelf objects
        TransformGroup shelvesTG1 = createShelves(5, 1.5f, "FloorTexture.jpg");
        TransformGroup shelvesTG2 = createShelves(5, 1.5f, "FloorTexture.jpg");

        // Create a Transform3D for the z offset:
        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(0.0f, 0.0f, 5.0f));  // Change '2.0f' to your desired z offset

        // Apply the transformation to shelvesTG2:
        shelvesTG2.setTransform(Offset);
	    
        //Creating the scene graph
        object3D[0].add_Child(shelvesTG1);                             // add the shelves
        object3D[0].add_Child(shelvesTG2);
        libraryTG.addChild(object3D[0].position_Object());             // add floorTG to library TG
	    libraryTG.addChild(frontWallTG);                               // add frontWallTG to library TG
	    libraryTG.addChild(backWallTG);                                // add backWallTG to library TG
	    libraryTG.addChild(leftWallTG);                                // add leftWallTG to library TG
	    libraryTG.addChild(rightWallTG);                               // add rightWallTG to library TG
	    libraryTG.addChild(characterTG);
	    
	    return libraryTG;
	}


	/* a function to build the content branch, including the fan and other environmental settings */
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();	   // make 'sceneTG' continuously rotating
		sceneTG.addChild(create_Library());                    // add the fan to the rotating 'sceneTG'

		sceneBG.addChild(sceneTG);                         // keep the following stationary
		sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1));

		return sceneBG;
	}

	 public MainClass(BranchGroup sceneBG) {
	        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
	        Canvas3D canvas = new Canvas3D(config);
	        canvas.setFocusable(true);             // ensure canvas receives key events
	        canvas.addKeyListener(this);           // add listener to this program
	        add(canvas);
	        
	        SimpleUniverse su = new SimpleUniverse(canvas);
	        CommonsSK.define_Viewer(su, new Point3d(0f, 13f, 0f)); //Change the eye to new Point3d(0f, 6f, 0f) to get birds eye view
	        sceneBG.compile();
	        su.addBranchGraph(sceneBG);
	        setLayout(new BorderLayout());
	        add("Center", canvas);
	        frame.setSize(800, 800);
	        frame.setVisible(true);
	    }
	 
	 
	  //Collision Detection
	 public void revertToSafePosition() {
	        // Reset the character's position to the last safe position.
	        position.set(lastSafePosition);
	        updatePosition();
	        System.out.println("Reverted to safe position: " + lastSafePosition);
	    }
	    
	   
	    public void updateSafePosition() {
	        // Update the last safe position to the current position.
	        lastSafePosition.set(position);
	        System.out.println("Updated safe position: " + position);
	    }
	    
	    // Get the current position.
	    public Vector3f getPosition() {
	        return position;
	    }
	    
	    private static void updatePosition() {
	        Transform3D transform = new Transform3D();
	        transform.setTranslation(position);
	        characterTG.setTransform(transform);
	    }
	    
	    // Movement via key presses.
	    @Override
	    public void keyPressed(KeyEvent e) {
	        // If collision is active, ignore further key input.
	        if (colliding) {
	            System.out.println("Movement ignored while colliding.");
	            return;
	        }
	        
	        // Save the current (safe) position only if not colliding.
	        // (This represents the last non-colliding state.)
	        lastSafePosition.set(position);
	        
	        // Calculate new position.
	        Vector3f newPos = new Vector3f(position);
	        switch(e.getKeyCode()){
	            case KeyEvent.VK_UP: 
	                newPos.z -= MOVE_STEP; 
	                break;
	            case KeyEvent.VK_DOWN: 
	                newPos.z += MOVE_STEP; 
	                break;
	            case KeyEvent.VK_LEFT: 
	                newPos.x -= MOVE_STEP; 
	                break;
	            case KeyEvent.VK_RIGHT: 
	                newPos.x += MOVE_STEP; 
	                break;
	        }
	        position.set(newPos);
	        updatePosition();
	    }

	    
	    private static class CollisionDetectCharacter extends Behavior {
	        private Shape3D shape;
	        private WakeupOnCollisionEntry wEnter;
	        private WakeupOnCollisionExit wExit;
	        
	        public CollisionDetectCharacter(Shape3D s) {
	            shape = s;
	        }
	        
	        @Override
	        public void initialize() {
	            wEnter = new WakeupOnCollisionEntry(shape, WakeupOnCollisionEntry.USE_GEOMETRY);
	            wExit  = new WakeupOnCollisionExit(shape, WakeupOnCollisionExit.USE_GEOMETRY);
	            wakeupOn(new WakeupOr(new WakeupCriterion[] { wEnter, wExit }));
	        }
	        
	        @Override
	        public void processStimulus(Iterator<WakeupCriterion> criteria) {
	            boolean collisionEntry = false;
	            boolean collisionExit  = false;
	            
	            while (criteria.hasNext()) {
	                WakeupCriterion wc = criteria.next();
	                if (wc instanceof WakeupOnCollisionEntry)
	                    collisionEntry = true;
	                if (wc instanceof WakeupOnCollisionExit)
	                    collisionExit = true;
	            }
	            
	            if (collisionEntry) {
	                colliding = true;  // lock movement
	                // Revert to last safe position.
	                position.set(lastSafePosition);
	                updatePosition();
	                System.out.println("Collision detected: reverting to safe position: " + lastSafePosition);
	            }
	            if (collisionExit) {
	                colliding = false; // allow movement again
	                // Update lastSafePosition to the current position.
	                lastSafePosition.set(position);
	                System.out.println("Collision resolved: updating safe position to: " + position);
	            }
	            
	            wakeupOn(new WakeupOr(new WakeupCriterion[] { wEnter, wExit }));
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


	
}


