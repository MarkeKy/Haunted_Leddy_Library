package scene_creation;

import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.Vector3f;

public class Movement {
	
	// Removed Library instance; using Library's static fields directly.
    // Get the current position.
    public Vector3f getPosition() {
        return Library.position;
    }
    
    protected static void updatePosition() {
        Transform3D transform = new Transform3D();
        transform.setTranslation(Library.position); // ADDED: Use Library static field
        Library.characterTG.setTransform(transform);  // ADDED: Use Library static field
    }
}
