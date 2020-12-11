package NavoServer;

import Repository.Crewmate;
import Repository.Room;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.json.simple.JSONObject;
import java.util.*;

// 초기화, 충돌 체크
public class Event {

    public static void event(ChannelHandlerContext ctx, JSONObject json) {

        JSONObject parentJson = new JSONObject();
        JSONObject childJson = new JSONObject();

        parentJson.put("Header", "Event");
        parentJson.put("Function", json.get("Function").toString());

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
                exit(ctx, parentJson, json.get("Body").toString(), Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "5": //changeSuper
                changeSuper(parentJson, json.get("Super").toString(), Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "6": //startGame
                startGame(parentJson, Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "7": //getItem
                getItem(parentJson, (JSONObject)json.get("Body"), Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "8" : //checkCollision
                checkCollision(parentJson, (JSONObject)json.get("Body"), Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "9" : //deleteCrew
                deleteCrew(ctx, (JSONObject)json.get("Body"), parentJson, Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "10": //broadcastStartItem
                break;
            default:
                childJson.put("result", "-1");
                parentJson.put("Body", childJson);
                ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
                System.out.println("[WRONG] 잘못된 데이터");
                break;
        }
    }

    // crewmate 접속 정보
    public static void enter(ChannelHandlerContext ctx, JSONObject parentJson, JSONObject childJson) {

        //참여 가능한 room 생성
        Room room = Room.selectRoom(childJson.get("owner").toString());

        parentJson.replace("Function", "1");
        parentJson.put("roomCode", room.getRoomCode());

        parentJson.put("Body", childJson);

        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
        System.out.println("새 crewmate의 정보가 모두에게 : " + parentJson.toJSONString());

        //room 에 crewmate 저장
        room.enter(ctx, childJson);
        JSONObject crewmateJson = new JSONObject();

        //room 에 접속해 있던 crewmate 들의 정보 저장
        if(!room.getCrewmates().isEmpty())
            for (Crewmate crewmate : room.getCrewmates())
                crewmateJson.put(room.getCrewmates().indexOf(crewmate), crewmate.getInitCrewmateJson());

        // 새 crewmate에게 결과 전송
        parentJson.replace("Function","0");
        parentJson.replace("Body", crewmateJson);
        parentJson.put("Super", room.getSuper());

        ctx.writeAndFlush(parentJson.toJSONString() + "\r\n");
        System.out.println("새 crewmate에게 전달되는 정보 : " + parentJson.toJSONString());

    }

    private static class Node {
        int x, y;
        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // startGame 방장이 게임 시작
    public static void startGame(JSONObject parentJson, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        Stack<Node> q = new Stack<>();
        q.push(new Node(13,17));
        q.push(new Node(13,641));
        q.push(new Node(13,1246));
        q.push(new Node(812,1249));
        q.push(new Node(1564,1246));
        q.push(new Node(1564,638));
        q.push(new Node(1564,17));
        q.push(new Node(812,17));
        Collections.shuffle(q);

        parentJson.put("x","0");
        parentJson.put("y","0");

        System.out.println("[START GAME] : " + parentJson.toJSONString());
        for(Channel ch:room.getChannelGroup()) {
            Node temp = q.pop();
            parentJson.replace("x", temp.x);
            parentJson.replace("y", temp.y);
            ch.writeAndFlush(parentJson.toJSONString() + "\r\n");
        }

        JSONObject childJson=new JSONObject();
        for(int i=0;i<=19;i++) {
            String temp = null;
            int num=(int)(Math.random()*3+1);
            switch(num) {
                case 1:
                    temp="BLUE";
                    break;
                case 2:
                    temp="RED";
                    break;
                case 3:
                    temp="GREEN";
                    break;
            }

            if(i<8) {
                childJson.put(i,temp);
            }
            else {
                childJson.put(i,num);
            }
        }

        parentJson.clear();
        parentJson.put("Header","Event");
        parentJson.put("Function","10");
        parentJson.put("Body",childJson);
        System.out.println(parentJson);
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString()+"\r\n");

        // 살아있는 crewmates 5명으로 초기화

        room.setAliveCrew(5);
    }

    // changeSuper 방장 변경
    public static void changeSuper(JSONObject parentJson, String Super, int roomCode) {

        Room.getRoomByCode(roomCode).setSuper(Super);

        parentJson.put("roomCode", roomCode);
        parentJson.put("Super", Room.getRoomByCode(roomCode).getSuper());

        Room.getRoomByCode(roomCode).getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
    }

    // changeColor crewmate 색 변경
    public static void changeColor(JSONObject parentJson, JSONObject json, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        for(Crewmate crewmate : room.getCrewmates())
            if(crewmate.getOwner().equals(json.get("owner").toString())) {
                crewmate.setColor(json.get("color").toString());
                break;
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

    // getItem 아이템 획득
    public static void getItem(JSONObject parentJson, JSONObject json, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        parentJson.put("Body", json);
        parentJson.put("roomCode", roomCode);
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
    }

    // checkCollision 충돌 감지+알림
    public static void checkCollision(JSONObject parentJson, JSONObject json, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        parentJson.put("Body", json);
        parentJson.put("roomCode", roomCode);

        for(int i = 0; i < room.getCrewmates().size(); i++) {
            if(!(room.getCrewmates().get(i).getOwner().equals(json.get("owner").toString())))
                room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
        }
    }

    // deleteCrew 죽은 크루메이트를 room 에서 삭제
    public static void deleteCrew(ChannelHandlerContext ctx, JSONObject json, JSONObject parentJson, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);

        // 죽은 crew 와 연결된 채널 삭제
        room.getChannelGroup().remove(ctx.channel());
        System.out.println("[DEAD] : " + json);

        // 죽은 crew 삭제
        for(int i = 0; i < room.getCrewmates().size(); i++) {
            if(room.getCrewmates().get(i).getOwner().equals(json.toString()))
                room.getCrewmates().remove(i);
        }

        // 살아있는 crew =- 1
        room.setAliveCrew(room.getAliveCrew()-1);

        // 살아있는 crew 들에게 죽은 crew 알려주기
        parentJson.put("Body", json);
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");

        // 살아있는 crew 가 1명일 때
        if (room.getAliveCrew() == 1) {
            JSONObject childJson = new JSONObject();
            childJson.put("winner", room.getCrewmates().get(0).getOwner()); // 살아있는 crew 이름
            parentJson.put("Body", childJson);
            // 살아있는 crew 에게 이겼다고 알려주기
            room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
            room.deleteRoom(roomCode);
        }
    }

    // exit
    public static void exit(ChannelHandlerContext ctx, JSONObject parentJson, String owner, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);
        System.out.println("삭제 할 채널 : " + ctx.channel().id());
        System.out.println("채널 삭제 여부 : " + room.getChannelGroup().remove(ctx.channel()));
        System.out.println("[EXIT] : " + owner);

        // room에서 crewmate 삭제
        for(int i = 0; i < room.getCrewmates().size(); i++) {
            if(room.getCrewmates().get(i).getOwner().equals(owner))
                room.getCrewmates().remove(i);
        }
        parentJson.put("Body", owner);
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");

        // 방장 변경
        if(owner.equals(room.getSuper()))
            room.getCrewmates().get(0).makeSuper();

        parentJson.replace("Function","5");
        parentJson.put("Super", room.getCrewmates().get(0).getOwner());
        room.getChannelGroup().writeAndFlush(parentJson.toJSONString() + "\r\n");
    }

    public static void exitEmergency(ChannelHandlerContext ctx, JSONObject json, int roomCode) {
        Room room = Room.getRoomByCode(roomCode);
        System.out.println("긴급 삭제 : " + json.toJSONString());
        System.out.println("삭제할 채널 : " + ctx.channel().id());
        System.out.println("[EXIT] : " + json.get("Body").toString());

        for(Channel ch : room.getChannelGroup())
            System.out.println("남은 채널 : " + ch.id());

        // room에서 crewmate 삭제
        for(int i = 0; i < room.getCrewmates().size(); i++)
            if (room.getCrewmates().get(i).getOwner().equals(json.get("Body").toString()))
                room.getCrewmates().remove(i);

        // 방장 변경
        try {
            if(json.get("Body").toString().equals(room.getSuper()))
                room.getCrewmates().get(0).makeSuper();
        } catch(Exception e) {
            Room.rooms.remove(room);
        }

        room.getChannelGroup().writeAndFlush(json.toJSONString() + "\r\n");
    }
}
