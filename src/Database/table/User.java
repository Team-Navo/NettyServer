package dev.game.netty.database.table;

public class User {
    private String id;
    private String pw;
    private String name;
    private String birth;
    private String phone;

    //Setter
    public void setName(String name) { this.name = name; }
    public void setBirth(String birth) { this.birth = birth; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setId(String id) { this.id = id; }
    public void setPw(String pw) { this.pw = pw; }

    //Getter
    public String getName() { return name; }
    public String getBirth() { return birth; }
    public String getPhone() { return phone; }
    public String getId() { return id; }
    public String getPw() { return pw; }
}
