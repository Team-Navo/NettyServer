package NavoServer;

import Repository.Crewmate;
import Repository.Room;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;

// 초기화, 충돌 체크
public class Event {

    public static void event(ChannelHandlerContext ctx, JSONObject json, int function, int roomCode) {

        JSONObject parentJson = new JSONObject();
        JSONObject childJson = new JSONObject();
        //for test
        System.out.println("Event Received : " + json.toJSONString());
        parentJson.put("Header", "Event");
        parentJson.put("Function", function);

        //enter, shoot, hit, exit, changeName, changeColor 구현
        switch (function) {
            case 0:
                enter(ctx, json, parentJson, childJson);
                break;
            case 1:
            case 2: //shoot
            case 3: //changeColor
                changeColor(json, parentJson, roomCode);
                break;
            case 4:
                logout(ctx, json, parentJson, childJson, roomCode);
                break;
            default:
                parentJson.replace("Function", function);
                childJson.put("result", -1);
                parentJson.put("Body", childJson);
                ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
                System.out.println("[WRONG] 잘못된 데이터");
                break;
        }
    }

    public static void changeColor(JSONObject json, JSONObject parentJson, int roomCode) {
        Room room=Room.getRoomByCode(roomCode);
        for(Crewmate crewmate:room.getCrewmates()) {
            if(crewmate.owner.equals(json.get("owner").toString())) {
                crewmate.color=json.get("color").toString();
                break;
            }
        }
        parentJson.put("Body",json);
        parentJson.put("roomCode",roomCode);
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString()+"\r\n");
    }

    //들어오는 유저들에게는 내 정보 전송, 모든 유저의 정보 나에게 전송
    public static void enter(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, JSONObject childJson) {

        //참여 가능한 방 생성 및 있던 유저 정보 저장
        Room room = Room.selectRoom();
        parentJson.put("roomCode", room.getRoomCode());
        parentJson.put("Body",json);
        parentJson.replace("Function","1");
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");

        //for test
        System.out.println("새 crewmate의 정보가 모두에게 : " + parentJson.toJSONString());

        //유저 방에 저장
        room.enter(ctx, json);

        //방에 있던 크루메이트들 정보 json으로 저장
        if(!room.getCrewmates().isEmpty())
            for (Crewmate crewmate : room.getCrewmates())
                childJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson());
        else {
            parentJson.put("Body","-1");
        }

        //결과 전송
        parentJson.replace("Body", childJson);
        parentJson.replace("Function","0");
        ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
        //for test
        System.out.println("새 crewmate가 받는 정보 : " + parentJson.toJSONString());

    }

    public static void logout(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, JSONObject childJson, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);
        System.out.println("삭제할 채널 : "+ctx.channel().id());
        //crewmate 찾아 제거
        System.out.println("삭제된 채널 : "+room.getChannelGroup().remove(ctx.channel()));
        room.getCrewmates().remove(json.get("owner").toString());
//        room.getChannelGroup().remove(ctx.channel()); //삭제가 잘 되는지 체크하기.
        System.out.println("[LOGOUT] : " + json.get("owner").toString());

        childJson.put("owner", json.get("owner").toString());
        parentJson.put("Body", childJson);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
    }
}
