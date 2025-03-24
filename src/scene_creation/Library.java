package scene_creation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

public class Library {
	
	private static final int OBJ_NUM = 8; //+4 objects (Shaft, Motor, Blade, Guard)
	private static Objects[] object3D = new Objects[OBJ_NUM];  //Number of objects
	// Made these static to be referenced throughout the program.
	protected static TransformGroup characterTG;
	private static TransformGroup Shifted;
 	private static TransformGroup Shifted2;
 	// ADDED: Using static fields for position and safe position so other classes can access them.
 	protected static Vector3f position = new Vector3f();         //2Update position of the character
    protected static Vector3f lastSafePosition = new Vector3f();   // Last non-colliding position
	
	//Wall method
    private static TransformGroup define_wall(TransformGroup wall,  Vector3f vector) {
    	TransformGroup WallTG = new TransformGroup();
 	    Transform3D WallTrans = new Transform3D();
 	    
 	    WallTrans.setTranslation(vector);
 	    WallTG.setTransform(WallTrans);
 	    WallTG.addChild(wall);
 	    
 	    return WallTG;
    }
    
    private static TransformGroup duplicateBooksZAxis(int numBooks, float spacing) {
        TransformGroup booksGroup = new TransformGroup();
        
        // Calculate starting z position so that the books are centered
        float totalSpacing = (numBooks - 1) * spacing;
        float startZ = -totalSpacing / 2;
     // Define the list of textures
        List<String> textures = new ArrayList<>();
        textures.add("RedImage.png");
        textures.add("YellowImage.png");
        textures.add("BlueImage.png");
        
        // Shuffle the list to assign different colors randomly
        Collections.shuffle(textures);
        
        for (int i = 0; i < numBooks; i++) {
        	// Assign texture from the shuffled list
            String texture = textures.get(i);
            GroupbooksObject books = new GroupbooksObject(texture, "Groupbooks1");
            TransformGroup bookTG = new TransformGroup();
            
         // Set capabilities for picking and transform modification
            bookTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            bookTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            bookTG.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
            
            // Tag as a book
            bookTG.setUserData("book");
            // Create a translation for the current book along the z axis
            Transform3D translation = new Transform3D();
            Vector3f offset = new Vector3f(0f, 0f, startZ + i * spacing);
            translation.setTranslation(offset);
            bookTG.setTransform(translation);
           
            bookTG.addChild(books.position_Object());
            
            // Add this book to the overall group
            booksGroup.addChild(bookTG);
        }
        
        return booksGroup;
    }
    
