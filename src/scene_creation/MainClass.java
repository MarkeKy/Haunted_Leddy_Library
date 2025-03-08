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

	
	/* a function to create the library */
	private static TransformGroup create_Library() {
	    TransformGroup libraryTG = new TransformGroup();
	    
	    characterTG = new TransformGroup();
        characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Appearance sphereAppearance = new Appearance();
        sphereAppearance.setMaterial(new Material());
        Sphere character = new Sphere(0.2f, sphereAppearance);
        
        characterTG.addChild(character);
        object3D[1] = new SinglebookObject("FloorTexture.jpg");
        
	    object3D[0] = new SquareShape("ImageEmrald.jpg");                   // create "FloorObject"
	    
	    object3D[0].add_Child(object3D[1].position_Object());
        object3D[0].add_Child(characterTG);
	    libraryTG = object3D[0].position_Object();             // set fanTG to FanStand's transform group
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
	        CommonsSK.define_Viewer(su, new Point3d(0f, 1f, 6f));
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


