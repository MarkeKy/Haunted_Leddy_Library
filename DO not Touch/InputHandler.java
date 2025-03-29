//package scene_creation;
//
//import org.jogamp.java3d.Transform3D;
//import org.jogamp.java3d.TransformGroup;
//import org.jogamp.vecmath.Vector3f;
//
//import java.awt.*;
//import java.awt.event.*;
//
//public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
//    private float yaw = 0.0f;
//    private float pitch = 0.0f;
//    private int lastMouseX = -1, lastMouseY = -1;
//    private boolean firstMouse = true;
//    private final float rotationSensitivity = 0.005f;
//    private final float MOVE_STEP = 0.15f;
//    private final float JUMP_HEIGHT = 1.0f;
//    private final float CROUCH_HEIGHT = 1.0f;
//    private boolean isCrouching = false;
//    private boolean isJumping = false;
//    private final TransformGroup viewTG;
//    private final Transform3D lastViewTransform;
//    private final float defaultHeight;
//    private boolean bookGameActive = false;
//    private boolean gameOver = false;
//
//    public InputHandler(Component component, TransformGroup viewTG, Transform3D lastViewTransform, float defaultHeight) {
//        this.viewTG = viewTG;
//        this.lastViewTransform = lastViewTransform;
//        this.defaultHeight = defaultHeight;
//
//        component.addKeyListener(this);
//        component.addMouseListener(this);
//        component.addMouseMotionListener(this);
//    }
//
//    public void setBookGameActive(boolean active) {
//        this.bookGameActive = active;
//    }
//
//    public void setGameOver(boolean over) {
//        this.gameOver = over;
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
//    public void restoreViewState() {
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
//    public void keyTyped(KeyEvent e) {}
//    @Override
//    public void mousePressed(MouseEvent e) {}
//    @Override
//    public void mouseReleased(MouseEvent e) {}
//    @Override
//    public void mouseEntered(MouseEvent e) {}
//    @Override
//    public void mouseExited(MouseEvent e) {}
//    @Override
//    public void mouseClicked(MouseEvent e) {}
//}