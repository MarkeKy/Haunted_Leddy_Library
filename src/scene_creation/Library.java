package scene_creation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.geometry.Text2D;
import org.jogamp.vecmath.*;

public class Library {

    private static final int OBJ_NUM = 15;
    private static Objects[] object3D = new Objects[OBJ_NUM];
    protected static TransformGroup characterTG;
    private static TransformGroup Shifted;
    private static TransformGroup Shifted2;
    protected static Vector3f position = new Vector3f();
    protected static Vector3f lastSafePosition = new Vector3f();

    private static TransformGroup define_wall(TransformGroup wall, Vector3f vector) {
        TransformGroup WallTG = new TransformGroup();
        Transform3D WallTrans = new Transform3D();

        WallTrans.setTranslation(vector);
        WallTG.setTransform(WallTrans);
        WallTG.addChild(wall);

        return WallTG;
    }

    private static TransformGroup duplicateBooksZAxis(int numBooks, float spacing) {
        TransformGroup booksGroup = new TransformGroup();

        float totalSpacing = (numBooks - 1) * spacing;
        float startZ = -totalSpacing / 2;
        List<String> textures = new ArrayList<>();
        textures.add("RedImage.png");
        textures.add("YellowImage.png");
        textures.add("BlueImage.png");

        Collections.shuffle(textures);

        for (int i = 0; i < numBooks; i++) {
            String texture = textures.get(i);
            GroupbooksObject books = new GroupbooksObject(texture, "Groupbooks1");
            TransformGroup bookTG = new TransformGroup();

            bookTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            bookTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            bookTG.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

            bookTG.setUserData("book");
            Transform3D translation = new Transform3D();
            Vector3f offset = new Vector3f(0f, 0f, startZ + i * spacing);
            translation.setTranslation(offset);
            bookTG.setTransform(translation);

            bookTG.addChild(books.position_Object());
            booksGroup.addChild(bookTG);
        }

        return booksGroup;
    }

    private static TransformGroup createShelf(String textureFile, Vector3f translation, int shelfNumber) {
        ShelfObject shelf = new ShelfObject(textureFile);
        TransformGroup ShelfTG = shelf.position_Object();

        // Add books to the shelf (unchanged)
        TransformGroup booksRowTG1 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG2 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG3 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG4 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG5 = duplicateBooksZAxis(3, 0.35f);
        Transform3D shift = new Transform3D();
        shift.setTranslation(new Vector3f(0, 0.39f, 0f));
        Shifted = new TransformGroup();
        Shifted.setCapability(Shape3D.ALLOW_PICKABLE_READ);
        Shifted2 = new TransformGroup();
        Shifted2.setCapability(Shape3D.ALLOW_PICKABLE_READ);
        TransformGroup Shifted3 = new TransformGroup();
        TransformGroup Shifted4 = new TransformGroup();
        TransformGroup Shifted5 = new TransformGroup();
        Shifted.setTransform(shift);
        Shifted.addChild(booksRowTG2);

        shift.setTranslation(new Vector3f(0, 0.79f, 0f));
        Shifted2.setTransform(shift);
        Shifted2.addChild(booksRowTG3);

        shift.setTranslation(new Vector3f(0, -0.39f, 0f));
        Shifted3.setTransform(shift);
        Shifted3.addChild(booksRowTG4);

        shift.setTranslation(new Vector3f(0, -0.79f, 0f));
        Shifted4.setTransform(shift);
        Shifted4.addChild(booksRowTG5);

//        //Light Object Offset
        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(0.4f, 1.1f, 0.3f)); // Changed from (0f, 0f, 5f) to (5f, 0f, 0f)
        
        TransformGroup Lights = new LightObject("LightTexture.jpg").position_Object();  //Create light objects
        Lights.setTransform(Offset);
        
     // Add second light
        Transform3D offset2 = new Transform3D();
        offset2.setTranslation(new Vector3f(5f, 1.1f, 0.3f));
        TransformGroup light2TG = new LightObject("LightTexture.jpg").position_Object();
        light2TG.setTransform(offset2);
        ShelfTG.addChild(light2TG);
        ShelfTG.addChild(booksRowTG1);
        ShelfTG.addChild(Shifted);
        ShelfTG.addChild(Shifted2);
        ShelfTG.addChild(Shifted3);
        ShelfTG.addChild(Shifted4);
        ShelfTG.addChild(Lights);   //Add lights to shelves

