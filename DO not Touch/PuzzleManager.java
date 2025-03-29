//package scene_creation;
//
//import java.awt.*;
//import java.awt.event.*;
//import javax.swing.*;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.Clip;
//import javax.sound.sampled.LineUnavailableException;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.jogamp.java3d.*;
//import org.jogamp.java3d.utils.geometry.Sphere;
//import org.jogamp.java3d.utils.picking.PickResult;
//import org.jogamp.java3d.utils.picking.PickTool;
//import org.jogamp.java3d.utils.universe.SimpleUniverse;
//import org.jogamp.vecmath.*;
//
//public class MainClass extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
//    private static final long serialVersionUID = 1L;
//    private static JFrame frame;
//    private final float MOVE_STEP = 0.15f;
//    private final float JUMP_HEIGHT = 1.0f;
//    private final float CROUCH_HEIGHT = 1.0f;
//    private float yaw = 0.0f;
//    private float pitch = 0.0f;
//    private int lastMouseX = -1, lastMouseY = -1;
//    private boolean firstMouse = true;
//    private float rotationSensitivity = 0.005f;
//    private TransformGroup viewTG;
//    private CustomCanvas3D canvas;
//    private BranchGroup sceneBG;
//    private PickTool pickTool;
//    private static BookGame bookgame;
//    private static Map<String, Boolean> bookshelfUsage = new HashMap<>();
//    private boolean bookGameActive = false;
//    private boolean isCrouching = false;
//    private boolean isJumping = false;
//    private float defaultHeight;
//    private Transform3D lastViewTransform = new Transform3D();
//    private static int points = 0;
//    private JLabel pointsLabel;
//    private JLabel timerLabel;
//    private Timer gameTimer;
//    private int timeRemaining = 300; // 5 minutes in seconds
//    private boolean gameOver = false;
//
//    private static class CustomCanvas3D extends Canvas3D {
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
//    public static BranchGroup create_Scene() {
//        BranchGroup sceneBG = new BranchGroup();
//        TransformGroup sceneTG = new TransformGroup();
//        sceneTG.addChild(Library.create_Library());
//        sceneBG.addChild(sceneTG);
//        sceneBG.addChild(CommonsSK.add_Lights(CommonsSK.White, 1));
//        return sceneBG;
//    }
//
//    public MainClass() {
//        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
//        canvas = new CustomCanvas3D(config);
//        canvas.setFocusable(true);
//        canvas.addKeyListener(this);
//        canvas.addMouseListener(this);
//        canvas.addMouseMotionListener(this);
//
//        setLayout(new BorderLayout());
//        add(canvas, BorderLayout.CENTER);
//
//        // Top panel for timer (left) and points (right)
//        JPanel topPanel = new JPanel();
//        topPanel.setOpaque(true);
//        topPanel.setBackground(Color.WHITE);
//        topPanel.setLayout(new BorderLayout()); // Use BorderLayout for left/right positioning
//
//        // Timer label (left)
//        timerLabel = new JLabel("Time: 05:00");
//        timerLabel.setForeground(Color.BLACK);
//        timerLabel.setFont(new Font("Serif", Font.BOLD, 16));
//        timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Add padding on the left
//        topPanel.add(timerLabel, BorderLayout.WEST);
//
//        // Points label (right)
//        pointsLabel = new JLabel("Puzzles Solved: " + points);
//        pointsLabel.setForeground(Color.BLACK);
//        pointsLabel.setFont(new Font("Serif", Font.BOLD, 16));
//        pointsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); // Add padding on the right
//        topPanel.add(pointsLabel, BorderLayout.EAST);
//
//        add(topPanel, BorderLayout.NORTH);
//
//        SimpleUniverse universe = new SimpleUniverse(canvas);
//        viewTG = universe.getViewingPlatform().getViewPlatformTransform();
//
//        if (Library.characterTG == null) {
//            Library.characterTG = new TransformGroup();
//        }
//        Library.characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//
//        sceneBG = create_Scene();
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
//        bookgame = new BookGame();
//
//        Transform3D initialView = new Transform3D();
//        initialView.setTranslation(new Vector3f(0f, 2f, 20f));
//        viewTG.setTransform(initialView);
//        lastViewTransform.set(initialView);
//
//        defaultHeight = Library.position.y;
//
//        sceneBG.compile();
//        universe.addBranchGraph(sceneBG);
//
//        // Start the timer
//        startTimer();
//    }
//
//    private void startTimer() {
//        gameTimer = new Timer(1000, e -> {
//            if (gameOver) return;
//            timeRemaining--;
//            updateTimerDisplay();
//            if (timeRemaining <= 0) {
//                gameTimer.stop();
//                handleGameOver();
//            }
//        });
//        gameTimer.start();
//    }
//
//    private void updateTimerDisplay() {
//        int minutes = timeRemaining / 60;
//        int seconds = timeRemaining % 60;
//        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
//    }
//
//    private void handleGameOver() {
//        gameOver = true;
//        playYouLoseSound();
//        int option = JOptionPane.showConfirmDialog(
//                this,
//                "Game Over! Time's up.\nDo you want to restart the game?",
//                "Game Over",
//                JOptionPane.YES_NO_OPTION,
//                JOptionPane.WARNING_MESSAGE
//        );
//
//        if (option == JOptionPane.YES_OPTION) {
//            restartGame();
//        } else {
//            System.exit(0);
//        }
//    }
//
//    private void playYouLoseSound() {
//        try {
//            URL soundURL = MainClass.class.getResource("youlose.wav");
//            if (soundURL == null) {
//                System.err.println("youlose.wav not found!");
//                return;
//            }
//            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
//            Clip clip = AudioSystem.getClip();
//            clip.open(audioIn);
//            clip.start();
//        } catch (Exception ex) {
//            System.err.println("Error playing 'You Lose' sound: " + ex.getMessage());
//        }
//    }
//
//    private void restartGame() {
//        // Reset game state
//        points = 0;
//        bookshelfUsage.clear();
//        timeRemaining = 300;
//        gameOver = false;
//
//        // Reset the view and position
//        Transform3D initialView = new Transform3D();
//        initialView.setTranslation(new Vector3f(0f, 2f, 20f));
//        viewTG.setTransform(initialView);
//        lastViewTransform.set(initialView);
//        Library.position.set(new Vector3f(0f, 2f, 20f));
//        yaw = 0.0f;
//        pitch = 0.0f;
//        firstMouse = true;
//
//        // Reset UI
//        pointsLabel.setText("Puzzles Solved: " + points);
//        timerLabel.setText("Time: 05:00");
//
//        // Restart the timer
//        startTimer();
//
//        // Request focus for the canvas
//        canvas.requestFocusInWindow();
//    }
//
//    public void incrementPoints() {
//        if (gameOver) return;
//        points++;
//        pointsLabel.setText("Puzzles Solved: " + points);
//        pointsLabel.revalidate();
//        pointsLabel.repaint();
//    }
//
//    public void markShelfAsUsed(String shelfId) {
//        bookshelfUsage.put(shelfId, true);
//    }
//
//    private boolean tryMove(Vector3f proposedPosition) {
//        Transform3D tempTransform = new Transform3D();
//        tempTransform.setTranslation(proposedPosition);
//        Library.characterTG.setTransform(tempTransform);
//
//        CollisionDetectCharacter.colliding = false;
//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException ex) {
//            ex.printStackTrace();
//        }
//
//        if (CollisionDetectCharacter.colliding) {
//            System.out.println("Collision detected at " + proposedPosition + ", reverting to " + Library.position);
//            tempTransform.setTranslation(Library.position);
//            Library.characterTG.setTransform(tempTransform);
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public void mouseMoved(MouseEvent e) {
//        if (!bookGameActive && !gameOver) {
//            processMouseMovement(e);
//        }
//    }
//
//    @Override
//    public void mouseDragged(MouseEvent e) {
//        if (!bookGameActive && !gameOver) {
//            processMouseMovement(e);
//        }
//    }
//
//    private void processMouseMovement(MouseEvent e) {
//        int x = e.getX();
//        int y = e.getY();
//        if (firstMouse) {
//            lastMouseX = x;
//            lastMouseY = y;
//            firstMouse = false;
//            System.out.println("Mouse reset to: (" + x + ", " + y + ")");
//            return;
//        }
//        int deltaX = x - lastMouseX;
//        int deltaY = y - lastMouseY;
//        lastMouseX = x;
//        lastMouseY = y;
//
//        if (!e.isShiftDown()) {
//            yaw += deltaX * rotationSensitivity;
//            pitch += deltaY * rotationSensitivity;
//            float pitchLimit = (float) Math.toRadians(89);
//            pitch = Math.max(-pitchLimit, Math.min(pitchLimit, pitch));
//            updateLook();
//            System.out.println("Mouse moved: deltaX=" + deltaX + ", deltaY=" + deltaY + ", yaw=" + yaw + ", pitch=" + pitch);
//        }
//    }
//
//    private void updateLook() {
//        if (!bookGameActive) {
//            Transform3D rotation = new Transform3D();
//            rotation.rotY(yaw);
//            Transform3D pitchRot = new Transform3D();
//            pitchRot.rotX(pitch);
//            rotation.mul(pitchRot);
//            Transform3D translation = new Transform3D();
//            translation.setTranslation(new Vector3f(Library.position.x, Library.position.y, Library.position.z));
//            Transform3D viewTransform = new Transform3D();
//            viewTransform.mul(translation, rotation);
//            viewTG.setTransform(viewTransform);
//            viewTG.getTransform(lastViewTransform);
//        }
//    }
//
//    private void restoreViewState() {
//        viewTG.setTransform(lastViewTransform);
//        Library.characterTG.setTransform(lastViewTransform);
//        Library.position.set(getPositionFromTransform(lastViewTransform));
//
//        Vector3f pos = new Vector3f();
//        lastViewTransform.get(pos);
//        System.out.println("View restored to transform position: " + pos + ", yaw: " + yaw + ", pitch: " + pitch);
//
//        firstMouse = true;
//        Point mousePos = canvas.getMousePosition();
//        if (mousePos != null) {
//            lastMouseX = mousePos.x;
//            lastMouseY = mousePos.y;
//            System.out.println("Mouse position reset to: (" + lastMouseX + ", " + lastMouseY + ") on restore");
//        }
//    }
//
//    private Vector3f getPositionFromTransform(Transform3D transform) {
//        Vector3f pos = new Vector3f();
//        transform.get(pos);
//        return pos;
//    }
//
//    @Override
//    public void mouseClicked(MouseEvent e) {
//        if (bookGameActive || gameOver) return;
//
//        Point3d point3d = new Point3d();
//        Point3d center = new Point3d();
//        canvas.getPixelLocationInImagePlate(e.getX(), e.getY(), point3d);
//        canvas.getCenterEyeInImagePlate(center);
//        Transform3D transform3D = new Transform3D();
//        canvas.getImagePlateToVworld(transform3D);
//        transform3D.transform(point3d);
//        transform3D.transform(center);
//        Vector3d mouseVec = new Vector3d();
//        mouseVec.sub(point3d, center);
//        mouseVec.normalize();
//        pickTool.setShapeRay(point3d, mouseVec);
//        PickResult pickResult = pickTool.pickClosest();
//
//        System.out.println("Click at (" + e.getX() + ", " + e.getY() + ") - Pick result: " + (pickResult != null ? "Hit" : "Miss"));
//
//        if (pickResult != null) {
//            Node node = pickResult.getNode(PickResult.SHAPE3D);
//            System.out.println("Picked node: " + (node != null ? node.getClass().getSimpleName() : "null"));
//
//            while (node != null) {
//                System.out.println("Node: " + node.getClass().getSimpleName());
//                if (node instanceof TransformGroup) {
//                    TransformGroup tg = (TransformGroup) node;
//                    Object userData = tg.getUserData();
//                    System.out.println("  UserData: " + userData);
//
//                    if ("book".equals(userData)) {
//                        Node parent = tg.getParent();
//                        while (parent != null) {
//                            if (parent instanceof TransformGroup) {
//                                TransformGroup parentTG = (TransformGroup) parent;
//                                Object parentUserData = parentTG.getUserData();
//                                System.out.println("  Parent UserData: " + parentUserData);
//                                if (parentUserData != null && parentUserData.toString().startsWith("shelf_")) {
//                                    String shelfId = (String) parentUserData;
//                                    System.out.println("Found book on shelf: " + shelfId);
//                                    if (bookshelfUsage.getOrDefault(shelfId, false)) {
//                                        JOptionPane.showMessageDialog(this, shelfId + " has already been solved!");
//                                        return;
//                                    }
//                                    int shelfNumber = Integer.parseInt(shelfId.split("_")[1]);
//                                    startBookOrderingGame(shelfNumber, shelfId);
//                                    return;
//                                }
//                            }
//                            parent = parent.getParent();
//                        }
//                        System.out.println("No shelf found in hierarchy above book!");
//                        return;
//                    }
//                }
//                node = node.getParent();
//            }
//            System.out.println("Clicked object is not a book or lacks 'book' userData");
//        } else {
//            System.out.println("Nothing picked at (" + e.getX() + ", " + e.getY() + ")");
//        }
//    }
//
//    private void startBookOrderingGame(int shelfNumber, String shelfId) {
//        SwingUtilities.invokeLater(() -> {
//            viewTG.getTransform(lastViewTransform);
//            System.out.println("Saved view transform before game: " + getPositionFromTransform(lastViewTransform));
//
//            bookGameActive = true;
//            BookOrderingGame game = new BookOrderingGame(shelfNumber, shelfId, this);
//            game.addWindowListener(new WindowAdapter() {
//                @Override
//                public void windowClosed(WindowEvent e) {
//                    bookGameActive = false;
//                    canvas.requestFocusInWindow();
//                    restoreViewState();
//                }
//            });
//            System.out.println("BookOrderingGame launched for Shelf " + shelfNumber);
//        });
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        if (bookGameActive || gameOver) return;
//
//        float moveX = 0, moveZ = 0, moveY = 0;
//        boolean updatePosition = false;
//
//        switch (e.getKeyCode()) {
//            case KeyEvent.VK_W:
//            case KeyEvent.VK_UP:
//                moveX = -(float) (Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
//                moveZ = -(float) (Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
//                updatePosition = true;
//                break;
//            case KeyEvent.VK_S:
//            case KeyEvent.VK_DOWN:
//                moveX = (float) (Math.sin(yaw) * Math.cos(pitch)) * MOVE_STEP;
//                moveZ = (float) (Math.cos(yaw) * Math.cos(pitch)) * MOVE_STEP;
//                updatePosition = true;
//                break;
//            case KeyEvent.VK_A:
//            case KeyEvent.VK_LEFT:
//                moveX = -(float) Math.cos(yaw) * MOVE_STEP;
//                moveZ = (float) Math.sin(yaw) * MOVE_STEP;
//                updatePosition = true;
//                break;
//            case KeyEvent.VK_D:
//            case KeyEvent.VK_RIGHT:
//                moveX = (float) Math.cos(yaw) * MOVE_STEP;
//                moveZ = -(float) Math.sin(yaw) * MOVE_STEP;
//                updatePosition = true;
//                break;
//            case KeyEvent.VK_SPACE:
//                if (!isJumping && !isCrouching) {
//                    isJumping = true;
//                    Vector3f jumpPosition = new Vector3f(Library.position);
//                    jumpPosition.y += JUMP_HEIGHT;
//                    System.out.println("Attempting jump to: " + jumpPosition);
//                    if (tryMove(jumpPosition)) {
//                        Library.lastSafePosition.set(Library.position);
//                        Library.position.set(jumpPosition);
//                        Movement.updatePosition();
//                        System.out.println("Jumped to: " + Library.position);
//                    } else {
//                        System.out.println("Jump blocked by collision");
//                    }
//                    Vector3f returnPosition = new Vector3f(Library.position);
//                    returnPosition.y = defaultHeight;
//                    if (tryMove(returnPosition)) {
//                        Library.lastSafePosition.set(Library.position);
//                        Library.position.set(returnPosition);
//                        Movement.updatePosition();
//                        System.out.println("Returned to: " + Library.position);
//                    } else {
//                        System.out.println("Return blocked by collision");
//                    }
//                    isJumping = false;
//                    updatePosition = false;
//                }
//                break;
//            case KeyEvent.VK_CONTROL:
//                if (!isCrouching && !isJumping) {
//                    moveY = -CROUCH_HEIGHT;
//                    isCrouching = true;
//                    updatePosition = true;
//                    System.out.println("Crouching to height: " + (Library.position.y + moveY));
//                }
//                break;
//            default:
//                return;
//        }
//
//        if (updatePosition) {
//            Vector3f proposedPosition = new Vector3f(Library.position);
//            proposedPosition.x += moveX;
//            proposedPosition.z += moveZ;
//            proposedPosition.y += moveY;
//
//            if (tryMove(proposedPosition)) {
//                Library.lastSafePosition.set(Library.position);
//                Library.position.set(proposedPosition);
//                Movement.updatePosition();
//                System.out.println("Moved to: " + Library.position);
//            } else {
//                System.out.println("Blocked by collision at: " + proposedPosition);
//            }
//
//            updateLook();
//        }
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//        if (bookGameActive || gameOver) return;
//
//        if (e.getKeyCode() == KeyEvent.VK_CONTROL && isCrouching) {
//            Vector3f proposedPosition = new Vector3f(Library.position);
//            proposedPosition.y = defaultHeight;
//
//            if (tryMove(proposedPosition)) {
//                Library.lastSafePosition.set(Library.position);
//                Library.position.set(proposedPosition);
//                Movement.updatePosition();
//                System.out.println("Standing up to: " + Library.position);
//            } else {
//                System.out.println("Can’t stand up due to collision at: " + proposedPosition);
//            }
//
//            isCrouching = false;
//            updateLook();
//        }
//    }
//
//    private static void startBackgroundSound() {
//        try {
//            URL soundURL = MainClass.class.getResource("Horrorsound.wav");
//            if (soundURL == null) {
//                System.err.println("Horrorsound.wav not found!");
//                return;
//            }
//            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
//            Clip clip = AudioSystem.getClip();
//            clip.open(audioIn);
//            clip.loop(Clip.LOOP_CONTINUOUSLY);
//        } catch (Exception ex) {
//            System.err.println("Error loading background sound: " + ex.getMessage());
//        }
//    }
//
//    public static void main(String[] args) {
//        startBackgroundSound();
//        SwingUtilities.invokeLater(() -> {
//            frame = new JFrame("Haunted Leddy");
//            MainClass mainPanel = new MainClass();
//            frame.getContentPane().add(mainPanel);
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(800, 800);
//            frame.setVisible(true);
//            mainPanel.canvas.requestFocusInWindow();
//        });
//    }
//
//    @Override
//    public void keyTyped(KeyEvent e) {}
//    @Override
//    public void mousePressed(MouseEvent e) {}
//    @Override
//    public void mouseReleased(MouseEvent e) {}
//    @Override
//    public void mouseEntered(MouseEvent e) {}
//    @Override
//    public void mouseExited(MouseEvent e) {}
//}