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
            case 0: //logout
                logout(ctx, json, parentJson, childJson, roomCode);
                break;
            case 1:
            case 2: //shoot
            case 3:
            case 4: //enter
                enter(ctx, json, parentJson, childJson);
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

    //들어오는 유저들에게는 내 정보 전송, 모든 유저의 정보 나에게 전송
    public static void enter(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, JSONObject childJson) {

        //참여 가능한 방 생성 및 있던 유저 정보 저장
        Room room = Room.selectRoom();
        parentJson.put("roomCode", room.getRoomCode());
        parentJson.put("Body",json);
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");

        //for test
        System.out.println("들어온 사람의 정보가 모두에게 : " + parentJson.toJSONString()); // function 1번으로 변경 바람

        //방에 있던 크루메이트들 정보 json으로 저장
        if(!room.getCrewmates().isEmpty())
            for (Crewmate crewmate : room.getCrewmates())
                childJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson());
        else {
            parentJson.put("Body",-1);
        }

        //유저 방에 저장
        room.enter(ctx, json);

        //결과 전송
        parentJson.replace("Body", childJson);
        ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
        //for test
        System.out.println("들어온 사람에게 들어가는 정보 : " + parentJson.toJSONString());

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
