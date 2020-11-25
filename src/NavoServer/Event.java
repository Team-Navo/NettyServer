package dev.game.netty.server;

import dev.game.netty.Util.JsonParser;
import dev.game.netty.repository.Crewmate;
import dev.game.netty.repository.RoomNetty;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class Event {
    private static JSONObject header;
    private static JSONObject body;

    public static void Event(ChannelHandlerContext ctx, String msg) {
        try {
            System.out.println(msg);
            JSONObject json = JsonParser.createJson(msg);
            header = new JSONObject();
            body = new JSONObject();

            header.put("Header", "Event");
            body.put("Function", json.get("Function"));
            //enter, shoot, hit, exit, changeName, changeColor 구현
            if(json.get("Function").equals("5")) { //enter
                enter(ctx, json);
            } else if(json.get("Function").equals("7")) { //shoot

            } else if(json.get("Function").equals("8")) { //hit

            } else if(json.get("Function").equals("9")) { //Exit
                logout(ctx, json);
            } else {
                body.replace("Function","-1");
                body.put("result","[WRONG] 잘못된 데이터");
                header.put("Body",body);

                ctx.writeAndFlush(header.toJSONString()+"\n");
                System.out.println("[WRONG] 잘못된 데이터");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enter(ChannelHandlerContext ctx, JSONObject json) throws ParseException {
        RoomNetty room = RoomNetty.getRoom();
        room.enter(ctx, JsonParser.createJson(json.get("crewmate").toString()));

        JSONObject crewmatesJson = new JSONObject();
        body.put("code", room.getRoomCode());

        for (Crewmate crewmate : room.getCrewmates())
            crewmatesJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson());

        body.put("crewmates", crewmatesJson); //?
        header.put("Body", body);

        System.out.println("enter : "+header.toJSONString());
        ctx.writeAndFlush(header.toJSONString() + "\r\n");
    }

    public static void logout(ChannelHandlerContext ctx, JSONObject json) {
        RoomNetty room=RoomNetty.getMyRoom(Integer.parseInt(json.get("code").toString()));
        System.out.println("[LOGOUT]");
        room.getCrewmates().remove(json.get("owner").toString()); //crewmate 찾아 제거
        room.getChannelGroup().remove(ctx.channel());
        body.put("owner",json.get("owner").toString());
        header.put("Body",body);
        room.getChannelGroup().writeAndFlush(header.toJSONString()+"\r\n");
    }

}
