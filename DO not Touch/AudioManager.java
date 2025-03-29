////package scene_creation;
//
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import javax.sound.sampled.Clip;
//import java.net.URL;
//
//public class AudioManager {
//    public void startBackgroundSound() {
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
//    public void playYouLoseSound() {
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
//}