package NavoServer;

import Repository.Crewmate;
import Repository.Room;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

// crewmates 변동 사항 업데이트
public class Update {

    public static void update(ChannelHandlerContext ctx, JSONObject json, int function, int roomCode) {
        JSONObject parentJson = new JSONObject();
        JSONObject childJson = new JSONObject();
        // for test
        System.out.println("Update Received : " + json);

        parentJson.put("Header", "Update"); //?
        parentJson.put("Function", function);

        switch(function) {
            case 0: // update
                update(json, roomCode, parentJson, childJson);
                break;
            default:
                parentJson.replace("Function", function);
                childJson.put("result", -1);
                parentJson.put("Body", childJson);

                ctx.writeAndFlush(parentJson + "\r\n");
                System.out.println("[WRONG] 잘못된 데이터" + "\n");
                break;
        }
    }

    public static void update(JSONObject json, int roomCode, JSONObject parentJson, JSONObject childJson) {

        childJson.put("roomCode", roomCode);

        Room room = Room.getRoomByCode(roomCode);
        room.update(json);

        for(Crewmate crewmate : room.getCrewmates())
            childJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getUpdateCrewmateJson());

        childJson.put("crewmates_size", room.getCrewmates().size());
        parentJson.put("Body", childJson);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
    }
}