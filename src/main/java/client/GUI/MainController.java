package client.GUI;

import client.Client;
import common.Match;
import common.MatchDetails;
import common.Message;
import common.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Timestamp;

public class MainController {

    @FXML
    private TableColumn<Match, String> matchTimeColumn;

    @FXML
    private TableColumn<Match, String> timeColumn;

    @FXML
    private TextField searchField;
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, Integer> pointsColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private Label statusLabel;

    private Client client;
    private ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    private TableView<User> leaderboardTable;
    @FXML
    private TableColumn<User, String> lbNameColumn;
    @FXML
    private TableColumn<User, Integer> lbPointsColumn;

    @FXML
    private TableView<Match> historyTable;
    @FXML
    private TableColumn<MatchDetails, String> historyResultColumn;

    @FXML
    private TableView<Match> matchesTable;
    @FXML
    private TableColumn<Match, Integer> matchIdColumn;
    @FXML
    private TableColumn<Match, String> opponentColumn;
    @FXML
    private TableColumn<Match, String> matchResultColumn;

    public void setClient(Client client) throws IOException {
        this.client = client;
        loadUsers();
        loadLeaderboard();
        loadUserMatches(); // Tải danh sách trận đấu
    }

    private void loadUserMatches() throws IOException {
        Message request = new Message("get_user_matches", null);
        client.sendMessage(request);
    }

    // Thêm phương thức để cập nhật bảng xếp hạng
    public void updateLeaderboard(List<User> leaderboard) {
        ObservableList<User> leaderboardList = FXCollections.observableArrayList(leaderboard);
        leaderboardTable.setItems(leaderboardList);
    }

    private void loadUsers() throws IOException {
        // Gửi yêu cầu lấy danh sách người chơi
        Message request = new Message("get_users", null);
        client.sendMessage(request);
    }

    @FXML
    private void handleLogout() throws IOException {
        if (client.getUser() != null) {
            Message logoutMessage = new Message("logout", client.getUser().getId());
            client.sendMessage(logoutMessage);
            client.handleLogout();
        }
    }

    @FXML
    private void handleFilterOnline() {
        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User user : usersList) {
            if (user.getStatus().equalsIgnoreCase("online")) {
                filtered.add(user);
            }
        }
        usersTable.setItems(filtered);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase();
        if (keyword.isEmpty()) {
            usersTable.setItems(usersList);
            return;
        }
        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User user : usersList) {
            if (user.getUsername().toLowerCase().contains(keyword)) {
                filtered.add(user);
            }
        }
        usersTable.setItems(filtered);
    }

    // Cập nhật danh sách người chơi từ server
    public void updateUsersList(List<User> newUsers) {
        usersList.setAll(newUsers);
        usersTable.setItems(usersList);
    }

    // Cập nhật trạng thái người chơi
    public void updateStatus(String statusUpdate) {
        if (statusUpdate == null || statusUpdate.isEmpty()) {
            return;
        }
        String[] parts = statusUpdate.split(" ");
        if (parts.length >= 3) {
            String username = parts[0];
            String status = parts[2].replace(".", "");
            for (User user : usersList) {
                if (user.getUsername().equalsIgnoreCase(username)) {
                    user.setStatus(status);
                    usersTable.refresh();
                    break;
                }
            }
        }
    }

    // Hiển thị yêu cầu trận đấu
    public void showMatchRequest(int requesterId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yêu Cầu Trận Đấu");
        alert.setHeaderText("Ngươi đã nhận được yêu cầu từ kẻ có ID là " + requesterId);
        alert.setContentText("Ngươi muốn tham gia không ?");

        alert.showAndWait().ifPresent(response -> {
            boolean accepted = response == ButtonType.OK;
            Object[] data = {requesterId, accepted};
            Message responseMessage = new Message("match_response", data);
            try {
                client.sendMessage(responseMessage);
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Xử lý phản hồi trận đấu
    public void handleMatchResponse(String response) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Trận Đấu");
        alert.setHeaderText(null);
        alert.setContentText(response);
        alert.showAndWait();
    }

    @FXML
    private void initialize() {
        // Cấu hình bảng người chơi
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom cell factory cho statusColumn
        statusColumn.setCellFactory(column -> new TableCell<User, String>() {
            private final HBox hBox = new HBox(5);
            private final Circle circle = new Circle(5);
            private final Label label = new Label();

            {
                hBox.getChildren().addAll(circle, label);
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    circle.setFill(status.equalsIgnoreCase("online") ? Color.GREEN : Color.RED);
                    label.setText(status);
                    setGraphic(hBox);
                    setText(null);
                }
            }
        });

        // Sự kiện double click để gửi yêu cầu trận đấu
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    User clickedUser = row.getItem();
                    if (clickedUser.getId() != client.getUser().getId()) {
                        Message matchRequest = new Message("request_match", clickedUser.getId());
                        try {
                            client.sendMessage(matchRequest);
                        } catch (IOException ex) {
                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            return row;
        });

        // Cấu hình bảng xếp hạng
        lbNameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        lbPointsColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

        // Cấu hình bảng danh sách trận đấu
        matchIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        opponentColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            String opponentName = match.getOpponentName(client.getUser().getId());
            return new SimpleStringProperty(opponentName);
        });
        matchResultColumn.setCellValueFactory(cellData -> {
            Match match = cellData.getValue();
            String result = match.getResult(client.getUser().getId());
            return new SimpleStringProperty(result);
        });
        // Cấu hình cột thời gian cho matchesTable
        timeColumn.setCellValueFactory(cellData -> {
            Timestamp time = cellData.getValue().getTime();
            return new SimpleStringProperty(time != null ? time.toString() : "");
        });
    }

    public void showMatchDetails(List<Match> details) {
        ObservableList<Match> detailsList = FXCollections.observableArrayList(details);
        historyTable.setItems(detailsList);
    }

    private void loadLeaderboard() throws IOException {
        Message request = new Message("get_leaderboard", null);
        client.sendMessage(request);
    }

}