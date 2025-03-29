////package scene_creation;
//
//import org.jogamp.java3d.*;
//import org.jogamp.java3d.utils.geometry.Sphere;
//import org.jogamp.java3d.utils.picking.PickTool;
//import org.jogamp.java3d.utils.universe.SimpleUniverse;
//import org.jogamp.vecmath.*;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class GameSetup {
//    private CustomCanvas3D canvas;
//    private TransformGroup viewTG;
//    private BranchGroup sceneBG;
//    private PickTool pickTool;
//    private float defaultHeight;
//    private Transform3D lastViewTransform;
//
//    public static class CustomCanvas3D extends Canvas3D {
//        public CustomCanvas3D(GraphicsConfiguration config) {
//            super(config);
//            setDoubleBufferEnable(true);
//        }
//
//        @Override
//        public void paint(Graphics g) {
//            super.paint(g);
//        }
//    }
//
//    public GameSetup(JPanel panel) {
//        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
//        canvas = new CustomCanvas3D(config);
//        canvas.setFocusable(true);
//
//        panel.setLayout(new BorderLayout());
//        panel.add(canvas, BorderLayout.CENTER);
//
//        SimpleUniverse universe = new SimpleUniverse(canvas);
//        viewTG = universe.getViewingPlatform().getViewPlatformTransform();
//
//        if (Library.characterTG == null) {
//            Library.characterTG = new TransformGroup();
//        }
//        Library.characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//
//        sceneBG = createScene();
//        pickTool = new PickTool(sceneBG);
//        pickTool.setMode(PickTool.GEOMETRY);
//
//        if (Library.characterTG.numChildren() > 0) {
//            Sphere characterSphere = (Sphere) Library.characterTG.getChild(0);
//            Shape3D characterShape = (Shape3D) characterSphere.getChild(0);
//            CollisionDetectCharacter collisionBehavior = new CollisionDetectCharacter(characterShape);
//            collisionBehavior.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
//            Library.characterTG.addChild(collisionBehavior);
//        }
//
//        Transform3D initialView = new Transform3D();
//        initialView.setTranslation(new Vector3f(0f, 2f, 20f));
//        viewTG.setTransform(initialView);
//        lastViewTransform = new Transform3D(initialView);
//
//        defaultHeight = Library.position.y;
//
//        sceneBG.compile();
//        universe.addBranchGraph(sceneBG);
//    }
//
//    private BranchGroup createScene() {
//        BranchGroup sceneBG = new BranchGroup();
//        TransformGroup sceneTG = new TransformGroup();
//        sceneTG.addChild(Library.create_Library());
//        sceneBG.addChild(sceneTG);
//        sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1));
//        return sceneBG;
//    }
//
//    public CustomCanvas3D getCanvas() {
//        return canvas;
//    }
//
//    public TransformGroup getViewTG() {
//        return viewTG;
//    }
//
//    public PickTool getPickTool() {
//        return pickTool;
//    }
//
//    public float getDefaultHeight() {
//        return defaultHeight;
//    }
//
//    public Transform3D getLastViewTransform() {
//        return lastViewTransform;
//    }
//
//    public void setLastViewTransform(Transform3D transform) {
//        this.lastViewTransform.set(transform);
//    }
//}