        // Add shelf number label (unchanged)
//        Text2D shelfLabel = new Text2D("Shelf " + shelfNumber, new Color3f(1f, 1f, 1f), "Serif", 12, Font.PLAIN);
//        TransformGroup labelTG = new TransformGroup();
//        Transform3D labelTrans = new Transform3D();
//        labelTrans.setTranslation(new Vector3f(1.0f, 1.0f, 1f)); // Above shelf
//        labelTG.setTransform(labelTrans);
//        labelTG.addChild(shelfLabel);
//        ShelfTG.addChild(labelTG);

        // Apply rotation and translation
        TransformGroup positionedShelfTG = new TransformGroup();
        Transform3D transform = new Transform3D();
        transform.rotY(Math.PI / 2); // Rotate 90 degrees around y-axis
        transform.setTranslation(translation); // Set the position (e.g., 0f, 2f, zPos)
        positionedShelfTG.setTransform(transform);
        positionedShelfTG.addChild(ShelfTG);

        // Assign shelf number as user data
        positionedShelfTG.setUserData("shelf_" + shelfNumber);

        return positionedShelfTG;
    }
    
    //Changed position of all shelves by swapping the z value with x value, to go from -- to |
    private static TransformGroup createShelves(int numShelves, float spacing, String textureFile, int startNumber) {
        TransformGroup shelvesTG = new TransformGroup();
        float totalLength = (numShelves - 1) * spacing; // Total span along z-axis
        float startZ = -totalLength / 2; // Center the shelves along z

        for (int i = 0; i < numShelves; i++) {
            float zPos = startZ + i * spacing; // Calculate z-position
            Vector3f shelfPos = new Vector3f(0f, 2f, zPos); // x=0, vary z
            TransformGroup shelfTG = createShelf(textureFile, shelfPos, startNumber + i);
            shelvesTG.addChild(shelfTG);
        }
        return shelvesTG;
    }

    protected static TransformGroup create_Library() {
        TransformGroup libraryTG = new TransformGroup();

        characterTG = new TransformGroup();
        characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Appearance sphereAppearance = new Appearance();
        sphereAppearance.setMaterial(new Material());
        Sphere character = new Sphere(0.2f, sphereAppearance);

        characterTG.addChild(character);
        Transform3D Offset1 = new Transform3D();
        position.set(0.0f, 2f, 0.0f);
        Offset1.setTranslation(new Vector3f(0f, 2f, 0.0f));
        characterTG.setTransform(Offset1);

        lastSafePosition.set(position);

        Shape3D characterShape = (Shape3D) character.getChild(0);
        CollisionDetectCharacter cds = new CollisionDetectCharacter(characterShape);
        cds.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        characterTG.addChild(cds);

        object3D[0] = new SquareShape("CarpetTexture.png", 6f, 0.01f, 8f,10f); // Create "FloorObject"
        object3D[1] = new SquareShape("beige_image2.jpg", 6f, 4f, 0.05f,10f); // Create Front and Back wall dimensions
        object3D[2] = new SquareShape("Capture2.JPG", 6f, 4f, 0.05f,1f); // Create Front and Back wall dimensions
        object3D[3] = new WallObject("beige_image2.jpg"); // Create dimensions for open wall
        object3D[4] = new SquareShape("beige_image2.jpg", 0.05f, 4f, 14f,10f); // Create Left and right wall dimensions
        object3D[5] = new SquareShape("ImageFloor2.jpg", 6f, 0.01f, 15f,5f); // Create ceiling, same dimensions as floor
        object3D[6] = new DoorObject("DoorTexture.jfif"); // Create the first door object
        object3D[7] = new DoorObject("DoorTexture.jfif"); // Create the second door object
        object3D[8] = new SquareShape("browntiledtexture.png", 6f, 0.01f, 7f,5f);     //Second floor
        object3D[9] = new HandleObject("ImageMetal2.jpg", "DoorHandleRight" );    //Handle Object (Right)
        object3D[10] = new HandleObject("ImageMetal2.jpg", "DoorHandleLeft");    //Handle Object (Left)

        Transform3D scaleTransform = new Transform3D();
        scaleTransform.setScale(object3D[7].scale);

        Transform3D rotation = new Transform3D();
        rotation.rotY(Math.PI);

        Transform3D translation = new Transform3D();
        translation.setTranslation(new Vector3f(0.185f, -0.03f, 0f));

        Transform3D combined = new Transform3D();
        combined.mul(translation, rotation);
        combined.mul(scaleTransform);

        object3D[7].objTG.setTransform(combined);
        object3D[3].add_Child(object3D[7].position_Object());
        Transform3D yAxis = new Transform3D();
        yAxis.rotY(Math.PI / 2);
        yAxis.setTranslation(new Vector3f(0f, 0.15f, -1f));   //position of wall

        TransformGroup frontWallTG = define_wall(object3D[1].position_Object(), new Vector3f(0f, 4f, 13f)); //Where the bookshelfs are
        TransformGroup backWallTG = define_wall(object3D[2].position_Object(), new Vector3f(0f, 4f, -8f));
        TransformGroup leftWallTG = define_wall(object3D[3].position_Object(), new Vector3f(-4f, 4f, 0f));
        leftWallTG.setTransform(yAxis);
        TransformGroup rightWallTG = define_wall(object3D[4].position_Object(), new Vector3f(6f, 4f, 0));
        TransformGroup ceilingTG = define_wall(object3D[5].position_Object(), new Vector3f(0f, 4.15f, 0));
        
        TransformGroup WoodFloorTG = define_wall(object3D[8].position_Object(), new Vector3f(0f, 0.001f, 10.2f)); //Wood floor for the shelves
        
        // Numbered shelves: 1-5 and 6-10
        TransformGroup shelvesTG1 = createShelves(5, 2f, "BookshelfTexture.jpg", 1);
        TransformGroup shelvesTG2 = createShelves(5, 2f, "BookshelfTexture.jpg", 6);
        
        //Offsets for shelves

        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(4.5f, 0.0f, 7f)); // Changed from (0f, 0f, 5f) to (5f, 0f, 0f)
        shelvesTG2.setTransform(Offset);
        
        Transform3D Offset2 = new Transform3D();             //To move shelves to wood floor
        Offset2.setTranslation(new Vector3f(0.5f, 0.0f, 7f));
        shelvesTG1.setTransform(Offset2);   //Translate the position
        
        //Scene Graph
        
        // Offsets for door handles 
        // RIGHT
        Transform3D Offset3 = new Transform3D();             // To position the door handles properly
        Offset3.setTranslation(new Vector3f(1f, 0.0f, 0.1f));
        
        // LEFT
        Transform3D Offset4 = new Transform3D();             // To position the door handles properly
        Offset4.setTranslation(new Vector3f(1.7f, 0.0f, 0.15f));
        
        // Get handle TransformGroups once and reuse them
        TransformGroup handleRightTG = new TransformGroup();
        handleRightTG.setTransform(Offset3);
        handleRightTG.addChild(object3D[9].position_Object());  // Get RIGHT handle TG once
        
        TransformGroup handleLeftTG = new TransformGroup();; // Get LEFT handle TG once
        handleRightTG.addChild(object3D[10].position_Object());
        handleLeftTG.setTransform(Offset4);                           // Set offset for LEFT door handle

        object3D[3].add_Child(object3D[6].position_Object());    // Add one of the doors to the wall object
        
        //Pillars
        
        // LEFT Wall offset
        Transform3D Offset5 = new Transform3D();             // Correction to position
        Offset5.setTranslation(new Vector3f(-4f, 0.0f, 10f));
        object3D[12] = new SquareShape("beige_image2.jpg", 0.05f, 4f, 9f,10f); // Create Left and right wall dimensions
        
        TransformGroup ExtraWallTG = object3D[12].position_Object();
        ExtraWallTG.setTransform(Offset5);
        
        //Cubicle offset
        Transform3D Offset6 = new Transform3D();             // Correction to position
        Offset6.setTranslation(new Vector3f(0f, 1f, 0f));
        
        object3D[11] = new CubicleObject("chairtexture.png");    //Cubicle object
        TransformGroup CubicleTG = object3D[11].position_Object();
        CubicleTG.setTransform(Offset6);                        //Set the offset for the transform group of the cubicle
        

        
        // Attaching shelves to floor
        object3D[0].add_Child(shelvesTG1);
        object3D[0].add_Child(shelvesTG2);
        object3D[0].add_Child(CubicleTG);
        
        // Adding door handles to DoorObject using the single TransformGroups
//        object3D[6].add_Child(handleRightTG);                    // Add RIGHT handle to door
//        object3D[6].add_Child(handleLeftTG);                     // Add LEFT handle to door

        
        libraryTG.addChild(object3D[0].position_Object());
        libraryTG.addChild(frontWallTG);
        libraryTG.addChild(backWallTG);
        libraryTG.addChild(leftWallTG);
        libraryTG.addChild(rightWallTG);
        libraryTG.addChild(ceilingTG);
        libraryTG.addChild(WoodFloorTG);           //Added additional floor to the game
        libraryTG.addChild(ExtraWallTG);           //Extra wall for the gap
        libraryTG.addChild(characterTG);

        return libraryTG;
    }
}