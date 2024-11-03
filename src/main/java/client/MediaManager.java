package client;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class MediaManager {
    private static MediaPlayer mediaPlayer;
    
    public static void playBackgroundMusic() {
        try {
            URL musicFile = MediaManager.class.getResource("/com/example/choosebutton/background_music.mp3");
            if (musicFile != null) {
                Media sound = new Media(musicFile.toString());
                mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Phát lặp vô hạn
                mediaPlayer.setVolume(0.5); // Âm lượng 50%
                mediaPlayer.play();
            } else {
                System.err.println("Không tìm thấy file nhạc");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
} 