package DTO;

public class UserData {
    private int id;
    private String name;
    private String email;
    private String password;
    private long score;
    private boolean is_available;
    private boolean is_onGame;

    public UserData(int id, String name, String email, String password, long score, boolean is_available, boolean is_onGame) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.score = score;
        this.is_available = is_available;
        this.is_onGame = is_onGame;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
    
    public void updateScore() {
        this.score += 1;
    }

    public boolean getIs_available() {
        return is_available;
    }

    public void setIs_available(boolean is_available) {
        this.is_available = is_available;
    }

    public boolean getIs_onGame() {
        return is_onGame;
    }

    public void setIs_onGame(boolean is_onGame) {
        this.is_onGame = is_onGame;
    }
    
}
