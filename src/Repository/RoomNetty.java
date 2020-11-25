package Repository;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class RoomNetty { // 게임 방
    ChannelGroup channels;
    ArrayList<Crewmate> crewmates; // 참가자들
    private static ArrayList<RoomNetty> rooms = new ArrayList<>();
    int roomCode;

    public void addChannel(Channel channel) { channels.add(channel); }

    public ChannelGroup getChannelGroup() { return channels;}

    public int getRoomCode() { return roomCode; }

    public static ArrayList<RoomNetty> getRooms() { return rooms; }

    public void enter(ChannelHandlerContext ctx, JSONObject json) {
        crewmates.add(new Crewmate(json));
        channels.add(ctx.channel());
    }

    public RoomNetty(int roomCode) {
        this.roomCode = roomCode;
        this.crewmates = new ArrayList<>();
        this.channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }
    //!
    public static RoomNetty getMyRoomByChannel(Channel channel) {
        for(int i = 0; i < rooms.size(); i++) {
            if(rooms.get(i).getChannelGroup().contains(channel)) {
                return rooms.get(i);
            }
        }
        return null;
    }
    //!
    public static RoomNetty getRoomCode(int code) {
            for(RoomNetty room : rooms) {
                if(room.getRoomCode() == code)
                    return room;
        }
        return null;
    }
    //!
    public ArrayList<Crewmate> getCrewmates() { return crewmates; }

    // 방 생성
    public static RoomNetty getRoom() {
        if(rooms.size() == 0) {// 방 하나도 없음
            rooms.add(new RoomNetty(1));
        }
        for(RoomNetty room : rooms)
            if(room.channels.size() < 5) {// 만들어진 방 중에 자리 있으면 그 방 리턴
                return room;
            }

        if(rooms.get(rooms.size()-1).channels.size() >= 5) { // 방 다 꽉참
            rooms.add(new RoomNetty(rooms.get(rooms.size()-1).roomCode)); // 마지막 코드+1 번호로 방 하나 새로 생성
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
}