package scene_creation;

import java.io.FileNotFoundException;
import java.util.*;
import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.*;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;






public abstract class Objects {
	protected BranchGroup objBG;                           // load external object to 'objBG'
	protected TransformGroup objTG;                        // use 'objTG' to position an object
	protected TransformGroup objRG;                        // use 'objRG' to rotate an object
	protected double scale;                                // use 'scale' to define scaling
	protected Vector3f post;                               // use 'post' to specify location
	protected Shape3D obj_shape;
	protected static String obj_name; //For FanBlades and Guard. Setting appearance for multiple parts of an object
	protected String texture_name; //Filename for texture string
	
	protected float x,y,z;    //Dimension for square shape
    
	public abstract TransformGroup position_Object();      // need to be defined in derived classes
	public abstract void add_Child(TransformGroup nextTG);
	
	/* a function to load and return object shape from the file named 'obj_name' */
	private Scene loadShape(String obj_name) {
		this.obj_name = obj_name; //Get name of object
		ObjectFile f = new ObjectFile(ObjectFile.RESIZE, (float) (60 * Math.PI / 180.0));
		Scene s = null;
		try {                                              // load object's definition file to 's'
			s = f.load(obj_name + ".obj");
		} catch (FileNotFoundException e) {
			System.err.println(e);
			System.exit(1);
		} catch (ParsingErrorException e) {
			System.err.println(e);
			System.exit(1);
		} catch (IncorrectFormatException e) {
			System.err.println(e);
			System.exit(1);
		}
		return s;                                          // return the object shape in 's'
	}
	
	/* function to set 'objTG' and attach object after loading the model from external file */
	protected void transform_Object(String obj_name) {
		Transform3D scaler = new Transform3D();
		scaler.setScale(scale);                            // set scale for the 4x4 matrix
		scaler.setTranslation(post);                       // set translations for the 4x4 matrix
		objTG = new TransformGroup(scaler);                // set the translation BG with the 4x4 matrix
		objBG = loadShape(obj_name).getSceneGroup();       // load external object to 'objBG'
		obj_shape = (Shape3D) objBG.getChild(0);           // get and cast the object to 'obj_shape'
		obj_shape.setName(obj_name);                       // use the name to identify the object 
	}
	
	protected Appearance app = new Appearance();
	private int shine = 32;                                // specify common values for object's appearance
	protected Color3f[] mtl_clr = {new Color3f(1.000000f, 1.000000f, 1.000000f),
			new Color3f(0.772500f, 0.654900f, 0.000000f),	
			new Color3f(0.175000f, 0.175000f, 0.175000f),
			new Color3f(0.000000f, 0.000000f, 0.000000f)};
	
	
	
	protected static Texture texture_App(String file_name) {
		TextureLoader loader = new TextureLoader(file_name, null);
		ImageComponent2D image = loader.getImage();        // get the image
		if (image == null)
			System.out.println("Cannot load file: " + file_name);

		Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL,
				Texture2D.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);                        // define the texture with the image

		return texture;
	}
	
	protected Appearance obj_Appearance() {		
		Material mtl = new Material();                     // define material's attributes
		mtl.setShininess(shine);
		mtl.setAmbientColor(mtl_clr[0]);                   // use them to define different materials
		mtl.setDiffuseColor(mtl_clr[1]);
		mtl.setSpecularColor(mtl_clr[2]);
		mtl.setEmissiveColor(mtl_clr[3]);                  // use it to enlighten a button
		mtl.setLightingEnable(true);

		app.setMaterial(mtl);                              // set appearance's material
		
		//Set appearance's texture for the object
		TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
				TexCoordGeneration.TEXTURE_COORDINATE_2);
		app.setTexCoordGeneration(tcg);
		app.setTexture(texture_App(texture_name)); //add texture
		
		TextureAttributes textureAttrib= new TextureAttributes();
		textureAttrib.setTextureMode(TextureAttributes.REPLACE);
		app.setTextureAttributes(textureAttrib);
		
		float scl = 4f;                                  // need to rearrange the four quarters
		Vector3d scale = new Vector3d(scl, scl, scl);
		Transform3D transMap = new Transform3D();
		transMap.setScale(scale);
		textureAttrib.setTextureTransform(transMap);
		return app;
	}

	// In your abstract class Objects, add the following method:
	public BoundingSphere getCollisionBounds() {
	    // If your object has dimensions x, y, z (for example, as used in SquareShape),
	    // a rough bounding sphere radius can be computed as half the diagonal:
	    double radius = Math.sqrt(Math.pow(x / 2.0, 2) + Math.pow(y / 2.0, 2) + Math.pow(z / 2.0, 2));
	    
	    // Use the 'post' vector as the center. Ensure post is initialized!
	    if (post == null) {
	        post = new Vector3f(0f, 0f, 0f);
	    }
	    Point3d center = new Point3d(post.x, post.y, post.z);
	    
	    return new BoundingSphere(center, radius);
	}

	
	
}


