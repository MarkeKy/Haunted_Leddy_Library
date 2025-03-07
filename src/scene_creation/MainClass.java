package scene_creation;

/* Copyright material for students working on assignments */
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;


public class MainClass extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;

	private static final int OBJ_NUM = 6; //+4 objects (Shaft, Motor, Blade, Guard)
	private static Objects[] object3D = new Objects[1];  //Number of objects
//	private static Alpha[] alpha = new Alpha[2]; //add another alpha
//	private boolean motorOscillationPaused = false;  // paused via 'z'
//	private boolean powerOff = false;                // paused via 'x'

    
//	/* a public function to build the base labeled with 'str' */
//	public static TransformGroup create_Base(String str) {
//		BaseShape baseShape = new BaseShape();
//		
//		Transform3D scaler = new Transform3D();
//		scaler.setScale(new Vector3d(4d, 2d, 4d));         // set scale for the 4x4 matrix
//		TransformGroup baseTG = new TransformGroup(scaler); 
//		baseTG.addChild(baseShape.position_Object());
//
//		ColorString clr_str = new ColorString(str, CommonsSK.Red, 0.06, 
//				new Point3f(-str.length() / 4f, -9.4f, 8.2f));
//		Transform3D r_axis = new Transform3D();            // default: rotate around Y-axis
//		r_axis.rotY(Math.PI);                              
//		TransformGroup objRG = new TransformGroup(r_axis); 
//		objRG.addChild(clr_str.position_Object());         // move string to baseShape's other side
//		baseTG.addChild(objRG);
//
//		return baseTG;
//	}
	
	/* a function to create the desk fan */
	private static TransformGroup create_Fan() {
	    TransformGroup fanTG = new TransformGroup();

	    object3D[0] = new FloorObject("Poliigon_WoodVeneerOak_7760_BaseColor");                   // create "FloorObject"
	    fanTG = object3D[0].position_Object();             // set fanTG to FanStand's transform group
	    
//	    object3D[1] = new SwitchObject();                  // create and attach "Switch" to "Stand"
//	    object3D[2] = new ShaftObject();                   // create and attach "Shaft"  to "Stand"
//	    object3D[3] = new MotorObject();                   // create and attach "Motor"  to "Shaft"
//	    object3D[4] = new BladeObject();                   // create "Blade" (fanblades)
//	    object3D[5] = new GuardObject();                   // create and attach "Guard"  to "Motor"
//	    
////	  
//		TransformGroup objRG1 = new TransformGroup();                // Create object rotation group for fan blades
//		alpha[0] = new Alpha(-1,500);                            // Create alpha for rotating blade
//		
//		TransformGroup objRG2 = new TransformGroup();
//		alpha[1] = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE, 0, 0, 5000, 2500, 200, 5000, 2500, 200);
//		objRG2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//		Transform3D yAxis = new Transform3D();
//		RotationInterpolator rot_beh = new RotationInterpolator(
//		    alpha[1], objRG2, yAxis, -(float) Math.PI / 2.0f, (float) Math.PI / 2.0f); //Starts at -90, ends at 90
//		rot_beh.setSchedulingBounds(new BoundingSphere(new Point3d(), 100));
//
//		
//		objRG1.addChild(object3D[4].rotation_object('x', objRG1, alpha[0])); //Creates rotation object for objRG, then attaches it to objRG
//		objRG1.addChild(object3D[4].position_Object());              // Add fan blades to the object rotation group
//		
//		objRG2.addChild(rot_beh);                                    // Add rotation behavior to objRG2
//		objRG2.addChild(object3D[3].position_Object());              // Add shaft object and everything attached to it to the rotation group
//
//	    object3D[3].add_Child(object3D[5].position_Object());        // attach Guard (5) to Motor (3)
//	    object3D[3].add_Child(objRG1);                               // attach Blade rotation group to Motor (3)
//	    object3D[2].add_Child(objRG2);        // attach Motor (3) to Shaft (2)
//	    object3D[0].add_Child(object3D[2].position_Object());                               // attach Shaft (2) rotation group to Stand (0)
//	    object3D[0].add_Child(object3D[1].position_Object());
//
//
//
//	    fanTG.addChild(create_Base("SK's Assignment 3"));  // attach "Base" to "FanStand"
	    return fanTG;
	}


	/* a function to build the content branch, including the fan and other environmental settings */
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();	   // make 'sceneTG' continuously rotating
		sceneTG.addChild(create_Fan());                    // add the fan to the rotating 'sceneTG'

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
	        CommonsSK.define_Viewer(su, new Point3d(0.25d, 0.25d, 10.0d));
	        sceneBG.compile();
	        su.addBranchGraph(sceneBG);
	        setLayout(new BorderLayout());
	        add("Center", canvas);
	        frame.setSize(800, 800);
	        frame.setVisible(true);
	    }
       

	public static void main(String[] args) {
		frame = new JFrame("SK's Assignment");                   // NOTE: change XY to student's initials
		frame.getContentPane().add(new MainClass(create_Scene()));  // start the program
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}


