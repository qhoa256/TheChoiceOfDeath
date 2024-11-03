package client.GUI;

import client.Client;
import common.Message;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class GameRoomController {

    @FXML
    private TextArea chatArea;
    @FXML
    private TextField chatInput;
    @FXML
    private Button sendButton;
    @FXML
    private Label player1Label;
    @FXML
    private Label player2Label;
    @FXML
    private Label player1ScoreLabel;
    @FXML
    private Label player2ScoreLabel;
    @FXML
    private Button button1_1;
    @FXML
    private Button button1_2;
    @FXML
    private Button button1_3;
    @FXML
    private Button button1_4;
    @FXML
    private Button button1_5;
    @FXML
    private Button button1_6;
    @FXML
    private Button button2_1;
    @FXML
    private Button button2_2;
    @FXML
    private Button button2_3;
    @FXML
    private Button button2_4;
    @FXML
    private Button button2_5;
    @FXML
    private Button button2_6;
    @FXML
    private Label timeCounter;
    private Client client;

    private int scoreP1 = 0;
    private int scoreP2 = 0;

    private boolean hasPromptedPlayAgain = false;

    private Timeline countdownTimer;
    private int timeLeft;
    private boolean clicked1 = false;
    private boolean clicked2 = false;
    private boolean isEnded = false;

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void initialize() throws IOException, SQLException {
        System.out.println("Initializing GameRoomController...");

    }

    @FXML
    private void handleSendChat() throws IOException {
        String message = chatInput.getText();
        if (!message.isEmpty()) {
            Message chatMessage = new Message("chat", message);
            client.sendMessage(chatMessage);
            chatInput.clear();
        }
    }

    private void startBlinkingEffect(Label label) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    label.setStyle("-fx-background-color: rgba(128, 0, 0, 0.9); " + // Semi-transparent dark red
                            "-fx-border-color: #FFD700; " + // Gold border
                            "-fx-border-width: 3; " +
                            "-fx-border-radius: 10; " +
                            "-fx-padding: 10px; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold;");
                    label.setEffect(new DropShadow(20, Color.YELLOW));
                }),
                new KeyFrame(Duration.seconds(0.5), e -> {
                    label.setStyle("-fx-background-color: rgba(128, 0, 0, 0.9); " +
                            "-fx-border-width: 0; " +
                            "-fx-padding: 10px; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 10;");
                    label.setEffect(new DropShadow(10, Color.RED));
                }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stopBlinkingEffect(Label label) {
        label.setStyle("-fx-background-color: rgba(128, 0, 0, 0.9); " +
                "-fx-padding: 10px; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,0,0,0.8), 10, 0.5, 0, 0);"); // Subtle red glow
        label.setEffect(new DropShadow(10, Color.RED));
    }

    public void handleYourTurn(String p) {
        Platform.runLater(() -> {
            isEnded = false;
            if (p.equals("P1")) {
                startBlinkingEffect(player1Label);
                stopBlinkingEffect(player2Label);
                button1_1.setDisable(false);
                button1_2.setDisable(false);
                button1_3.setDisable(false);
                button1_4.setDisable(false);
                button1_5.setDisable(false);
                button1_6.setDisable(false);
                button2_1.setDisable(true);
                button2_2.setDisable(true);
                button2_3.setDisable(true);
                button2_4.setDisable(true);
                button2_5.setDisable(true);
                button2_6.setDisable(true);
                timeCountingP1();
            } else {
                startBlinkingEffect(player2Label);
                stopBlinkingEffect(player1Label);
                button2_1.setDisable(false);
                button2_2.setDisable(false);
                button2_3.setDisable(false);
                button2_4.setDisable(false);
                button2_5.setDisable(false);
                button2_6.setDisable(false);
                button1_1.setDisable(true);
                button1_2.setDisable(true);
                button1_3.setDisable(true);
                button1_4.setDisable(true);
                button1_5.setDisable(true);
                button1_6.setDisable(true);
                timeCountingP2();
            }
        });
    }

    public void disableAll() {
        button1_1.setDisable(true);
        button1_2.setDisable(true);
        button1_3.setDisable(true);
        button1_4.setDisable(true);
        button1_5.setDisable(true);
        button1_6.setDisable(true);
        button2_1.setDisable(true);
        button2_2.setDisable(true);
        button2_3.setDisable(true);
        button2_4.setDisable(true);
        button2_5.setDisable(true);
        button2_6.setDisable(true);
    }

    @FXML
    private void handleChoosingBox(javafx.event.ActionEvent event) throws IOException {
        Button clickedButton = (Button) event.getSource();
        String buttonId = clickedButton.getId();
        if (buttonId.charAt(6) == '1'){
            clicked1 = true;
        }
        else{
            clicked2 = true;
        }
        System.out.println(buttonId);
        Message choosingBoxMessage = new Message("choose", buttonId);
        client.sendMessage(choosingBoxMessage);
    }

    private void setPropButton(Button b, int n) {
        Platform.runLater(() -> {
            if (n == 0) {
                b.setText("ALIVE");
                b.setStyle("-fx-background-color: rgba(26, 74, 26, 0.8); " + // Semi-transparent dark green
                        "-fx-text-fill: #7DFD79; " + // Light green text
                        "-fx-background-radius: 10; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 30px; " +
                        "-fx-effect: dropshadow(gaussian, #7DFD79, 10, 0.5, 0, 0);"); // Green glow effect
            } else {
                b.setText("DEATH");
                b.setStyle("-fx-background-color: #000000; " + // Semi-transparent dark red
                        "-fx-text-fill: #FFFFFF; " + // Light red text
                        "-fx-background-radius: 10; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 30px; " +
                        "-fx-effect: dropshadow(gaussian, #FF0000, 15, 0.7, 0, 0);"); // Red glow effect
            }
            b.setDisable(true);
        });
    }

    public void safeProcess(String[] part) throws IOException {
        if (part[0].equals("P1")) {
            scoreP1++;
            String butId = part[1];
            player1ScoreLabel.setText("Score: " + scoreP1);
            while (true) {
                if (butId.equals(button1_1.getId())) {
                    setPropButton(button1_1, 0);
                    break;
                }
                if (butId.equals(button1_2.getId())) {
                    setPropButton(button1_2, 0);
                    break;
                }
                if (butId.equals(button1_3.getId())) {
                    setPropButton(button1_3, 0);
                    break;
                }
                if (butId.equals(button1_4.getId())) {
                    setPropButton(button1_4, 0);
                    break;
                }
                if (butId.equals(button1_5.getId())) {
                    setPropButton(button1_5, 0);
                    break;
                }
                if (butId.equals(button1_6.getId())) {
                    setPropButton(button1_6, 0);
                    break;
                }
            }
        } else {
            scoreP2++;
            String butId = part[1];
            player2ScoreLabel.setText("Score: " + scoreP2);
            while (true) {
                if (butId.equals(button2_1.getId())) {
                    setPropButton(button2_1, 0);
                    break;
                }
                if (butId.equals(button2_2.getId())) {
                    setPropButton(button2_2, 0);
                    break;
                }
                if (butId.equals(button2_3.getId())) {
                    setPropButton(button2_3, 0);
                    break;
                }
                if (butId.equals(button2_4.getId())) {
                    setPropButton(button2_4, 0);
                    break;
                }
                if (butId.equals(button2_5.getId())) {
                    setPropButton(button2_5, 0);
                    break;
                }
                if (butId.equals(button2_6.getId())) {
                    setPropButton(button2_6, 0);
                    break;
                }
            }
        }
    }

    public void dangerProcess(String[] part) throws IOException {
        if (part[0].equals("P1")) {
            String butId = part[1];
            player1ScoreLabel.setText("Đã chết.");
            while (true) {
                if (butId.equals(button1_1.getId())) {
                    setPropButton(button1_1, 1);
                    break;
                }
                if (butId.equals(button1_2.getId())) {
                    setPropButton(button1_2, 1);
                    break;
                }
                if (butId.equals(button1_3.getId())) {
                    setPropButton(button1_3, 1);
                    break;
                }
                if (butId.equals(button1_4.getId())) {
                    setPropButton(button1_4, 1);
                    break;
                }
                if (butId.equals(button1_5.getId())) {
                    setPropButton(button1_5, 1);
                    break;
                }
                if (butId.equals(button1_6.getId())) {
                    setPropButton(button1_6, 1);
                    break;
                }
            }
        } else {
            String butId = part[1];
            player2ScoreLabel.setText("Đã chết.");
            while (true) {
                if (butId.equals(button2_1.getId())) {
                    setPropButton(button2_1, 1);
                    break;
                }
                if (butId.equals(button2_2.getId())) {
                    setPropButton(button2_2, 1);
                    break;
                }
                if (butId.equals(button2_3.getId())) {
                    setPropButton(button2_3, 1);
                    break;
                }
                if (butId.equals(button2_4.getId())) {
                    setPropButton(button2_4, 1);
                    break;
                }
                if (butId.equals(button2_5.getId())) {
                    setPropButton(button2_5, 1);
                    break;
                }
                if (butId.equals(button2_6.getId())) {
                    setPropButton(button2_6, 1);
                    break;
                }
            }
        }
    }

    public void updateChat(String message) {
        Platform.runLater(() -> {
            chatArea.appendText(message);
        });
    }

    public void showRoundResult(String roundResult) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Kết Quả Lượt");
            alert.setHeaderText(null);
            alert.setContentText(roundResult);
            alert.showAndWait();
        });
    }

    public void timeCountingP1(){
        clicked2 = false;
        if(isEnded){
            return;
        }
        timeLeft = 30; // Đặt lại thời gian
        timeCounter.setText(timeLeft + ":00");

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (isEnded) { // Kiểm tra thêm để dừng khi trận đấu kết thúc
                countdownTimer.stop();
                return;
            }
            timeLeft--;
            timeCounter.setText(timeLeft + ":00");
            if(clicked1 && timeLeft >= 0){
                countdownTimer.stop();
            }
            if (timeLeft < 0) {
                endTurn();
                return;
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }
    public void timeCountingP2(){
        clicked1 = false;
        if(isEnded){
            return;
        }
        timeLeft = 30; // Đặt lại thời gian
        timeCounter.setText(timeLeft + ":00");

        countdownTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (isEnded) { // Kiểm tra thêm để dừng khi trận đấu kết thúc
                countdownTimer.stop();
                return;
            }
            timeLeft--;
            timeCounter.setText(timeLeft + ":00");
            if(clicked2 && timeLeft >= 0){
                countdownTimer.stop();
            }
            if (timeLeft < 0) {
                endTurn();
                return;
            }
        }));
        countdownTimer.setCycleCount(Timeline.INDEFINITE);
        countdownTimer.play();
    }
    public void endTurn() {
        countdownTimer.stop(); // Dừng bộ đếm
        // Gửi thông báo đến server để xử lý tình huống thua cuộc
        Message lossMessage = new Message("player_time_out", "Bạn đã thua vì hết thời gian.");
        try {
            client.sendMessage(lossMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void endMatch(String result) {
        if (countdownTimer != null) {
            countdownTimer.stop(); // Đảm bảo dừng mọi bộ đếm khi trận đấu kết thúc
        }
        isEnded = true;
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);

            alert.setTitle("Kết Thúc Trận Đấu");
            alert.setHeaderText(result);
            ButtonType okButton = new ButtonType("Hư vô");
            alert.getButtonTypes().clear();

            // Thêm label để hiển thị thời gian còn lại
            Label timerLabel = new Label("Ta cần dọn dẹp bọn thua cuộc trong 3 giây");
            alert.getDialogPane().setContent(timerLabel);

            // Bắt đầu bộ đếm ngược 3 giây
            Timeline countdownTimer2 = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                int timeLeft = Integer.parseInt(timerLabel.getText().replaceAll("[^0-9]", "")) - 1;
                timerLabel.setText("Ta cần dọn dẹp bọn thua cuộc trong " + timeLeft + " giây");
                if (timeLeft <= 0) {
                    alert.getButtonTypes().setAll(okButton);
                    alert.close(); // Đóng thông báo
                    promptPlayAgain(); // Gọi hàm yêu cầu chơi lại
                }
            }));
            countdownTimer2.setCycleCount(3); // Đếm 5 giây
            countdownTimer2.play(); // Bắt đầu đếm ngược

            alert.show(); // Hiển thị thông báo


            // Chỉ gọi hàm yêu cầu chơi lại khi đếm ngược kết thúc
            alert.setOnCloseRequest(event -> {
                if (timerLabel.getText().contains("0 giây")) {
                    alert.getButtonTypes().clear();
                    alert.resultProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue == okButton) {
                            countdownTimer2.stop(); // Dừng bộ đếm khi nhấn OK
                            alert.close(); // Đóng thông báo
                        }
                    });// Thêm nút OK // Cho phép đóng thông báo nếu là OK sau đếm ngược
                } else {
                    event.consume(); // Ngăn chặn đóng thông báo nếu chưa hết thời gian
                }
            });
        });
    }
    public void endMatchLeft(String result) {
        if (countdownTimer != null) {
            countdownTimer.stop(); // Đảm bảo dừng mọi bộ đếm khi trận đấu kết thúc
        }
        isEnded = true;
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);

            alert.setTitle("Kết Thúc Trận Đấu");
            alert.setHeaderText(result);
            ButtonType okButton = new ButtonType("Hư vô");
            alert.getButtonTypes().clear();

            // Thêm label để hiển thị thời gian còn lại
            Label timerLabel = new Label("Ta cần dọn dẹp bọn thua cuộc trong 3 giây");
            alert.getDialogPane().setContent(timerLabel);

            // Bắt đầu bộ đếm ngược 3 giây
            Timeline countdownTimer2 = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
                int timeLeft = Integer.parseInt(timerLabel.getText().replaceAll("[^0-9]", "")) - 1;
                timerLabel.setText("Ta cần dọn dẹp bọn thua cuộc trong " + timeLeft + " giây");
                if (timeLeft <= 0) {
                    alert.getButtonTypes().setAll(okButton);
                    alert.close(); // Đóng thông báo
                    client.showMainUI(); // Gọi hàm yêu cầu chơi lại
                }
            }));
            countdownTimer2.setCycleCount(3); // Đếm 5 giây
            countdownTimer2.play(); // Bắt đầu đếm ngược

            alert.show(); // Hiển thị thông báo

            alert.setOnCloseRequest(event -> {
                if (timerLabel.getText().contains("0 giây")) {
                    alert.getButtonTypes().clear();
                    alert.resultProperty().addListener((obs, oldValue, newValue) -> {
                        if (newValue == okButton) {
                            countdownTimer2.stop(); // Dừng bộ đếm khi nhấn OK
                            alert.close(); // Đóng thông báo
                        }
                    });// Thêm nút OK // Cho phép đóng thông báo nếu là OK sau đếm ngược
                } else {
                    event.consume(); // Ngăn chặn đóng thông báo nếu chưa hết thời gian
                }
            });
        });
    }

    // Loi: ca 2 cung choi lai thi chi 1 nguoi co the choi lai
    public void promptPlayAgain() {

        if(hasPromptedPlayAgain){
            return;
        }
        hasPromptedPlayAgain = true;
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Chơi Lại");
            alert.setHeaderText(null);
            alert.setContentText("Ngươi muốn chơi lại không?");
            ButtonType yesButton = new ButtonType("Có", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("Không", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                boolean playAgain = result.get() == yesButton;
                Message playAgainResponse = new Message("play_again_response", playAgain);
                System.out.println(client.getUser().getUsername() + " sent play again!");
                try {
                    client.sendMessage(playAgainResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            hasPromptedPlayAgain = false;
        });
    }

    public void handleRematchDeclined(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Chơi Lại");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            try {
                client.showMainUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleQuitGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thoát Trò Chơi");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn thoát trò chơi không?");
        ButtonType yesButton = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Không", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            Message quitMessage = new Message("quit_game", null);
            try {
                client.sendMessage(quitMessage);
                client.showMainUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setPlayerNames(String player1Name, String player2Name) {
        Platform.runLater(() -> {
            player1Label.setText(player1Name);
            player2Label.setText(player2Name);
        });
    }
}