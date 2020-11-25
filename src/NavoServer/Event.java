package NavoServer;

import Repository.Crewmate;
import Repository.RoomNetty;
import Util.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Event {

    public static void Event(ChannelHandlerContext ctx, JSONObject json, int function, int roomCode) {

        JSONObject parentJson = new JSONObject();
        JSONObject childJson = new JSONObject();
        //for test
        System.out.println("Event Received : "+json.toJSONString());
        parentJson.put("Header","Event");
        parentJson.put("Function",function);
        //enter, shoot, hit, exit, changeName, changeColor 구현
        switch (function) {
            case 0: //shoot
            case 1: //hit
            case 2:
            case 3:
            case 4: //exit
                logout(ctx, json, parentJson, childJson, roomCode);
            default:
                parentJson.replace("Function",-1);
                childJson.put("result","[WRONG] 잘못된 데이터");
                parentJson.put("Body",childJson);
                ctx.writeAndFlush(parentJson.toJSONString()+"\r\n");
                System.out.println("[WRONG] 잘못된 데이터");
        }

//            if(json.get("Function").equals("5")) { //enter, Auth로 옮겼어
////                enter(ctx, json, parentJson, childJson);
//            } else if(json.get("Function").equals("7")) { //shoot, 번호 바꿔야해
//
//            } else if(json.get("Function").equals("8")) { //hit, 번호 바꿔야해
//
//            } else if(json.get("Function").equals("9")) { //Exit
////                logout(ctx, json,parentJson,childJson,roomCode);
//            } else {
//                parentJson.replace("Function","-1");
//                childJson.put("result","[WRONG] 잘못된 데이터");
//                parentJson.put("Body",childJson);
//                ctx.writeAndFlush(parentJson.toJSONString()+"\n");
//                System.out.println("[WRONG] 잘못된 데이터");
//            }
    }

    public static void logout(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, JSONObject childJson, int roomCode) {
        RoomNetty room=RoomNetty.getMyRoom(roomCode);
        System.out.println("[LOGOUT] "+json.get("owner").toString());
        room.getCrewmates().remove(json.get("owner").toString()); //crewmate 찾아 제거
        room.getChannelGroup().remove(ctx.channel());
        childJson.put("owner",json.get("owner").toString());
        parentJson.put("Body",childJson);
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString()+"\r\n");
    }

}
