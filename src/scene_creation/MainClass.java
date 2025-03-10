package scene_creation;

/* Copyright material for students working on assignments */
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

import com.jogamp.newt.event.KeyListener;


public class MainClass extends JPanel implements KeyListener {
	
	private static final long serialVersionUID = 1L;
	private static JFrame frame;

	private static final int OBJ_NUM = 6; //+4 objects (Shaft, Motor, Blade, Guard)
	private static Objects[] object3D = new Objects[OBJ_NUM];  //Number of objects
	private static TransformGroup characterTG;
    private Vector3f position = new Vector3f();
    private final float MOVE_STEP = 0.2f;
    
    //Wall method
    private static TransformGroup define_wall(TransformGroup wall,  Vector3f vector) {
    	TransformGroup WallTG = new TransformGroup();
 	    Transform3D WallTrans = new Transform3D();
 	    
 	    WallTrans.setTranslation(vector);
 	    WallTG.setTransform(WallTrans);
 	    WallTG.addChild(wall);
 	    
 	    return WallTG;
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
        
	    object3D[0] = new SquareShape("ImageEmrald.jpg",4f,0.01f,4f);                   // create "FloorObject"
	    object3D[1] = new SquareShape("MarbleTexture.jpg",4f,1.5f,0.05f);                   // create Front and Back wall dimensions
	    object3D[2] = new SquareShape("MarbleTexture.jpg",4f,1.5f,0.05f);                   // create Front and Back wall dimensions
	    object3D[3] = new SquareShape("ImageEmrald.jpg",0.05f, 1.5f, 4f);                   // create Left and right wall dimensions
	    object3D[4] = new SquareShape("ImageEmrald.jpg",0.05f, 1.5f, 4f);                   // create Left and right wall dimensions
	    
	    // Front Wall translation group
	    TransformGroup frontWallTG = define_wall(object3D[1].position_Object(), new Vector3f(0f, 1.5f, 4f));
	    
	    // Back Wall translation group
	    TransformGroup backWallTG = define_wall(object3D[2].position_Object(), new Vector3f(0f, 1.5f, -4f));
	    
	    // Left Wall translation group
	    TransformGroup leftWallTG = define_wall(object3D[3].position_Object(), new Vector3f(-4f, 1.5f, 0));
	    
	    // Left Wall translation group
	    TransformGroup rightWallTG = define_wall(object3D[4].position_Object(), new Vector3f(4f, 1.5f, 0));
	    
	    
        object3D[5] = new SinglebookObject("FloorTexture.jpg");

	    
	    
        //Creating the scene graph
        object3D[0].add_Child(characterTG);
	    libraryTG.addChild(object3D[0].position_Object());             // add floorTG to library TG
	    libraryTG.addChild(frontWallTG);                               // add frontWallTG to library TG
	    libraryTG.addChild(backWallTG);                                // add backWallTG to library TG
	    libraryTG.addChild(leftWallTG);                                // add leftWallTG to library TG
	    libraryTG.addChild(rightWallTG);                               // add rightWallTG to library TG

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
	        canvas.setFocusable(true);  // ensure canvas receives key events
	        
	       
//	        canvas.addKeyListener(new KeyAdapter() {  // Add key listener for pause/resume functionality
//	            
//	            public void keyPressed(KeyEvent e) {
//	                char keyChar = e.getKeyChar();
//	                switch (keyChar) {
//	                    case 'z': 
//	                    case 'Z':
//	                        if (!powerOff) {  // Only allow toggling motor oscillation if fan is powered on
//	                            if (!motorOscillationPaused) {
//	                                alpha[1].pause();   // Pause the motor's oscillation
//	                                motorOscillationPaused = true;
//	                            } else {
//	                                alpha[1].resume();  // Resume the motor's oscillation
//	                                motorOscillationPaused = false;
//	                            }
//	                        }
//	                        break;
//	                    case 'x': 
//	                    case 'X':
//	                        if (!powerOff) {
//	                            alpha[0].pause();  // Pause blade rotation
//	                            alpha[1].pause();  // Pause motor oscillation
//	                            powerOff = true;
//	                            motorOscillationPaused = false;
//	                        } else {
//	                            alpha[0].resume(); // Resume blade rotation
//	                            alpha[1].resume(); // Resume motor oscillation
//	                            powerOff = false;
//	                        }
//	                        break;
//	                    default:
//	                        break;
//	                }
//	            }
//	        });
	        
	        SimpleUniverse su = new SimpleUniverse(canvas);
	        CommonsSK.define_Viewer(su, new Point3d(0f, 13f, 0f)); //Change the eye to new Point3d(0f, 6f, 0f) to get birds eye view
	        sceneBG.compile();
	        su.addBranchGraph(sceneBG);
	        setLayout(new BorderLayout());
	        add("Center", canvas);
	        frame.setSize(800, 800);
	        frame.setVisible(true);
	    }
	 
	  public void keyPressed(KeyEvent e) {
	        switch(e.getKeyCode()){
	            case KeyEvent.VK_UP: position.z -= MOVE_STEP; break;
	            case KeyEvent.VK_DOWN: position.z += MOVE_STEP; break;
	            case KeyEvent.VK_LEFT: position.x -= MOVE_STEP; break;
	            case KeyEvent.VK_RIGHT: position.x += MOVE_STEP; break;
	        }
	        updatePosition();
	    }

	    private void updatePosition() {
	        Transform3D transform = new Transform3D();
	        transform.setTranslation(position);
	        characterTG.setTransform(transform);
	    }

       

	public static void main(String[] args) {
		frame = new JFrame("Haunted Leddy");                   // NOTE: change XY to student's initials
		frame.getContentPane().add(new MainClass(create_Scene()));  // start the program
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	@Override
	public void keyPressed(com.jogamp.newt.event.KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyReleased(com.jogamp.newt.event.KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}


