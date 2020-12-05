package NavoServer;


import Repository.Room;
import Util.JsonParser;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.json.simple.JSONObject;



public class ServerHandler extends ChannelInboundHandlerAdapter {

    private static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    //private static final ArrayList<>
    private class Node {
        Channel ch;
        int roomCode;
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("handlerAdded of [SERVER]");
//        Channel incoming = ctx.channel();
//        for (Channel channel : channelGroup) {
//            //사용자가 추가되었을 때 기존 사용자에게 알림
//            channel.write("[SERVER] - " + incoming.remoteAddress() + "has joined!\n");
//        }
//        channelGroup.add(incoming);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        // 사용자가 접속했을 때 서버에 표시.
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
//        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ctx.flush();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        //Event.logout(ctx);
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
