package scene_creation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.*;

// The Library class is responsible for creating the 3D scene of a library, including walls, shelves, books, doors, and other objects.
public class Library {

    // Constant defining the number of objects in the scene (e.g., walls, doors, shelves, etc.).
    private static final int OBJ_NUM = 18; // Increased to accommodate GhostObject

    // Array to store all 3D objects in the scene (e.g., walls, doors, shelves, etc.).
    private static Objects[] object3D = new Objects[OBJ_NUM];

    // TransformGroup for the player character, allowing its position to be updated dynamically.
    protected static TransformGroup characterTG;

    // TransformGroups for shifting book rows on shelves (used to position books at different heights).
    private static TransformGroup Shifted;
    private static TransformGroup Shifted2;

    // Current position of the character in the 3D scene.
    protected static Vector3f position = new Vector3f();

    // Last safe position of the character, used for collision detection or resetting.
    protected static Vector3f lastSafePosition = new Vector3f();

    // List to track all doors in the scene for interaction or collision purposes.
    protected static List<TransformGroup> doors = new ArrayList<>();

    // Method to create a wall at a specified position by applying a translation to the wall's TransformGroup.
    private static TransformGroup define_wall(TransformGroup wall, Vector3f vector) {
        // Create a new TransformGroup to hold the wall.
        TransformGroup WallTG = new TransformGroup();
        // Create a Transform3D to define the wall's position.
        Transform3D WallTrans = new Transform3D();

        // Set the translation of the wall to the specified vector.
        WallTrans.setTranslation(vector);
        // Apply the transformation to the TransformGroup.
        WallTG.setTransform(WallTrans);
        // Add the wall as a child to the TransformGroup.
        WallTG.addChild(wall);

        return WallTG;
    }

    // Method to create a row of books along the Z-axis with specified spacing.
    private static TransformGroup duplicateBooksZAxis(int numBooks, float spacing) {
        // Create a TransformGroup to hold the row of books.
        TransformGroup booksGroup = new TransformGroup();

        // Calculate the total spacing needed for the books and determine the starting Z position.
        float totalSpacing = (numBooks - 1) * spacing;
        float startZ = -totalSpacing / 2;

        // List of textures for the books (Red, Yellow, Blue).
        List<String> textures = new ArrayList<>();
        textures.add("RedImage.png");
        textures.add("YellowImage.png");
        textures.add("BlueImage.png");

        // Shuffle the textures to randomize the initial order of books.
        Collections.shuffle(textures);

        // Loop to create the specified number of books.
        for (int i = 0; i < numBooks; i++) {
            // Cycle through textures using modulo to assign a texture to each book.
            String texture = textures.get(i % textures.size());
            // Create a GroupbooksObject for the book with the selected texture.
            GroupbooksObject books = new GroupbooksObject(texture, "Groupbooks1");
            // Create a TransformGroup for the individual book.
            TransformGroup bookTG = new TransformGroup();

            // Set capabilities to allow reading/writing transformations and enable picking.
            bookTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
            bookTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            bookTG.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

            // Set user data to identify this TransformGroup as a book.
            bookTG.setUserData("book");

            // Create a Transform3D to position the book along the Z-axis.
            Transform3D translation = new Transform3D();
            Vector3f offset = new Vector3f(0f, 0f, startZ + i * spacing);
            translation.setTranslation(offset);
            // Apply the transformation to the book's TransformGroup.
            bookTG.setTransform(translation);

            // Add the book object to its TransformGroup.
            bookTG.addChild(books.position_Object());
            // Add the book's TransformGroup to the row (booksGroup).
            booksGroup.addChild(bookTG);
        }

        return booksGroup;
    }

