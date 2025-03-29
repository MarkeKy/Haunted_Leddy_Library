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

    private static final int OBJ_NUM = 12;
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

        shift.setTranslation(new Vector3f(0, 0f, 0f));
        Shifted5.setTransform(shift);
        Shifted5.addChild(new SinglebookObject("ImageEmrald.jpg").position_Object());

        ShelfTG.addChild(booksRowTG1);
        ShelfTG.addChild(Shifted);
        ShelfTG.addChild(Shifted2);
        ShelfTG.addChild(Shifted3);
        ShelfTG.addChild(Shifted4);
        ShelfTG.addChild(Shifted5);

        // Add shelf number label (unchanged)
        Text2D shelfLabel = new Text2D("Shelf " + shelfNumber, new Color3f(1f, 1f, 1f), "Serif", 12, Font.PLAIN);
        TransformGroup labelTG = new TransformGroup();
        Transform3D labelTrans = new Transform3D();
        labelTrans.setTranslation(new Vector3f(1.0f, 1.0f, 1f)); // Above shelf
        labelTG.setTransform(labelTrans);
        labelTG.addChild(shelfLabel);
        ShelfTG.addChild(labelTG);

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

        object3D[0] = new SquareShape("CarpetTexture.jpeg", 4f, 0.01f, 4f); // Create "FloorObject"
        object3D[1] = new SquareShape("MarbleTexture.jpg", 4f, 4f, 0.05f); // Create Front and Back wall dimensions
        object3D[2] = new SquareShape("MarbleTexture.jpg", 4f, 4f, 0.05f); // Create Front and Back wall dimensions
        object3D[3] = new WallObject("MarbleTexture.jpg"); // Create dimensions for open wall
        object3D[4] = new SquareShape("MarbleTexture.jpg", 0.05f, 4f, 8f); // Create Left and right wall dimensions
        object3D[5] = new SquareShape("MarbleTexture.jpg", 4f, 0.01f, 4f); // Create ceiling, same dimensions as floor
        object3D[6] = new DoorObject("DoorTexture.jfif"); // Create the first door object
        object3D[7] = new DoorObject("DoorTexture.jfif"); // Create the second door object
        object3D[8] = new SquareShape("leddyfloor.jpeg", 4f, 0.01f, 4f);
        object3D[9] = new HandleObject("ImageMetal2.jpg");    //Handle Object
        object3D[10] = new HandleObject("ImageMetal2.jpg");    //Handle Object
        
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
        yAxis.setTranslation(new Vector3f(0f, 0.15f, 0));
        object3D[4] = new SquareShape("MarbleTexture.jpg", 0.05f, 4f, 4f);

        TransformGroup frontWallTG = define_wall(object3D[1].position_Object(), new Vector3f(0f, 4f, 9f)); //Where the bookshelfs are
        TransformGroup backWallTG = define_wall(object3D[2].position_Object(), new Vector3f(0f, 4f, -4f));
        TransformGroup leftWallTG = define_wall(object3D[3].position_Object(), new Vector3f(-4f, 4f, 4));
        leftWallTG.setTransform(yAxis);
        TransformGroup rightWallTG = define_wall(object3D[4].position_Object(), new Vector3f(4f, 4f, 0));
        TransformGroup ceilingTG = define_wall(object3D[5].position_Object(), new Vector3f(0f, 4.15f, 0));
        
        TransformGroup WoodFloorTG = define_wall(object3D[8].position_Object(), new Vector3f(0f, 0f, 5f)); //Wood floor for the shelves

        // Numbered shelves: 1-5 and 6-10
        TransformGroup shelvesTG1 = createShelves(5, 1.5f, "ImageFloor2.jpg", 1);
        TransformGroup shelvesTG2 = createShelves(5, 1.5f, "ImageFloor2.jpg", 6);
        

        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(5.0f, 0.0f, 5f)); // Changed from (0f, 0f, 5f) to (5f, 0f, 0f)
        shelvesTG2.setTransform(Offset);
        
        Transform3D Offset2 = new Transform3D();             //To move shelves to wood floor
        Offset2.setTranslation(new Vector3f(0f, 0.0f, 5f));
        shelvesTG1.setTransform(Offset2);   //Translate the position
        
        //Scene Graph
        
        //Adding Shelves
        object3D[3].add_Child(object3D[6].position_Object());
        object3D[0].add_Child(shelvesTG1);
        object3D[0].add_Child(shelvesTG2);
        
        //Adding door to object
        object3D[6].add_Child(object3D[9].position_Object());  //Add Handles to door
        object3D[7].add_Child(object3D[10].position_Object());  //Add Handles to door

        
        libraryTG.addChild(object3D[0].position_Object());
        libraryTG.addChild(frontWallTG);
        libraryTG.addChild(backWallTG);
        libraryTG.addChild(leftWallTG);
        libraryTG.addChild(rightWallTG);
        libraryTG.addChild(ceilingTG);
        libraryTG.addChild(WoodFloorTG);           //Added additional floor to the game
        libraryTG.addChild(characterTG);

        return libraryTG;
    }
}