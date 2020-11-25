package NavoServer;

import Repository.Crewmate;
import Repository.RoomNetty;
import Util.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

// crewmates 변동 사항 업데이트
public class Update {
    private static JSONObject parentJson;
    private static JSONObject childJson;

    public Update(ChannelHandlerContext ctx, JSONObject json, int function) {
        parentJson = new JSONObject();
        childJson = new JSONObject();
        // for test
        System.out.println("Update Received : " + json);

        parentJson.put("Header", "Update");
        parentJson.put("Function", function);

        switch(function) {
            case 0: // update
                update(ctx, json);
                break;
            default:
                parentJson.replace("Function", "-1");
                childJson.put("result", "-1");
                parentJson.put("Body", childJson);

                ctx.writeAndFlush(parentJson + "\n");
                System.out.println("[WRONG] 잘못된 데이터" + "\n");
                break;
        }

        /*
        try {
            JSONObject json = JsonParser.createJson(msg);
            childJson.put("Function", json.get("Function"));

            if(json.get("Function").equals("6")) {
                update(ctx, json);
            } else {
                childJson.replace("Function", "-1");
                childJson.put("result", "[WRONG] 잘못된 데이터");
                parentJson.put("Body", childJson);
                ctx.writeAndFlush(parentJson.toJSONString() + "\n");
                System.out.println("[WRONG] 잘못된 데이터" + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
                ctx.close();
        }
        */
    }

    public static void update(ChannelHandlerContext ctx, JSONObject json) {

        RoomNetty room = RoomNetty.getRoomCode(Integer.parseInt(json.get("Code").toString()));
        childJson.put("code", room.getRoomCode());

        try {
            room.update(JsonParser.createJson(json.get("json").toString())); // 변경 사항 업데이트
        } catch(ParseException e) {
            e.printStackTrace();
        }

        for(Crewmate crewmate : room.getCrewmates()) {
            childJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getUpdateCrewmateJson());
        }
        childJson.put("crewmates_size", room.getCrewmates().size());
        parentJson.put("Body", childJson);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
        ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
    }

    /*
    public static void update(ChannelHandlerContext ctx, JSONObject json) { // 각 클라이언트로부터 전달받은 내용 저장

        System.out.println("updateReceive " + json.toJSONString());
        RoomNetty room = RoomNetty.getMyRoom(Integer.parseInt(json.get("code").toString()));
        header = new JSONObject();
        body = new JSONObject();
        header.put("Header", "InGame");

        JSONObject roomInfoJson = new JSONObject();
        JSONObject crewmatesJson = new JSONObject();
        roomInfoJson.put("code", room.getRoomCode());
        try {
//            room.update(JsonParser.createJson(json.get("update").toString()));
            room.update(JsonParser.createJson(json.get("crewmate").toString()));
        } catch(ParseException e) {
            e.printStackTrace();
        }

        roomInfoJson.put("code", room.getRoomCode());
        for(Crewmate crewmate : room.getCrewmates())
            crewmatesJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson());

        crewmatesJson.put("crewmates_size", room.getCrewmates().size());
        roomInfoJson.put("crewmates", crewmatesJson);

        body.put("update",roomInfoJson);
        header.put("Body",body);
        room.getChannelGroup().writeAndFlush(header.toJSONString()+"\r\n");
        ctx.writeAndFlush(header.toJSONString() + "\r\n");
    }
    */
}