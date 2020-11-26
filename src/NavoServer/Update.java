package NavoServer;

import Repository.Crewmate;
import Repository.RoomNetty;
import Util.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

// crewmates 변동 사항 업데이트
public class Update {

    public Update(ChannelHandlerContext ctx, JSONObject json, int function, int roomCode) {
        JSONObject parentJson = new JSONObject();
        JSONObject childJson = new JSONObject();
        // for test
        System.out.println("Update Received : " + json);

        parentJson.put("Header", "Update"); //?
        parentJson.put("Function", function);

        switch(function) {
            case 0: // update
                update(json, parentJson, childJson, roomCode);
                break;
            default:
                parentJson.replace("Function", -1);
                childJson.put("result", -1);
                parentJson.put("Body", childJson);

                ctx.writeAndFlush(parentJson + "\r\n");
                System.out.println("[WRONG] 잘못된 데이터" + "\n");
                break;
        }
    }

    public static void update(JSONObject json, JSONObject parentJson, JSONObject childJson, int roomCode) {

        RoomNetty room = RoomNetty.getRoomByCode(roomCode);
        childJson.put("roomCode", roomCode);

        //?
//        try {
            //room.update(JsonParser.createJson(json.get("crewmates").toString())); // 변경 사항 업데이트
            room.update(json);
//        } catch(ParseException e) {
//            e.printStackTrace();
//        }

        for(Crewmate crewmate : room.getCrewmates()) {
            childJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getUpdateCrewmateJson());
        }
        childJson.put("crewmates_size", room.getCrewmates().size());
        parentJson.put("Body", childJson);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
//        ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
    }
}