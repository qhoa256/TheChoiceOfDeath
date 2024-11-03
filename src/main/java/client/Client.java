package client;

import client.GUI.GameRoomController;
import client.GUI.LoginController;
import client.GUI.MainController;
import common.Match;
import common.MatchDetails;
import common.Message;
import common.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.List;

public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private User user;
    private Stage primaryStage;

    // Controllers
    private LoginController loginController;
    private MainController mainController;
    private GameRoomController gameRoomController;

    private volatile boolean isRunning = true;

    public Client(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void showErrorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void startConnection(String address, int port) {
        try {
            socket = new Socket(address, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            listenForMessages();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể kết nối tới server.");
        }
    }

    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (isRunning) {
                    Message message = (Message) in.readObject();
                    if (message != null) {
                        handleMessage(message);
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                if (isRunning) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        showErrorAlert("Kết nối tới server bị mất.");
                        try {
                            showLoginUI();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    System.out.println("Đã đóng kết nối, dừng luồng lắng nghe.");
                }
            }
        }).start();
    }

    private void handleMessage(Message message) {
        if (message == null) {
            return;
        }
        switch (message.getType()) {
            case "login_success":
                this.user = (User) message.getContent();
                Platform.runLater(() -> showMainUI());
                break;
            case "login_failure":
                Platform.runLater(() -> {
                    if (loginController != null) {
                        loginController.showError((String) message.getContent());
                    }
                });
                break;
            case "user_list":
                List<User> users = (List<User>) message.getContent();
                Platform.runLater(() -> {
                    if (mainController != null) {
                        mainController.updateUsersList(users);
                    }
                });
                break;
            case "status_update":
                Platform.runLater(() -> {
                    if (mainController != null) {
                        mainController.updateStatus((String) message.getContent());
                    }
                });
                break;
            case "match_request":
                Platform.runLater(() -> {
                    if (mainController != null) {
                        mainController.showMatchRequest((int) message.getContent());
                    }
                });
                break;
            case "match_response":
                Platform.runLater(() -> {
                    if (mainController != null) {
                        mainController.handleMatchResponse((String) message.getContent());
                    }
                });
                break;
            case "chat":
                Platform.runLater(() -> {
                    if (gameRoomController != null) {
                        gameRoomController.updateChat((String) message.getContent());
                    }
                });
                break;
            case "match_start":
                String[] playerNames = (String[]) message.getContent();
                Platform.runLater(() -> {
                    showGameRoomUI();
                    if (gameRoomController != null) {
                        gameRoomController.setPlayerNames(playerNames[0], playerNames[1]);
                    }
                });
                break;
            case "round_result":
                Platform.runLater(() -> {
                    if (gameRoomController != null) {
                        gameRoomController.showRoundResult((String) message.getContent());
                    }
                });
                break;
            case "match_end":
                Platform.runLater(() -> {
                    if (gameRoomController != null) {
                        gameRoomController.endMatch((String) message.getContent());
                    }
                });
                break;
            case "match_end_left":
                Platform.runLater(() -> {
                    if (gameRoomController != null) {
                        gameRoomController.endMatchLeft((String) message.getContent());
                    }
                });
                break;
            case "your_turn":
                String process = (String) message.getContent();
                Platform.runLater(() -> {
                    if (gameRoomController != null) {
                        gameRoomController.handleYourTurn(process);
                    }
                });
                break;
            case "disable_all":
                Platform.runLater(() -> {
                    if (gameRoomController != null) {
                        gameRoomController.disableAll();
                    }
                });
                break;
            case "play_again_request":
                Platform.runLater(() -> {
                    if (gameRoomController != null) {
                        gameRoomController.promptPlayAgain();
                    }
                });
                break;
            case "rematch_declined":
                Platform.runLater(() -> {
                    if (gameRoomController != null) {
                        gameRoomController.handleRematchDeclined((String) message.getContent());
                    }
                });
                break;
            case "leaderboard":
                List<User> leaderboard = (List<User>) message.getContent();
                Platform.runLater(() -> {
                    if (mainController != null) {
                        mainController.updateLeaderboard(leaderboard);
                    }
                });
                break;

            case "user_matches":
                List<Match> details = (List<Match>) message.getContent();
                Platform.runLater(() -> {
                    if (mainController != null) {
                        mainController.showMatchDetails(details);
                    }
                });
                break;
            case "safe":
                String sprocess = (String) message.getContent();
                String[] spart = sprocess.trim().split("\\s+");
                Platform.runLater(() ->{
                    if (gameRoomController != null){
                        try {
                            gameRoomController.safeProcess(spart);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                break;
            case "danger":
                String dprocess = (String) message.getContent();
                String[] dpart = dprocess.trim().split("\\s+");
                Platform.runLater(() ->{
                    if (gameRoomController != null){
                        try {
                            gameRoomController.dangerProcess(dpart);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                break;
            // Các loại message khác
            // ...
        }
    }

    public void sendMessage(Message message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public void showMainUI() {
        try {
            System.out.println("Loading MainUI.fxml...");
            FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/com/example/choosebutton/MainUI.fxml"));
            Parent root = loader.load();
            mainController = loader.getController();

            if (mainController == null) {
                System.err.println("Controller is null for MainUI.fxml");
                showErrorAlert("Không thể tải controller giao diện chính.");
                return;
            }

            mainController.setClient(this);
            Scene scene = new Scene(root);

            URL cssLocation = MainController.class.getResource("/com/example/choosebutton/style.css");
            if (cssLocation != null) {
                scene.getStylesheets().add(cssLocation.toExternalForm());
                System.out.println("CSS file loaded: " + cssLocation.toExternalForm());
            } else {
                System.err.println("Cannot find CSS file: style.css");
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Death Choice - Main");
            primaryStage.setMinWidth(400);
            primaryStage.setMinHeight(300);
            primaryStage.show();
            sendMessage(new Message("get_users", null));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải giao diện chính.");
        }
    }

    public void showLoginUI() {
        try {
            System.out.println("Loading LoginUI.fxml...");
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/com/example/choosebutton/LoginUI.fxml"));
            Parent root = loader.load();
            loginController = loader.getController();

            if (loginController == null) {
                System.err.println("Controller is null for LoginUI.fxml");
                showErrorAlert("Không thể tải controller giao diện đăng nhập.");
                return;
            }

            loginController.setClient(this);
            Scene scene = new Scene(root);

            URL cssLocation = LoginController.class.getResource("/com/example/choosebutton/style.css");
            if (cssLocation != null) {
                scene.getStylesheets().add(cssLocation.toExternalForm());
                System.out.println("CSS file loaded: " + cssLocation.toExternalForm());
            } else {
                System.err.println("Cannot find CSS file: style.css");
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Death Choice");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải giao diện đăng nhập.");
        }
    }

    public void showGameRoomUI() {
        try {
            System.out.println("Loading GameRoomUI.fxml...");
            FXMLLoader loader = new FXMLLoader(GameRoomController.class.getResource("/com/example/choosebutton/GameRoomUI.fxml"));
            Parent root = loader.load();
            gameRoomController = loader.getController();

            if (gameRoomController == null) {
                System.err.println("Controller is null for GameRoomUI.fxml");
                showErrorAlert("Không thể tải controller giao diện phòng chơi.");
                return;
            }

            gameRoomController.setClient(this);
            Scene scene = new Scene(root);

            URL cssLocation = GameRoomController.class.getResource("/com/example/choosebutton/style.css");
            if (cssLocation != null) {
                scene.getStylesheets().add(cssLocation.toExternalForm());
                System.out.println("CSS file loaded: " + cssLocation.toExternalForm());
            } else {
                System.err.println("Cannot find CSS file: style.css");
            }

            primaryStage.setScene(scene);
            primaryStage.setTitle("Death Choice - Game Room");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể tải giao diện phòng chơi.");
        }
    }

    public User getUser() {
        return user;
    }

    public void closeConnection() throws IOException {
        isRunning = false; // Dừng luồng lắng nghe
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(ClientApp.class, args);
    }
}