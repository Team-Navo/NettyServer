package NavoServer;

import Repository.Crewmate;
import Repository.Room;
import Util.JsonParser;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Parameter;
import java.util.Iterator;

// 초기화, 충돌 체크
public class Event {

    public static void event(ChannelHandlerContext ctx, JSONObject json) throws ParseException {

        JSONObject parentJson = new JSONObject();
        JSONObject childJson = new JSONObject();
        //for test
        System.out.println("Event Received : " + json.toJSONString());
        parentJson.put("Header", "Event");
        parentJson.put("Function", json.get("Function").toString());

        //enter, shoot, hit, exit, changeName, changeColor 구현
        switch (json.get("Function").toString()) {
            case "0": //enter
                enter(ctx, JsonParser.createJson(json.get("Body").toString()), parentJson);
                break;
            case "1": //enter
                break;
            case "2": //changeColor
                changeColor(json, parentJson, Integer.parseInt(json.get("roomCode").toString()));
            case "3": //shoot
            case "4": //logout
                exit(ctx, json, parentJson, Integer.parseInt(json.get("roomCode").toString()));
                break;
            default:
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
            if(crewmate.getOwner().equals(json.get("owner").toString())) {
                crewmate.setColor(json.get("color").toString());
                break;
            }
        }
        parentJson.put("Body",json);
        parentJson.put("roomCode",roomCode);
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString()+"\r\n");
    }
    //들어오는 유저들에게는 내 정보 전송, 모든 유저의 정보 나에게 전송
    public static void enter(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson) {

        //참여 가능한 방 생성 및 있던 유저 정보 저장
        Room room = Room.selectRoom();
        parentJson.put("roomCode", room.getRoomCode());
        parentJson.put("Body",json);
        parentJson.replace("Function","1");
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");

        //for test
        System.out.println("들어온 사람의 정보가 모두에게 : " + parentJson.toJSONString()); // function 1번으로 변경 바람

        //유저 방에 저장
        room.enter(ctx, json);
        JSONObject crewmateJson=new JSONObject();
        //방에 있던 크루메이트들 정보 json으로 저장
        if(!room.getCrewmates().isEmpty())
            for (Crewmate crewmate : room.getCrewmates())
                crewmateJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson());
        else {
            parentJson.put("Body",-1);
        }

        //결과 전송
        parentJson.replace("Body", crewmateJson);
        parentJson.replace("Function","0");
        ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
        //for test
        System.out.println("들어온 사람에게 들어가는 정보 : " + parentJson.toJSONString());

    }

    public static void exit(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);
        System.out.println("삭제할 채널 : "+ctx.channel().id());
        //crewmate 찾아 제거
        System.out.println("삭제된 채널 : "+room.getChannelGroup().remove(ctx.channel()));
//        room.getCrewmates().remove(json.get("owner").toString());
//        room.getChannelGroup().remove(ctx.channel()); //삭제가 잘 되는지 체크하기.
        System.out.println("[LOGOUT] : " + json.get("Body").toString());
        for(Channel ch:room.getChannelGroup()) {
            System.out.println("남은 채널 : "+ch.id());
        }

//        for(Crewmate crewmate:room.getCrewmates()) {
//            if(crewmate.getOwner().equals(json.get("Body").toString()))
//                room.getCrewmates().remove(crewmate);
//        }
        for(int i=0;i<room.getCrewmates().size();i++) {
            if(room.getCrewmates().get(i).getOwner().equals(json.get("Body").toString()))
                room.getCrewmates().remove(i);
        }
        parentJson.put("Body", json.get("Body").toString());

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
    }
}
