package server;

import common.Match;
import common.User;
import common.MatchDetails;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/btlltm2";
    private static final String USER = "root"; // Thay bằng user MySQL của bạn
    private static final String PASSWORD = "1"; // Thay bằng password MySQL của bạn

    private Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Phương thức đăng nhập
    public User authenticate(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, username);
        stmt.setString(2, password);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("score"),
                    rs.getString("status")
            );
        }
        return null;
    }

    // Cập nhật trạng thái người dùng
    public void updateUserStatus(int userId, String status) throws SQLException {
        String query = "UPDATE users SET status = ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, status);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    }

    // Lấy danh sách người chơi
    public List<User> getUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("score"),
                    rs.getString("status")
            ));
        }
        return users;
    }

    // Lưu lịch sử đấu
    public int saveMatch(int player1Id, int player2Id, int winnerId) throws SQLException {
        String query = "INSERT INTO matches (player1_id, player2_id, winner_id) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, player1Id);
        stmt.setInt(2, player2Id);
        if (winnerId > 0) {
            stmt.setInt(3, winnerId);
        } else {
            stmt.setNull(3, Types.INTEGER);
        }
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    public void updateMatchWinner(int matchId, int winnerId) throws SQLException {
        String query = "UPDATE matches SET winner_id = ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, winnerId);
        stmt.setInt(2, matchId);
        stmt.executeUpdate();
    }

    // Cập nhật điểm số
    public void updateUserScore(int userId, int score) throws SQLException {
        String query = "UPDATE users SET score = score + ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, score);
        stmt.setInt(2, userId);
        stmt.executeUpdate();
    }

    // Phương thức lưu chi tiết trận đấu
    public void saveMatchDetails(int matchId, int round, String result) throws SQLException {
        String query = "INSERT INTO match_details (match_id, round, result) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, matchId);
        stmt.setInt(2, round);
        stmt.setString(3, result);
        stmt.executeUpdate();
    }

    // Lấy lịch sử đấu theo match ID
    public List<MatchDetails> getMatchDetails(int matchId) throws SQLException {
        List<MatchDetails> detailsList = new ArrayList<>();
        String query = "SELECT *, timestamp AS time FROM match_details WHERE match_id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, matchId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            detailsList.add(new MatchDetails(
                    rs.getInt("id"),
                    rs.getInt("match_id"),
                    rs.getInt("round"),
                    rs.getString("result"),
                    rs.getTimestamp("time")
            ));
        }
        return detailsList;
    }


    // Các phương thức khác như lấy lịch sử đấu, bảng xếp hạng, v.v.
    // ...

    public List<User> getLeaderboard() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY score DESC";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getInt("score"),
                    rs.getString("status")
            ));
        }
        return users;
    }

    public List<MatchDetails> getUserMatchHistory(int userId) throws SQLException {
        List<MatchDetails> history = new ArrayList<>();
        String query = "SELECT md.*, md.timestamp AS time FROM match_details md " +
                "JOIN matches m ON md.match_id = m.id " +
                "WHERE m.player1_id = ? OR m.player2_id = ? ORDER BY md.match_id DESC, md.round ASC";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        stmt.setInt(2, userId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            history.add(new MatchDetails(
                    rs.getInt("id"),
                    rs.getInt("match_id"),
                    rs.getInt("round"),
                    rs.getString("result"),
                    rs.getTimestamp("time") // Lấy cột timestamp
            ));
        }
        return history;
    }

    public List<Match> getUserMatches(int userId) throws SQLException {
        List<Match> matches = new ArrayList<>();
        String query = "SELECT m.*, m.timestamp AS time, u1.username AS player1_name, u2.username AS player2_name FROM matches m " +
                "JOIN users u1 ON m.player1_id = u1.id " +
                "JOIN users u2 ON m.player2_id = u2.id " +
                "WHERE m.player1_id = ? OR m.player2_id = ? ORDER BY m.id DESC";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        stmt.setInt(2, userId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            matches.add(new Match(
                    rs.getInt("id"),
                    rs.getInt("player1_id"),
                    rs.getInt("player2_id"),
                    rs.getObject("winner_id") != null ? rs.getInt("winner_id") : null,
                    rs.getString("player1_name"),
                    rs.getString("player2_name"),
                    rs.getTimestamp("time")
            ));
        }
        return matches;
    }
/*
create TABLE USERS(
	id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    score INT DEFAULT 0,
    status VARCHAR(10) DEFAULT 'offline'
);
CREATE TABLE matches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    winner_id INT DEFAULT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player1_id) REFERENCES users(id),
    FOREIGN KEY (player2_id) REFERENCES users(id),
    FOREIGN KEY (winner_id) REFERENCES users(id)
);
CREATE TABLE match_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    match_id INT NOT NULL,
    round INT NOT NULL,
    result VARCHAR(10) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id) REFERENCES matches(id)
);
INSERT INTO users (username, password, score, status) VALUES
('player1', 'password1', 10, 'offline'),
('player2', 'password2', 15, 'offline'),
('player3', 'password3', 5, 'offline');
 */



}
