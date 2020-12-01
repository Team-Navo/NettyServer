package NavoServer;


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

//        Channel incoming = ctx.channel();
//
//        //사용자가 나갔을 때 기존 사용자에게 알림
//        for (Channel channel : channelGroup) {
//            channel.write("[SERVER] - " + incoming.remoteAddress() + "has left!\n");
//            channelGroup.remove(incoming);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // handlerRemoved Exception Catch
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
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
