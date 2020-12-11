package Repository;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;
import java.util.ArrayList;

public class Room { // 게임 방

    ChannelGroup channels;
    ArrayList<Crewmate> crewmates; // 참가자들

    public static ArrayList<Room> rooms = new ArrayList<>();
    int roomCode;

    int aliveCrew;

    String Super;

    public Room(int roomCode) {
        this.roomCode = roomCode;
        this.crewmates = new ArrayList<>();
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public ChannelGroup getChannelGroup() { return channels;}

    public ArrayList<Crewmate> getCrewmates() { return crewmates; }

    public int getRoomCode() { return roomCode; }

    public void setAliveCrew(int aliveCrew) { this.aliveCrew = aliveCrew; }

    public int getAliveCrew() { return aliveCrew; }

    public static Room getRoomByCode(int code) {
        for (Room room : rooms) {
            if (room.getRoomCode() == code)
                return room;
        }
        return null;
    }

    public void setSuper(String Super) {
        this.Super = Super;
    }

    public String getSuper() {
        return Super;
    }

    public void enter(ChannelHandlerContext ctx, JSONObject json) {
        crewmates.add(new Crewmate(json,ctx.channel().id()));
        channels.add(ctx.channel());
    }

    // 방 생성 + 생성되어 있는 방 리턴
    public static Room selectRoom(String owner) {
        if(rooms.size() == 0) { // 방 없음
            rooms.add(new Room(1));
            Room.getRoomByCode(1).Super = owner;
        }

        for(Room room : rooms)
            if(room.channels.size() < 5) {// 만들어진 방 중에 자리 있으면 그 방 리턴
                return room;
            }

        if(rooms.get(rooms.size()-1).channels.size() >= 5) { // 방 다 꽉참
            rooms.add(new Room(rooms.get(rooms.size()-1).roomCode)); // 마지막 코드+1 번호로 방 하나 새로 생성
        }

        return rooms.get(rooms.size()-1); // 끝방 리턴
    }

    // 방 정보 업데이트
    public void update(JSONObject requestJson) {
        for(Crewmate crewmate : crewmates) {
            if(crewmate.owner.equals(requestJson.get("owner"))) {
                crewmate.update(requestJson);
                crewmate.rate = 1;
            } else {
                crewmate.rate++;
            }

            if(crewmate.rate > 1000)
                crewmates.remove(crewmate);
            //System.out.println(crewmate.getInitCrewmateJson().toJSONString());
        }
    }

    public void deleteRoom(int roomCode) {
        rooms.remove(Room.getRoomByCode(roomCode));
    }
}