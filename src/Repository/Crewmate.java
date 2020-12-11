package Repository;

import io.netty.channel.ChannelId;
import org.json.simple.JSONObject;

public class Crewmate {

    //처음 한번만 전송
    String owner; // 캐릭터 주인 아이디
    String name; // 캐릭터 이름
    String color; // 캐릭터 컬러
    String gun; // 캐릭터 총
    int maxHP; // 최대 체력

    //지속적으로 업데이트
    int x; // 캐릭터 x 좌표 Update
    int y; // 캐릭터 y 좌표 Update

    float drmX;
    float drmY;

    int frameNum;

    // 이벤트
    int HP; // 현재 체력

    int rate;
    boolean Super = false;

    ChannelId id;

    public ChannelId getId() {return id;}

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setColor(String color) { this.color = color; }

    public void makeSuper() { this.Super = true; }


    public Crewmate(JSONObject json, ChannelId id) {
        this.id = id;
        this.owner = json.get("owner").toString();
        this.name = json.get("name").toString();
        this.color = "Blue";

        this.x = 0;
        this.y = 0;
        this.drmX = 0;
        this.drmY = 0;

        this.maxHP = 10;
        this.HP = 10;

        this.frameNum = 0;
    }

    @SuppressWarnings("unchecked")
    // 처음 유저가 입장 할 때는 전부 받아야 하니까 전부 출력
    public JSONObject getInitCrewmateJson(){
        JSONObject result = new JSONObject();

        result.put("owner", owner);
        result.put("name", name);
        result.put("color", color);

        result.put("x", x);
        result.put("y", y);
        result.put("drmX", drmX);
        result.put("drmY", drmY);

        result.put("maxHP", maxHP);
        result.put("HP", HP);

        result.put("frameNum", frameNum);

        return result;
    }

    @SuppressWarnings("unchecked")
    // 지속적으로 업데이트 해야 할 정보
    public JSONObject getUpdateCrewmateJson() {
        JSONObject result = new JSONObject();
        result.put("color",color);
        result.put("owner",owner);

        result.put("x", x);
        result.put("y", y);
        result.put("drmX",drmX);
        result.put("drmY",drmY);

        result.put("HP", HP);

        result.put("frameNum",frameNum);

        return result;
    }

    double temp; // x, y
    // 받은 Json 객체로 업데이트
    public void update(JSONObject requestJson) {
        temp = Double.parseDouble(requestJson.get("x").toString());
        this.x = (int)temp;
        temp = Double.parseDouble(requestJson.get("y").toString());
        this.y = (int)temp;

        this.drmX = Float.parseFloat(requestJson.get("drmX").toString());
        this.drmY = Float.parseFloat(requestJson.get("drmY").toString());

        temp = Double.parseDouble(requestJson.get("HP").toString());
        this.HP = (int)temp;

        this.frameNum = Integer.parseInt(requestJson.get("frameNum").toString());
    }
}