    // Method to create a single shelf with multiple rows of books at a specified position.
    private static TransformGroup createShelf(String textureFile, Vector3f translation, int shelfNumber) {
        // Create a ShelfObject with the specified texture.
        ShelfObject shelf = new ShelfObject(textureFile);
        // Get the TransformGroup for the shelf.
        TransformGroup ShelfTG = shelf.position_Object();

        // Create five rows of books for the shelf.
        TransformGroup booksRowTG1 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG2 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG3 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG4 = duplicateBooksZAxis(3, 0.35f);
        TransformGroup booksRowTG5 = duplicateBooksZAxis(3, 0.35f);

        // Create a Transform3D to shift rows vertically.
        Transform3D shift = new Transform3D();
        shift.setTranslation(new Vector3f(0, 0.39f, 0f));

        // Create TransformGroups for shifting rows and set picking capabilities.
        Shifted = new TransformGroup();
        Shifted.setCapability(Shape3D.ALLOW_PICKABLE_READ); // Note: This capability is incorrect for TransformGroup; should be TransformGroup.ENABLE_PICK_REPORTING.
        Shifted2 = new TransformGroup();
        Shifted2.setCapability(Shape3D.ALLOW_PICKABLE_READ); // Same issue as above.
        TransformGroup Shifted3 = new TransformGroup();
        TransformGroup Shifted4 = new TransformGroup();
        TransformGroup Shifted5 = new TransformGroup();

        // Apply the shift transformation to position the second row.
        Shifted.setTransform(shift);
        Shifted.addChild(booksRowTG2);

        // Adjust the shift for the third row.
        shift.setTranslation(new Vector3f(0, 0.79f, 0f));
        Shifted2.setTransform(shift);
        Shifted2.addChild(booksRowTG3);

        // Adjust the shift for the fourth row.
        shift.setTranslation(new Vector3f(0, -0.39f, 0f));
        Shifted3.setTransform(shift);
        Shifted3.addChild(booksRowTG4);

        // Adjust the shift for the fifth row.
        shift.setTranslation(new Vector3f(0, -0.79f, 0f));
        Shifted4.setTransform(shift);
        Shifted4.addChild(booksRowTG5);

        // Position a light on the shelf.
        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(0.4f, 1.1f, 0.3f));
        TransformGroup Lights = new LightObject("LightTexture.jpg").position_Object();
        Lights.setTransform(Offset);

        // Position a second light on the shelf.
        Transform3D offset2 = new Transform3D();
        offset2.setTranslation(new Vector3f(5f, 1.1f, 0.3f));
        TransformGroup light2TG = new LightObject("LightTexture.jpg").position_Object();
        light2TG.setTransform(offset2);

        // Add all components to the shelf's TransformGroup.
        ShelfTG.addChild(light2TG);
        ShelfTG.addChild(booksRowTG1);
        ShelfTG.addChild(Shifted);
        ShelfTG.addChild(Shifted2);
        ShelfTG.addChild(Shifted3);
        ShelfTG.addChild(Shifted4);
        ShelfTG.addChild(Lights);

        // Create a TransformGroup to position and rotate the shelf in the scene.
        TransformGroup positionedShelfTG = new TransformGroup();
        Transform3D transform = new Transform3D();
        transform.rotY(Math.PI / 2); // Rotate the shelf 90 degrees around the Y-axis.
        transform.setTranslation(translation);
        positionedShelfTG.setTransform(transform);
        positionedShelfTG.addChild(ShelfTG);

        // Set user data to identify the shelf (e.g., "shelf_1").
        positionedShelfTG.setUserData("shelf_" + shelfNumber);

