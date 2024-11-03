package server;

import common.Message;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class GameRoom {
    private ClientHandler player1Handler;
    private ClientHandler player2Handler;
    private DatabaseManager dbManager;
    private int matchId;
    private int player1Score;
    private int player2Score;
    private int currentRound;
    private boolean isPlayer1Dead = false;
    private boolean isPlayer2Dead = false;
    private int boxPlayer1 = 0;
    private int boxPlayer2 = 0;
    private Boolean player1WantsRematch = null;
    private Boolean player2WantsRematch = null;
    private int[][] matrixPlayer1 = new int[3][2];
    private int[][] matrixPlayer2 = new int[3][2];
    private boolean turn1 = false;
    private boolean turn2 = true;
    public GameRoom(ClientHandler player1, ClientHandler player2, DatabaseManager dbManager) throws SQLException {
        this.dbManager = dbManager;
        this.matchId = dbManager.saveMatch(player1.getUser().getId(), player2.getUser().getId(), 0);
        this.player1Score = 0;
        this.player2Score = 0;
        this.currentRound = 1;

        randMatrix();
        this.player1Handler = player1;
        this.player2Handler = player2;
    }
    //Tạo ma trận 3x2 ngẫu nhiên, mỗi phần tử nhận 1 trong 2 gi trị.
    public void randMatrix(){
        //1 nguy hiem, 0 an toan
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            int onePosition = random.nextInt(2);
            for (int j = 0; j < 2; j++) {
                if (j == onePosition) {
                    matrixPlayer1[i][j] = 1;
                } else {
                    matrixPlayer1[i][j] = 0;
                }
            }
            int twoPosition = random.nextInt(2);
            for (int j = 0; j < 2; j++) {
                if (j == twoPosition) {
                    matrixPlayer2[i][j] = 1;
                } else {
                    matrixPlayer2[i][j] = 0;
                }
            }
        }
    }
    public void startMatch() {
        try {
            //Gửi thông tin tên người chơi để hiển thị ở GameRoom
            String[] playerNames = {
                    player1Handler.getUser().getUsername(),
                    player2Handler.getUser().getUsername()
            };
            player1Handler.sendMessage(new Message("match_start", playerNames));
            player2Handler.sendMessage(new Message("match_start", playerNames));
            //Đánh dấu đây là lượt của người chơi thứ 1
            turn1 = false;
            turn2 = true;
            player2Handler.sendMessage(new Message("disable_all", null));
            requestNextMoveP1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Nước di chuyển của người thứ 1
    private void requestNextMoveP1() {
        try {
            //kiểm tra các điều kiện kết thúc
            if(isPlayer2Dead && isPlayer1Dead){
                endMatch();
                return;
            }
            if(player1Score == 3 && isPlayer2Dead){
                endMatch();
                return;
            }
            if(player2Score == 3 && isPlayer1Dead){
                endMatch();
                return;
            }
            if(player2Score == 3 && player1Score == 3){
                endMatch();
                return;
            }
            //Lượt của người thứ 1 thì người thứ 2 khng được bấm gì
            player1Handler.sendMessage(new Message("your_turn", "P1"));
            player2Handler.sendMessage(new Message("disable_all", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Lượt người thứ 2, tương tự
    private void requestNextMoveP2() {
        try {
            if(isPlayer2Dead && isPlayer1Dead){
                endMatch();
                return;
            }
            if(player1Score == 3 && isPlayer2Dead){
                endMatch();
                return;
            }
            if(player2Score == 3 && isPlayer1Dead){
                endMatch();
                return;
            }
            if(player2Score == 3 && player1Score == 3){
                endMatch();
                return;
            }
            player2Handler.sendMessage(new Message("your_turn", "P2"));
            player1Handler.sendMessage(new Message("disable_all", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Xử lý chọn ô
    public synchronized void handleChoice(String choosingBox, ClientHandler player) throws SQLException, IOException{
        //Xu ly
        System.out.println(choosingBox);
        //Laasy vị tr button và đối chiếu với vị trí trong bảng.
        String[] parts = choosingBox.split("_");
        int pos1 = Integer.parseInt(parts[0].replace("button", "")); // Lấy x
        int pos2 = Integer.parseInt(parts[1]); // Lấy y
        System.out.println(pos1 + " " + pos2);
        //Nếu là người thứ 1 chọn
        if(pos1 == 1){
            System.out.print(player1Handler.getUser().getUsername() + ": ");
            System.out.print((pos2 - 1) / 2 + " " + (pos2 - 1) % 2);
            int value = matrixPlayer1[(pos2 - 1)/2][(pos2 - 1)%2];
            System.out.println(" " + value);
            //Nếu ô chọn an toàn
            if(value == 0){
                player1Score++;
                System.out.println("SAFE");
                player1Handler.sendMessage(new Message("safe", "P1" + " " + choosingBox));
                player2Handler.sendMessage(new Message("safe", "P1" + " " + choosingBox));
            } else {
                //ô chọn nguy hiểm
                isPlayer1Dead = true;
                System.out.println("Danger");
                player1Handler.sendMessage(new Message("danger", "P1" + " " + choosingBox));
                player2Handler.sendMessage(new Message("danger", "P1" + " " + choosingBox));
            }
        } else {
            //Nếu là người thứ 2 chọn, tương tự.
            System.out.print(player2Handler.getUser().getUsername() + ": ");
            System.out.print((pos2 - 1) / 2 + " " + (pos2 - 1) % 2);
            int value = matrixPlayer2[(pos2 - 1)/2][(pos2 - 1)%2];
            System.out.println(" " + value);
            if(value == 0){
                player2Score++;
                System.out.println("SAFE");
                player1Handler.sendMessage(new Message("safe", "P2" + " " + choosingBox));
                player2Handler.sendMessage(new Message("safe", "P2" + " " + choosingBox));
                if(!isPlayer1Dead)
                    requestNextMoveP1();
                else
                    requestNextMoveP2();
            } else {
                isPlayer2Dead = true;
                System.out.println("Danger");
                player1Handler.sendMessage(new Message("danger", "P2" + " " + choosingBox));
                player2Handler.sendMessage(new Message("danger", "P2" + " " + choosingBox));
                requestNextMoveP1();
            }
        }
        //kiểm tra điều kiện kết thúc
//        if ((isPlayer1Dead && isPlayer2Dead )|| player1Score == 3 || player2Score == 3) {
//            determineWinner();
//        }
//        else{
        //Nếu người 2 vừa đi và người 1 chưa chết thì đến lượt người 1
        if (turn1 && !isPlayer1Dead){
            turn1 = false;
            turn2 = true;
            requestNextMoveP1();
        }
        //Nếu người 1 vừa đi và người 2 chưa chết...
        else if (turn2 && !isPlayer2Dead){
            turn1 = true;
            turn2 = false;
            requestNextMoveP2();
        }
        //Nếu người 2 vừa đi và người 1 đã chết thì người 2 chơi tiếp
        else if (turn1 && isPlayer1Dead){
            turn1 = false;
            turn2 = true;
            requestNextMoveP2();
        }
        //Nếu người 1 vừa đi và nười 2 đã chết...
        else if (turn2 && isPlayer2Dead){
            turn1 = true;
            turn2 = false;
            requestNextMoveP1();
        }
        // }
    }
    //Tổng kết kết quả
    private void determineWinner() throws SQLException, IOException {
        //winner dùng để lưu id nguwfoi thắng. Nếu hoà thì id sẽ  = 0 và import vào trong csdl giá trị là null
        int winnerId = 0;
        String resultMessage = "";
        System.out.println(player1Score + " " + player2Score);
        if (player1Score > player2Score) {
            winnerId = player1Handler.getUser().getId();
            resultMessage = player1Handler.getUser().getUsername() + " thắng trận đấu!";
        } else if (player2Score > player1Score) {
            winnerId = player2Handler.getUser().getId();
            resultMessage = player2Handler.getUser().getUsername() + " thắng trận đấu!";
        } else {
            resultMessage = "Trận đấu hòa!";
        }
        System.out.println(resultMessage);
        if (winnerId != 0) {
            dbManager.updateUserScore(winnerId, 3);
            dbManager.updateMatchWinner(matchId, winnerId);
        } else {
            // Nếu hòa, cập nhật winnerId là 0
            dbManager.updateMatchWinner(matchId, 0);
            dbManager.updateUserScore(player1Handler.getUser().getId(), 1);
            dbManager.updateUserScore(player2Handler.getUser().getId(), 1);
        }

        player1Handler.sendMessage(new Message("match_end", resultMessage));
        player2Handler.sendMessage(new Message("match_end", resultMessage));

    }

    // Xử lý yêu cầu chơi lại
    public synchronized void handlePlayAgainResponse(boolean playAgain, ClientHandler responder) throws SQLException, IOException {
        if (responder == player1Handler) {
            player1WantsRematch = playAgain;
        } else if (responder == player2Handler) {
            player2WantsRematch = playAgain;
        }

        // Kiểm tra nếu cả hai người chơi đã phản hồi
        if (player1WantsRematch != null && player2WantsRematch != null) {
            if (player1WantsRematch && player2WantsRematch) {
                player1Score = 0;
                player2Score = 0;
                currentRound = 1;
                isPlayer1Dead = false;
                isPlayer2Dead = false;
                player1WantsRematch = null;
                player2WantsRematch = null;
                randMatrix();
                String[] playername = {player1Handler.getUser().getUsername(), player2Handler.getUser().getUsername()};
                player1Handler.sendMessage(new Message("match_start", playername));
                player2Handler.sendMessage(new Message("match_start", playername));
                turn1 = false;
                turn2 = true;
                requestNextMoveP1();
                player2Handler.sendMessage(new Message("disable_all", null));
            } else {
                // Một trong hai hoặc cả hai không đồng ý chơi lại
                if (!player1WantsRematch) {
                    player1Handler.sendMessage(new Message("rematch_declined", "Ngươi đã từ chối chơi lại."));
                    player2Handler.sendMessage(new Message("rematch_declined", "Đối thủ đã không đồng ý chơi lại."));
                } else {
                    player1Handler.sendMessage(new Message("rematch_declined", "Đối thủ đã không đồng ý chơi lại."));
                    player2Handler.sendMessage(new Message("rematch_declined", "Ngươi đã từ chối chơi lại."));
                }
                // Đặt lại biến
                player1WantsRematch = null;
                player2WantsRematch = null;
            }
        }
    }

    // Đảm bảo rằng phương thức endMatch() tồn tại và được định nghĩa chính xác
    private void endMatch() throws SQLException, IOException {
        determineWinner();
    }
    public void handleTimeOut(ClientHandler clientHandler) throws IOException, SQLException {
        if(clientHandler == player1Handler){
            player1Handler.sendMessage(new Message("match_end", "Ngươi đã thua do mất quá nhiều thời gian."));
            player2Handler.sendMessage(new Message("match_end", "Ngươi đã thắng do đối thủ hết thời gian."));
            int winnerId = player2Handler.getUser().getId();
            dbManager.updateUserScore(winnerId, 3);
            dbManager.updateMatchWinner(matchId, winnerId);
        } else {
            player2Handler.sendMessage(new Message("match_end", "Ngươi đã thua do mất quá nhiều thời gian."));
            player1Handler.sendMessage(new Message("match_end", "Ngươi đã thắng do đối thủ hết thời gian."));
            int winnerId = player1Handler.getUser().getId();
            dbManager.updateUserScore(winnerId, 3);
            dbManager.updateMatchWinner(matchId, winnerId);
        }

    }

    public void handlePlayerDisconnect(ClientHandler disconnectedPlayer) throws SQLException, IOException {
        String resultMessage = "";
        int winnerId = 0;

        if (disconnectedPlayer == player1Handler) {
            winnerId = player2Handler.getUser().getId();
            resultMessage = "Đối thủ đã mất tích. Ngươi đã thắng trận đấu!";
            player2Handler.sendMessage(new Message("match_end_left", resultMessage));
        } else if (disconnectedPlayer == player2Handler) {
            winnerId = player1Handler.getUser().getId();
            resultMessage = "Đối thủ đã mất tích. Ngươi đã thắng trận đấu!";
            player1Handler.sendMessage(new Message("match_end_left", resultMessage));
        }

        if (winnerId != 0) {
            dbManager.updateUserScore(winnerId, 3);
            dbManager.updateMatchWinner(matchId, winnerId);
        }

        // Sử dụng phương thức clearGameRoom() để đặt gameRoom thành null
        if (player1Handler != null) player1Handler.clearGameRoom();
        if (player2Handler != null) player2Handler.clearGameRoom();
    }

    public void handlePlayerQuit(ClientHandler quittingPlayer) throws SQLException, IOException {
        String resultMessage = "";
        int winnerId = 0;

        if (quittingPlayer == player1Handler) {
            winnerId = player2Handler.getUser().getId();
            resultMessage = "Đối thủ đã mất tích. Ngươi đã thắng trận đấu!";
            player2Handler.sendMessage(new Message("match_end_left", resultMessage));
        } else if (quittingPlayer == player2Handler) {
            winnerId = player1Handler.getUser().getId();
            resultMessage = "Đối thủ đã mất tích. Ngươi đã thắng trận đấu!";
            player1Handler.sendMessage(new Message("match_end_left", resultMessage));
        }

        if (winnerId != 0) {
            dbManager.updateUserScore(winnerId, 3);
            dbManager.updateMatchWinner(matchId, winnerId);
        }

        // Sử dụng phương thức clearGameRoom() để đặt gameRoom thành null
        if (player1Handler != null) player1Handler.clearGameRoom();
        if (player2Handler != null) player2Handler.clearGameRoom();
    }
}