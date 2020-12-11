package NavoServer;

import Util.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.json.simple.JSONObject;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("handlerAdded of [SERVER]");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        System.out.println("User Access!");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.println("handlerRemoved of [SERVER]");

//        JSONObject temp=new JSONObject();
//        for(Room room:Room.rooms) {
//            for(int i=0;i<room.getCrewmates().size();i++) {
//                if(room.getCrewmates().get(i).getId().equals(ctx.channel().id())) {
//                    System.out.println(i);
//                    temp.put("Header","Event");
//                    temp.put("Functon","4");
//                    temp.put("roomCode",room.getRoomCode());
//                    temp.put("Body",room.getCrewmates().get(i).getOwner());
//                    Event.exitEmergency(ctx,temp,room.getRoomCode());
//                    break;
//                    //room.getChannelGroup().writeAndFlush(temp.toJSONString()+"\r\n");
//                    //room.getCrewmates().remove(i);
//                }
//            }
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // handlerRemoved Exception Catch
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        JSONObject json = JsonParser.createJson(msg.toString());

        switch ((String)json.get("Header")) {
            case "Auth":
                System.out.println("[Auth Received] : " + json);
                Auth.auth(ctx, (JSONObject)json.get("Body"), json.get("Function").toString());
                break;
            case "Update":
                System.out.println("[Update Received] : " + json);
                Update.update(ctx, (JSONObject)(json.get("Body"))
                        ,json.get("Function").toString()
                        ,Integer.parseInt(json.get("roomCode").toString()));
                break;
            case "Event":
                System.out.println("[Event Received] : " + json);
                Event.event(ctx, json);
        }
    }
}
