package client;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class AudioPlayer {
    private static MediaPlayer mediaPlayer;
    private static boolean isMuted = false;

    public static void initializeBackgroundMusic() {
        try {
            URL resourceUrl = AudioPlayer.class.getResource("/sounds/background_music.mp3");
            if (resourceUrl != null) {
                Media media = new Media(resourceUrl.toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.play();
            } else {
                System.err.println("Could not find background music file");
            }
        } catch (Exception e) {
            System.err.println("Error playing background music: " + e.getMessage());
        }
    }

    public static void toggleSound() {
        if (mediaPlayer != null) {
            isMuted = !isMuted;
            mediaPlayer.setMute(isMuted);
        }
    }

    public static boolean isMuted() {
        return isMuted;
    }
} 