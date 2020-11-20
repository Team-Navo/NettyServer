package Repository;

import org.json.simple.JSONObject;

public class Crewmate {
    //처음 한번만 전송
    String owner; // 캐릭터 주인 아이디
    int maxHP; // 최대 체력
    String name; // 캐릭터 이름
    String color; // 캐릭터 컬러

    //지속적으로 업데이트
    int x; // 캐릭터 x 좌표 Update
    int y; // 캐릭터 y 좌표 Update

    float drmX;
    float drmY;

    int frameNum;

    // 이벤트
    int HP; // 현재 체력


    public Crewmate(String owner){
        this.owner = owner;
        this.x = 150;
        this.y = 150;
        this.name = "성경이";
        this.color = "Red";
        this.maxHP = 10;
        this.HP = 10;
    }

    @SuppressWarnings("unchecked")
    public JSONObject getInitCrewmateJson(){ // 처음 유저가 입장 할 때는 전부 받아야 하니까 전부 출력
        JSONObject result = new JSONObject();

        result.put("owner", owner); //
        result.put("x", x);
        result.put("y", y);
        result.put("name", name); //
        result.put("color", color); //
        result.put("maxHP", maxHP);
        result.put("HP", HP);
        result.put("frameNum", frameNum);
        result.put("drmX", drmX);
        result.put("drmY", drmY);
        return result;
    }
    @SuppressWarnings("unchecked")
    public JSONObject getUpdateCrewmateJson(){ // 지속적으로 업데이트 해야할 정보
        JSONObject result = new JSONObject();

        result.put("x", x);
        result.put("y", y);
        result.put("HP", HP);
        return result;
    }

    public void update(JSONObject requestJson) { // 받은 Json 객체로 업데이트
        double temp = Double.parseDouble(requestJson.get("x").toString());
        this.x = (int)temp;
        temp = Double.parseDouble(requestJson.get("y").toString());
        this.y = (int)temp;
        temp = Double.parseDouble(requestJson.get("maxHP").toString());
        this.maxHP = (int)temp;
        temp = Double.parseDouble(requestJson.get("HP").toString());
        this.HP = (int)temp;

        this.frameNum = Integer.parseInt(requestJson.get("frameNum").toString());

        this.drmX = Float.parseFloat(requestJson.get("drmX").toString());
        this.drmY = Float.parseFloat(requestJson.get("drmY").toString());

        this.name = requestJson.get("name").toString();
        this.color = requestJson.get("color").toString();
    }
}
