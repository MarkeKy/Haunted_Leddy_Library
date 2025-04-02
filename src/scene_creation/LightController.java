package scene_creation;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.jogamp.java3d.*;
import org.jogamp.vecmath.*;


// Add this class to your scene_creation package
public class LightController {
    private static List<Light> sceneLights = new ArrayList<>();
    private static Timer lightTimer;
    private static boolean lightsOn = true;
    
    // Method to register lights with the controller
    public static void registerLights(List<Light> lights) {
        sceneLights.addAll(lights);
    }
    
    // Method to start toggling lights
    public static void startLightToggling(int intervalMillis) {
        lightTimer = new Timer(intervalMillis, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleLights();
            }
        });
        lightTimer.start();
    }
    
    // Method to stop toggling lights
    public static void stopLightToggling() {
        if (lightTimer != null) {
            lightTimer.stop();
        }
    }
    
    // Method to toggle lights manually
 // In LightController.java
    public static void toggleLights() {
        lightsOn = !lightsOn;
        System.out.println("Attempting to toggle " + sceneLights.size() + " lights to " + (lightsOn ? "ON" : "OFF"));
        for (Light light : sceneLights) {
            try {
                light.setEnable(lightsOn);
                System.out.println("Light toggled successfully");
            } catch (Exception e) {
                System.err.println("Error toggling light: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Add this method to check if lights are registered
    public static boolean hasLights() {
        return !sceneLights.isEmpty();
    }
}