        return positionedShelfTG;
    }

    // Method to create multiple shelves with specified spacing.
    private static TransformGroup createShelves(int numShelves, float spacing, String textureFile, int startNumber) {
        // Create a TransformGroup to hold all shelves.
        TransformGroup shelvesTG = new TransformGroup();

        // Calculate the total length and starting Z position for the shelves.
        float totalLength = (numShelves - 1) * spacing;
        float startZ = -totalLength / 2;

        // Loop to create the specified number of shelves.
        for (int i = 0; i < numShelves; i++) {
            float zPos = startZ + i * spacing;
            Vector3f shelfPos = new Vector3f(0f, 2f, zPos);
            TransformGroup shelfTG = createShelf(textureFile, shelfPos, startNumber + i);
            shelvesTG.addChild(shelfTG);
        }
        return shelvesTG;
    }

    // Method to create the entire library scene.
    protected static TransformGroup create_Library() {
        // Create the root TransformGroup for the library scene.
        TransformGroup libraryTG = new TransformGroup();
        System.out.println("Creating Library scene..."); // Debug log to indicate scene creation start.

        // Initialize the character's TransformGroup.
        characterTG = new TransformGroup();
        characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        // Create the appearance for the character (a sphere).
        Appearance sphereAppearance = new Appearance();
        Material sphereMaterial = new Material();
        sphereMaterial.setAmbientColor(new Color3f(0.3f, 0.3f, 0.3f));
        sphereMaterial.setDiffuseColor(new Color3f(0.7f, 0.7f, 0.7f));
        sphereMaterial.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
        sphereMaterial.setShininess(32.0f);
        sphereMaterial.setLightingEnable(true);
        sphereAppearance.setMaterial(sphereMaterial);

        // Create a sphere to represent the character.
        Sphere character = new Sphere(0.2f, sphereAppearance);

        // Add the character sphere to its TransformGroup.
        characterTG.addChild(character);

        // Position the character at the starting point.
        Transform3D Offset1 = new Transform3D();
        position.set(0.0f, 2f, 0.0f);
        Offset1.setTranslation(new Vector3f(0f, 2f, 0.0f));
        characterTG.setTransform(Offset1);

        // Set the last safe position to the starting position.
        lastSafePosition.set(position);

        // Get the Shape3D of the character sphere for collision detection.
        Shape3D characterShape = (Shape3D) character.getChild(0);
        CollisionDetectCharacter cds = new CollisionDetectCharacter(characterShape);
        cds.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        characterTG.addChild(cds);

        // Initialize all objects in the scene.
        object3D[0] = new SquareShape("CarpetTexture.png", 6f, 0.01f, 8f, 10f); // Floor
        object3D[1] = new SquareShape("beige_image2.jpg", 6f, 4f, 0.05f, 10f); // Front wall
        object3D[2] = new SquareShape("Capture2.JPG", 6f, 4f, 0.05f, 1f); // Back wall
        object3D[3] = new WallObject("beige_image2.jpg"); // Left wall (with doors)
        object3D[4] = new SquareShape("beige_image2.jpg", 0.05f, 4f, 14f, 10f); // Right wall
        object3D[5] = new SquareShape("ImageFloor2.jpg", 6f, 0.01f, 15f, 5f); // Ceiling
        object3D[6] = new DoorObject("DoorTexture.jfif"); // Right door
        object3D[7] = new DoorObject("DoorTexture.jfif"); // Left door
        object3D[8] = new SquareShape("browntiledtexture.png", 6f, 0.01f, 7f, 5f); // Second floor
        object3D[9] = new HandleObject("ImageMetal2.jpg", "DoorHandleLeft"); // Right handle
        object3D[10] = new HandleObject("ImageMetal2.jpg", "DoorHandleRight"); // Left handle
        object3D[11] = new CubicleObject("chairtexture.png"); // Cubicle
        object3D[12] = new SquareShape("beige_image2.jpg", 0.05f, 4f, 9f, 10f); // Extra wall
        object3D[13] = new GhostObject("GhostTexture.png"); // Ghost object
        // object3D[14] = new PillarObject("PillerTexture.jpg"); // Commented out pillar object
        //object3D[15] = new FullShelfObject("Diffuse.png"); // Additional shelf object

        // Configure the second door (object3D[7]) with scaling, rotation, and translation.
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

        // Add the first door to the left wall and track it.
        TransformGroup door1TG = object3D[6].position_Object();
        door1TG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        door1TG.setUserData("door");
        object3D[3].add_Child(door1TG);
        doors.add(door1TG);

        // Add the second door to the left wall and track it.
        TransformGroup door2TG = object3D[7].position_Object();
        door2TG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        door2TG.setUserData("door");
        object3D[3].add_Child(door2TG);
        doors.add(door2TG);

        // Define the transformation for the left wall (rotated and translated).
        Transform3D yAxis = new Transform3D();
        yAxis.rotY(Math.PI / 2);
        yAxis.setTranslation(new Vector3f(0f, 0.15f, -1f));

        // Position all walls in the scene.
        TransformGroup frontWallTG = define_wall(object3D[1].position_Object(), new Vector3f(0f, 4f, 13f));
        TransformGroup backWallTG = define_wall(object3D[2].position_Object(), new Vector3f(0f, 4f, -8f));
        TransformGroup leftWallTG = define_wall(object3D[3].position_Object(), new Vector3f(0f, 2f, 0f));
        leftWallTG.setTransform(yAxis);
        TransformGroup rightWallTG = define_wall(object3D[4].position_Object(), new Vector3f(6f, 4f, 0));
        TransformGroup ceilingTG = define_wall(object3D[5].position_Object(), new Vector3f(0f, 4.2f, 0));
        TransformGroup WoodFloorTG = define_wall(object3D[8].position_Object(), new Vector3f(0f, 0.001f, 10.2f));

        // Create two sets of shelves.
        TransformGroup shelvesTG1 = createShelves(4, 2f, "BookshelfTexture.jpg", 1);
        TransformGroup shelvesTG2 = createShelves(4, 2f, "BookshelfTexture.jpg", 5);

        // Position the first set of shelves.
        Transform3D Offset = new Transform3D();
        Offset.setTranslation(new Vector3f(4.5f, 0.0f, 7f));
        shelvesTG2.setTransform(Offset);

        // Position the second set of shelves.
        Transform3D Offset2 = new Transform3D();
        Offset2.setTranslation(new Vector3f(0.5f, 0.0f, 7f));
        shelvesTG1.setTransform(Offset2);

        // Position the right handle on the first door.
        Transform3D Offset3 = new Transform3D();
        Offset3.setTranslation(new Vector3f(0.65f, 0.0f, 0.1f));
        TransformGroup handleRightTG = new TransformGroup();
        handleRightTG.setTransform(Offset3);
        handleRightTG.addChild(object3D[9].position_Object());

        // Position the left handle on the second door with a 180-degree rotation.
        Transform3D Offset4 = new Transform3D();
        Offset4.setTranslation(new Vector3f(-0.008f, -0.02f, -0.05f));
        Transform3D rotationLeft = new Transform3D();
        rotationLeft.rotY(Math.toRadians(180));
        Transform3D combinedTransform = new Transform3D();
        combinedTransform.mul(Offset4, rotationLeft);
        TransformGroup handleLeftTG = new TransformGroup();
        handleLeftTG.setTransform(combinedTransform);
        handleLeftTG.addChild(object3D[10].position_Object());

        // Position an extra wall in the scene.
        Transform3D Offset5 = new Transform3D();
        Offset5.setTranslation(new Vector3f(-4f, 0.0f, 10f));
        TransformGroup ExtraWallTG = object3D[12].position_Object();
        ExtraWallTG.setTransform(Offset5);

        // Position the cubicle in the scene.
        Transform3D Offset6 = new Transform3D();
        Offset6.setTranslation(new Vector3f(0f, 1f, 0f));
        TransformGroup CubicleTG = new TransformGroup();
        CubicleTG.setTransform(Offset6);
        CubicleTG.addChild(object3D[11].position_Object());

        // Position the ghost object near the door.
        object3D[13] = new GhostObject("GhostTexture.png");
        TransformGroup ghostTG = object3D[13].position_Object();
        Transform3D ghostTransform = new Transform3D();
        ghostTransform.setTranslation(new Vector3f(1f, -2f, 0f));
        ghostTG.setTransform(ghostTransform);

        // Add shelves and cubicle to the floor.
        object3D[0].add_Child(shelvesTG1);
        object3D[0].add_Child(shelvesTG2);
        object3D[0].add_Child(CubicleTG);

        // Add handles to the doors.
        object3D[6].add_Child(handleRightTG);
        object3D[7].add_Child(handleLeftTG);

        // Add all components to the library scene.
        libraryTG.addChild(object3D[0].position_Object());
        libraryTG.addChild(frontWallTG);
        libraryTG.addChild(backWallTG);
        libraryTG.addChild(leftWallTG);
        libraryTG.addChild(rightWallTG);
        libraryTG.addChild(ceilingTG);
        libraryTG.addChild(WoodFloorTG);
        libraryTG.addChild(ExtraWallTG);
        libraryTG.addChild(characterTG);
        libraryTG.addChild(ghostTG);

        System.out.println("Library scene graph constructed successfully");
        return libraryTG;
    }

    // Getter method to access the object3D array.
    public static Objects[] getObjects() {
        return object3D;
    }
}