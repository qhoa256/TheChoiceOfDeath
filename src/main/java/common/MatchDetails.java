package common;

import java.io.Serializable;
import java.sql.Timestamp;

public class MatchDetails implements Serializable {
    private int id;
    private int matchId;
    private int round;
    private String result;
    private Timestamp time;

    public MatchDetails(int id, int matchId, int round, String result, Timestamp time) {
        this.id = id;
        this.matchId = matchId;
        this.round = round;
        this.result = result;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

}