    private static TransformGroup createShelf(String textureFile, Vector3f translation) {
        ShelfObject shelf = new ShelfObject(textureFile);  // Define shelf object
        TransformGroup ShelfTG = shelf.position_Object();
        
        
        TransformGroup booksRowTG1 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG2 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG3 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG4 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG5 = duplicateBooksZAxis(3, 0.35f);
        Transform3D shift = new Transform3D();
		shift.setTranslation(new Vector3f(0,0.39f,0f));
        Shifted = new TransformGroup();
        Shifted.setCapability(Shape3D.ALLOW_PICKABLE_READ);  //Allow the books to be clickable
        Shifted2 = new TransformGroup();
        Shifted2.setCapability(Shape3D.ALLOW_PICKABLE_READ);
        TransformGroup Shifted3 = new TransformGroup();
        TransformGroup Shifted4 = new TransformGroup();
        TransformGroup Shifted5 = new TransformGroup();
        Shifted.setTransform(shift);
        Shifted.addChild(booksRowTG2);
        
        shift.setTranslation(new Vector3f(0,0.79f,0f));
        Shifted2.setTransform(shift);
        Shifted2.addChild(booksRowTG3);
        
        shift.setTranslation(new Vector3f(0,-0.39f,0f));
        Shifted3.setTransform(shift);
        Shifted3.addChild(booksRowTG4);
        
        shift.setTranslation(new Vector3f(0,-0.79f,0f));
        Shifted4.setTransform(shift);
        Shifted4.addChild(booksRowTG5);
        
        shift.setTranslation(new Vector3f(0,0f,0f));
        Shifted5.setTransform(shift);
        Shifted5.addChild(new SinglebookObject("ImageEmrald.jpg").position_Object());
        
        ShelfTG.addChild(booksRowTG1);
        ShelfTG.addChild(Shifted);
        ShelfTG.addChild(Shifted2);
        ShelfTG.addChild(Shifted3);
        ShelfTG.addChild(Shifted4);
        ShelfTG.addChild(Shifted5);
        
        // Create a TransformGroup for positioning the shelf
        TransformGroup positionedShelfTG = new TransformGroup();
        Transform3D translationTransform = new Transform3D();
        translationTransform.setTranslation(translation);
        positionedShelfTG.setTransform(translationTransform);
        
        // Add the shelf (with the books) to the positioned transform group
        positionedShelfTG.addChild(ShelfTG);
        
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
	protected static TransformGroup create_Library() {
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
        
	    object3D[0] = new SquareShape("CarpetTexture.jpeg",4f,0.01f,4f);                   // create "FloorObject"
	    object3D[1] = new SquareShape("MarbleTexture.jpg",4f,4f,0.05f);                   // create Front and Back wall dimensions
	    object3D[2] = new SquareShape("MarbleTexture.jpg",4f,4f,0.05f);                   // create Front and Back wall dimensions
	    object3D[3] = new WallObject("MarbleTexture.jpg");                              // create dimensions for open wall
	    object3D[5] = new SquareShape("MarbleTexture.jpg",4f,0.01f,4f);             //Create ceiling, same dimensions as floor object
	    object3D[6] = new DoorObject("DoorTexture.jfif");                                //Create the door object
	    object3D[7] = new DoorObject("DoorTexture.jfif");                                //Create the door object
	 

	    // Define the transformations for object3D[7]
	    Transform3D scaleTransform = new Transform3D();
	    scaleTransform.setScale(object3D[7].scale); // Use the existing scale (0.4d)

	    Transform3D rotation = new Transform3D();
	    rotation.rotY(Math.PI); // 180-degree rotation around Y-axis

	    Transform3D translation = new Transform3D();
	    translation.setTranslation(new Vector3f(0.185f, -0.03f, 0f)); // Position next to object3D[6]

	    // Combine transformations: scale, then rotate, then translate
	    Transform3D combined = new Transform3D();
	    combined.mul(translation, rotation); // Apply rotation, then translation
	    combined.mul(scaleTransform);        // Apply scale first

	    // Apply the combined transform to object3D[7]'s TransformGroup
	    object3D[7].objTG.setTransform(combined);

	    // Add object3D[7] to the left wall, alongside object3D[6]
	    object3D[3].add_Child(object3D[7].position_Object());
	    Transform3D yAxis = new Transform3D();
	    yAxis.rotY(Math.PI/2); //Rotate along the y axis
	    yAxis.setTranslation(new Vector3f(0f, 0.15f, 0));
	    object3D[4] = new SquareShape("MarbleTexture.jpg",0.05f, 4f, 4f);                   // create Left and right wall dimensions
	    
	    // Front Wall translation group
	    TransformGroup frontWallTG = define_wall(object3D[1].position_Object(), new Vector3f(0f, 4f, 4f));
	    
	    // Back Wall translation group
	    TransformGroup backWallTG = define_wall(object3D[2].position_Object(), new Vector3f(0f, 4f, -4f));
	    
	    // Left Wall translation group
	    TransformGroup leftWallTG = define_wall(object3D[3].position_Object(), new Vector3f(-4f, 4f, 4));
	    leftWallTG.setTransform(yAxis);
	    // Right Wall translation group
	    TransformGroup rightWallTG = define_wall(object3D[4].position_Object(), new Vector3f(4f, 4f, 0));
	    
	    TransformGroup ceilingTG = define_wall(object3D[5].position_Object(), new Vector3f(0f, 4.15f, 0));       //Position of the ceiling
        
//	    //Create shelf objects
        TransformGroup shelvesTG1 = createShelves(5, 1.5f, "ImageFloor2.jpg");
        TransformGroup shelvesTG2 = createShelves(5, 1.5f, "ImageFloor2.jpg");

        // Create a Transform3D for the z offset:
        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(0.0f, 0.0f, 5.0f));  // Change '2.0f' to your desired z offset

        // Apply the transformation to shelvesTG2:
        shelvesTG2.setTransform(Offset);
//	    
        //Creating the scene graph
        object3D[3].add_Child(object3D[6].position_Object());          // add the door

        object3D[0].add_Child(shelvesTG1);                             // add the shelves
        object3D[0].add_Child(shelvesTG2);
        libraryTG.addChild(object3D[0].position_Object());             // add floorTG to library TG
	    libraryTG.addChild(frontWallTG);                               // add frontWallTG to library TG
	    libraryTG.addChild(backWallTG);                                // add backWallTG to library TG
	    libraryTG.addChild(leftWallTG);                                // add leftWallTG to library TG
	    libraryTG.addChild(rightWallTG);                               // add rightWallTG to library TG
	    libraryTG.addChild(ceilingTG);                                 // add ceilingTG to library TG
	    libraryTG.addChild(characterTG);
	    //libraryTG.addChild(object3D[6].position_Object());
	    return libraryTG;
	}
}
