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

    public static void event(ChannelHandlerContext ctx, JSONObject json) {

        JSONObject parentJson = new JSONObject();
        JSONObject childJson = new JSONObject();

        parentJson.put("Header", "Event");
        parentJson.put("Function", json.get("Function").toString());

        //enter, shoot, hit, exit, changeName, changeColor
        switch (json.get("Function").toString()) {
            case "0": //enter
                enter(ctx, parentJson, (JSONObject)json.get("Body"));
                break;
            case "1":
                break;
            case "2": //changeColor
                changeColor(parentJson, (JSONObject)json.get("Body"), Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "3": //shoot
                shoot(parentJson, (JSONObject)json.get("Body"), Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "4": //exit
                exit(ctx, json, parentJson, Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "5": //changeSuper
                changeSuper(parentJson, json.get("Super").toString(), Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "6": //startGame
                startGame(parentJson, Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "7": //changeGun
                changeGun(parentJson, (JSONObject)json.get("Body"), Integer.parseInt(json.get("roomCode").toString()));
                break;
            default:
                childJson.put("result", "-1");
                parentJson.put("Body", childJson);
                ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
                System.out.println("[WRONG] 잘못된 데이터");
                break;
        }
    }

    // startGame 방장이 게임 시작
    public static void startGame(JSONObject parentJson, int roomCode) {
        System.out.println("startGame : " + parentJson.toJSONString());
        Room.getRoomByCode(roomCode).getChannelGroup().writeAndFlush(parentJson.toJSONString()+"\r\n");
    }

    // crewmate 접속 정보
    public static void enter(ChannelHandlerContext ctx, JSONObject parentJson, JSONObject childJson) {

        //참여 가능한 room 생성
        Room room = Room.selectRoom(childJson.get("owner").toString());

        parentJson.replace("Function", "1");
        parentJson.put("roomCode", room.getRoomCode());

        childJson.put("Super", room.getSuper());
        parentJson.put("Body", childJson);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
        System.out.println("새 crewmate의 정보가 모두에게 : " + parentJson.toJSONString());

        //room 에 crewmate 저장
        room.enter(ctx, childJson);
        JSONObject crewmateJson = new JSONObject();

        //room 에 접속해 있던 crewmate 들의 정보 저장
        if(!room.getCrewmates().isEmpty()) {
            for (Crewmate crewmate : room.getCrewmates())
                crewmateJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson());
        } else {
            parentJson.put("Body", "-1");
        }

        // 새 crewmate에게 결과 전송
        parentJson.replace("Function","0");
        parentJson.replace("Body", crewmateJson);
        parentJson.put("Super", room.getSuper());

        ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
        System.out.println("새 crewmate에게 전달되는 정보 : " + parentJson.toJSONString());

    }

    // changeSuper 방장 변경
    public static void changeSuper(JSONObject parentJson, String Super, int roomCode) {

        Room.getRoomByCode(roomCode).setSuper(Super);

        parentJson.put("roomCode",roomCode);
        parentJson.put("Super", Room.getRoomByCode(roomCode).getSuper());

        Room.getRoomByCode(roomCode).getChannelGroup().writeAndFlush(parentJson.toJSONString()+"\r\n");
    }

    // changeGun 총 변경
    public static void changeGun(JSONObject parentJson, JSONObject json, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        for(Crewmate crewmate : room.getCrewmates()) {
            if(crewmate.getOwner().equals(json.get("owner").toString())) {
                crewmate.setGun(json.get("gun").toString());
                break;
            }
        }

        parentJson.put("roomCode", roomCode);
        parentJson.put("Body", json);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n"); // 다른 crewmates 에게 전송
    }

    // changeColor crewmate 색 변경
    public static void changeColor(JSONObject parentJson, JSONObject json, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        for(Crewmate crewmate : room.getCrewmates()) {
            if(crewmate.getOwner().equals(json.get("owner").toString())) {
                crewmate.setColor(json.get("color").toString());
                break;
            }
        }

        parentJson.put("roomCode", roomCode);
        parentJson.put("Body", json);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
    }

    // shoot 총알
    public static void shoot(JSONObject parentJson, JSONObject json, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        parentJson.put("Body", json);
        parentJson.put("roomCode", roomCode);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n"); // 다른 crewmates 에게 전송
    }

    // exit
    public static void exit(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        System.out.println("삭제할 채널 : " + ctx.channel().id());
        System.out.println("채널 삭제 여부 : " + room.getChannelGroup().remove(ctx.channel()));
        System.out.println("[EXIT] : " + json.get("Body").toString());

        for(Channel ch : room.getChannelGroup())
            System.out.println("남은 채널 : " + ch.id());

        // room에서 crewmate 삭제
        for(int i = 0; i < room.getCrewmates().size(); i++) {
            if(room.getCrewmates().get(i).getOwner().equals(json.get("Body").toString()))
                room.getCrewmates().remove(i);
        }

        // 방장 변경
        if(json.get("Body").toString().equals(room.getSuper())) {
            room.getCrewmates().get(0).makeSuper();
        }

        parentJson.put("Body", json.get("Body").toString());
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
    }
}