//Classes for each 3D objects (Floor, Ceiling, etc.)

class WallObject extends Objects {
	public WallObject(String texture_name) {                 //Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 4d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0.05f, 1.5f, -4f);                // Define the location of the wall object
		transform_Object("DoorOpeningWall");                     // set transformation to 'objTG' and load object file
		obj_Appearance();                                  // set appearance after converting object node to Shape3D
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);                             // attach "FanSwitch" to 'objTG'
		return objTG;                                      // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                            // attach the next transformGroup to 'objTG'
	}
}

class ShelfObject extends Objects {
	public ShelfObject(String texture_name) {                 //Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 1d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0f, 0f, -2.5f);                // location to connect "FanSwitch" with "FanStand"
		transform_Object("EmptySelf");                     // set transformation to 'objTG' and load object file
		obj_Appearance();                                  // set appearance after converting object node to Shape3D
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);                             // attach "FanSwitch" to 'objTG'
		return objTG;                                      // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                            // attach the next transformGroup to 'objTG'
	}
}

class GroupbooksObject extends Objects {
	public GroupbooksObject(String texture_name, String object_name) {                 //Filename for the texture and for the object, since there are two group books
		super();
		this.texture_name = texture_name;
		scale = 0.2d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0f, 0f, 0f);                   // location to connect "FanSwitch" with "FanStand"
		transform_Object(object_name);                     // set transformation to 'objTG' and load object file
		obj_Appearance();                                  // set appearance after converting object node to Shape3D
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);                             // attach "FanSwitch" to 'objTG'
		return objTG;                                      // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                            // attach the next transformGroup to 'objTG'
	}
}

class SinglebookObject extends Objects {
	public SinglebookObject(String texture_name) {                 //Filename for the object
		super();
		this.texture_name = texture_name;
		scale = 1d;                                      // actual scale is 0.3 = 1.0 x 0.3
		post = new Vector3f(0f, 1f, 0f);         // location to connect "FanSwitch" with "FanStand"
		transform_Object("Singlebook1");                     // set transformation to 'objTG' and load object file
		obj_Appearance();                                  // set appearance after converting object node to Shape3D
	}

	public TransformGroup position_Object() {
		objTG.addChild(objBG);                             // attach "FanSwitch" to 'objTG'
		return objTG;                                      // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                            // attach the next transformGroup to 'objTG'
	}
}

class SquareShape extends Objects {
	public SquareShape(String texture_name, float x, float y, float z) { //Define the texture file and the dimensions for the box
		this.x = x; this.y = y; this.z = z;                           //Initialize the values
		this.texture_name = texture_name;
		Transform3D translator = new Transform3D();
		translator.setTranslation(new Vector3d(0.0, -0.54, 0));
		objTG = new TransformGroup(translator);            // down half of the tower and base's heights

		objTG.addChild(create_Object());                   // attach the object to 'objTG'
	}
	
	public TransformGroup position_Object() {
		objTG.addChild(objBG);                             // attach "FanSwitch" to 'objTG'
		return objTG;                                      // use 'objTG' to attach "FanSwitch" to the previous TG
	}

	
	protected Node create_Object() {
		app = CommonsSK.set_Appearance(CommonsSK.White);   // set the appearance for the base
		app.setTexture(Objects.texture_App(texture_name));
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);       // show both sides
		app.setPolygonAttributes(pa);
		return new Box(x, y, z, Primitive.GENERATE_NORMALS, app);
	}
	
	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                            // attach the next transformGroup to 'objTG'
	}
}
