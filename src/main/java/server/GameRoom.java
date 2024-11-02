package server;

import common.Message;
import common.User;
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
    public GameRoom(ClientHandler player1, ClientHandler player2, DatabaseManager dbManager) throws SQLException {
        this.dbManager = dbManager;
        this.matchId = dbManager.saveMatch(player1.getUser().getId(), player2.getUser().getId(), 0);
        this.player1Score = 0;
        this.player2Score = 0;
        this.currentRound = 1;

        randMatrix();
        // Random chọn người sút và người bắt
        this.player1Handler = player1;
        this.player2Handler = player2;
    }
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
            String[] playerNames = {
                player1Handler.getUser().getUsername(),
                player2Handler.getUser().getUsername()
            };
            player1Handler.sendMessage(new Message("match_start", playerNames));
            player2Handler.sendMessage(new Message("match_start", playerNames));
            requestNextMoveP1();
            player2Handler.sendMessage(new Message("disable_all", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestNextMoveP1() {
        try {
            if ((isPlayer1Dead && isPlayer2Dead ) || player1Score == 3 || player2Score == 3) {
                endMatch();
                return;
            }
            player1Handler.sendMessage(new Message("your_turn", "P1"));
            player2Handler.sendMessage(new Message("disable_all", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void requestNextMoveP2() {
        try {
            if ((isPlayer1Dead && isPlayer2Dead ) || player1Score == 3 || player2Score == 3) {
                endMatch();
                return;
            }
            player2Handler.sendMessage(new Message("your_turn", "P2"));
            player1Handler.sendMessage(new Message("disable_all", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Khi nguoi choi chon o Danger ngay dau tien, ngay lap tuc mat ket noi
    public synchronized void handleChoice(String choosingBox, ClientHandler player) throws SQLException, IOException{
        //Xu ly
        System.out.println(choosingBox);
        int pos1 = Integer.parseInt(String.valueOf(choosingBox.charAt(6)));
        int pos2 = Integer.parseInt(String.valueOf(choosingBox.charAt(8)));
        System.out.println(pos1 + " " + pos2);
        if(pos1 == 1){
            int value = matrixPlayer1[(pos2 - 1)/2][(pos2 - 1)%2];
            if(value == 1){
                player1Score++;
                System.out.println("SAFE");
                player1Handler.sendMessage(new Message("safe", "P1" + " " + choosingBox));
                player2Handler.sendMessage(new Message("safe", "P1" + " " + choosingBox));
                if(!isPlayer2Dead)
                    requestNextMoveP2();
                else
                    requestNextMoveP1();
            } else {
                isPlayer1Dead = true;
                System.out.println("Danger");
                player1Handler.sendMessage(new Message("danger", "P1" + " " + choosingBox));
                player2Handler.sendMessage(new Message("danger", "P1" + " " + choosingBox));
                requestNextMoveP2();
            }
        } else {
            int value = matrixPlayer2[(pos2 - 1)/2][(pos2 - 1)%2];
            if(value == 1){
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
//        if ((isPlayer1Dead && isPlayer2Dead )|| player1Score == 3 || player2Score == 3) {
//           determineWinner();
//        }
    }

    private void determineWinner() throws SQLException, IOException {
        int winnerId = 0;
        String resultMessage = "";
        if (player1Score > player2Score) {
            winnerId = player1Handler.getUser().getId();
            resultMessage = player1Handler.getUser().getUsername() + " thắng trận đấu!";
        } else if (player2Score > player1Score) {
            winnerId = player2Handler.getUser().getId();
            resultMessage = player2Handler.getUser().getUsername() + " thắng trận đấu!";
        } else {
            resultMessage = "Trận đấu hòa!";
        }
        if (winnerId != 0) {
            dbManager.updateUserScore(winnerId, 3);
            dbManager.updateMatchWinner(matchId, winnerId);
        } else {
            // Nếu hòa, cập nhật winnerId là 0
            dbManager.updateMatchWinner(matchId, 0);
            dbManager.updateUserScore(player1Handler.getUser().getId(), 1);
            dbManager.updateUserScore(player2Handler.getUser().getId(), 1);
        }
        // Yêu cầu chơi lại nếu cả hai đồng ý
        player1Handler.sendMessage(new Message("play_again_request", "Bạn có muốn chơi lại không?"));
        player2Handler.sendMessage(new Message("play_again_request", "Bạn có muốn chơi lại không?"));


        // Thông báo kết quả trận đấu cho cả hai người chơi
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
                player1Handler.sendMessage(new Message("match_start", "Trận đấu mới bắt đầu!"));
                player2Handler.sendMessage(new Message("match_start", "Trận đấu mới bắt đầu!"));
                requestNextMoveP1();
                player2Handler.sendMessage(new Message("disable_all", null));
            } else {
                // Một trong hai hoặc cả hai không đồng ý chơi lại
                if (!player1WantsRematch) {
                    player1Handler.sendMessage(new Message("rematch_declined", "Bạn đã không đồng ý chơi lại."));
                    player2Handler.sendMessage(new Message("rematch_declined", "Đối thủ đã không đồng ý chơi lại."));
                } else {
                    player1Handler.sendMessage(new Message("rematch_declined", "Đối thủ đã không đồng ý chơi lại."));
                    player2Handler.sendMessage(new Message("rematch_declined", "Bạn đã không đồng ý chơi lại."));
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

    public void handlePlayerDisconnect(ClientHandler disconnectedPlayer) throws SQLException, IOException {
        String resultMessage = "";
        int winnerId = 0;

        if (disconnectedPlayer == player1Handler) {
            winnerId = player2Handler.getUser().getId();
            resultMessage = "Đối thủ đã thoát. Bạn thắng trận đấu!";
            player2Handler.sendMessage(new Message("match_end", resultMessage));
        } else if (disconnectedPlayer == player2Handler) {
            winnerId = player1Handler.getUser().getId();
            resultMessage = "Đối thủ đã thoát. Bạn thắng trận đấu!";
            player1Handler.sendMessage(new Message("match_end", resultMessage));
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
            resultMessage = "Đối thủ đã thoát. Bạn thắng trận đấu!";
            player2Handler.sendMessage(new Message("match_end", resultMessage));
        } else if (quittingPlayer == player2Handler) {
            winnerId = player1Handler.getUser().getId();
            resultMessage = "Đối thủ đã thoát. Bạn thắng trận đấu!";
            player1Handler.sendMessage(new Message("match_end", resultMessage));
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
