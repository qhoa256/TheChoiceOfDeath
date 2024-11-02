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
    private Client client;

    private int scoreP1 = 0;
    private int scoreP2 = 0;

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

    public String splitID(String id) {
        return new String("" + id.charAt(6) + id.charAt(8));
    }

    private void startBlinkingEffect(Label label) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    label.setStyle(label.getStyle() +
                            "-fx-border-color: yellow; " +
                            "-fx-border-width: 3; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-color: #800000; " +
                            "-fx-padding: 10px; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold;");
                    label.setEffect(new DropShadow(20, Color.YELLOW));
                }),
                new KeyFrame(Duration.seconds(0.2), e -> {
                    label.setStyle(label.getStyle() +
                            "-fx-border-width: 0; " +
                            "-fx-background-color: #800000; " +
                            "-fx-padding: 10px; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 18px; " +
                            "-fx-font-weight: bold;");
                    label.setEffect(new DropShadow(10, Color.RED));
                }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void stopBlinkingEffect(Label label) {
        label.setStyle("-fx-background-color: #800000; " +
                "-fx-padding: 10px; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-border-width: 0;");
        label.setEffect(new DropShadow(10, Color.RED));
    }

    public void handleYourTurn(String p) {
        Platform.runLater(() -> {
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
        System.out.println(buttonId);
        Message choosingBoxMessage = new Message("choose", buttonId);
        client.sendMessage(choosingBoxMessage);
    }

    private void setPropButton(Button b, int n) {
        Platform.runLater(() -> {
            if (n == 0) {
                b.setText("SAFE");
                b.setStyle("-fx-background-color: #1a4a1a; " + // Dark green
                        "-fx-text-fill: #7DFD79; " + // Light green text
                        "-fx-background-radius: 10; " +
                        "-fx-font-weight: bold;");
            } else {
                b.setText("DANGER");
                b.setStyle("-fx-background-color: #4a1a1a; " + // Dark red
                        "-fx-text-fill: #FF7C7C; " + // Light red text
                        "-fx-background-radius: 10; " +
                        "-fx-font-weight: bold;");
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
            chatArea.appendText(message + "\n");
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

    public void endMatch(String result) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Kết Thúc Trận Đấu");
            alert.setHeaderText(null);
            alert.setContentText(result);
            alert.showAndWait();
            try {
                client.showMainUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Loi: ca 2 cung choi lai thi chi 1 nguoi co the choi lai
    public void promptPlayAgain() {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Chơi Lại");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có muốn chơi lại không?");
            ButtonType yesButton = new ButtonType("Có", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("Không", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                boolean playAgain = result.get() == yesButton;
                Message playAgainResponse = new Message("play_again_response", playAgain);
                try {
                    client.sendMessage(playAgainResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
