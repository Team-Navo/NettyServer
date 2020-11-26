package Repository;

import org.json.simple.JSONObject;

public class Crewmate {
    //처음 한번만 전송
    String owner; // 캐릭터 주인 아이디
    String name; // 캐릭터 이름
    String color; // 캐릭터 컬러
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

    //to json
//    public Crewmate(JSONObject json) {
//        this.owner=json.get("owner").toString();
//        this.name=json.get("name").toString();
//        this.color=json.get("color").toString();
//
//        this.x=(int)Double.parseDouble(json.get("x").toString());
//        this.y=(int)Double.parseDouble(json.get("y").toString());
//        this.drmX=Float.parseFloat(json.get("drmX").toString());
//        this.drmY=Float.parseFloat(json.get("drmY").toString());
//        this.maxHP=(int)Double.parseDouble(json.get("maxHP").toString());
//        this.HP=(int)Double.parseDouble(json.get("HP").toString());
//        this.frameNum=Integer.parseInt(json.get("frameNum").toString());
//    }
    public Crewmate(JSONObject json) {
        this.owner=json.get("owner").toString();
        this.name=json.get("name").toString();
        this.color="Blue";

        this.x=0;
        this.y=0;
        this.drmX=0;
        this.drmY=0;
        this.maxHP=10;
        this.HP=10;
        this.frameNum=0;
    }



    @SuppressWarnings("unchecked")
    public JSONObject getInitCrewmateJson(){ // 처음 유저가 입장 할 때는 전부 받아야 하니까 전부 출력
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
    public JSONObject getUpdateCrewmateJson(){ // 지속적으로 업데이트 해야 할 정보
        JSONObject result = new JSONObject();

        result.put("owner",owner);
        result.put("x", x);
        result.put("y", y);
        result.put("drmX",drmX);
        result.put("drmY",drmY);
        result.put("HP", HP);
        result.put("frameNum",frameNum);

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
