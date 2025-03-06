import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.event.*;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;


public class HauntedLibraryScene extends JFrame implements KeyListener {
    private TransformGroup characterTG;
    private Vector3f position = new Vector3f();
    private final float MOVE_STEP = 0.2f;

    public HauntedLibraryScene() {
        setTitle("Haunted Library with Floor Texture");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        canvas.addKeyListener(this);
        add(canvas);

        SimpleUniverse universe = new SimpleUniverse(canvas);

        Transform3D viewTransform = new Transform3D();
        viewTransform.setTranslation(new Vector3f(0f, 1f, 6f));
        universe.getViewingPlatform().getViewPlatformTransform().setTransform(viewTransform);

        BranchGroup scene = createSceneGraph();
        scene.compile();
        universe.addBranchGraph(scene);

        setVisible(true);
    }

    private BranchGroup createSceneGraph() {
        BranchGroup root = new BranchGroup();

        // Floor setup with image texture
        TextureLoader loader = new TextureLoader("floor.jpg", null);
        ImageComponent2D floorImage = loader.getImage();
        Texture2D floorTexture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
                floorImage.getWidth(), floorImage.getHeight());
        floorTexture.setImage(0, floorImage);

        Appearance floorAppearance = new Appearance();
        floorAppearance.setTexture(floorTexture);

        Box floor = new Box(4f, 0.01f, 4f,
                Primitive.GENERATE_TEXTURE_COORDS | Primitive.GENERATE_NORMALS, floorAppearance);
        TransformGroup floorTG = new TransformGroup();
        floorTG.setTransform(new Transform3D());
        root.addChild(floorTG);
        floorTG.addChild(floor);

        // Character (sphere)
        characterTG = new TransformGroup();
        characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        Appearance sphereAppearance = new Appearance();
        sphereAppearance.setMaterial(new Material());
        Sphere character = new Sphere(0.2f, sphereAppearance);

        characterTG.addChild(character);
        root.addChild(characterTG);

        // Lighting
        BoundingSphere bounds = new BoundingSphere(new Point3d(), 1000);
        DirectionalLight dirLight = new DirectionalLight(new Color3f(1f,1f,1f), new Vector3f(-1,-1,-1));
        dirLight.setInfluencingBounds(bounds);
        root.addChild(dirLight);

        AmbientLight ambient = new AmbientLight(new Color3f(0.4f,0.4f,0.4f));
        ambient.setInfluencingBounds(bounds);
        root.addChild(ambient);

        return root;
    }

    @Override
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

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        new HauntedLibraryScene();
    }
}
